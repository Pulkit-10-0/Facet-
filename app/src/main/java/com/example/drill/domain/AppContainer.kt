package com.example.drill.domain

import android.content.Context
import com.example.drill.data.FacetRepositoryImpl

class AppContainer(context: Context) {
    val facetRepository: FacetRepository = FacetRepositoryImpl(context)
}
