package com.Lia.assistant.voice

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

sealed class ChatReplyResult {
    data class Success(val text: String) : ChatReplyResult()
    data class Error(val message: String) : ChatReplyResult()
}

data class ChatTurn(val isUser: Boolean, val text: String)

object GeminiTextClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/"
    @Volatile private var cachedModel: String? = null

    private val client = OkHttpClient.Builder()
        .readTimeout(60, TimeUnit.SECONDS)
        .connectTimeout(20, TimeUnit.SECONDS)
        .build()

    fun resolveModel(apiKey: String): String {
        cachedModel?.let { return it }
        if (apiKey.isBlank()) return "models/gemini-2.5-flash"

        try {
            val request = Request.Builder()
                .url("${BASE_URL}models?key=$apiKey")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: return@use
                    val json = JSONObject(body)
                    val models = json.optJSONArray("models")
                    if (models != null) {
                        for (i in 0 until models.length()) {
                            val m = models.getJSONObject(i)
                            val name = m.optString("name")
                            val methods = m.optJSONArray("supportedGenerationMethods")
                            var canGenerate = false
                            if (methods != null) {
                                for (j in 0 until methods.length()) {
                                    if (methods.getString(j) == "generateContent") canGenerate = true
                                }
                            }
                            val lower = name.lowercase()
                            if (canGenerate && lower.contains("flash") && !lower.contains("live") && !lower.contains("image") && !lower.contains("tts")) {
                                cachedModel = name
                                return name
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {}

        return "models/gemini-2.5-flash".also { cachedModel = it }
    }

    fun reply(apiKey: String, systemInstruction: String, history: List<ChatTurn>): ChatReplyResult {
        if (apiKey.isBlank()) return ChatReplyResult.Error("API key missing")
        if (history.isEmpty()) return ChatReplyResult.Error("Empty conversation")

        val model = resolveModel(apiKey)
        val url = "$BASE_URL$model:generateContent?key=$apiKey"

        val root = JSONObject().apply {
            put("system_instruction", JSONObject().apply {
                put("parts", JSONArray().put(JSONObject().put("text", systemInstruction)))
            })
            val contentsArray = JSONArray()
            for (turn in history) {
                contentsArray.put(JSONObject().apply {
                    put("role", if (turn.isUser) "user" else "model")
                    put("parts", JSONArray().put(JSONObject().put("text", turn.text)))
                })
            }
            put("contents", contentsArray)
        }

        val reqBody = root.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder().url(url).post(reqBody).build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return ChatReplyResult.Error("Gemini error (${response.code})")
                }
                val body = response.body?.string() ?: return ChatReplyResult.Error("Empty response")
                val json = JSONObject(body)
                val candidates = json.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val text = parts.getJSONObject(0).optString("text", "")
                        if (text.isNotBlank()) {
                            return ChatReplyResult.Success(text)
                        }
                    }
                }
                return ChatReplyResult.Error("Blocked or empty reply")
            }
        } catch (e: IOException) {
            return ChatReplyResult.Error("Couldn't reach Gemini. Check connection.")
        } catch (e: Exception) {
            return ChatReplyResult.Error("Error: ${e.localizedMessage}")
        }
    }
}
