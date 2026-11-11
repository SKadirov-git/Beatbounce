package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.game.MusicTrack
import kotlinx.coroutines.*
import kotlin.math.*

class SynthesizedMusicEngine {
    private val sampleRate = 44100
    private var audioTrack: AudioTrack? = null
    private var musicVolume = 0.85f
    private var isMuted = false

    private var currentTrack: MusicTrack? = null
    private var isPlaying = false
    private var playbackJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    // Synchronized playback timing
    private var startPlaybackNanoTime = 0L
    private var pausedPositionSec = 0.0
    private var totalFramesWritten = 0L

    companion object {
        val ALL_TRACKS = listOf(
            MusicTrack(
                id = "track_neon_velocity",
                title = "Neon Velocity",
                artist = "SUBHON & SAYOD",
                genre = "Cyberpunk EDM",
                bpm = 128,
                difficulty = "Medium",
                durationSec = 90,
                description = "Driving bassline, relentless 128 BPM cyberpunk synths and punchy drops",
                synthPatternId = 1
            ),
            MusicTrack(
                id = "track_cyber_pulse",
                title = "Cyber Pulse",
                artist = "SUBHON & SAYOD",
                genre = "Future Bass",
                bpm = 135,
                difficulty = "Hard",
                durationSec = 85,
                description = "Soaring supersaw chords, emotional synth arpeggios and high-octane drops",
                synthPatternId = 2
            ),
            MusicTrack(
                id = "track_synthwave_sunset",
                title = "Synthwave Sunset",
                artist = "SUBHON & SAYOD",
                genre = "Synthwave",
                bpm = 115,
                difficulty = "Normal",
                durationSec = 96,
                description = "Nostalgic 80s gated reverbs, lush retro analog pads and catchy neon melodies",
                synthPatternId = 3
            ),
            MusicTrack(
                id = "track_electric_horizon",
                title = "Electric Horizon",
                artist = "SUBHON & SAYOD",
                genre = "High Energy EDM",
                bpm = 140,
                difficulty = "Expert",
                durationSec = 80,
                description = "Fast-paced four-on-the-floor kick, electrifying stabs and maximum speed",
                synthPatternId = 4
            ),
            MusicTrack(
                id = "track_galactic_drift",
                title = "Galactic Drift",
                artist = "SUBHON & SAYOD",
                genre = "Deep Cosmic Trance",
                bpm = 120,
                difficulty = "Easy",
                durationSec = 100,
                description = "Hypnotic space arpeggios, atmospheric sub-bass and celestial harmonics",
                synthPatternId = 5
            )
        )
    }

    fun setVolume(vol: Float) {
        musicVolume = vol.coerceIn(0f, 1f)
        try {
            audioTrack?.setVolume(if (isMuted) 0f else musicVolume)
        } catch (_: Exception) {}
    }

    fun setMuted(muted: Boolean) {
        isMuted = muted
        try {
            audioTrack?.setVolume(if (isMuted) 0f else musicVolume)
        } catch (_: Exception) {}
    }

    fun getCurrentTrack(): MusicTrack? = currentTrack
    fun isMusicPlaying(): Boolean = isPlaying

