package com.waveforge.audio.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.waveforge.audio.ui.components.WaveForgeCard
import com.waveforge.audio.ui.components.SectionHeader

@Composable
fun OutputScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        SectionHeader("CURRENT OUTPUT")
        Spacer(modifier = Modifier.height(16.dp))
        WaveForgeCard {
            Text("Not reported by Android", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
