package com.example.drill.ui.viewmodel

import com.example.drill.domain.FacetRepository
import com.example.drill.model.FacetProfile
import com.example.drill.storage.FacetEntity
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

class ProfileViewModel(private val repo: FacetRepository) : BaseViewModel() {
    private val _entity = MutableStateFlow<FacetEntity?>(null)
    val entity: StateFlow<FacetEntity?> = _entity

    private val _profile = MutableStateFlow<FacetProfile?>(null)
    val profile: StateFlow<FacetProfile?> = _profile

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun load(id: String) {
        scope.launch {
            _error.value = null
            try {
                val ent = repo.getFacet(id)
                _entity.value = ent
                if (ent == null) {
                    _profile.value = null
                    return@launch
                }
                repo.markOpened(id, System.currentTimeMillis())
                val jsonString = File(ent.localJsonPath).readText(Charsets.UTF_8)
                val json = JSONObject(jsonString)
                _profile.value = FacetProfile.parse(json, ent.id)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load."
            }
        }
    }
}
