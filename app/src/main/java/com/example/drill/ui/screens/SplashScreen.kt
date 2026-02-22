package com.example.drill.ui.screens

import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import com.example.drill.ai.CactusEngine
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onRoute: (hasModelDownloaded: Boolean) -> Unit) {
    var visible by remember { mutableStateOf(false) }
    val alpha by
            androidx.compose.animation.core.animateFloatAsState(
                    targetValue = if (visible) 1f else 0f,
                    animationSpec = tween(durationMillis = 420),
                    label = "facet_splash_alpha"
            )

    LaunchedEffect(Unit) {
        visible = true
        delay(800)
        onRoute(CactusEngine.isDownloaded())
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
                text = "FACET",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.alpha(alpha),
                color = MaterialTheme.colorScheme.onBackground
        )
    }
}
