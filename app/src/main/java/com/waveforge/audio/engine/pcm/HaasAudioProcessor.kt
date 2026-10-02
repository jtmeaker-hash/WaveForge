package com.waveforge.audio.engine.pcm

import androidx.media3.common.audio.BaseAudioProcessor
import androidx.media3.common.audio.AudioProcessor.AudioFormat
import com.waveforge.audio.engine.HaasConfig
import java.nio.ByteBuffer

class HaasAudioProcessor : BaseAudioProcessor() {
    private var config = HaasConfig()
    private var delayBuffer: FloatArray = FloatArray(0)
    private var writeIndex: Int = 0

    fun setConfig(newConfig: HaasConfig) {
        val wasEnabled = config.enabled
        config = newConfig
        if (wasEnabled != newConfig.enabled) {
            // Need to flush/reconfigure when active state changes, ExoPlayer does this via active state
        }
    }

    override fun onConfigure(inputAudioFormat: AudioFormat): AudioFormat {
        if (!config.enabled) {
            return AudioFormat.NOT_SET
        }
        if (inputAudioFormat.channelCount != 2 || inputAudioFormat.encoding != 2) {
            return AudioFormat.NOT_SET
        }

        val requiredBufferLength = (inputAudioFormat.sampleRate * 100 / 1000) // max 100ms
        if (delayBuffer.size != requiredBufferLength) {
            delayBuffer = FloatArray(requiredBufferLength)
            writeIndex = 0
        }
        return inputAudioFormat
    }

    override fun isActive(): Boolean = config.enabled

    override fun queueInput(inputBuffer: ByteBuffer) {
        val position = inputBuffer.position()
        val limit = inputBuffer.limit()
        val frameCount = (limit - position) / 4 // 4 bytes per frame for stereo 16-bit
        val outputBuffer = replaceOutputBuffer(frameCount * 4)

        val sampleRate = inputAudioFormat.sampleRate
        val delayFrames = (sampleRate * config.delayMs / 1000).coerceIn(0, delayBuffer.size - 1)
        val wet = config.amount / 100f
        val dry = 1.0f - wet
        val delayRight = config.delayedChannel == "Right"

        for (i in 0 until frameCount) {
            val leftIn = inputBuffer.short
            val rightIn = inputBuffer.short

            val readIndex = (writeIndex - delayFrames + delayBuffer.size) % delayBuffer.size
            var leftOut = leftIn.toFloat()
            var rightOut = rightIn.toFloat()

            if (delayRight) {
                delayBuffer[writeIndex] = rightIn.toFloat()
                val delayedRight = delayBuffer[readIndex]
                rightOut = dry * rightIn + wet * delayedRight
            } else {
                delayBuffer[writeIndex] = leftIn.toFloat()
                val delayedLeft = delayBuffer[readIndex]
                leftOut = dry * leftIn + wet * delayedLeft
            }

            writeIndex = (writeIndex + 1) % delayBuffer.size

            outputBuffer.putShort(leftOut.coerceIn(Short.MIN_VALUE.toFloat(), Short.MAX_VALUE.toFloat()).toInt().toShort())
            outputBuffer.putShort(rightOut.coerceIn(Short.MIN_VALUE.toFloat(), Short.MAX_VALUE.toFloat()).toInt().toShort())
        }

        inputBuffer.position(limit)
        outputBuffer.flip()
    }
    
    override fun onFlush() {
        writeIndex = 0
        delayBuffer.fill(0f)
    }
    
    override fun onReset() {
        delayBuffer = FloatArray(0)
        writeIndex = 0
    }
}
