package com.waveforge.audio.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.waveforge.audio.ui.screens.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaveForgeApp(viewModel: WaveForgeViewModel) {
    val repository = viewModel.repository
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
        modifier = Modifier.fillMaxSize(),
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
                        label = { Text(labels[index], maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis) },
                        icon = { /* Material icons can be added here */ },
                        alwaysShowLabel = true
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(padding).systemBarsPadding() // Handled here to apply uniformly
        ) {
            composable("dashboard") { DashboardScreen(viewModel) }
            composable("eq") { EqualizerScreen(viewModel) }
            composable("processing") { ProcessingScreen(viewModel) }
            composable("output") { OutputScreen(viewModel) }
            composable("settings") { 
                SettingsScreen(
                    viewModel = viewModel,
                    onRunSetupAgain = {
                        coroutineScope.launch { repository.setOnboardingComplete(false) }
                    }
                ) 
            }
        }
    }
}
