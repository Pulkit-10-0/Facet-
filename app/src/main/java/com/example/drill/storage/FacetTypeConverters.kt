package com.example.drill.storage

import androidx.room.TypeConverter
import com.example.drill.model.FacetType

class FacetTypeConverters {
    @TypeConverter
    fun fromType(type: FacetType): String = FacetType.toWire(type)

    @TypeConverter
    fun toType(value: String): FacetType = FacetType.fromWire(value) ?: FacetType.Person
}

