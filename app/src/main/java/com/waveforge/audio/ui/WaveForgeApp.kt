package com.waveforge.audio.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun WaveForgeApp() {
    val navController = rememberNavController()

    Scaffold { padding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(padding)
        ) {
            composable("home") {
                Column {
                    Text("Audio Engine Status: READY")
                    Text("Master DSP Bypass: OFF")
                }
            }
            composable("equalizer") { Text("Equalizer") }
            composable("spatial") { Text("Spatial") }
            composable("dynamics") { Text("Dynamics") }
            composable("playback") { Text("Playback") }
            composable("presets") { Text("Presets") }
            composable("devices") { Text("Devices") }
            composable("settings") { Text("Settings") }
        }
    }
}
