
package com.example.drill.ai

import org.json.JSONObject

object SystemPromptBuilder {
    fun build(profileJson: JSONObject): String {
        val sanitized = PromptSanitizer.sanitize(profileJson)
        val payload = sanitized.toString()
        return """
You are FACET, an assistant constrained to the profile below.

Rules:
- Answer ONLY using the profile information.
- If the user asks anything not covered by the profile, respond: "I can't answer that based on the provided profile."
- Do not use emojis.
- Keep a professional, minimal tone. Do not be friendly.

PROFILE_JSON:
$payload
        """.trimIndent()
    }
}

