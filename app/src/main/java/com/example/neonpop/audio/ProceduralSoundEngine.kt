package com.example.neonpop.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Procedural Synthesizer Audio Engine for Android.
 * Generates dynamic sound effects and arcade synthwave background music
 * entirely in real-time using AudioTrack PCM synthesis.
 *
 * Designed for extreme reliability:
 * 1. Background music uses exactly ONE static looping AudioTrack with hardware looping.
 * 2. Sound effects are rate-limited to avoid exhausting system AudioFlinger tracks.
 * 3. Graceful fallback on headless or audio-disabled emulators to prevent log spam.
 */
class ProceduralSoundEngine {

    var sfxEnabled: Boolean = true
    var musicEnabled: Boolean = true
        set(value) {
            field = value
            updateMusicPlayback()
        }

    var masterVolume: Float = 0.8f
        set(value) {
            field = value
            updateMusicPlayback()
        }

    private val sampleRate = 22050 // Optimized 22.05 kHz for fast, responsive arcade synthesis
    private val scope = CoroutineScope(Dispatchers.Default)

    private var musicAudioTrack: AudioTrack? = null
    private var isAudioSupported = true
    private var isFeverActive = false

    // Concurrent SFX track limit (max 2 concurrent tracks to protect AudioFlinger)
    private val maxConcurrentSfxTracks = 2
    private val activeSfxCount = AtomicInteger(0)

    // Musical pentatonic scale frequencies for combos
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

    // ==========================================
    // SOUND EFFECTS ENGINE
    // ==========================================

    private fun playSfxPcm(samples: ShortArray, volumeMult: Float = 1.0f) {
        if (!sfxEnabled || !isAudioSupported) return
        val finalVol = (masterVolume * volumeMult).coerceIn(0f, 1f)
        if (finalVol <= 0.01f) return

        if (activeSfxCount.get() >= maxConcurrentSfxTracks) return

        scope.launch {
            if (activeSfxCount.incrementAndGet() > maxConcurrentSfxTracks) {
                activeSfxCount.decrementAndGet()
                return@launch
            }
            var track: AudioTrack? = null
            try {
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

                track = AudioTrack.Builder()
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

                if (track.state == AudioTrack.STATE_INITIALIZED) {
                    track.write(adjustedSamples, 0, adjustedSamples.size)
                    track.play()
                    val durationMs = (adjustedSamples.size * 1000L) / sampleRate + 25L
                    delay(durationMs)
                } else {
                    // AudioFlinger unable to allocate tracks; disable to prevent log flooding
                    isAudioSupported = false
                }
            } catch (_: Throwable) {
                isAudioSupported = false
            } finally {
                try {
                    track?.stop()
                } catch (_: Throwable) {}
                try {
                    track?.release()
                } catch (_: Throwable) {}
                activeSfxCount.decrementAndGet()
            }
        }
    }

    /**
     * Bubbly pop with pitch variation based on combo count.
     */
    fun playPop(pitchStep: Int = 0) {
        val duration = 0.08f
        val numSamples = (duration * sampleRate).toInt()
        val samples = ShortArray(numSamples)
        val pitchMultiplier = (1.0f + (pitchStep.coerceIn(0, 15) * 0.07f))
        val startFreq = 580.0f * pitchMultiplier
        val endFreq = 210.0f * pitchMultiplier

        var phase = 0.0
        var phaseHarmonic = 0.0
        for (i in 0 until numSamples) {
            val t = i.toFloat() / numSamples
            val freq = startFreq + (endFreq - startFreq) * t
            val envelope = (1.0f - t) * (1.0f - t)
            phase += 2.0 * PI * freq / sampleRate
            phaseHarmonic += 2.0 * PI * (freq * 1.5f) / sampleRate
            val sample = (sin(phase) * 0.8 + sin(phaseHarmonic) * 0.25) * envelope
            samples[i] = (sample * 32767).toInt().toShort()
        }
        playSfxPcm(samples, 0.95f)
    }

    /**
     * Launch whoosh & pneumatic pop.
     */
    fun playLaunch() {
        val duration = 0.11f
        val numSamples = (duration * sampleRate).toInt()
        val samples = ShortArray(numSamples)
        var phase = 0.0
        for (i in 0 until numSamples) {
            val t = i.toFloat() / numSamples
            val freq = 200.0f + 720.0f * (t * t)
            val envelope = sin(t * PI.toFloat()) * (1.0f - t * 0.2f)
            phase += 2.0 * PI * freq / sampleRate
            val sample = sin(phase) * envelope
            samples[i] = (sample * 32767).toInt().toShort()
        }
        playSfxPcm(samples, 0.78f)
    }

