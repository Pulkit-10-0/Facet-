package com.example.drill.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.drill.storage.FacetEntity
import com.example.drill.ui.viewmodel.ScanViewModel
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

@Composable
fun ScanScreen(viewModel: ScanViewModel, onBack: () -> Unit, onImported: (FacetEntity) -> Unit) {
    val url by viewModel.url.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val launcher =
            rememberLauncherForActivityResult(contract = ScanContract()) { result ->
                val contents = result.contents?.trim().orEmpty()
                if (contents.isNotEmpty()) {
                    viewModel.setUrl(contents)
                }
            }

    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.Top
        ) {
            TextButton(onClick = onBack) { Text("Back") }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                    text = "SCAN",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                    onClick = {
                        val options =
                                ScanOptions().apply {
                                    setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                                    setBeepEnabled(false)
                                    setPrompt("")
                                }
                        launcher.launch(options)
                    },
                    enabled = !isLoading
            ) { Text("Open Camera Scanner") }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                    value = url,
                    onValueChange = viewModel::setUrl,
                    label = { Text("Facet URL (HTTPS)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = { viewModel.import(onImported) }, enabled = !isLoading) {
                Text(if (isLoading) "Importing" else "Import")
            }
            if (!error.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                        text = error.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
