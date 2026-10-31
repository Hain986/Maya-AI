package com.Lia.assistant.voice

import org.json.JSONObject

object LiaToolCatalog {
    fun declarations(): List<JSONObject> {
        return listOf(
            LiaToolCatalog.decl("open_app", "Open an installed application by app name", listOf("app_name")),
            LiaToolCatalog.decl("call_contact", "Call a contact by name or phone number", listOf("contact")),
            LiaToolCatalog.decl("message_contact", "Prepare a message for a contact or WhatsApp chat", listOf("contact", "message"))
        )
    }
}
