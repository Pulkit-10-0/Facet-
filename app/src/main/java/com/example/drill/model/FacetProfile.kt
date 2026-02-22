package com.example.drill.model

import org.json.JSONArray
import org.json.JSONObject

sealed class FacetProfile(
    open val id: String,
    open val type: FacetType,
    open val name: String,
    open val version: Int,
    open val glbUrl: String,
    open val json: JSONObject
) {
    data class Person(
        override val id: String,
        override val name: String,
        val title: String,
        val bio: String,
        override val version: Int,
        override val glbUrl: String,
        override val json: JSONObject
    ) : FacetProfile(
        id = id,
        type = FacetType.Person,
        name = name,
        version = version,
        glbUrl = glbUrl,
        json = json
    )

    data class Product(
        override val id: String,
        override val name: String,
        val description: String,
        val keyFeatures: List<String>,
        override val version: Int,
        override val glbUrl: String,
        override val json: JSONObject
    ) : FacetProfile(
        id = id,
        type = FacetType.Product,
        name = name,
        version = version,
        glbUrl = glbUrl,
        json = json
    )

    fun toProfileJsonString(): String = json.toString()

    companion object {
        fun requireString(obj: JSONObject, key: String, maxLen: Int = 1_000): String {
            val value =
                if (obj.has(key) && !obj.isNull(key)) obj.getString(key)
                else throw IllegalArgumentException("Missing $key")
            val trimmed = value.trim()
            if (trimmed.isEmpty()) throw IllegalArgumentException("Empty $key")
            return if (trimmed.length > maxLen) trimmed.take(maxLen) else trimmed
        }

        fun requireInt(obj: JSONObject, key: String, defaultValue: Int = 1): Int {
            if (!obj.has(key)) return defaultValue
            val v = obj.optInt(key, defaultValue)
            return if (v <= 0) defaultValue else v
        }

        fun requireStringList(obj: JSONObject, key: String, maxItems: Int = 12, itemMaxLen: Int = 120): List<String> {
            val arr = obj.optJSONArray(key) ?: return emptyList()
            val out = ArrayList<String>(minOf(arr.length(), maxItems))
            for (i in 0 until minOf(arr.length(), maxItems)) {
                val item = arr.optString(i, "").trim()
                if (item.isNotEmpty()) out += if (item.length > itemMaxLen) item.take(itemMaxLen) else item
            }
            return out
        }

        fun parse(json: JSONObject, id: String): FacetProfile {
            val type = FacetType.fromWire(requireString(json, "type", maxLen = 20))
                ?: throw IllegalArgumentException("Unsupported type")
            val name = requireString(json, "name", maxLen = 80)
            val version = requireInt(json, "version", defaultValue = 1)
            val glbUrl = requireString(json, "glb_url", maxLen = 2_048)

            return when (type) {
                FacetType.Person -> {
                    Person(
                        id = id,
                        name = name,
                        title = requireString(json, "title", maxLen = 80),
                        bio = requireString(json, "bio", maxLen = 1_500),
                        version = version,
                        glbUrl = glbUrl,
                        json = json
                    )
                }
                FacetType.Product -> {
                    Product(
                        id = id,
                        name = name,
                        description = requireString(json, "description", maxLen = 1_500),
                        keyFeatures = requireStringList(json, "key_features"),
                        version = version,
                        glbUrl = glbUrl,
                        json = json
                    )
                }
            }
        }

        fun ensureArray(json: JSONObject, key: String): JSONArray {
            if (json.optJSONArray(key) != null) return json.getJSONArray(key)
            val arr = JSONArray()
            json.put(key, arr)
            return arr
        }
    }
}

