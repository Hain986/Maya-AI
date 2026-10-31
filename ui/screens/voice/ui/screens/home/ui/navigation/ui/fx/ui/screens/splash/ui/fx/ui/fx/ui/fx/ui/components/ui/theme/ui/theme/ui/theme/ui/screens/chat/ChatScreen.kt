package com.Lia.assistant.ui.screens.chat

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ContextCompat
import androidx.compose.ui.unit.dp
import com.Lia.assistant.data.ApiKeyStore
import com.Lia.assistant.data.NovaAppState
import com.Lia.assistant.forge.ForgeController
import com.Lia.assistant.voice.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val isStreaming: Boolean = false,
    val isError: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(appState: NovaAppState, onBack: () -> Unit, onOpenForge: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var messages by remember {
        mutableStateOf(
            listOf(
                ChatMessage(text = "Hello! I am ${appState.assistantName}, your AI companion. How can I help you today?", isUser = false)
            )
        )
    }
    var inputText by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }

    var tts: TextToSpeech? by remember { mutableStateOf(null) }
    DisposableEffect(context) {
        val t = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.getDefault()
            }
        }
        tts = t
        onDispose {
            t.stop()
            t.shutdown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(appState.assistantName, style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Surface(modifier = Modifier.fillMaxWidth(), tonalElevation = 8.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Message ${appState.assistantName}...") },
                        maxLines = 4
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            val text = inputText.trim()
                            if (text.isNotBlank() && !isSending) {
                                inputText = ""
                                isSending = true
                                messages = messages + ChatMessage(text = text, isUser = true)

                                scope.launch(Dispatchers.IO) {
                                    // Check for Forge Intent
                                    val forgePrompt = ForgeIntent.extract(text)
                                    if (forgePrompt != null) {
                                        ForgeController.start(context, forgePrompt)
                                        messages = messages + ChatMessage(text = "Opening the Forge to build your website...", isUser = false)
                                        isSending = false
                                        kotlinx.coroutines.withContext(Dispatchers.Main) {
                                            onOpenForge()
                                        }
                                        return@launch
                                    }

                                    val apiKey = ApiKeyStore.getKey(context) ?: ""
                                    val sysPrompt = ChatPromptBuilder.build(appState.personality, appState.language, appState.assistantName)
                                    val history = messages.map { ChatTurn(it.isUser, it.text) } + ChatTurn(true, text)

                                    when (val result = GeminiTextClient.reply(apiKey, sysPrompt, history)) {
                                        is ChatReplyResult.Success -> {
                                            messages = messages + ChatMessage(text = result.text, isUser = false)
                                        }
                                        is ChatReplyResult.Error -> {
                                            messages = messages + ChatMessage(text = result.message, isUser = false, isError = true)
                                        }
                                    }
                                    isSending = false
                                }
                            }
                        },
                        enabled = !isSending && inputText.isNotBlank()
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send")
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages) { msg ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = if (msg.isUser) Alignment.End else Alignment.Start
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = if (msg.isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.widthIn(max = 300.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = msg.text, style = MaterialTheme.typography.bodyMedium)
                            if (!msg.isUser) {
                                Row(modifier = Modifier.padding(top = 4.dp)) {
                                    TextButton(onClick = {
                                        tts?.speak(msg.text, TextToSpeech.QUEUE_FLUSH, null, null)
                                    }) {
                                        Text("Speak", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
