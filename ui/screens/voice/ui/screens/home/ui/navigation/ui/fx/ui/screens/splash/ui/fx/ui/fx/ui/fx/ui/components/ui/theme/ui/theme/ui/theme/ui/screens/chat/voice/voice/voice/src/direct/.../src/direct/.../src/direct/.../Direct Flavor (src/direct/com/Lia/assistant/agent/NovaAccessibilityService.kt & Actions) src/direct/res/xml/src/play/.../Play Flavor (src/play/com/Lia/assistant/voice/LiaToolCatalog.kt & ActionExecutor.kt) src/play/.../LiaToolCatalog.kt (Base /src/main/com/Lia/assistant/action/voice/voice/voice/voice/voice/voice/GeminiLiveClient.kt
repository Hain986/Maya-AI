package com.Lia.assistant.voice

import android.util.Base64
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiLiveClient(private val listener: Listener) {

    interface Listener {
        fun onSetupComplete()
        fun onAudioChunk(pcmBytes: ByteArray)
        fun onInputTranscript(text: String)
        fun onOutputTranscript(text: String)
        fun onToolCall(id: String, name: String, args: JSONObject)
        fun onTurnComplete()
        fun onInterrupted()
        fun onError(message: String)
        fun onClosed()
    }

    companion object {
        const val MODEL = "models/gemini-3.1-flash-live-preview"
        private const val BASE_WS_URL = "wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage.v1beta.GenerativeService.BidiGenerateContent"
    }

    private var webSocket: WebSocket? = null
    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .connectTimeout(20, TimeUnit.SECONDS)
        .pingInterval(20, TimeUnit.SECONDS)
        .build()

    private val deduplicator = ToolCallDeduplicator()

    fun connect(apiKey: String, systemInstruction: String, voiceName: String = "Aoede") {
        if (apiKey.isBlank()) {
            listener.onError("API key is missing")
            return
        }

        val url = "$BASE_WS_URL?key=$apiKey"
        val request = Request.Builder().url(url).build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                sendSetupMessage(ws, systemInstruction, voiceName)
            }

            override fun onMessage(ws: WebSocket, text: String) {
                handleServerMessage(text)
            }

            override fun onMessage(ws: WebSocket, bytes: okio.ByteString) {
                handleServerMessage(bytes.utf8())
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                listener.onClosed()
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                listener.onError(t.localizedMessage ?: "WebSocket connection error")
            }
        })
    }

    private fun sendSetupMessage(ws: WebSocket, systemInstructionText: String, voiceName: String) {
        val setupJson = JSONObject().apply {
            put("setup", JSONObject().apply {
                put("model", MODEL)
                put("generationConfig", JSONObject().apply {
                    put("responseModalities", JSONArray().put("AUDIO"))
                    put("speechConfig", JSONObject().apply {
                        put("voiceConfig", JSONObject().apply {
                            put("prebuiltVoiceConfig", JSONObject().apply {
                                put("voiceName", voiceName)
                            })
                        })
                    })
                })
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().put("text", systemInstructionText)))
                })
                put("inputAudioTranscription", JSONObject())
                put("outputAudioTranscription", JSONObject())
                
                // Attach tool declarations from Flavor catalog
                val toolsArray = JSONArray()
                val toolObj = JSONObject()
                val declsArray = JSONArray()
                for (decl in LiaToolCatalog.declarations()) {
                    declsArray.put(decl)
                }
                toolObj.put("functionDeclarations", declsArray)
                toolsArray.put(toolObj)
                put("tools", toolsArray)
            })
        }
        ws.send(setupJson.toString())
    }

    fun sendAudio(pcmBytes: ByteArray) {
        if (pcmBytes.isEmpty()) return
        val base64Data = Base64.encodeToString(pcmBytes, Base64.NO_WRAP)
        val audioMsg = JSONObject().apply {
            put("realtimeInput", JSONObject().apply {
                put("audio", JSONObject().apply {
                    put("mimeType", "audio/pcm;rate=16000")
                    put("data", base64Data)
                })
            })
        }
        webSocket?.send(audioMsg.toString())
    }

    fun sendToolResponse(id: String, name: String, resultJson: JSONObject) {
        val toolResp = JSONObject().apply {
            put("toolResponse", JSONObject().apply {
                put("functionResponses", JSONArray().put(JSONObject().apply {
                    put("id", id)
                    put("name", name)
                    put("response", resultJson)
                }))
            })
        }
        webSocket?.send(toolResp.toString())
    }

    fun disconnect() {
        webSocket?.close(1000, "Session closed by client")
        webSocket = null
        deduplicator.clear()
    }

    private fun handleServerMessage(jsonText: String) {
        try {
            val root = JSONObject(jsonText)

            if (root.has("setupComplete")) {
                listener.onSetupComplete()
                return
            }

            if (root.has("sessionResumptionUpdate") || root.has("goAway")) {
                return // Silently ignore as specified
            }

            val serverContent = root.optJSONObject("serverContent")
            if (serverContent != null) {
                if (serverContent.optBoolean("interrupted", false)) {
                    listener.onInterrupted()
                }
                if (serverContent.optBoolean("turnComplete", false)) {
                    listener.onTurnComplete()
                }

                val modelTurn = serverContent.optJSONObject("modelTurn")
                if (modelTurn != null) {
                    val parts = modelTurn.optJSONArray("parts")
                    if (parts != null) {
                        for (i in 0 until parts.length()) {
                            val part = parts.getJSONObject(i)
                            // Handle inline audio (24 kHz PCM16)
                            val inlineData = part.optJSONObject("inlineData")
                            if (inlineData != null) {
                                val dataBase64 = inlineData.optString("data")
                                if (dataBase64.isNotBlank()) {
                                    val pcm = Base64.decode(dataBase64, Base64.DEFAULT)
                                    listener.onAudioChunk(pcm)
                                }
                            }
                        }
                    }
                }

                val inputTrans = serverContent.optJSONObject("inputTranscription")
                if (inputTrans != null) {
                    val text = inputTrans.optString("text", "")
                    if (text.isNotBlank()) listener.onInputTranscript(text)
                }

                val outputTrans = serverContent.optJSONObject("outputTranscription")
                if (outputTrans != null) {
                    val text = outputTrans.optString("text", "")
                    if (text.isNotBlank()) listener.onOutputTranscript(text)
                }
            }

            // Handle Tool Calls
            val toolCall = root.optJSONObject("toolCall")
            if (toolCall != null) {
                val functionCalls = toolCall.optJSONArray("functionCalls")
                if (functionCalls != null) {
                    for (i in 0 until functionCalls.length()) {
                        val fc = functionCalls.getJSONObject(i)
                        val id = fc.optString("id")
                        val name = fc.optString("name")
                        val args = fc.optJSONObject("args") ?: JSONObject()

                        if (!deduplicator.isDuplicate(id)) {
                            listener.onToolCall(id, name, args)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Malformed frame or parse exception handling without raw key leaks
        }
    }
}
