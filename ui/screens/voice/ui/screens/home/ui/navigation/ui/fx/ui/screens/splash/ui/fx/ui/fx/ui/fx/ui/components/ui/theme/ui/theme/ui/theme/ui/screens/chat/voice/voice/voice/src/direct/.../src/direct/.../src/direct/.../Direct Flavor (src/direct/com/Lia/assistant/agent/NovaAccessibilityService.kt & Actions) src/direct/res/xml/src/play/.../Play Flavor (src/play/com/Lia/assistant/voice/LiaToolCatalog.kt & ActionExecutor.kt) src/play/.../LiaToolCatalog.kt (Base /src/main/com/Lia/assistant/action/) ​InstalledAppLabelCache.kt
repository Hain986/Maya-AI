package com.Lia.assistant.action

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

object InstalledAppLabelCache {
    private val mutex = Mutex()
    private var appMap = mapOf<String, String>() // lowercase label -> package name
    private var lastFetchTime = 0L
    private const val TTL_MS = 15 * 60 * 1000L // 15 minutes

    suspend fun get(context: Context): Map<String, String> {
        val now = System.currentTimeMillis()
        if (appMap.isNotEmpty() && (now - lastFetchTime) < TTL_MS) {
            return appMap
        }
        mutex.withLock {
            if (appMap.isNotEmpty() && (now - lastFetchTime) < TTL_MS) {
                return appMap
            }
            appMap = withContext(Dispatchers.IO) {
                val map = mutableMapOf<String, String>()
                val pm = context.packageManager
                val intent = Intent(Intent.ACTION_MAIN, null).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                }
                val resolveInfos = pm.queryIntentActivities(intent, 0)
                for (ri in resolveInfos) {
                    val label = ri.loadLabel(pm)?.toString()?.trim() ?: continue
                    val pkg = ri.activityInfo.packageName
                    if (label.isNotBlank() && pkg.isNotBlank()) {
                        map[label.lowercase()] = pkg
                    }
                }
                map
            }
            lastFetchTime = System.currentTimeMillis()
        }
        return appMap
    }

    fun prewarm(context: Context) {
        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
            get(context)
        }
    }
}
