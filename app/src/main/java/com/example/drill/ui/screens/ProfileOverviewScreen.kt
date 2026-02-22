package com.example.drill.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.drill.model.FacetProfile
import com.example.drill.ui.viewmodel.ProfileViewModel

@Composable
fun ProfileOverviewScreen(
        facetId: String,
        viewModel: ProfileViewModel,
        onBack: () -> Unit,
        onOpenAr: (localGlbPath: String) -> Unit,
        onTalk: () -> Unit,
        onDelete: () -> Unit
) {
    val profile by viewModel.profile.collectAsState()
    val entity by viewModel.entity.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(facetId) { viewModel.load(facetId) }

    var showDelete by remember { mutableStateOf(false) }
    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurface,
            title = { Text("Delete Facet?") },
            text = { Text("This will remove the downloaded facet and model from local storage.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDelete = false
                        onDelete()
                    }
                ) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDelete = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    }

    Surface(color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            TextButton(onClick = onBack) { Text("Back") }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                    text = "PROFILE",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(14.dp))

            if (!error.isNullOrBlank()) {
                Text(text = error.orEmpty(), color = MaterialTheme.colorScheme.error)
                return@Column
            }

            when (val p = profile) {
                null -> {
                    Text(
                            text = "Loading.",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f)
                    )
                }
                is FacetProfile.Person -> {
                    Text(
                            text = p.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                            text = p.title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                            text = p.bio,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground
                    )
                }
                is FacetProfile.Product -> {
                    Text(
                            text = p.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                            text = p.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground
                    )
                    if (p.keyFeatures.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                                text = "Key features",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        for (f in p.keyFeatures.take(8)) {
                            Text(
                                    text = "• $f",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color =
                                            MaterialTheme.colorScheme.onBackground.copy(
                                                    alpha = 0.9f
                                            )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                        onClick = { entity?.localGlbPath?.let(onOpenAr) },
                        enabled = entity != null
                ) { Text("Open in AR") }
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = onTalk, enabled = entity != null) { Text("Talk") }
            }
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(onClick = { showDelete = true }) { Text("Delete") }
        }
    }
}
