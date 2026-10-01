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
    val context = LocalContext.current
    val eqCreated by viewModel.eqCreated.collectAsState()
    val eqHasControl by viewModel.eqHasControl.collectAsState()
    val eqEnabled by viewModel.eqEnabled.collectAsState()
    val eqBands by viewModel.eqBands.collectAsState()
    val bassCreated by viewModel.bassCreated.collectAsState()
    val bassHasControl by viewModel.bassHasControl.collectAsState()
    val bassStrengthSupported by viewModel.bassStrengthSupported.collectAsState()
    val loudnessCreated by viewModel.loudnessCreated.collectAsState()
    val loudnessHasControl by viewModel.loudnessHasControl.collectAsState()
    val lastSessionEvent by viewModel.lastSessionEvent.collectAsState()
    val engineState by viewModel.engineState.collectAsState()
    val lastError by viewModel.lastError.collectAsState()

    val activePackage = if (engineState is EngineState.Attached) (engineState as EngineState.Attached).packageName else "None"
    val activeSessionId = if (engineState is EngineState.Attached) (engineState as EngineState.Attached).sessionId else -1

    val diagnosticText = """
        Engine enabled: true
        Service running: true
        Active package: $activePackage
        Active audio session ID: $activeSessionId
        Last session event: $lastSessionEvent
        Equalizer created: $eqCreated
        Equalizer has control: $eqHasControl
        Equalizer enabled: $eqEnabled
        EQ bands: ${eqBands.size}
        BassBoost created: $bassCreated
        BassBoost has control: $bassHasControl
        BassBoost strength supported: $bassStrengthSupported
        LoudnessEnhancer created: $loudnessCreated
        Loudness has control: $loudnessHasControl
        Last engine error: ${lastError ?: "None"}
    """.trimIndent()

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
                SectionHeader("DIAGNOSTICS")
                Text(diagnosticText, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 8.dp))
                
                Button(onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("WaveForge Diagnostics", diagnosticText))
                }) {
                    Text("Copy diagnostics")
                }
            }
        }
    }
}
