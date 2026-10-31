package com.Lia.assistant.action

import android.content.Context
import android.content.Intent

object AppLauncher {
    suspend fun findAndOpenApp(context: Context, query: String): String {
        val q = query.trim().lowercase()
        if (q.isBlank()) return "app_not_found"

        val map = InstalledAppLabelCache.get(context)
        // 1. Exact match
        var pkg = map[q]
        if (pkg != null) {
            return launchPackage(context, pkg)
        }

        // 2. Fuzzy prefix / word match
        var bestPkg: String? = null
        var bestLenDiff = Int.MAX_VALUE

        for ((label, p) in map) {
            if (label == q) {
                bestPkg = p
                break
            }
            val matches = if (label.startsWith(q) || q.startsWith(label)) {
                true
            } else {
                val qWords = q.split(" ")
                val lWords = label.split(" ")
                qWords.all { qw -> lWords.any { lw -> lw.startsWith(qw) } }
            }

            if (matches) {
                val diff = kotlin.math.abs(label.length - q.length)
                if (diff < bestLenDiff) {
                    bestLenDiff = diff
                    bestPkg = p
                }
            }
        }

        return if (bestPkg != null) {
            launchPackage(context, bestPkg)
        } else {
            "app_not_found"
        }
    }

    private fun launchPackage(context: Context, packageName: String): String {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                "app_opened"
            } else {
                "app_launch_failed"
            }
        } catch (e: Exception) {
            "app_launch_failed"
        }
    }
}
