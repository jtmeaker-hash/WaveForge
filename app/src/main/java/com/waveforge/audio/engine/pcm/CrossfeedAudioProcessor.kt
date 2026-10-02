package com.waveforge.audio.engine.pcm

import androidx.media3.common.audio.BaseAudioProcessor
import androidx.media3.common.audio.AudioProcessor.AudioFormat
import com.waveforge.audio.engine.CrossfeedConfig
import java.nio.ByteBuffer
import kotlin.math.pow

class CrossfeedAudioProcessor : BaseAudioProcessor() {
    private var config = CrossfeedConfig()
    
    private var prevLLowpass: Float = 0f
    private var prevRLowpass: Float = 0f
    private var alpha: Float = 0f

    fun setConfig(newConfig: CrossfeedConfig) {
        val oldConfig = config
        config = newConfig
        
        if (oldConfig.cutoffHz != newConfig.cutoffHz && isActive) {
            updateFilterCoefficients(inputAudioFormat.sampleRate)
        }
    }

    private fun updateFilterCoefficients(sampleRate: Int) {
        if (sampleRate == 0) return
        val rc = 1.0 / (2.0 * Math.PI * config.cutoffHz)
        val dt = 1.0 / sampleRate
        alpha = (dt / (rc + dt)).toFloat()
    }

    override fun onConfigure(inputAudioFormat: AudioFormat): AudioFormat {
        if (!config.enabled) {
            return AudioFormat.NOT_SET
        }
        if (inputAudioFormat.channelCount != 2 || inputAudioFormat.encoding != 2) {
            return AudioFormat.NOT_SET
        }
        
        updateFilterCoefficients(inputAudioFormat.sampleRate)
        return inputAudioFormat
    }

    override fun isActive(): Boolean = config.enabled

    override fun queueInput(inputBuffer: ByteBuffer) {
        val position = inputBuffer.position()
        val limit = inputBuffer.limit()
        val frameCount = (limit - position) / 4 // 4 bytes per frame for stereo 16-bit
        val outputBuffer = replaceOutputBuffer(frameCount * 4)

        // Convert dB to linear gain
        val amountMult = config.amount / 100f
        val directGainLin = 10.0.pow(config.directLevel / 20.0).toFloat()
        val crossGainLin = 10.0.pow(config.crossfeedLevel / 20.0).toFloat() * amountMult

        for (i in 0 until frameCount) {
            val leftIn = inputBuffer.short.toFloat()
            val rightIn = inputBuffer.short.toFloat()

            // Filter for cross channel
            val lLowpass = prevLLowpass + alpha * (rightIn - prevLLowpass)
            prevLLowpass = lLowpass

            val rLowpass = prevRLowpass + alpha * (leftIn - prevRLowpass)
            prevRLowpass = rLowpass

            val leftOut = leftIn * directGainLin + lLowpass * crossGainLin
            val rightOut = rightIn * directGainLin + rLowpass * crossGainLin

            outputBuffer.putShort(leftOut.coerceIn(Short.MIN_VALUE.toFloat(), Short.MAX_VALUE.toFloat()).toInt().toShort())
            outputBuffer.putShort(rightOut.coerceIn(Short.MIN_VALUE.toFloat(), Short.MAX_VALUE.toFloat()).toInt().toShort())
        }

        inputBuffer.position(limit)
        outputBuffer.flip()
    }

    override fun onFlush() {
        prevLLowpass = 0f
        prevRLowpass = 0f
    }
    
    override fun onReset() {
        prevLLowpass = 0f
        prevRLowpass = 0f
    }
}
