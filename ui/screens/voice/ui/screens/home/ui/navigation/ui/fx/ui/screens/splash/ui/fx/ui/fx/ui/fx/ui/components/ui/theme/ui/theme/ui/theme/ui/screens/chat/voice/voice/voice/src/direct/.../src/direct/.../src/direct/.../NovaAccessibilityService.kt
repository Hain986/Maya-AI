package com.Lia.assistant.agent

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Bundle
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityEvent

class NovaAccessibilityService : AccessibilityService() {
    companion object {
        var instance: NovaAccessibilityService? = null
            private set
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        if (instance == this) instance = null
    }

    fun readScreen(): String {
        val root = rootInActiveWindow ?: return "no_active_window"
        val sb = StringBuilder()
        traverseNode(root, sb, 0, 40)
        root.recycle()
        return sb.toString().ifBlank { "screen_empty" }
    }

    private fun traverseNode(node: AccessibilityNodeInfo, sb: StringBuilder, depth: Int, max: Int) {
        if (sb.length > 2000 || depth > 15) return
        val text = node.text?.toString()?.trim()
        val desc = node.contentDescription?.toString()?.trim()
        val className = node.className?.toString()?.substringAfterLast('.') ?: ""

        if (!text.isNullOrBlank()) {
            sb.append("[$className] $text\n")
        } else if (!desc.isNullOrBlank()) {
            sb.append("[$className desc] $desc\n")
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            traverseNode(child, sb, depth + 1, max)
            child.recycle()
        }
    }

    fun tapText(targetText: String): Boolean {
        val root = rootInActiveWindow ?: return false
        val found = findAndClickNode(root, targetText.lowercase())
        root.recycle()
        return found
    }

    private fun findAndClickNode(node: AccessibilityNodeInfo, query: String): Boolean {
        val text = node.text?.toString()?.lowercase() ?: ""
        val desc = node.contentDescription?.toString()?.lowercase() ?: ""
        if (text.contains(query) || desc.contains(query)) {
            if (node.isClickable) {
                return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            } else {
                var parent = node.parent
                while (parent != null) {
                    if (parent.isClickable) {
                        val clicked = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        parent.recycle()
                        return clicked
                    }
                    val old = parent
                    parent = parent.parent
                    old.recycle()
                }
            }
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            if (findAndClickNode(child, query)) {
                child.recycle()
                return true
            }
            child.recycle()
        }
        return false
    }

    fun gestureTap(x: Float, y: Float): Boolean {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N) return false
        val path = Path().apply { moveTo(x, y) }
        val stroke = GestureDescription.StrokeDescription(path, 0, 100)
        val desc = GestureDescription.Builder().addStroke(stroke).build()
        return dispatchGesture(desc, null, null)
    }
}
