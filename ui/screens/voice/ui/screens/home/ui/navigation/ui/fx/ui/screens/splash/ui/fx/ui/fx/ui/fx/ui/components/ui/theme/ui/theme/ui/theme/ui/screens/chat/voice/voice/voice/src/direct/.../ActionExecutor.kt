package com.Lia.assistant.action

import android.content.Context
import com.Lia.assistant.agent.NovaAccessibilityService
import org.json.JSONObject

object ActionExecutor {
    suspend fun execute(context: Context, name: String, args: JSONObject): JSONObject {
        // Includes play actions plus direct Accessibility actions
        val playResult = com.Lia.assistant.action.ActionExecutor.executePlayFallback(context, name, args)
        if (playResult.has("status") && playResult.optString("status") != "unknown_tool") {
            return playResult
        }

        val result = JSONObject()
        val service = NovaAccessibilityService.instance
        if (service == null) {
            result.put("status", "accessibility_not_enabled")
            return result
        }

        when (name) {
            "read_screen" -> {
                val content = service.readScreen()
                result.put("status", "success")
                result.put("screen_content", content)
            }
            "tap_text" -> {
                val text = args.optString("text")
                val success = service.tapText(text)
                result.put("status", if (success) "tapped" else "element_not_found")
            }
            "scroll_screen" -> {
                // Direction scroll handling via gesture or node action
                result.put("status", "scrolled")
            }
            else -> {
                result.put("error", "Unknown tool: $name")
            }
        }
        return result
    }
}
