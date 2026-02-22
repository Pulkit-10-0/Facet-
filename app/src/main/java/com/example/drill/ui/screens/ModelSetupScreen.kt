package com.example.drill.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.drill.ai.ModelState
import com.example.drill.ui.viewmodel.ModelSetupViewModel

@Composable
fun ModelSetupScreen(viewModel: ModelSetupViewModel, onReady: () -> Unit) {
    val state by viewModel.state.collectAsState()
    val modelName by viewModel.modelName.collectAsState()
    val sizeMb by viewModel.modelSizeMb.collectAsState()

    LaunchedEffect(Unit) { viewModel.start() }
    LaunchedEffect(state) { if (state is ModelState.Ready) onReady() }

    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
                modifier = Modifier.fillMaxSize().padding(20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
        ) {
            Text(
                    text = "MODEL SETUP",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                    text = "FunctionGemma 270M", // Displaying requested name
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                    text = sizeMb?.let { "$it MB" } ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f)
            )
            Spacer(modifier = Modifier.height(18.dp))

            when (val s = state) {
                ModelState.NotDownloaded -> {
                    Text(
                            text = "Preparing download.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator()
                }
                is ModelState.Downloading -> {
                    Text(
                            text = "${s.status} • ${s.progress}%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(progress = { (s.progress.coerceIn(0, 100) / 100f) })
                }
                ModelState.Initializing -> {
                    Text(
                            text = "Initializing.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator()
                }
                ModelState.Ready -> {
                    Text(
                            text = "Ready.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f)
                    )
                }
                is ModelState.Error -> {
                    Text(
                            text = "Error: ${s.message}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
