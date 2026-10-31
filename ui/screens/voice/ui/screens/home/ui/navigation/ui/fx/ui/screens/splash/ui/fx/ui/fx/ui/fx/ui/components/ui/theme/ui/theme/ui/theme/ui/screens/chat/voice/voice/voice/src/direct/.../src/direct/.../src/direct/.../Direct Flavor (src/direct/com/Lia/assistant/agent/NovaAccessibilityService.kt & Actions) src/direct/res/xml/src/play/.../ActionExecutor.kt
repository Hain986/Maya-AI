package com.Lia.assistant.action

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import org.json.JSONObject

object ActionExecutor {
    suspend fun execute(context: Context, name: String, args: JSONObject): JSONObject {
        val result = JSONObject()
        when (name) {
            "open_app" -> {
                val appName = args.optString("app_name")
                val res = AppLauncher.findAndOpenApp(context, appName)
                result.put("status", res)
            }
            "call_contact" -> {
                val query = args.optString("contact")
                when (val contactRes = ContactResolver.lookup(context, query)) {
                    is ContactResult.Found -> {
                        val intent = if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                            Intent(Intent.ACTION_CALL, Uri.parse("tel:${contactRes.phoneNumber}"))
                        } else {
                            Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contactRes.phoneNumber}"))
                        }
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                        result.put("status", "calling")
                        result.put("contact", contactRes.name)
                    }
                    is ContactResult.NoPermission -> result.put("status", "no_contacts_permission")
                    is ContactResult.NotFound -> result.put("status", "contact_not_found")
                }
            }
            "message_contact" -> {
                val query = args.optString("contact")
                val message = args.optString("message")
                when (val contactRes = ContactResolver.lookup(context, query)) {
                    is ContactResult.Found -> {
                        // Open WhatsApp or SMS composer without sending silently (Play policy compliant)
                        val encodedMsg = Uri.encode(message)
                        val waIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/${contactRes.phoneNumber.filter { it.isDigit() }}?text=$encodedMsg")).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        try {
                            context.startActivity(waIntent)
                            result.put("status", "composer_opened")
                        } catch (e: Exception) {
                            val smsIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${contactRes.phoneNumber}")).apply {
                                putExtra("sms_body", message)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(smsIntent)
                            result.put("status", "composer_opened")
                        }
                    }
                    else -> result.put("status", "contact_not_found")
                }
            }
            else -> {
                result.put("error", "Unknown tool: $name")
            }
        }
        return result
    }
}
