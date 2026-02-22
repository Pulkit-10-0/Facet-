package com.example.drill.domain

import com.example.drill.storage.FacetEntity
import kotlinx.coroutines.flow.StateFlow

interface FacetRepository {
    val facets: StateFlow<List<FacetEntity>>

    suspend fun refresh()
    suspend fun getFacet(id: String): FacetEntity?
    suspend fun deleteFacet(id: String)
    suspend fun markOpened(id: String, atMillis: Long)
    suspend fun importFacetFromUrl(url: String): FacetEntity
}

