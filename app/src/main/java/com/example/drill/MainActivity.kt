package com.example.drill

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.cactus.CactusContextInitializer
import com.example.drill.ai.CactusEngine
import com.example.drill.ui.DeepLink
import com.example.drill.ui.DeepLinkBus
import com.example.drill.ui.FacetApp
import com.example.drill.ui.theme.DrillTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CactusContextInitializer.initialize(this)
        CactusEngine.init(this)
        handleDeepLink(intent)
        setContent { DrillTheme { FacetApp() } }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        val facetId = intent?.getStringExtra(EXTRA_FACET_ID)?.trim().orEmpty()
        if (facetId.isBlank()) return
        val initialMessage = intent?.getStringExtra(EXTRA_INITIAL_MESSAGE)
        DeepLinkBus.publish(DeepLink(facetId = facetId, initialMessage = initialMessage))
    }

    companion object {
        const val EXTRA_FACET_ID = "facet_id"
        const val EXTRA_INITIAL_MESSAGE = "facet_initial_message"
    }
}
