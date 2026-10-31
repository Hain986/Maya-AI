package com.Lia.assistant.voice

import org.json.JSONObject

object LiaToolCatalog {
    fun declarations(): List<JSONObject> {
        return listOf(
            LiaToolCatalog.decl("open_app", "Open an installed application by app name", listOf("app_name")),
            LiaToolCatalog.decl("call_contact", "Call a contact by name or phone number", listOf("contact")),
            LiaToolCatalog.decl("message_contact", "Prepare or send a message via WhatsApp or SMS", listOf("contact", "message")),
            LiaToolCatalog.decl("read_screen", "Read the current device screen content", listOf()),
            LiaToolCatalog.decl("tap_text", "Tap on screen text using Accessibility", listOf("text")),
            LiaToolCatalog.decl("scroll_screen", "Scroll down or up on screen", listOf("direction"))
        )
    }
}
