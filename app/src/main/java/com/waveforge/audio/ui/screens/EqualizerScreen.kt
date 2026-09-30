package com.waveforge.audio.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.waveforge.audio.ui.components.WaveForgeCard
import com.waveforge.audio.ui.components.SectionHeader

@Composable
fun EqualizerScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        SectionHeader("PARAMETRIC EQ")
        Text("Precision frequency control", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(16.dp))
        
        val primaryColor = MaterialTheme.colorScheme.primary
        val gridColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
        
        WaveForgeCard(modifier = Modifier.height(200.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                // Draw mock grid
                for (i in 1..4) {
                    drawLine(gridColor, Offset(0f, h * i / 5), Offset(w, h * i / 5))
                }
                for (i in 1..9) {
                    drawLine(gridColor, Offset(w * i / 10, 0f), Offset(w * i / 10, h))
                }
                // Draw flat response
                drawLine(primaryColor, Offset(0f, h / 2), Offset(w, h / 2), strokeWidth = 4f)
            }
        }
    }
}
