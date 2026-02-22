package com.example.drill.model

enum class FacetType {
    Person,
    Product;

    companion object {
        fun fromWire(value: String): FacetType? {
            return when (value.trim().lowercase()) {
                "person" -> Person
                "product" -> Product
                else -> null
            }
        }

        fun toWire(type: FacetType): String {
            return when (type) {
                Person -> "person"
                Product -> "product"
            }
        }
    }
}

