package com.waveforge.audio.ui.screens

import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.waveforge.audio.ui.WaveForgeViewModel
import com.waveforge.audio.ui.components.WaveForgeCard
import com.waveforge.audio.ui.components.SectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: WaveForgeViewModel, onRunSetupAgain: () -> Unit) {
    val clipboardManager = LocalClipboardManager.current
    val engineState by viewModel.engineState.collectAsState()
    val error by viewModel.lastError.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SETTINGS") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp).verticalScroll(rememberScrollState())
        ) {
            WaveForgeCard {
                SectionHeader("ONBOARDING")
                Button(onClick = onRunSetupAgain, modifier = Modifier.fillMaxWidth()) {
                    Text("Run setup again")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            WaveForgeCard {
                SectionHeader("DIAGNOSTICS")
                Text("WaveForge version: 1.0", style = MaterialTheme.typography.bodyMedium)
                Text("Android version: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})", style = MaterialTheme.typography.bodyMedium)
                Text("Device model: ${Build.MANUFACTURER} ${Build.MODEL}", style = MaterialTheme.typography.bodyMedium)
                Text("Audio engine state: $engineState", style = MaterialTheme.typography.bodyMedium)
                if (error != null) {
                    Text("Last error: $error", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val diag = """WaveForge 1.0
Android ${Build.VERSION.RELEASE}
Device: ${Build.MODEL}
Engine: $engineState
Error: $error"""
                        clipboardManager.setText(AnnotatedString(diag))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Copy diagnostics")
                }
            }
        }
    }
}
