--- app/src/main/java/com/waveforge/audio/engine/AudioEngine.kt
+++ app/src/main/java/com/waveforge/audio/engine/AudioEngine.kt
@@ -125,6 +125,15 @@
         }
     }
 
+    fun attachPcmBackend(pcmBackend: com.waveforge.audio.engine.pcm.PcmDspBackend) {
+        release()
+        backend = pcmBackend
+        _activeSessionId.value = -1 // internal PCM
+        _activePackageName.value = "com.waveforge.audio"
+        _engineState.value = EngineState.Attached(-1, "com.waveforge.audio", "Internal PCM Engine")
+        applyCurrentStateToBackend(pcmBackend)
+        updateDiagnostics()
+    }
+
     private fun applyCurrentStateToBackend(b: AudioProcessingBackend?) {
         if (b == null) return
