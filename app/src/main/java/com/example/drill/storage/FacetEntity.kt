package com.example.drill.storage

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.drill.model.FacetType

@Entity(tableName = "facets")
data class FacetEntity(
        @PrimaryKey val id: String,
        val type: FacetType,
        val name: String,
        val version: Int,
        val localJsonPath: String,
        val localGlbPath: String,
        val lastOpened: Long
)
