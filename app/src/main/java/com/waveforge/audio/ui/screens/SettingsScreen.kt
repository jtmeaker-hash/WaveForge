package com.waveforge.audio.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.waveforge.audio.engine.EngineState
import com.waveforge.audio.ui.WaveForgeViewModel
import com.waveforge.audio.ui.components.SectionHeader
import com.waveforge.audio.ui.components.WaveForgeCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: WaveForgeViewModel, onRunSetupAgain: () -> Unit) {
    val navController = androidx.navigation.compose.rememberNavController()
    val context = LocalContext.current
    val diagnostics by viewModel.diagnosticsInfo.collectAsState()
    val lastSessionEvent by viewModel.lastSessionEvent.collectAsState()

    val diagnosticText = """
        Processing Mode: ${diagnostics.processingMode}
        Active Package: ${diagnostics.activePackage ?: "None"}
        Active Session: ${diagnostics.activeSessionId ?: "None"}
        Last Event: $lastSessionEvent
        Native EQ Bands: ${diagnostics.nativeEqBandCount}
        
        Capabilities:
        ${diagnostics.capabilities.entries.joinToString("\n        ") { "${it.key}: ${it.value}" }}
        
        Errors:
        ${diagnostics.errors.joinToString("\n        ")}
    """.trimIndent()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SETTINGS & DIAGNOSTICS") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp).verticalScroll(rememberScrollState())
        ) {
            WaveForgeCard {
                SectionHeader("DIAGNOSTICS")
                Text(diagnosticText, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 8.dp))
                
                Button(onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("WaveForge Diagnostics", diagnosticText))
                }) {
                    Text("Copy diagnostics")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(onClick = onRunSetupAgain, modifier = Modifier.fillMaxWidth()) {
                Text("Run Setup Again")
            }
        }
    }
}
