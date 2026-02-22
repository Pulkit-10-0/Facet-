package com.example.drill.ui.model

data class ChatItem(
        val id: String,
        val role: Role,
        val text: String,
        val timestampMillis: Long = System.currentTimeMillis()
) {
    enum class Role {
        System,
        User,
        Assistant
    }
}
