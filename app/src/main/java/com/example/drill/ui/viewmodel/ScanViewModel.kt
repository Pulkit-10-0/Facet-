package com.example.drill.ui.viewmodel

import com.example.drill.domain.FacetRepository
import com.example.drill.storage.FacetEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ScanViewModel(private val repo: FacetRepository) : BaseViewModel() {
    private val _url = MutableStateFlow("")
    val url: StateFlow<String> = _url

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun setUrl(value: String) {
        _url.value = value
    }

    fun clearError() {
        _error.value = null
    }

    fun import(onSuccess: (FacetEntity) -> Unit) {
        val current = _url.value.trim()
        if (current.isEmpty()) {
            _error.value = "Paste a valid HTTPS URL."
            return
        }
        scope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val entity = repo.importFacetFromUrl(current)
                onSuccess(entity)
            } catch (e: Exception) {
                _error.value = e.message ?: "Import failed."
            } finally {
                _isLoading.value = false
            }
        }
    }
}
