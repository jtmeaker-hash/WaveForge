package com.waveforge.audio.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.waveforge.audio.domain.AppAudioMode
import com.waveforge.audio.setup.AudioCapabilityScanner
import com.waveforge.audio.ui.components.WaveForgeCard
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    onComplete: (AppAudioMode) -> Unit
) {
    var step by remember { mutableIntStateOf(1) }
    var selectedMode by remember { mutableStateOf(AppAudioMode.FULL_PLAYER) }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (step) {
                1 -> WelcomeStep(onNext = { step = 2 })
                2 -> ModeSelectionStep(
                    selectedMode = selectedMode,
                    onModeSelect = { selectedMode = it },
                    onNext = { step = 3 }
                )
                3 -> CapabilityScanStep(
                    onNext = { onComplete(selectedMode) }
                )
            }
        }
    }
}

@Composable
private fun WelcomeStep(onNext: () -> Unit) {
    Text(
        text = "WAVEFORGE",
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 32.dp)
    )
    Text(
        text = "Shape every detail.",
        style = MaterialTheme.typography.displayLarge,
        color = MaterialTheme.colorScheme.onBackground
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = "A high-control audio processing environment built for listeners who want more than presets.",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(48.dp))
    Button(
        onClick = onNext,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Text("Begin setup", color = MaterialTheme.colorScheme.onPrimary)
    }
}

@Composable
private fun ModeSelectionStep(
    selectedMode: AppAudioMode,
    onModeSelect: (AppAudioMode) -> Unit,
    onNext: () -> Unit
) {
    Text(
        text = "PROCESSING MODE",
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(bottom = 24.dp)
    )
    
    ModeCard(
        title = "FULL DSP PLAYER",
        subtitle = "Recommended",
        description = "Audio played through WaveForge can use the complete WaveForge processing chain.\n\nThis will eventually support the full custom DSP engine.",
        isSelected = selectedMode == AppAudioMode.FULL_PLAYER,
        onClick = { onModeSelect(AppAudioMode.FULL_PLAYER) }
    )
    
    Spacer(modifier = Modifier.height(16.dp))
    
    ModeCard(
        title = "EXTERNAL SESSION",
        subtitle = "Limited feature set",
        description = "WaveForge can use Android audio effects when another player exposes a compatible audio session.\n\nWarning: Feature availability depends on the player, device and Android audio implementation.",
        isSelected = selectedMode == AppAudioMode.EXTERNAL_SESSION,
        onClick = { onModeSelect(AppAudioMode.EXTERNAL_SESSION) }
    )
    
    Spacer(modifier = Modifier.height(32.dp))
    Button(
        onClick = onNext,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text("Continue", color = MaterialTheme.colorScheme.onPrimary)
    }
}

@Composable
private fun ModeCard(
    title: String,
    subtitle: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    WaveForgeCard(
        raised = isSelected
    ) {
        Surface(
            onClick = onClick,
            color = androidx.compose.ui.graphics.Color.Transparent
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CapabilityScanStep(onNext: () -> Unit) {
    val context = LocalContext.current
    val scanner = remember { AudioCapabilityScanner(context) }
    val report = remember { scanner.scanCapabilities() }

    Text(
        text = "DEVICE AUDIO CAPABILITIES",
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(bottom = 24.dp)
    )

    WaveForgeCard {
        CapabilityRow("Dynamics Processing", report.hasDynamicsProcessing)
        CapabilityRow("Android Equalizer", report.hasEqualizer)
        CapabilityRow("Bass Boost", report.hasBassBoost)
        CapabilityRow("Loudness Enhancer", report.hasLoudnessEnhancer)
        CapabilityRow("Spatializer", report.hasSpatializer)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "API Level ${report.apiLevel}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    
    Spacer(modifier = Modifier.height(32.dp))
    Button(
        onClick = onNext,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text("Finish setup", color = MaterialTheme.colorScheme.onPrimary)
    }
}

@Composable
fun CapabilityRow(name: String, available: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = if (available) "Available" else "Unavailable",
            style = MaterialTheme.typography.labelMedium,
            color = if (available) com.waveforge.audio.ui.theme.WaveForgeSuccess else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
