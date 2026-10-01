package com.waveforge.audio.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.waveforge.audio.ui.WaveForgeViewModel
import com.waveforge.audio.ui.components.WaveForgeCard
import com.waveforge.audio.ui.components.SectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicsScreen(viewModel: WaveForgeViewModel) {
    val diagnostics by viewModel.diagnosticsInfo.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("DYNAMICS") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp).verticalScroll(rememberScrollState())
        ) {
            
            WaveForgeCard {
                SectionHeader("COMPRESSOR")
                Spacer(modifier = Modifier.height(8.dp))
                val cap = diagnostics.capabilities["Compressor"]?.name ?: "UNAVAILABLE_NO_SESSION"
                if (cap.contains("UNSUPPORTED")) {
                    Text("Not supported by current session/device.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                } else {
                    Text("Status: $cap", style = MaterialTheme.typography.bodyMedium)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            WaveForgeCard {
                SectionHeader("LIMITER")
                Spacer(modifier = Modifier.height(8.dp))
                val cap = diagnostics.capabilities["Limiter"]?.name ?: "UNAVAILABLE_NO_SESSION"
                if (cap.contains("UNSUPPORTED")) {
                    Text("Not supported by current session/device.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                } else {
                    Text("Status: $cap", style = MaterialTheme.typography.bodyMedium)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

        }
    }
}
