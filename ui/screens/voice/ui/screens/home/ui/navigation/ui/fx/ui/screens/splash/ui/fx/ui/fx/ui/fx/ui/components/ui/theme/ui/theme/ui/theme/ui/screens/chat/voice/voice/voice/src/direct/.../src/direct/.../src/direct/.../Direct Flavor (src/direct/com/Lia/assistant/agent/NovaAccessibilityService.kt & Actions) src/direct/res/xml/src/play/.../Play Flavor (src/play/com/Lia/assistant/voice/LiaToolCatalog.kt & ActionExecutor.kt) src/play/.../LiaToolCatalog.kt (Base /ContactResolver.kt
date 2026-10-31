package com.Lia.assistant.action

import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

sealed class ContactResult {
    data class Found(val name: String, val phoneNumber: String) : ContactResult()
    object NoPermission : ContactResult()
    object NotFound : ContactResult()
}

object ContactResolver {
    fun lookup(context: Context, query: String): ContactResult {
        val q = query.trim()
        if (q.isEmpty()) return ContactResult.NotFound

        // If query looks like a raw phone number
        if (q.all { it.isDigit() || it == '+' || it == ' ' || it == '-' || it == '(' || it == ')' } && q.count { it.isDigit() } >= 5) {
            return ContactResult.Found(q, q)
        }

        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            return ContactResult.NoPermission
        }

        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )
        val selection = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?"
        val selectionArgs = arrayOf("%$q%")

        val cursor: Cursor? = context.contentResolver.query(uri, projection, selection, selectionArgs, null)
        cursor?.use {
            val nameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            if (nameIdx != -1 && numIdx != -1 && it.moveToFirst()) {
                val name = it.getString(nameIdx) ?: q
                val number = it.getString(numIdx) ?: ""
                if (number.isNotBlank()) {
                    return ContactResult.Found(name, number)
                }
            }
        }
        return ContactResult.NotFound
    }
}