    /**
     * Rubbery neon wall bounce ping.
     */
    fun playBounce() {
        val duration = 0.065f
        val numSamples = (duration * sampleRate).toInt()
        val samples = ShortArray(numSamples)
        var phase = 0.0
        for (i in 0 until numSamples) {
            val t = i.toFloat() / numSamples
            val freq = 820.0f - 240.0f * t
            val envelope = exp(-t * 14.0f)
            phase += 2.0 * PI * freq / sampleRate
            val sample = (sin(phase) + 0.35 * sin(phase * 2.2)) * envelope * 0.75
            samples[i] = (sample * 32767).toInt().toShort()
        }
        playSfxPcm(samples, 0.7f)
    }

    /**
     * Combo chord arpeggio based on combo count.
     */
    fun playCombo(comboCount: Int) {
        val noteIndex = (comboCount - 1).coerceIn(0, pentatonic.size - 1)
        val baseFreq = pentatonic[noteIndex]
        val duration = 0.24f
        val numSamples = (duration * sampleRate).toInt()
        val samples = ShortArray(numSamples)

        var p1 = 0.0
        var p2 = 0.0
        var p3 = 0.0
        for (i in 0 until numSamples) {
            val t = i.toFloat() / numSamples
            val envelope = exp(-t * 5.2f) * (1.0f - t * 0.3f)
            p1 += 2.0 * PI * baseFreq / sampleRate
            p2 += 2.0 * PI * (baseFreq * 1.5) / sampleRate
            p3 += 2.0 * PI * (baseFreq * 2.0) / sampleRate
            val s = (sin(p1) * 0.55 + sin(p2) * 0.32 + sin(p3) * 0.22) * envelope
            samples[i] = (s * 32767).toInt().toShort()
        }
        playSfxPcm(samples, 1.0f)
    }

    /**
     * Deep bomb explosion with sub-bass rumble.
     */
    fun playExplosion() {
        val duration = 0.35f
        val numSamples = (duration * sampleRate).toInt()
        val samples = ShortArray(numSamples)
        var phase = 0.0
        val random = java.util.Random()
        for (i in 0 until numSamples) {
            val t = i.toFloat() / numSamples
            val freq = 135.0f * exp(-t * 4.5f) + 38.0f
            phase += 2.0 * PI * freq / sampleRate
            val sine = sin(phase) * 0.65
            val noise = (random.nextFloat() * 2f - 1f) * 0.45f
            val envelope = exp(-t * 5.8f)
            val s = (sine + noise) * envelope
            samples[i] = (s * 32767).toInt().toShort()
        }
        playSfxPcm(samples, 1.0f)
    }

    /**
     * High tech laser/lightning zap.
     */
    fun playLaser() {
        val duration = 0.22f
        val numSamples = (duration * sampleRate).toInt()
        val samples = ShortArray(numSamples)
        var phase = 0.0
        for (i in 0 until numSamples) {
            val t = i.toFloat() / numSamples
            val freq = 2100.0f * exp(-t * 9.0f) + 110.0f
            phase += 2.0 * PI * freq / sampleRate
            val mod = sin(phase * 0.45) * 0.3
            val envelope = (1.0f - t)
            val s = (sin(phase) + mod) * envelope * 0.85
            samples[i] = (s * 32767).toInt().toShort()
        }
        playSfxPcm(samples, 0.9f)
    }

    /**
     * Prismatic shimmering rainbow glissando.
     */
    fun playRainbow() {
        playCombo(6)
    }

    /**
     * Crystalline freeze bell chime.
     */
    fun playFreeze() {
        val duration = 0.22f
        val numSamples = (duration * sampleRate).toInt()
        val samples = ShortArray(numSamples)
        var p1 = 0.0
        var p2 = 0.0
        for (i in 0 until numSamples) {
            val t = i.toFloat() / numSamples
            val envelope = exp(-t * 7.5f)
            p1 += 2.0 * PI * 1480.0 / sampleRate
            p2 += 2.0 * PI * 2220.0 / sampleRate
            val s = (sin(p1) * 0.6 + sin(p2) * 0.4) * envelope
            samples[i] = (s * 32767).toInt().toShort()
        }
        playSfxPcm(samples, 0.82f)
    }

