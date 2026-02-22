package com.example.drill.ui

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class DeepLink(val facetId: String, val initialMessage: String? = null)

object DeepLinkBus {
    private val _deepLink = MutableStateFlow<DeepLink?>(null)
    val deepLink: StateFlow<DeepLink?> = _deepLink

    fun publish(value: DeepLink?) {
        _deepLink.value = value
    }

    fun consume(): DeepLink? {
        val current = _deepLink.value
        _deepLink.value = null
        return current
    }
}
