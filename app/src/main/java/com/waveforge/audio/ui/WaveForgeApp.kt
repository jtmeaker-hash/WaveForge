package com.waveforge.audio.ui
import androidx.compose.ui.unit.dp

import androidx.compose.foundation.layout.*
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

                val items = listOf("dashboard", "eq", "dsp", "playback", "settings")
                val labels = listOf("Dashboard", "EQ", "DSP", "Playback", "Settings")

                items.forEachIndexed { index, screen ->
                    NavigationBarItem(
                        selected = currentRoute?.startsWith(screen) == true,
                        onClick = {
                            navController.navigate(screen) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        label = { Text(labels[index], maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis) },
                        icon = { /* Icons here */ },
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
            modifier = Modifier.padding(padding).systemBarsPadding()
        ) {
            composable("dashboard") { DashboardScreen(viewModel) }
            composable("eq") { EqualizerScreen(viewModel) }
            composable("dsp") { DspHubScreen(navController) }
            composable("dsp/spatial") { SpatialScreen(viewModel) }
            composable("dsp/dynamics") { DynamicsScreen(viewModel) }
            composable("dsp/enhancement") { EnhancementScreen(viewModel) }
            composable("dsp/processing") { ProcessingScreen(viewModel) }
            composable("playback") { PlaybackScreen(viewModel) }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DspHubScreen(navController: androidx.navigation.NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("DSP MODULES") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp)) {
            Button(onClick = { navController.navigate("dsp/spatial") }, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Text("Spatial (Haas, Crossfeed)")
            }
            Button(onClick = { navController.navigate("dsp/dynamics") }, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Text("Dynamics (Compressor, Limiter)")
            }
            Button(onClick = { navController.navigate("dsp/enhancement") }, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Text("Enhancement (PBE, AFR)")
            }
            Button(onClick = { navController.navigate("dsp/processing") }, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Text("Processing (Channel Config, Stereo Width)")
            }
        }
    }
}
