package com.waveforge.audio.ui.screens

import android.media.AudioManager
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.waveforge.audio.ui.WaveForgeViewModel
import com.waveforge.audio.ui.components.WaveForgeCard
import com.waveforge.audio.ui.components.SectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutputScreen(viewModel: WaveForgeViewModel) {
    val context = LocalContext.current
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    val isWiredHeadsetOn = audioManager.isWiredHeadsetOn
    val isBluetoothA2dpOn = audioManager.isBluetoothA2dpOn

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CURRENT OUTPUT") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp)) {
            WaveForgeCard {
                Text("Wired Headset: ${if (isWiredHeadsetOn) "Connected" else "Disconnected"}")
                Text("Bluetooth A2DP: ${if (isBluetoothA2dpOn) "Connected" else "Disconnected"}")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Advanced routing info is unsupported on this device.", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
