package com.example.neonpop.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Procedural Synthesizer Audio Engine for Android.
 * Generates dynamic sound effects and ambient interactive music using AudioTrack PCM synthesis.
 */
class ProceduralSoundEngine {

    var sfxEnabled: Boolean = true
    var musicEnabled: Boolean = true
    var masterVolume: Float = 0.8f

    private val sampleRate = 44100
    private val scope = CoroutineScope(Dispatchers.Default)
    private var musicJob: Job? = null

    // Pentatonic scale frequencies for combos
    private val pentatonic = floatArrayOf(
        261.63f, // C4
        293.66f, // D4
        329.63f, // E4
        392.00f, // G4
        440.00f, // A4
        523.25f, // C5
        587.33f, // D5
        659.25f, // E5
        783.99f, // G5
        880.00f, // A5
        1046.50f // C6
    )

    private val maxConcurrentTracks = 2
    private val activeTrackCount = java.util.concurrent.atomic.AtomicInteger(0)
    private var isAudioSupported = true

    private fun playPcm(samples: ShortArray, volumeMult: Float = 1.0f) {
        if (!sfxEnabled || !isAudioSupported) return
        val finalVol = (masterVolume * volumeMult).coerceIn(0f, 1f)
        if (finalVol <= 0.01f) return

        if (activeTrackCount.get() >= maxConcurrentTracks) return

        scope.launch {
            if (activeTrackCount.incrementAndGet() > maxConcurrentTracks) {
                activeTrackCount.decrementAndGet()
                return@launch
            }
            var audioTrack: AudioTrack? = null
            try {
                // Apply volume
                val adjustedSamples = ShortArray(samples.size)
                for (i in samples.indices) {
                    adjustedSamples[i] = (samples[i] * finalVol).toInt().coerceIn(-32767, 32767).toShort()
                }

                val minBuffer = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                if (minBuffer <= 0) {
                    isAudioSupported = false
                    return@launch
                }
                val bufferSize = maxOf(adjustedSamples.size * 2, minBuffer)

                audioTrack = AudioTrack.Builder()
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
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                if (audioTrack.state == AudioTrack.STATE_INITIALIZED) {
                    audioTrack.write(adjustedSamples, 0, adjustedSamples.size)
                    audioTrack.play()
                    val durationMs = (adjustedSamples.size * 1000L) / sampleRate + 30L
                    delay(durationMs)
                }
            } catch (t: Throwable) {
                // Safe AudioTrack fallback for headless/emulator environments
            } finally {
                try {
                    audioTrack?.stop()
                } catch (_: Throwable) {}
                try {
                    audioTrack?.release()
                } catch (_: Throwable) {}
                activeTrackCount.decrementAndGet()
            }
        }
    }

    /**
     * Bubbly pop with pitch variation based on combos or semitone offset.
     */
    fun playPop(pitchStep: Int = 0) {
        val duration = 0.09f
        val numSamples = (duration * sampleRate).toInt()
        val samples = ShortArray(numSamples)
        val startFreq = 420.0f * (1.0f + pitchStep * 0.08f)
        val endFreq = 180.0f * (1.0f + pitchStep * 0.08f)

        var phase = 0.0
        for (i in 0 until numSamples) {
            val t = i.toFloat() / numSamples
            val freq = startFreq + (endFreq - startFreq) * t
            val envelope = (1.0f - t) * (1.0f - t) // Fast decay
            phase += 2.0 * PI * freq / sampleRate
            val sample = sin(phase) * envelope * 0.85
            samples[i] = (sample * 32767).toInt().toShort()
        }
        playPcm(samples, 0.9f)
    }

    /**
     * Launch whoosh.
     */
    fun playLaunch() {
        val duration = 0.12f
        val numSamples = (duration * sampleRate).toInt()
        val samples = ShortArray(numSamples)
        var phase = 0.0
        for (i in 0 until numSamples) {
            val t = i.toFloat() / numSamples
            val freq = 220.0f + 700.0f * (t * t)
            val envelope = sin(t * PI.toFloat()) * 0.7f
            phase += 2.0 * PI * freq / sampleRate
            val sample = sin(phase) * envelope
            samples[i] = (sample * 32767).toInt().toShort()
        }
        playPcm(samples, 0.75f)
    }