    /**
     * Double-clink metallic coin reward sound.
     */
    fun playCoin() {
        val duration = 0.18f
        val numSamples = (duration * sampleRate).toInt()
        val samples = ShortArray(numSamples)
        var p1 = 0.0
        var p2 = 0.0
        for (i in 0 until numSamples) {
            val t = i.toFloat() / numSamples
            val envelope = exp(-t * 8.5f)
            p1 += 2.0 * PI * 987.77 / sampleRate
            p2 += 2.0 * PI * 1318.51 / sampleRate
            val s = (sin(p1) * 0.55 + sin(p2) * 0.45) * envelope
            samples[i] = (s * 32767).toInt().toShort()
        }
        playSfxPcm(samples, 0.85f)
    }

    /**
     * Star rescue celestial chime.
     */
    fun playStarChime() {
        playCombo(8)
    }

    /**
     * Quick two-tone blip when swapping bubbles.
     */
    fun playBubbleSwap() {
        playPop(4)
    }

    /**
     * Power-up charge whoosh for booster selection.
     */
    fun playBoosterSelect() {
        val duration = 0.16f
        val numSamples = (duration * sampleRate).toInt()
        val samples = ShortArray(numSamples)
        var phase = 0.0
        for (i in 0 until numSamples) {
            val t = i.toFloat() / numSamples
            val freq = 320.0f + 680.0f * (t * t)
            val envelope = sin(t * PI.toFloat()) * 0.85f
            phase += 2.0 * PI * freq / sampleRate
            val s = (sin(phase) + 0.2 * sin(phase * 2.0)) * envelope
            samples[i] = (s * 32767).toInt().toShort()
        }
        playSfxPcm(samples, 0.85f)
    }

    /**
     * Triumphant fanfare for Fever mode.
     */
    fun playFeverFanfare() {
        playCombo(10)
    }

    /**
     * Celebratory victory fanfare.
     */
    fun playLevelComplete() {
        scope.launch {
            val chords = listOf(
                Pair(523.25f, 659.25f), // C5 + E5
                Pair(659.25f, 783.99f), // E5 + G5
                Pair(783.99f, 1046.50f) // G5 + C6
            )
            for (chord in chords) {
                playTwoTone(chord.first, chord.second, 0.18f)
                delay(120)
            }
        }
    }

    /**
     * Melancholic descending jingle for Level Failed.
     */
    fun playGameOver() {
        scope.launch {
            val notes = listOf(440.00f, 392.00f, 349.23f, 293.66f)
            for (note in notes) {
                playTwoTone(note, note * 0.5f, 0.18f)
                delay(130)
            }
        }
    }

    /**
     * UI button tap.
     */
    fun playUiTap() {
        val duration = 0.035f
        val numSamples = (duration * sampleRate).toInt()
        val samples = ShortArray(numSamples)
        var phase = 0.0
        for (i in 0 until numSamples) {
            val t = i.toFloat() / numSamples
            val freq = 650.0f - 150.0f * t
            val envelope = 1.0f - t
            phase += 2.0 * PI * freq / sampleRate
            val s = sin(phase) * envelope * 0.5
            samples[i] = (s * 32767).toInt().toShort()
        }
        playSfxPcm(samples, 0.55f)
    }

    private fun playTwoTone(f1: Float, f2: Float, duration: Float) {
        val numSamples = (duration * sampleRate).toInt()
        val samples = ShortArray(numSamples)
        var p1 = 0.0
        var p2 = 0.0
        for (i in 0 until numSamples) {
            val t = i.toFloat() / numSamples
            val envelope = exp(-t * 4.5f)
            p1 += 2.0 * PI * f1 / sampleRate
            p2 += 2.0 * PI * f2 / sampleRate
            val s = (sin(p1) * 0.55 + sin(p2) * 0.45) * envelope * 0.85
            samples[i] = (s * 32767).toInt().toShort()
        }
        playSfxPcm(samples, 0.9f)
    }

    // ==========================================
    // DYNAMIC ARCADE SYNTHWAVE MUSIC ENGINE
    // Uses exactly ONE static looping AudioTrack
    // ==========================================

    fun setFeverMusic(active: Boolean) {
        isFeverActive = active
        try {
            musicAudioTrack?.let { track ->
                if (track.state == AudioTrack.STATE_INITIALIZED) {
                    val targetRate = if (active) (sampleRate * 1.25f).toInt() else sampleRate
                    track.playbackRate = targetRate
                }
            }
        } catch (_: Throwable) {}
    }

