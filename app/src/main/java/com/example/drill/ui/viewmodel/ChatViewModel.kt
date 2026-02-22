package com.example.drill.ui.viewmodel

import com.cactus.CactusCompletionParams
import com.cactus.ChatMessage
import com.cactus.InferenceMode
import com.example.drill.ai.CactusEngine
import com.example.drill.ai.SystemPromptBuilder
import com.example.drill.domain.FacetRepository
import com.example.drill.ui.model.ChatItem
import java.io.File
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class ChatViewModel(
        private val repo: FacetRepository,
        private val engine: CactusEngine = CactusEngine
) : BaseViewModel() {
    private val _items = MutableStateFlow<List<ChatItem>>(emptyList())
    val items: StateFlow<List<ChatItem>> = _items

    private val _input = MutableStateFlow("")
    val input: StateFlow<String> = _input

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var systemPrompt: String? = null

    fun setInput(value: String) {
        _input.value = value
    }

    fun loadFacet(facetId: String, initialMessage: String? = null) {
        scope.launch {
            _error.value = null
            try {
                val ent = repo.getFacet(facetId) ?: throw IllegalStateException("Facet not found")
                val json = JSONObject(File(ent.localJsonPath).readText(Charsets.UTF_8))
                systemPrompt = SystemPromptBuilder.build(json)
                _items.value = emptyList()
                repo.markOpened(facetId, System.currentTimeMillis())
                val initial = initialMessage?.trim().orEmpty()
                if (initial.isNotEmpty()) {
                    _input.value = initial
                    send()
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load chat."
            }
        }
    }

    fun send() {
        val text = _input.value.trim()
        if (text.isEmpty()) return
        _input.value = ""

        val userItem =
                ChatItem(id = UUID.randomUUID().toString(), role = ChatItem.Role.User, text = text)
        val assistantId = UUID.randomUUID().toString()
        val assistantItem = ChatItem(id = assistantId, role = ChatItem.Role.Assistant, text = "")
        _items.value = _items.value + userItem + assistantItem

        scope.launch {
            try {
                engine.ensureReady()
                val prompt = systemPrompt ?: throw IllegalStateException("Missing profile")
                val messages = buildCactusMessages(prompt, _items.value)

                withContext(Dispatchers.Default) {
                    engine.getOrCreate()
                            .generateCompletion(
                                    messages = messages,
                                    params =
                                            CactusCompletionParams(
                                                    mode = InferenceMode.LOCAL_FIRST,
                                                    maxTokens = 240,
                                                    temperature = 0.2
                                            ),
                                    onToken = { token, _ ->
                                        scope.launch { appendAssistantToken(assistantId, token) }
                                    }
                            )
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Generation failed."
            }
        }
    }

    private fun buildCactusMessages(
            systemPrompt: String,
            items: List<ChatItem>
    ): List<ChatMessage> {
        val out = ArrayList<ChatMessage>(items.size + 1)
        out += ChatMessage(content = systemPrompt, role = "system")
        for (item in items) {
            when (item.role) {
                ChatItem.Role.User -> out += ChatMessage(content = item.text, role = "user")
                ChatItem.Role.Assistant ->
                        if (item.text.isNotBlank())
                                out += ChatMessage(content = item.text, role = "assistant")
                ChatItem.Role.System -> {}
            }
        }
        return out
    }

    private fun appendAssistantToken(assistantId: String, token: String) {
        val current = _items.value
        val idx = current.indexOfFirst { it.id == assistantId }
        if (idx < 0) return
        val updated = current[idx].copy(text = current[idx].text + token)
        val next = current.toMutableList()
        next[idx] = updated
        _items.value = next
    }
}
