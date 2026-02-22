package com.example.drill.ai

sealed class ModelState {
    data object NotDownloaded : ModelState()
    data class Downloading(val progress: Int, val status: String) : ModelState()
    data object Initializing : ModelState()
    data object Ready : ModelState()
    data class Error(val message: String) : ModelState()
}

