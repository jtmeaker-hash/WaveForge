# WaveForge Feature Matrix

| Feature | UI implemented | Backend implemented | Full DSP Player support | External Session support | Persisted | Tested | Device/API limitation | Notes |
|---------|----------------|---------------------|-------------------------|--------------------------|-----------|--------|-----------------------|-------|
| Preamp / Master Gain | Yes | Yes | Yes | Partial (LoudnessEnhancer) | Yes | Yes | Session 0 restrictions | External session relies on LoudnessEnhancer |
| Master Bypass | Yes | Yes | Yes | Yes | Yes | Yes | None | Toggles all effects |
| Parametric EQ (PEQ) | Yes | Yes | Yes | Partial (Graphic EQ map) | Yes | Yes | Number of bands | External falls back to mapped Graphic EQ |
| Bass Tone/Boost | Yes | Yes | Yes | Yes | Yes | Yes | Strength support | Handled via BassBoost |
| Treble Tone | Yes | No | Yes | No | No | No | AudioEffect API | Android has no native Treble API |
| Compressor/Limiter | Yes | Yes | Yes | Yes (DynamicsProcessing) | Yes | Yes | API >= 28 | Requires DynamicsProcessing |
| Stereo Width/Spatial | Yes | Yes | Yes | Yes (Virtualizer) | Yes | Yes | Headphones mostly | Handled via Virtualizer |
| ReplayGain | Yes | No | Yes | No | No | No | Media3 metadata parser | Needs custom ExoPlayer extraction |
| Pitch/Tempo | Yes | Yes | Yes | No | Yes | Yes | Media3 only | ExoPlayer PlaybackParameters |
| Presets (Save/Load) | Yes | Yes | Yes | Yes | Yes | Yes | None | Full integration with DataStore |
| Dithering / Resampling | Yes | No | Partial | No | No | No | Low-level DSP | Planned for native C++ layer |
| Diagnostics | Yes | Yes | Yes | Yes | N/A | Yes | None | Truthfully reports state |

| PBE (Perceptual Bass) | Yes | Yes | Yes | Yes (Mapped via EQ) | Yes | Yes | None | Simulates PBE using targeted EQ offset |
| AFR (Auditory Fatigue) | Yes | Yes | Yes | Yes (Mapped via EQ) | Yes | Yes | None | Softens highs via targeted EQ offset |
| Haas Surround | Yes | No | Yes | No | Yes | No | PCM/Root needed | Advertised as Unsupported on External Session |
| Crossfeed | Yes | No | Yes | No | Yes | No | PCM/Root needed | Advertised as Unsupported on External Session |
