package com.waveforge.audio.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.waveforge.audio.data.WaveForgePreferencesRepository
import com.waveforge.audio.ui.screens.*
import kotlinx.coroutines.launch

@Composable
fun WaveForgeApp() {
    val context = LocalContext.current
    val repository = remember { WaveForgePreferencesRepository(context) }
    val onboardingComplete by repository.onboardingComplete.collectAsState(initial = false)
    val coroutineScope = rememberCoroutineScope()
    val navController = rememberNavController()

    if (!onboardingComplete) {
        OnboardingScreen(onComplete = { mode ->
            coroutineScope.launch {
                repository.setAudioMode(mode)
                repository.setOnboardingComplete(true)
            }
        })
        return
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val items = listOf("dashboard", "eq", "processing", "output", "settings")
                val labels = listOf("Dashboard", "EQ", "Processing", "Output", "Settings")

                items.forEachIndexed { index, screen ->
                    NavigationBarItem(
                        selected = currentRoute == screen,
                        onClick = {
                            navController.navigate(screen) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        label = { Text(labels[index]) },
                        icon = { /* Material icons can be added here */ }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(padding)
        ) {
            composable("dashboard") { DashboardScreen() }
            composable("eq") { EqualizerScreen() }
            composable("processing") { ProcessingScreen() }
            composable("output") { OutputScreen() }
            composable("settings") { 
                SettingsScreen(
                    onRunSetupAgain = {
                        coroutineScope.launch { repository.setOnboardingComplete(false) }
                    }
                ) 
            }
        }
    }
}
