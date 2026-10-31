package com.Lia.assistant.voice

class ToolCallDeduplicator(private val maxSize: Int = 50) {
    private val seenIds = object : LinkedHashSet<String>() {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, String>?): Boolean {
            return size > maxSize
        }
    }

    @Synchronized
    fun isDuplicate(callId: String): Boolean {
        if (callId.isBlank()) return false
        if (seenIds.contains(callId)) {
            return true
        }
        seenIds.add(callId)
        return false
    }

    @Synchronized
    fun clear() {
        seenIds.clear()
    }
}
