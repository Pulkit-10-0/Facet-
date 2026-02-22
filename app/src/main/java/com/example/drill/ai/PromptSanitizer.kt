package com.example.drill.ai

import org.json.JSONArray
import org.json.JSONObject

object PromptSanitizer {
    private const val MAX_DEPTH = 4
    private const val MAX_STRING = 800
    private const val MAX_KEYS = 80
    private const val MAX_ARRAY = 60

    fun sanitize(json: JSONObject): JSONObject {
        return sanitizeValue(json, depth = 0) as JSONObject
    }

    private fun sanitizeValue(value: Any?, depth: Int): Any {
        if (depth > MAX_DEPTH) return JSONObject.NULL
        return when (value) {
            null, JSONObject.NULL -> JSONObject.NULL
            is JSONObject -> {
                val out = JSONObject()
                val keys = value.keys().asSequence().toList().take(MAX_KEYS)
                for (k in keys) {
                    val safeKey = sanitizeString(k, max = 60)
                    out.put(safeKey, sanitizeValue(value.opt(k), depth + 1))
                }
                out
            }
            is JSONArray -> {
                val out = JSONArray()
                val len = minOf(value.length(), MAX_ARRAY)
                for (i in 0 until len) out.put(sanitizeValue(value.opt(i), depth + 1))
                out
            }
            is String -> sanitizeString(value, MAX_STRING)
            is Number, is Boolean -> value
            else -> sanitizeString(value.toString(), MAX_STRING)
        }
    }

    private fun sanitizeString(input: String, max: Int): String {
        val noControls =
                buildString(input.length) {
                            for (c in input) {
                                if (c == '\n' || c == '\r' || c == '\t') {
                                    append(' ')
                                } else if (!c.isISOControl()) {
                                    append(c)
                                }
                            }
                        }
                        .trim()
        val collapsed = noControls.replace(Regex("\\s+"), " ")
        return if (collapsed.length > max) collapsed.take(max) else collapsed
    }
}
