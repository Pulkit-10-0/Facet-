package com.example.drill.ui.viewmodel

import com.cactus.CactusLM
import com.example.drill.ai.CactusEngine
import com.example.drill.ai.ModelState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ModelSetupViewModel(private val engine: CactusEngine = CactusEngine) : BaseViewModel() {
    private val _state =
            MutableStateFlow<ModelState>(
                    if (engine.isDownloaded()) ModelState.Initializing else ModelState.NotDownloaded
            )
    val state: StateFlow<ModelState> = _state

    private val _modelName = MutableStateFlow(CactusEngine.MODEL_SLUG)
    val modelName: StateFlow<String> = _modelName

    private val _modelSizeMb = MutableStateFlow<Int?>(null)
    val modelSizeMb: StateFlow<Int?> = _modelSizeMb

    private var started = false

    fun start() {
        if (started) return
        started = true
        scope.launch {
            discoverModelMetadata(engine.getOrCreate())
            try {
                engine.ensureReady { s -> _state.value = s }
            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = ModelState.Error(e.toString())
            }
        }
    }

    private suspend fun discoverModelMetadata(lm: CactusLM) {
        try {
            val models = lm.getModels()
            val target = models.firstOrNull { it.slug == CactusEngine.MODEL_SLUG }
            if (target != null) {
                _modelName.value = target.name.ifBlank { target.slug }
                _modelSizeMb.value = target.size_mb
            }
        } catch (_: Exception) {}
    }
}
