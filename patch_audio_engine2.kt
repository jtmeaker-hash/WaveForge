--- app/src/main/java/com/waveforge/audio/engine/AudioEngine.kt
+++ app/src/main/java/com/waveforge/audio/engine/AudioEngine.kt
@@ -233,14 +233,24 @@
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
