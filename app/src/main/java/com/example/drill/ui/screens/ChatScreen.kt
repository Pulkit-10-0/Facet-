package com.example.drill.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.drill.ui.model.ChatItem
import com.example.drill.ui.viewmodel.ChatViewModel

@Composable
fun ChatScreen(
        facetId: String,
        initialMessage: String?,
        viewModel: ChatViewModel,
        onBack: () -> Unit
) {
    val items by viewModel.items.collectAsState()
    val input by viewModel.input.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(facetId, initialMessage) { viewModel.loadFacet(facetId, initialMessage) }

    Surface(color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            TextButton(onClick = onBack) { Text("Back") }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                    text = "CHAT",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
            )
            if (!error.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                        text = error.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(items, key = { it.id }) { item ->
                    MessageRow(item)
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                        value = input,
                        onValueChange = viewModel::setInput,
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        label = { Text("Message") }
                )
                Spacer(modifier = Modifier.height(0.dp))
                TextButton(onClick = { viewModel.send() }) { Text("Send") }
            }
        }
    }
}

@Composable
private fun MessageRow(item: ChatItem) {
    val label =
            when (item.role) {
                ChatItem.Role.User -> "You"
                ChatItem.Role.Assistant -> "Facet"
                ChatItem.Role.System -> "System"
            }
    
    // Clean up thinking tags
    val displayText = item.text.replace(Regex("<thinking>.*?</thinking>", RegexOption.DOT_MATCHES_ALL), "").trim()
    if (displayText.isEmpty()) return

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (item.role == ChatItem.Role.User) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
                text = displayText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
        )
    }
}
