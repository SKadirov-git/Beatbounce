package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

class SoundEffects {
    private val sampleRate = 22050
    private var volume = 0.8f
    private var isMuted = false
    private val scope = CoroutineScope(Dispatchers.Default)

    // Pre-synthesized SFX buffers
    private val jumpBuffer = synthesizeJump()
    private val normalLandingBuffer = synthesizeNormalLanding()
    private val perfectLandingBuffer = synthesizePerfectLanding()
    private val excellentLandingBuffer = synthesizeExcellentLanding()
    private val comboMilestoneBuffer = synthesizeComboMilestone()
    private val coinBuffer = synthesizeCoin()
    private val clickBuffer = synthesizeClick()
    private val gameOverBuffer = synthesizeGameOver()

    fun setVolume(vol: Float) {
        volume = vol.coerceIn(0f, 1f)
    }

    fun setMuted(muted: Boolean) {
        isMuted = muted
    }

    fun playJump() = playBuffer(jumpBuffer)
    fun playNormalLanding() = playBuffer(normalLandingBuffer)
    fun playPerfectLanding() = playBuffer(perfectLandingBuffer)
    fun playExcellentLanding() = playBuffer(excellentLandingBuffer)
    fun playComboMilestone() = playBuffer(comboMilestoneBuffer)
    fun playCoin() = playBuffer(coinBuffer)
    fun playClick() = playBuffer(clickBuffer)
    fun playGameOver() = playBuffer(gameOverBuffer)

    private fun playBuffer(buffer: ShortArray) {
        if (isMuted || volume <= 0.01f) return
        scope.launch {
            try {
                val scaledBuffer = ShortArray(buffer.size) { i ->
                    (buffer[i] * volume).toInt().coerceIn(-32768, 32767).toShort()
                }
                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(scaledBuffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(scaledBuffer, 0, scaledBuffer.size)
                audioTrack.play()

                // Release after playback finishes
                val durationMs = (scaledBuffer.size * 1000L) / sampleRate + 50
                kotlinx.coroutines.delay(durationMs)
                audioTrack.release()
            } catch (_: Exception) {
                // Ignore audio glitches if device is under heavy load
            }
        }
    }

    private fun synthesizeJump(): ShortArray {
        val durationMs = 120
        val count = (sampleRate * durationMs) / 1000
        val buf = ShortArray(count)
        for (i in 0 until count) {
            val t = i.toFloat() / count
            val freq = 200f + t * 450f
            val env = 1f - t
            val sample = sin(2.0 * PI * freq * i / sampleRate).toFloat() * env
            buf[i] = (sample * 16000f).toInt().toShort()
        }
        return buf
    }

    private fun synthesizeNormalLanding(): ShortArray {
        val durationMs = 140
        val count = (sampleRate * durationMs) / 1000
        val buf = ShortArray(count)
        for (i in 0 until count) {
            val t = i.toFloat() / count
            val freq = 180f * exp(-t * 6f)
            val env = exp(-t * 5f)
            val sample = sin(2.0 * PI * freq * i / sampleRate).toFloat() * env
            buf[i] = (sample * 24000f).toInt().toShort()
        }
        return buf
    }

    private fun synthesizePerfectLanding(): ShortArray {
        val durationMs = 280
        val count = (sampleRate * durationMs) / 1000
        val buf = ShortArray(count)
        for (i in 0 until count) {
            val t = i.toFloat() / count
            val kick = sin(2.0 * PI * (140f * exp(-t * 8f)) * i / sampleRate).toFloat() * exp(-t * 6f)
            val chime1 = sin(2.0 * PI * 880f * i / sampleRate).toFloat() * exp(-t * 4f)
            val chime2 = sin(2.0 * PI * 1320f * i / sampleRate).toFloat() * exp(-t * 5f)
            val sample = kick * 0.6f + chime1 * 0.25f + chime2 * 0.15f
            buf[i] = (sample * 26000f).toInt().toShort()
        }
        return buf
    }

    private fun synthesizeExcellentLanding(): ShortArray {
        val durationMs = 380
        val count = (sampleRate * durationMs) / 1000
        val buf = ShortArray(count)
        for (i in 0 until count) {
            val t = i.toFloat() / count
            val sub = sin(2.0 * PI * (120f * exp(-t * 6f)) * i / sampleRate).toFloat() * exp(-t * 4f)
            val c1 = sin(2.0 * PI * 1046.5f * i / sampleRate).toFloat() * exp(-t * 3.5f) // C6
            val c2 = sin(2.0 * PI * 1318.5f * i / sampleRate).toFloat() * exp(-t * 4.0f) // E6
            val c3 = sin(2.0 * PI * 1567.9f * i / sampleRate).toFloat() * exp(-t * 4.5f) // G6
            val c4 = sin(2.0 * PI * 2093.0f * i / sampleRate).toFloat() * exp(-t * 5.0f) // C7
            val sample = sub * 0.4f + (c1 + c2 + c3 + c4) * 0.15f
            buf[i] = (sample * 28000f).toInt().toShort()
        }
        return buf
    }

    private fun synthesizeComboMilestone(): ShortArray {
        val durationMs = 450
        val count = (sampleRate * durationMs) / 1000
        val buf = ShortArray(count)
        for (i in 0 until count) {
            val t = i.toFloat() / count
            val freq = 440f + t * t * 1200f
            val env = (1f - t) * (0.8f + 0.2f * sin(2.0 * PI * 25.0 * t).toFloat())
            val sample = (sin(2.0 * PI * freq * i / sampleRate) + 0.5 * sin(4.0 * PI * freq * i / sampleRate)).toFloat() * env * 0.5f
            buf[i] = (sample * 25000f).toInt().toShort()
        }
        return buf
    }

    private fun synthesizeCoin(): ShortArray {
        val durationMs = 200
        val count = (sampleRate * durationMs) / 1000
        val buf = ShortArray(count)
        val split = count / 2
        for (i in 0 until count) {
            val freq = if (i < split) 987.77f else 1318.51f // B5 -> E6
            val t = (i % split).toFloat() / split
            val env = 1f - t
            val sample = sin(2.0 * PI * freq * i / sampleRate).toFloat() * env
            buf[i] = (sample * 22000f).toInt().toShort()
        }
        return buf
    }

    private fun synthesizeClick(): ShortArray {
        val durationMs = 40
        val count = (sampleRate * durationMs) / 1000
        val buf = ShortArray(count)
        for (i in 0 until count) {
            val t = i.toFloat() / count
            val sample = (Math.random().toFloat() * 2f - 1f) * (1f - t) * (1f - t)
            buf[i] = (sample * 16000f).toInt().toShort()
        }
        return buf
    }

    private fun synthesizeGameOver(): ShortArray {
        val durationMs = 600
        val count = (sampleRate * durationMs) / 1000
        val buf = ShortArray(count)
        for (i in 0 until count) {
            val t = i.toFloat() / count
            val freq = (220f * (1f - t * 0.7f)).coerceAtLeast(40f)
            val noise = (Math.random().toFloat() * 2f - 1f) * 0.2f
            val tone = sin(2.0 * PI * freq * i / sampleRate).toFloat()
            val sample = (tone + noise) * (1f - t)
            buf[i] = (sample * 26000f).toInt().toShort()
        }
        return buf
    }
}
