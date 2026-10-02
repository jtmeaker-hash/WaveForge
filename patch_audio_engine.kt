--- app/src/main/java/com/waveforge/audio/engine/AudioEngine.kt
+++ app/src/main/java/com/waveforge/audio/engine/AudioEngine.kt
@@ -142,8 +142,15 @@
         b.setLoudnessGain(_loudnessGain.value)
         
         val st = _dspState.value
-        b.setHaasSurround(!bypass && st.haas.enabled, st.haas.delayMs, st.haas.amount, 0, 0, false, st.haas.width)
-        b.setCrossfeed(if (!bypass && st.crossfeed.enabled) "Custom" else "Off", st.crossfeed.directLevel, st.crossfeed.crossfeedLevel, 0, st.crossfeed.cutoffHz)
+        if (b is com.waveforge.audio.engine.pcm.PcmDspBackend) {
+            b.setHaasSurroundConfig(st.haas.copy(enabled = !bypass && st.haas.enabled))
+            b.setCrossfeedConfig(st.crossfeed.copy(enabled = !bypass && st.crossfeed.enabled))
+        } else {
+            b.setHaasSurround(!bypass && st.haas.enabled, st.haas.delayMs, st.haas.amount, 0, 0, false, st.haas.width)
+            b.setCrossfeed(if (!bypass && st.crossfeed.enabled) "Custom" else "Off", st.crossfeed.directLevel, st.crossfeed.crossfeedLevel, 0, st.crossfeed.cutoffHz)
+        }
+        
         b.setCompressor(!bypass && st.compressor.enabled, st.compressor.threshold, st.compressor.makeupGain, st.compressor.ratio, st.compressor.knee, st.compressor.attackMs, st.compressor.releaseMs)
         b.setLimiter(!bypass && st.limiter.enabled, st.limiter.threshold)
         b.setPerceptualBass(if (!bypass && st.pbe.enabled) st.pbe.strength else 0, st.pbe.preCut)
@@ -233,14 +240,24 @@
     fun updateHaasConfig(config: HaasConfig) {
         _dspState.update { it.copy(haas = config) }
         val bypass = !_dspState.value.masterEnabled
-        backend?.setHaasSurround(!bypass && config.enabled, config.delayMs, config.amount, 0, 0, false, config.width)
+        val b = backend
+        if (b is com.waveforge.audio.engine.pcm.PcmDspBackend) {
+            b.setHaasSurroundConfig(config.copy(enabled = !bypass && config.enabled))
+        } else {
+            b?.setHaasSurround(!bypass && config.enabled, config.delayMs, config.amount, 0, 0, false, config.width)
+        }
     }
 
     fun updateCrossfeedConfig(config: CrossfeedConfig) {
         _dspState.update { it.copy(crossfeed = config) }
         val bypass = !_dspState.value.masterEnabled
         val mode = if (!bypass && config.enabled) "Custom" else "Off"
-        backend?.setCrossfeed(mode, config.directLevel, config.crossfeedLevel, 0, config.cutoffHz)
+        val b = backend
+        if (b is com.waveforge.audio.engine.pcm.PcmDspBackend) {
+            b.setCrossfeedConfig(config.copy(enabled = !bypass && config.enabled))
+        } else {
+            b?.setCrossfeed(mode, config.directLevel, config.crossfeedLevel, 0, config.cutoffHz)
+        }
     }
     
     fun updateCompressorConfig(config: CompressorConfig) {