    /**
     * Wall bounce ping.
     */
    fun playBounce() {
        val duration = 0.06f
        val numSamples = (duration * sampleRate).toInt()
        val samples = ShortArray(numSamples)
        var phase = 0.0
        for (i in 0 until numSamples) {
            val t = i.toFloat() / numSamples
            val freq = 750.0f - 200.0f * t
            val envelope = exp(-t * 12.0f)
            phase += 2.0 * PI * freq / sampleRate
            val sample = (sin(phase) + 0.3 * sin(phase * 2.0)) * envelope * 0.7
            samples[i] = (sample * 32767).toInt().toShort()
        }
        playPcm(samples, 0.65f)
    }

    /**
     * Combo chord arpeggio based on combo count.
     */
    fun playCombo(comboCount: Int) {
        val noteIndex = (comboCount - 1).coerceIn(0, pentatonic.size - 1)
        val baseFreq = pentatonic[noteIndex]
        val duration = 0.22f
        val numSamples = (duration * sampleRate).toInt()
        val samples = ShortArray(numSamples)

        var p1 = 0.0
        var p2 = 0.0
        var p3 = 0.0
        for (i in 0 until numSamples) {
            val t = i.toFloat() / numSamples
            val envelope = exp(-t * 5.0f) * (1.0f - t * 0.4f)
            p1 += 2.0 * PI * baseFreq / sampleRate
            p2 += 2.0 * PI * (baseFreq * 1.5) / sampleRate // Fifth
            p3 += 2.0 * PI * (baseFreq * 2.0) / sampleRate // Octave
            val s = (sin(p1) * 0.6 + sin(p2) * 0.3 + sin(p3) * 0.2) * envelope
            samples[i] = (s * 32767).toInt().toShort()
        }
        playPcm(samples, 1.0f)
    }

    /**
     * Deep bomb explosion with rumble and noise decay.
     */
    fun playExplosion() {
        val duration = 0.35f
        val numSamples = (duration * sampleRate).toInt()
        val samples = ShortArray(numSamples)
        var phase = 0.0
        val random = java.util.Random()
        for (i in 0 until numSamples) {
            val t = i.toFloat() / numSamples
            val freq = 120.0f * exp(-t * 4.0f) + 40.0f
            phase += 2.0 * PI * freq / sampleRate
            val sine = sin(phase) * 0.6
            val noise = (random.nextFloat() * 2f - 1f) * 0.4f
            val envelope = exp(-t * 6.0f)
            val s = (sine + noise) * envelope
            samples[i] = (s * 32767).toInt().toShort()
        }
        playPcm(samples, 1.0f)
    }

    /**
     * High tech laser/lightning zap.
     */
    fun playLaser() {
        val duration = 0.25f
        val numSamples = (duration * sampleRate).toInt()
        val samples = ShortArray(numSamples)
        var phase = 0.0
        for (i in 0 until numSamples) {
            val t = i.toFloat() / numSamples
            val freq = 1800.0f * exp(-t * 8.0f) + 120.0f
            phase += 2.0 * PI * freq / sampleRate
            val mod = sin(phase * 0.5) * 0.3
            val envelope = (1.0f - t)
            val s = (sin(phase) + mod) * envelope * 0.8
            samples[i] = (s * 32767).toInt().toShort()
        }
        playPcm(samples, 0.85f)
    }

    /**
     * Crystalline freeze or bell chime.
     */
    fun playFreeze() {
        val duration = 0.28f
        val numSamples = (duration * sampleRate).toInt()
        val samples = ShortArray(numSamples)
        var p1 = 0.0
        var p2 = 0.0
        for (i in 0 until numSamples) {
            val t = i.toFloat() / numSamples
            val envelope = exp(-t * 7.0f)
            p1 += 2.0 * PI * 1480.0 / sampleRate
            p2 += 2.0 * PI * 2220.0 / sampleRate
            val s = (sin(p1) * 0.6 + sin(p2) * 0.4) * envelope
            samples[i] = (s * 32767).toInt().toShort()
        }
        playPcm(samples, 0.8f)
    }

    /**
     * Coin clink reward sound.
     */
    fun playCoin() {
        val duration = 0.18f
        val numSamples = (duration * sampleRate).toInt()
        val samples = ShortArray(numSamples)
        var p1 = 0.0
        var p2 = 0.0
        for (i in 0 until numSamples) {
            val t = i.toFloat() / numSamples
            val envelope = exp(-t * 10.0f)
            p1 += 2.0 * PI * 987.77 / sampleRate // B5
            p2 += 2.0 * PI * 1318.51 / sampleRate // E6
            val s = (sin(p1) * 0.5 + sin(p2) * 0.5) * envelope
            samples[i] = (s * 32767).toInt().toShort()
        }
        playPcm(samples, 0.75f)
    }

