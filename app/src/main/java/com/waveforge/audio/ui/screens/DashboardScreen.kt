package com.waveforge.audio.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.waveforge.audio.ui.components.WaveForgeCard
import com.waveforge.audio.ui.components.SectionHeader

@Composable
fun DashboardScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("WAVEFORGE", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        Text("AUDIO PROCESSING", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(24.dp))
        
        WaveForgeCard {
            SectionHeader("MASTER PROCESSING")
            Text("DSP engine connection pending", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