    private fun updateMusicPlayback() {
        try {
            musicAudioTrack?.let { track ->
                if (track.state == AudioTrack.STATE_INITIALIZED) {
                    if (musicEnabled && masterVolume > 0.02f) {
                        val vol = (masterVolume * 0.35f).coerceIn(0f, 1f)
                        track.setVolume(vol)
                        if (track.playState != AudioTrack.PLAYSTATE_PLAYING) {
                            track.play()
                        }
                    } else {
                        if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                            track.pause()
                        }
                    }
                }
            }
        } catch (_: Throwable) {}
    }

    fun startDynamicMusic() {
        if (!musicEnabled || !isAudioSupported || masterVolume <= 0.02f) return
        if (musicAudioTrack != null && musicAudioTrack?.state == AudioTrack.STATE_INITIALIZED) {
            updateMusicPlayback()
            return
        }

        scope.launch {
            try {
                // Generate a seamless 4-second (4-bar) arcade synthwave loop
                val loopDuration = 4.0f
                val totalFrames = (loopDuration * sampleRate).toInt()
                val loopBuffer = ShortArray(totalFrames)

                val chordRoots = floatArrayOf(261.63f, 220.00f, 174.61f, 196.00f) // C, Am, F, G
                val chordBass = floatArrayOf(130.81f, 110.00f, 87.31f, 98.00f)
                val framesPerBar = totalFrames / 4
                val framesPerStep = framesPerBar / 8 // 8 steps per bar (8th notes)

                var pArp = 0.0
                var pBass = 0.0
                val random = java.util.Random(42L)

                for (frame in 0 until totalFrames) {
                    val barIndex = (frame / framesPerBar).coerceIn(0, 3)
                    val frameInBar = frame % framesPerBar
                    val stepInBar = (frameInBar / framesPerStep).coerceIn(0, 7)
                    val frameInStep = frameInBar % framesPerStep
                    val tStep = frameInStep.toFloat() / framesPerStep

                    val root = chordRoots[barIndex]
                    val bass = chordBass[barIndex]

                    val noteMult = when (stepInBar) {
                        0 -> 1.0f
                        1 -> 1.25f
                        2 -> 1.5f
                        3 -> 2.0f
                        4 -> 1.5f
                        5 -> 1.25f
                        6 -> 2.0f
                        else -> 1.125f
                    }
                    val arpFreq = root * noteMult
                    val bassFreq = if (stepInBar % 2 == 0) bass else bass * 1.5f

                    // 1. Synth Pluck Arp
                    pArp += 2.0 * PI * arpFreq / sampleRate
                    val arpEnv = exp(-tStep * 6.0f)
                    val arp = (sin(pArp) * 0.7 + sin(pArp * 2.0) * 0.3) * arpEnv

                    // 2. Punchy Arcade Bass
                    pBass += 2.0 * PI * bassFreq / sampleRate
                    val bassEnv = (1.0f - tStep * 0.4f)
                    val bassSound = sin(pBass) * bassEnv * 0.55

                    // 3. Subtle Rhythm Percussion
                    val isKick = (stepInBar == 0 || stepInBar == 4)
                    val kick = if (isKick && tStep < 0.35f) {
                        val kFreq = 120.0f * exp(-tStep * 12.0f) + 40.0f
                        sin(2.0 * PI * kFreq * tStep) * (1.0f - tStep * 2.8f) * 0.4
                    } else 0.0

                    val isHat = (stepInBar % 2 == 1)
                    val hat = if (isHat && tStep < 0.15f) {
                        (random.nextFloat() * 2f - 1f) * (1.0f - tStep * 6.6f) * 0.12
                    } else 0.0

                    val mixed = (arp * 0.5 + bassSound * 0.35 + kick + hat)
                    loopBuffer[frame] = (mixed.coerceIn(-1.0, 1.0) * 32767).toInt().toShort()
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

                val track = AudioTrack.Builder()
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
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(maxOf(loopBuffer.size * 2, minBuffer))
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                if (track.state == AudioTrack.STATE_INITIALIZED) {
                    track.write(loopBuffer, 0, loopBuffer.size)
                    track.setLoopPoints(0, loopBuffer.size, -1) // Hardware infinite loop
                    val vol = (masterVolume * 0.35f).coerceIn(0f, 1f)
                    track.setVolume(vol)
                    track.play()
                    musicAudioTrack = track
                } else {
                    isAudioSupported = false
                    try {
                        track.release()
                    } catch (_: Throwable) {}
                }
            } catch (_: Throwable) {
                isAudioSupported = false
            }
        }
    }

    fun stopDynamicMusic() {
        try {
            musicAudioTrack?.stop()
        } catch (_: Throwable) {}
        try {
            musicAudioTrack?.release()
        } catch (_: Throwable) {}
        musicAudioTrack = null
    }

    fun release() {
        stopDynamicMusic()
    }
}