    fun play(track: MusicTrack) {
        stop()
        currentTrack = track
        isPlaying = true
        pausedPositionSec = 0.0

        playbackJob = scope.launch {
            try {
                // Generate a seamless multi-bar EDM loop buffer (e.g. 16 beats = 4 bars)
                val bpm = track.bpm.toFloat()
                val secPerBeat = 60f / bpm
                val beatsInLoop = 16
                val loopDurationSec = beatsInLoop * secPerBeat
                val framesInLoop = (loopDurationSec * sampleRate).toInt()
                val bufferShorts = ShortArray(framesInLoop * 2) // Stereo (L+R)

                synthesizeLoop(track.synthPatternId, bpm, framesInLoop, bufferShorts)

                val minBufSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT
                )

                val trackBufSize = max(minBufSize * 2, bufferShorts.size * 2)

                audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                            .build()
                    )
                    .setBufferSizeInBytes(trackBufSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                audioTrack?.setVolume(if (isMuted) 0f else musicVolume)
                audioTrack?.play()
                startPlaybackNanoTime = System.nanoTime()
                totalFramesWritten = 0L

                // Stream audio loop continuously in chunks
                val chunkSize = 4096
                var offset = 0
                while (isActive && isPlaying) {
                    val remaining = bufferShorts.size - offset
                    val toWrite = min(chunkSize, remaining)
                    val written = audioTrack?.write(bufferShorts, offset, toWrite) ?: 0
                    if (written > 0) {
                        totalFramesWritten += written / 2
                        offset = (offset + written) % bufferShorts.size
                    } else {
                        delay(5)
                    }
                }
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun pause() {
        if (!isPlaying) return
        pausedPositionSec = getPlaybackDspTimeSeconds()
        isPlaying = false
        try {
            audioTrack?.pause()
        } catch (_: Exception) {}
    }

    fun resume() {
        if (isPlaying || currentTrack == null) return
        isPlaying = true
        startPlaybackNanoTime = System.nanoTime() - (pausedPositionSec * 1_000_000_000.0).toLong()
        try {
            audioTrack?.play()
        } catch (_: Exception) {}
    }

    fun stop() {
        isPlaying = false
        playbackJob?.cancel()
        playbackJob = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
        startPlaybackNanoTime = 0L
        pausedPositionSec = 0.0
    }

    /**
     * Accurately returns playback time in seconds with zero drift.
     */
    fun getPlaybackDspTimeSeconds(): Double {
        if (currentTrack == null) return 0.0
        if (!isPlaying) return pausedPositionSec
        val track = audioTrack
        if (track != null) {
            try {
                val headPos = track.playbackHeadPosition
                if (headPos > 0) {
                    return headPos.toDouble() / sampleRate
                }
            } catch (_: Exception) {}
        }
        val elapsedNanos = System.nanoTime() - startPlaybackNanoTime
        return max(0.0, elapsedNanos / 1_000_000_000.0)
    }

    /**
     * Dynamic bass pulse for pulsing buildings and glowing platforms.
     */
    fun getCurrentBassEnergy(): Float {
        val track = currentTrack ?: return 0.2f
        val sec = getPlaybackDspTimeSeconds()
        val secPerBeat = 60.0 / track.bpm
        val beatPhase = (sec % secPerBeat) / secPerBeat
        // Spike at beat start (0.0) decaying rapidly
        return exp(-beatPhase * 6.0).toFloat().coerceIn(0.1f, 1.0f)
    }

    /**
     * Synthesize multi-instrument rich electronic music pattern.
     */
    private fun synthesizeLoop(patternId: Int, bpm: Float, frames: Int, outStereo: ShortArray) {
        val secPerBeat = 60f / bpm
        val framesPerBeat = (secPerBeat * sampleRate).toInt()

        // Scale notes (pentatonic / minor synth scales)
        val rootFreq = when (patternId) {
            1 -> 130.81f // C3 Cyberpunk
            2 -> 146.83f // D3 Future Bass
            3 -> 110.00f // A2 Synthwave
            4 -> 164.81f // E3 High Energy
            else -> 123.47f // B2 Deep Cosmic
        }

        val scaleMultipliers = floatArrayOf(1.0f, 1.189f, 1.335f, 1.498f, 1.782f, 2.0f, 2.378f)

        for (f in 0 until frames) {
            val tSec = f.toFloat() / sampleRate
            val currentBeatFloat = tSec / secPerBeat
            val currentBeatInt = currentBeatFloat.toInt() % 16
            val beatPhase = currentBeatFloat - currentBeatFloat.toInt()

            var mixL = 0f
            var mixR = 0f

            // 1. Kick Drum: on every beat (4-on-the-floor)
            val kickT = beatPhase * secPerBeat
            if (kickT < 0.25f) {
                val kickEnv = exp(-kickT * 18f)
                val kickFreq = 140f * exp(-kickT * 28f) + 42f
                val kickSample = sin(2.0 * PI * kickFreq * kickT).toFloat() * kickEnv
                mixL += kickSample * 0.45f
                mixR += kickSample * 0.45f
            }

            // 2. Snare / Clap: on beats 4, 8, 12, 16 (i.e. every 2nd or 4th beat)
            val isSnareBeat = (currentBeatInt % 4 == 2)
            if (isSnareBeat && kickT < 0.22f) {
                val snareEnv = exp(-kickT * 16f)
                val noise = (Math.random().toFloat() * 2f - 1f) * 0.35f
                val tone = sin(2.0 * PI * 220.0 * kickT).toFloat() * 0.25f
                val snare = (noise + tone) * snareEnv
                mixL += snare * 0.35f
                mixR += snare * 0.35f
            }

            // 3. Hi-Hat: 16th notes
            val hatPhase = (currentBeatFloat * 4f) % 1f
            val hatT = hatPhase * (secPerBeat / 4f)
            if (hatT < 0.05f) {
                val hatEnv = exp(-hatT * 60f)
                val hatNoise = (Math.random().toFloat() * 2f - 1f) * hatEnv * 0.12f
                mixL += hatNoise * 0.8f
                mixR += hatNoise * 1.2f
            }

            // 4. Bassline: Rolling 16th or 8th note sub-bass
            val bassStep = (currentBeatFloat * 2f).toInt() % 16
            val bassNoteIndex = when (bassStep % 8) {
                0, 1 -> 0
                2, 3 -> 2
                4, 5 -> 3
                else -> 4
            }
            val bassFreq = rootFreq * 0.5f * scaleMultipliers[bassNoteIndex % scaleMultipliers.size]
            val bassStepPhase = (currentBeatFloat * 2f) % 1f
            val bassEnv = (1f - bassStepPhase * 0.7f).coerceAtLeast(0.1f)
            // Sidechain ducking during kick
            val sidechain = (1f - exp(-beatPhase * 8f)).coerceIn(0.15f, 1f)
            val bassSample = (sin(2.0 * PI * bassFreq * tSec) + 0.3 * sin(4.0 * PI * bassFreq * tSec)).toFloat() * bassEnv * sidechain
            mixL += bassSample * 0.30f
            mixR += bassSample * 0.30f

            // 5. Arpeggio / Melody Synth: 16th note pattern
            val arpStep = (currentBeatFloat * 4f).toInt() % 16
            val arpNoteIndex = when (patternId) {
                1 -> (arpStep * 2 + (arpStep / 4)) % scaleMultipliers.size
                2 -> (arpStep * 3) % scaleMultipliers.size
                3 -> (arpStep + (if (arpStep % 2 == 0) 2 else 0)) % scaleMultipliers.size
                4 -> (arpStep * 2 + 1) % scaleMultipliers.size
                else -> (arpStep) % scaleMultipliers.size
            }
            val arpFreq = rootFreq * 2f * scaleMultipliers[arpNoteIndex]
            val arpPhase = (currentBeatFloat * 4f) % 1f
            val arpEnv = exp(-arpPhase * 7f)
            val sawSample = (sin(2.0 * PI * arpFreq * tSec) + 0.5 * sin(4.0 * PI * arpFreq * tSec) + 0.25 * sin(6.0 * PI * arpFreq * tSec)).toFloat() * arpEnv

            // Stereo panning for arpeggio
            val panL = 0.5f + 0.3f * sin(2.0 * PI * tSec * 0.5).toFloat()
            val panR = 1f - panL
            mixL += sawSample * 0.22f * panL
            mixR += sawSample * 0.22f * panR

            // 6. Cyber Pad / Chords
            val chordStep = currentBeatInt / 4
            val chordFreq = rootFreq * scaleMultipliers[(chordStep * 2) % scaleMultipliers.size]
            val padSample = sin(2.0 * PI * chordFreq * tSec).toFloat() * 0.12f
            mixL += padSample
            mixR += padSample

            // Clip and convert to 16-bit PCM
            val clampedL = mixL.coerceIn(-0.95f, 0.95f)
            val clampedR = mixR.coerceIn(-0.95f, 0.95f)

            outStereo[f * 2] = (clampedL * 32767f).toInt().toShort()
            outStereo[f * 2 + 1] = (clampedR * 32767f).toInt().toShort()
        }
    }
}
