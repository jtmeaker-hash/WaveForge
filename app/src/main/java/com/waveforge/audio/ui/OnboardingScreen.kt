package com.waveforge.audio.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.waveforge.audio.setup.AudioCapabilityScanner

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    var step by remember { mutableIntStateOf(1) }
    val context = LocalContext.current
    val scanner = remember { AudioCapabilityScanner(context) }

    Column(modifier = Modifier.padding(16.dp)) {
        when (step) {
            1 -> {
                Text("Welcome to WaveForge, an advanced Rockbox-inspired audio processor.")
                Button(onClick = { step = 2 }) { Text("Start Setup") }
            }
            2 -> {
                Text("Choose Primary Mode")
                Text("1) Full DSP Player - Applies only to audio played through this app.")
                Text("2) External Session EQ - Attempts to attach to other media players.")
                Button(onClick = { step = 3 }) { Text("Select Full DSP & Continue") }
            }
            3 -> {
                val report = remember { scanner.scanCapabilities() }
                Text("Device Capabilities Scan:")
                Text("API Level: ${report.apiLevel}")
                Text("DynamicsProcessing: ${report.hasDynamicsProcessing}")
                Text("Test Equalizer: ${report.canCreateTestEqualizer}")
                Text("Spatializer: ${report.hasSpatializer}")
                Button(onClick = { step = 4 }) { Text("Next") }
            }
            4 -> {
                Text("Media Access: Choose music/files when needed via Storage Access Framework.")
                Button(onClick = { step = 5 }) { Text("Next") }
            }
            5 -> {
                Text("Notifications: This app uses a media foreground service to process audio in the background.")
                Button(onClick = { step = 6 }) { Text("Next") }
            }
            6 -> {
                Text("Default Sound Profile: Flat / Safe.")
                Button(onClick = { step = 7 }) { Text("Next") }
            }
            7 -> {
                Text("Output Profile: Default Speaker.")
                Button(onClick = { step = 8 }) { Text("Next") }
            }
            8 -> {
                Text("A/B Audio Check (Setup test only).")
                Button(onClick = { step = 9 }) { Text("Next") }
            }
            9 -> {
                Text("Setup Complete! Selected Mode: Full DSP Player.")
                Button(onClick = onComplete) { Text("Finish") }
            }
        }
    }
}
