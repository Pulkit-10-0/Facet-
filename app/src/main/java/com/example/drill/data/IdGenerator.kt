package com.example.drill.data

import java.security.MessageDigest

object IdGenerator {
    fun idFromUrl(url: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(url.trim().toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { b -> "%02x".format(b) }.take(32)
    }
}

