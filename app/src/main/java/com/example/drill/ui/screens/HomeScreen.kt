package com.example.drill.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.drill.model.FacetType
import com.example.drill.storage.FacetEntity
import com.example.drill.ui.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
        viewModel: HomeViewModel,
        onScan: () -> Unit,
        onOpenFacet: (facetId: String) -> Unit
) {
    val facets by viewModel.facets.collectAsState()
    LaunchedEffect(Unit) { viewModel.load() }

    Surface(color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text(
                    text = "FACET",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(18.dp))
            Button(onClick = onScan, shape = MaterialTheme.shapes.small) { Text("Scan New QR") }
            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                    text = "Saved Facets",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(10.dp))
            FacetList(
                    facets = facets,
                    onOpenFacet = onOpenFacet,
                    onDeleteFacet = { id -> viewModel.deleteFacet(id) }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FacetList(
        facets: List<FacetEntity>,
        onOpenFacet: (String) -> Unit,
        onDeleteFacet: (String) -> Unit
) {
    var deleteTarget by remember { mutableStateOf<FacetEntity?>(null) }
    if (deleteTarget != null) {
        AlertDialog(
                onDismissRequest = { deleteTarget = null },
                title = { Text("Delete facet") },
                text = { Text(deleteTarget?.name.orEmpty()) },
                confirmButton = {
                    TextButton(
                            onClick = {
                                deleteTarget?.id?.let(onDeleteFacet)
                                deleteTarget = null
                            }
                    ) { Text("Delete") }
                },
                dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Cancel") } }
        )
    }

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    if (facets.isEmpty()) {
        Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                contentAlignment = Alignment.CenterStart
        ) {
            Text(
                    text = "No saved facets.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f)
            )
        }
        return
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(facets, key = { it.id }) { facet ->
            Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.extraSmall,
                    modifier =
                            Modifier.fillMaxWidth()
                                    .combinedClickable(
                                            onClick = { onOpenFacet(facet.id) },
                                            onLongClick = { deleteTarget = facet }
                                    )
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                    Text(
                            text = facet.name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                                text =
                                        when (facet.type) {
                                            FacetType.Person -> "Person"
                                            FacetType.Product -> "Product"
                                        },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                                text = dateFormat.format(Date(facet.lastOpened)),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                        )
                    }
                }
            }
        }
    }
}
