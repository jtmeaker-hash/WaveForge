package com.waveforge.audio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.waveforge.audio.engine.AudioEngine
import com.waveforge.audio.data.WaveForgePreferencesRepository
import com.waveforge.audio.ui.WaveForgeApp
import com.waveforge.audio.ui.WaveForgeViewModel
import com.waveforge.audio.ui.WaveForgeViewModelFactory
import com.waveforge.audio.ui.theme.WaveForgeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val engine = (applicationContext as WaveForgeApplication).engine
        val repository = WaveForgePreferencesRepository(applicationContext)
        val factory = WaveForgeViewModelFactory(engine, repository)
        
        setContent {
            WaveForgeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: WaveForgeViewModel = viewModel(factory = factory)
                    WaveForgeApp(viewModel)
                }
            }
        }
    }
}