    /**
     * Star chime sound.
     */
    fun playStarChime() {
        playCombo(7)
    }

    /**
     * Triumphant fast synth fanfare for Fever mode activation.
     */
    fun playFeverFanfare() {
        playCombo(8)
        scope.launch {
            delay(80)
            playCombo(10)
        }
    }

    /**
     * UI button tap.
     */
    fun playUiTap() {
        val duration = 0.04f
        val numSamples = (duration * sampleRate).toInt()
        val samples = ShortArray(numSamples)
        var phase = 0.0
        for (i in 0 until numSamples) {
            val t = i.toFloat() / numSamples
            val freq = 600.0f
            val envelope = 1.0f - t
            phase += 2.0 * PI * freq / sampleRate
            val s = sin(phase) * envelope * 0.5
            samples[i] = (s * 32767).toInt().toShort()
        }
        playPcm(samples, 0.5f)
    }

    /**
     * Celebratory victory fanfare.
     */
    fun playLevelComplete() {
        scope.launch {
            val chords = listOf(
                Pair(523.25f, 659.25f), // C5 + E5
                Pair(587.33f, 698.46f), // D5 + F5
                Pair(659.25f, 783.99f), // E5 + G5
                Pair(783.99f, 1046.50f) // G5 + C6
            )
            for (chord in chords) {
                playTwoTone(chord.first, chord.second, 0.18f)
                delay(120)
            }
        }
    }

    private fun playTwoTone(f1: Float, f2: Float, duration: Float) {
        val numSamples = (duration * sampleRate).toInt()
        val samples = ShortArray(numSamples)
        var p1 = 0.0
        var p2 = 0.0
        for (i in 0 until numSamples) {
            val t = i.toFloat() / numSamples
            val envelope = exp(-t * 4.0f)
            p1 += 2.0 * PI * f1 / sampleRate
            p2 += 2.0 * PI * f2 / sampleRate
            val s = (sin(p1) * 0.5 + sin(p2) * 0.5) * envelope * 0.8
            samples[i] = (s * 32767).toInt().toShort()
        }
        playPcm(samples, 0.9f)
    }

    /**
     * Dynamic ambient cyberpunk dreamscape music generator.
     * Gently loops chord progressions in the background.
     */
    private var isFeverActive = false

    fun setFeverMusic(active: Boolean) {
        isFeverActive = active
    }

    fun startDynamicMusic() {
        if (musicJob != null && musicJob?.isActive == true) return
        musicJob = scope.launch {
            val progression = listOf(
                floatArrayOf(261.63f, 329.63f, 392.00f), // C maj
                floatArrayOf(220.00f, 261.63f, 329.63f), // A min
                floatArrayOf(174.61f, 220.00f, 261.63f), // F maj
                floatArrayOf(196.00f, 246.94f, 293.66f)  // G maj
            )
            var progIdx = 0

            while (isActive) {
                if (musicEnabled && isAudioSupported && masterVolume > 0.05f) {
                    val chord = progression[progIdx % progression.size]
                    val delayStep = if (isFeverActive) 260L else 480L

                    for (note in chord) {
                        if (!isActive || !musicEnabled || !isAudioSupported) break
                        playSoftPluck(note * if (isFeverActive) 2.0f else 1.0f, if (isFeverActive) 0.18f else 0.32f)
                        delay(delayStep)
                    }
                    progIdx++
                    delay(if (isFeverActive) 350L else 1000L)
                } else {
                    delay(1000)
                }
            }
        }
    }

    private fun playSoftPluck(freq: Float, duration: Float) {
        if (!musicEnabled) return
        val numSamples = (duration * sampleRate).toInt()
        val samples = ShortArray(numSamples)
        var phase = 0.0
        var phaseHarmonic = 0.0
        for (i in 0 until numSamples) {
            val t = i.toFloat() / numSamples
            val env = exp(-t * 5.0f) * 0.25f
            phase += 2.0 * PI * freq / sampleRate
            phaseHarmonic += 2.0 * PI * (freq * 2) / sampleRate
            val s = (sin(phase) * 0.7 + sin(phaseHarmonic) * 0.3) * env
            samples[i] = (s * 32767).toInt().toShort()
        }
        playPcm(samples, 0.28f)
    }

    fun stopDynamicMusic() {
        musicJob?.cancel()
        musicJob = null
    }

    fun release() {
        stopDynamicMusic()
    }
}
