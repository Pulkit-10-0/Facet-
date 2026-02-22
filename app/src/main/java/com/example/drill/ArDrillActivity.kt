package com.example.drill

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import com.example.drill.domain.AppContainer
import com.example.drill.ui.model.ChatItem
import com.example.drill.ui.viewmodel.ChatViewModel
import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.BaseArFragment
import java.io.File

class ArDrillActivity : AppCompatActivity() {

    private lateinit var arFragment: ArFragment
    private var placedObject: AnchorNode? = null
    private var modelRenderable: ModelRenderable? = null
    private var hasAutoPlaced = false
    private lateinit var chatViewModel: ChatViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar_drill)

        // Initialize ViewModel
        val appContainer = AppContainer(applicationContext)
        chatViewModel = ChatViewModel(appContainer.facetRepository)
        
        val facetId = intent.getStringExtra(EXTRA_FACET_ID)?.trim().orEmpty()
        if (facetId.isNotBlank()) {
            chatViewModel.loadFacet(facetId)
        }

        val overlay = findViewById<ComposeView>(R.id.arOverlayCompose)
        overlay.setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        overlay.setContent {
            com.example.drill.ui.theme.DrillTheme {
                Surface(color = Color.Transparent) {
                    ArOverlay(viewModel = chatViewModel)
                }
            }
        }

        // Initialize AR Fragment with proper fragment manager
        val fragment = supportFragmentManager.findFragmentById(R.id.arFragment)
        arFragment =
                fragment as? ArFragment
                        ?: run {
                            val newFragment = ArFragment()
                            supportFragmentManager
                                    .beginTransaction()
                                    .replace(R.id.arFragment, newFragment)
                                    .commit()
                            newFragment
                        }

        arFragment.setOnTapArPlaneListener(
                object : BaseArFragment.OnTapArPlaneListener {
                    override fun onTapPlane(
                            hitResult: HitResult,
                            plane: Plane,
                            motionEvent: android.view.MotionEvent
                    ) {
                        placeObject(hitResult.createAnchor())
                    }
                }
        )

        // Auto-placement logic
        arFragment.arSceneView.scene.addOnUpdateListener {
            if (placedObject == null && modelRenderable != null && !hasAutoPlaced) {
                val frame = arFragment.arSceneView.arFrame ?: return@addOnUpdateListener
                if (frame.camera.trackingState != TrackingState.TRACKING) return@addOnUpdateListener

                // Find the first tracking plane
                val plane = frame.getUpdatedTrackables(Plane::class.java)
                    .firstOrNull { it.trackingState == TrackingState.TRACKING }
                
                if (plane != null) {
                    hasAutoPlaced = true
                    placeObject(plane.createAnchor(plane.centerPose))
                    runOnUiThread {
                         android.widget.Toast.makeText(this, "Auto-placed Model", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        val path = intent.getStringExtra(EXTRA_GLB_PATH)
        if (!path.isNullOrBlank()) {
            loadGlbFromPath(path)
        } else {
            // Fallback to assets if no path provided or empty
            loadGlbFromAssets("fallback.glb")
        }
    }

    private fun placeObject(anchor: Anchor) {
        val renderable = modelRenderable ?: return
        
        // Remove existing object if any
        placedObject?.let { arFragment.arSceneView.scene.removeChild(it) }

        placedObject =
                AnchorNode(anchor).apply {
                    this.renderable = renderable
                    arFragment.arSceneView.scene.addChild(this)
                }
    }

    private fun loadGlbFromPath(localPath: String) {
        val file = File(localPath)
        if (!file.exists() || file.length() == 0L) {
            // If file missing or empty, fallback to asset
            loadGlbFromAssets("fallback.glb")
            return
        }
        
        val source =
                RenderableSource.builder()
                        .setSource(
                                this,
                                android.net.Uri.fromFile(file),
                                RenderableSource.SourceType.GLB
                        )
                        .setRecenterMode(RenderableSource.RecenterMode.CENTER)
                        .setScale(0.5f)
                        .build()

        ModelRenderable.builder()
                .setSource(this, source)
                .setRegistryId(file.absolutePath)
                .build()
                .thenAccept { renderable: ModelRenderable -> 
                    modelRenderable = renderable
                    runOnUiThread {
                         android.widget.Toast.makeText(this, "Model Loaded (Tap to Place)", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
                .exceptionally { 
                    it.printStackTrace()
                    // If file load fails, try fallback
                    loadGlbFromAssets("fallback.glb")
                    null 
                }
    }

    private fun loadGlbFromAssets(assetName: String) {
        val source =
            RenderableSource.builder()
                .setSource(
                    this,
                    android.net.Uri.parse("file:///android_asset/$assetName"),
                    RenderableSource.SourceType.GLB
                )
                .setRecenterMode(RenderableSource.RecenterMode.CENTER)
                .setScale(0.5f)
                .build()

        ModelRenderable.builder()
            .setSource(this, source)
            .setRegistryId(assetName)
            .build()
            .thenAccept { renderable -> 
                modelRenderable = renderable 
                runOnUiThread {
                    android.widget.Toast.makeText(this, "Loaded Fallback Model", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
            .exceptionally {
                it.printStackTrace()
                null
            }
    }

    override fun onResume() {
        super.onResume()
        arFragment.onResume()
    }

    override fun onPause() {
        super.onPause()
        arFragment.onPause()
    }

    companion object {
        const val EXTRA_GLB_PATH = "glb_path"
        const val EXTRA_FACET_ID = "facet_id"
    }
}

@Composable
private fun ArOverlay(viewModel: ChatViewModel) {
    val items by viewModel.items.collectAsState()
    val input by viewModel.input.collectAsState()
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Chat List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp), // Space for input
            reverseLayout = false
        ) {
            item {
                // Guide text as header
                Box(modifier = Modifier.fillMaxWidth().padding(top = 48.dp, bottom = 16.dp)) {
                    Text(
                        text = "Scan floor & Tap to Place Model",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
            
            items(items) { item ->
                ArChatMessageRow(item)
            }
        }

        // Input Area
        Surface(
            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            tonalElevation = 3.dp
        ) {
            Row(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                OutlinedTextField(
                        value = input,
                        onValueChange = { viewModel.setInput(it) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        label = { Text("Chat with Model") }
                )
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(
                        onClick = { viewModel.send() }
                ) { Text("Send") }
            }
        }
    }
}

@Composable
fun ArChatMessageRow(item: ChatItem) {
    val isUser = item.role == ChatItem.Role.User
    val align = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    val bg = if (isUser) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) 
             else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
    val color = if (isUser) MaterialTheme.colorScheme.onPrimary 
                else MaterialTheme.colorScheme.onSurfaceVariant
    
    // Clean up thinking tags
    val displayText = item.text.replace(Regex("<thinking>.*?</thinking>", RegexOption.DOT_MATCHES_ALL), "").trim()
    if (displayText.isEmpty()) return

    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
        Column(
            modifier = Modifier
                .align(align)
                .background(bg, RoundedCornerShape(12.dp))
                .padding(8.dp)
                .width(IntrinsicSize.Max) // Wrap content width
                .widthIn(max = 280.dp) // Max width constraint
        ) {
            Text(
                text = if (isUser) "You" else "Facet",
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 0.7f)
            )
            Text(
                text = displayText,
                style = MaterialTheme.typography.bodyMedium,
                color = color
            )
        }
    }
}
