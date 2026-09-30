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

    var onboardingComplete by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    if (!onboardingComplete) {
        OnboardingScreen(onComplete = { onboardingComplete = true })
        return
    }

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
            composable("settings") { 
                Column {
                    Text("Settings")
                    androidx.compose.material3.Button(onClick = { onboardingComplete = false }) {
                        Text("Run setup again")
                    }
                    val context = androidx.compose.ui.platform.LocalContext.current
                    var scanResult by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
                    androidx.compose.material3.Button(onClick = {
                        val scanner = com.waveforge.audio.setup.AudioCapabilityScanner(context)
                        val report = scanner.scanCapabilities()
                        scanResult = "API: ${report.apiLevel}, DSP: ${report.hasDynamicsProcessing}"
                    }) {
                        Text("Re-scan audio capabilities")
                    }
                    if (scanResult.isNotEmpty()) {
                        Text(scanResult)
                    }
                }
            }
        }
    }
}
