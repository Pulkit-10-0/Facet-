package com.example.drill.ui

sealed class Screen {
    data object Splash : Screen()
    data object ModelSetup : Screen()
    data object Home : Screen()
    data class Scan(val prefillUrl: String = "") : Screen()
    data class ProfileOverview(val facetId: String) : Screen()
    data class Chat(val facetId: String, val initialMessage: String? = null) : Screen()
}
