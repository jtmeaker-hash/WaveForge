package com.waveforge.audio.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.waveforge.audio.ui.components.WaveForgeCard
import com.waveforge.audio.ui.components.SectionHeader

@Composable
fun SettingsScreen(onRunSetupAgain: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        SectionHeader("SETTINGS")
        Spacer(modifier = Modifier.height(16.dp))
        WaveForgeCard {
            Button(onClick = onRunSetupAgain, modifier = Modifier.fillMaxWidth()) {
                Text("Run setup again")
            }
        }
    }
}
