package com.austin.aiapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.austin.aiapp.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: ChatViewModel = viewModel()
) {
    val serverUrl by viewModel.serverUrl.collectAsState()
    val modelName by viewModel.modelName.collectAsState()

    var urlText by remember { mutableStateOf(serverUrl) }
    var modelText by remember { mutableStateOf(modelName) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Server URL (Tailscale IP)", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = urlText,
                onValueChange = { urlText = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("http://100.x.x.x:11434/v1") },
                singleLine = true
            )

            Text("Model name", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = modelText,
                onValueChange = { modelText = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("gemma-4-abliterated") },
                singleLine = true
            )

            Button(
                onClick = {
                    viewModel.updateServerUrl(urlText.trim())
                    viewModel.updateModelName(modelText.trim())
                    onBack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }

            Text(
                "Tip: Use your Tailscale IP (100.x.x.x). For Ollama keep the /v1 suffix.\n\n" +
                "Ollama low-latency flags already documented in README.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
