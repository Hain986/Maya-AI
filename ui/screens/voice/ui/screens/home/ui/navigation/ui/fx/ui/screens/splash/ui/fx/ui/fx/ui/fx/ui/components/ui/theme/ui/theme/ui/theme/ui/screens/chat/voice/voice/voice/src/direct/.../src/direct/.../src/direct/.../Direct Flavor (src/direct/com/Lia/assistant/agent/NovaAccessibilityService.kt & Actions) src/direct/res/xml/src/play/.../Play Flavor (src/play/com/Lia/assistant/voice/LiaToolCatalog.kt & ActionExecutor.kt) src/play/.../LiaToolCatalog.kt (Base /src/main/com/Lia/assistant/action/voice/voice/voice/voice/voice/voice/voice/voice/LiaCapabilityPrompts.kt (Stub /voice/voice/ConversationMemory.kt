package com.Lia.assistant.voice

class ConversationMemory(private val maxTurns: Int = 12, private val maxChars: Int = 1600) {
    private data class Turn(val isUser: Boolean, val text: String)
    private val turns = mutableListOf<Turn>()

    @Synchronized
    fun addTurn(isUser: Boolean, text: String) {
        if (text.isBlank()) return
        turns.add(Turn(isUser, text))
        if (turns.size > maxTurns) {
            turns.removeAt(0)
        }
    }

    @Synchronized
    fun clear() {
        turns.clear()
    }

    @Synchronized
    fun recap(): String? {
        if (turns.isEmpty()) return null
        val sb = StringBuilder()
        for (turn in turns) {
            val prefix = if (turn.isUser) "User: " else "You: "
            sb.append(prefix).append(turn.text).append("\n")
        }
        val result = sb.toString().trim()
        if (result.length <= maxChars) return result
        return result.takeLast(maxChars)
    }
}
