package com.example.drill.ai

import android.content.Context
import com.cactus.CactusInitParams
import com.cactus.CactusLM
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File

object CactusEngine {
    // Reverting to Qwen3-0.6 as requested for stability
    const val MODEL_SLUG = "qwen3-0.6"
    const val CONTEXT_SIZE = 2048

    private val mutex = Mutex()
    private var lm: CactusLM? = null
    private var isInitialized: Boolean = false
    @Volatile private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    // Check if model is downloaded using the SDK's cached list of models
    // This is safer than checking file paths manually
    suspend fun isModelDownloaded(): Boolean {
        return try {
            getOrCreate().getModels().find { it.slug == MODEL_SLUG }?.isDownloaded == true
        } catch (e: Exception) {
            false
        }
    }

    // Keep this for synchronous calls but make it try to guess the path more robustly
    // or return false to force a check in ensureReady
    fun isDownloaded(): Boolean {
        // Fallback to file check if needed, but prefer suspend version where possible
        val ctx = appContext ?: return false
        // Check for both nested and flattened paths just in case
        val nested = File(ctx.filesDir, "models/$MODEL_SLUG")
        val flattened = File(ctx.filesDir, "models/${MODEL_SLUG.replace("/", "_")}")
        return nested.exists() || flattened.exists()
    }

    fun isReady(): Boolean = isInitialized && (lm?.isLoaded() == true)

    fun getOrCreate(): CactusLM {
        return lm ?: CactusLM().also { lm = it }
    }

    suspend fun ensureReady(onState: ((ModelState) -> Unit)? = null) {
        mutex.withLock {
            if (isReady()) return
            val engine = getOrCreate()
            
            // Use the SDK's authoritative check
            val alreadyDownloaded = isModelDownloaded()
            
            if (!alreadyDownloaded) {
                onState?.invoke(ModelState.Downloading(progress = 0, status = "Downloading model"))
                val finished = downloadWithProgress(engine, onState)
                if (!finished) throw IllegalStateException("Model download failed. Check internet or model slug.")
            }
            onState?.invoke(ModelState.Initializing)
            try {
                engine.initializeModel(CactusInitParams(model = MODEL_SLUG, contextSize = CONTEXT_SIZE))
                isInitialized = true
                onState?.invoke(ModelState.Ready)
            } catch (e: Exception) {
                 throw IllegalStateException("Initialization failed: ${e.message}")
            }
        }
    }

    fun unload() {
        lm?.unload()
        isInitialized = false
    }

    private suspend fun downloadWithProgress(
            engine: CactusLM,
            onState: ((ModelState) -> Unit)?
    ): Boolean {
        var progress = 0
        // Fake progress since the native SDK doesn't expose it yet
        val progressJob =
                CoroutineScope(Dispatchers.Default).launch {
                    while (progress < 90) {
                        delay(500)
                        progress += 1
                        onState?.invoke(
                                ModelState.Downloading(
                                        progress = progress,
                                        status = "Downloading model..."
                                )
                        )
                    }
                }
        return try {
            // Blocking download
            engine.downloadModel(MODEL_SLUG)
            progress = 100
            onState?.invoke(ModelState.Downloading(progress = 100, status = "Downloaded"))
            true
        } catch (e: Exception) {
            e.printStackTrace()
            // Log the actual error
            android.util.Log.e("CactusEngine", "Download failed", e)
            onState?.invoke(ModelState.Error("Download failed: ${e.message}"))
            false
        } finally {
            progressJob.cancel()
        }
    }
}
