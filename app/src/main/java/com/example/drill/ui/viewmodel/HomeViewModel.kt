package com.example.drill.ui.viewmodel

import com.example.drill.domain.FacetRepository
import com.example.drill.storage.FacetEntity
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val repo: FacetRepository) : BaseViewModel() {
    val facets: StateFlow<List<FacetEntity>> = repo.facets

    fun load() {
        scope.launch { repo.refresh() }
    }

    fun deleteFacet(id: String) {
        scope.launch { repo.deleteFacet(id) }
    }
}
