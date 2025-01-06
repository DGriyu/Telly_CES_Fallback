package com.example.telly_ces_fallback.model.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Process
import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean

class AudioPlayer(
    private val sampleRate: Int = 16000,
    private val channelConfig: Int = AudioFormat.CHANNEL_OUT_MONO,
    private val encodingFormat: Int = AudioFormat.ENCODING_PCM_16BIT
) {
    private var audioTrack: AudioTrack? = null

    private val jitterBuffer = LinkedBlockingQueue<AudioEvent.Success>()
    private val bufferLock = Object()

    private var baseBufferSize = 0
    private var bufferSize = 0

    private var playbackJob: Job? = null

    private val isReleased = AtomicBoolean(false)

    private val playbackScope = CoroutineScope(
        Dispatchers.Default +
                SupervisorJob() +
                CoroutineExceptionHandler { _, e ->
                    Log.e("AudioPlayer", "Playback error", e)
                    if (e !is IllegalStateException && !isReleased.get()) {
                        initializeAudioTrack()
                    }
                }
    )

    fun initializeAudioTrack() {
        try {
            baseBufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, encodingFormat)
            bufferSize = baseBufferSize * 2
            isReleased.set(false)

            Log.d(
                "AudioPlayer",
                "Initializing AudioTrack - Sample Rate: $sampleRate Hz, Buffer Size: $bufferSize bytes"
            )

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()

            val audioFormat = AudioFormat.Builder()
                .setSampleRate(sampleRate)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()

            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(audioAttributes)
                .setAudioFormat(audioFormat)
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
                .build()

            audioTrack?.play()
            processAudioBuffer()
            monitorBufferHealth()

            Log.d("AudioPlayer", "AudioTrack initialized and started")
        } catch (e: Exception) {
            release()
            Log.e("AudioPlayer", "Failed to initialize AudioTrack: ${e.message}", e)
        }
    }

    private fun processAudioBuffer() {
        playbackJob?.cancel()
        playbackJob = playbackScope.launch {
            Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO)

            while (isActive && !isReleased.get()) {
                val packet: AudioEvent.Success? = synchronized(bufferLock) {
                    jitterBuffer.poll()
                }

                if (packet != null) {
                    // We have a chunk to play
                    val written = processPacket(packet.audioData)
                    if (written <= 0) {
                        Log.w("AudioPlayer", "AudioTrack write returned $written, potential issue.")
                    }
                } else {
                    // No data yet -> short delay to avoid busy loop
                    delay(10)
                }
            }
        }
    }

    private fun processPacket(packet: ByteArray): Int {
        val track = audioTrack ?: return 0

        if (track.playState != AudioTrack.PLAYSTATE_PLAYING) {
            Log.d("AudioPlayer", "Track not playing ${track.playState}")
            track.play()
        }

        var totalWritten = 0
        var offset = 0
        val data = packet
        while (offset < data.size) {
            val result = track.write(data, offset, data.size - offset, AudioTrack.WRITE_BLOCKING)
            if (result <= 0) {
                Log.e("AudioPlayer", "AudioTrack write returned: $result")
                break
            }
            offset += result
            totalWritten += result
        }

        if (totalWritten > 0) {
            Log.d("AudioPlayer", "Successfully wrote $totalWritten bytes to AudioTrack")
        }
        return totalWritten
    }

    fun playAudioData(audioData: AudioEvent.Success) {
        if (audioData.audioData.isEmpty()) {
            Log.w("AudioPlayer", "Attempting to play empty audio data")
            return
        }
        synchronized(bufferLock) {
            val chunkBytes = 960
            var offset = 0
            while (offset < audioData.audioData.size) {
                val remaining = audioData.audioData.size - offset
                val currSize = minOf(chunkBytes, remaining)
                val chunk = ByteArray(currSize)
                System.arraycopy(audioData.audioData, offset, chunk, 0, currSize)

                jitterBuffer.offer(
                    AudioEvent.Success(
                        audioData.eventId,
                        chunk,
                        audioData.arrivalTime
                    )
                )

                offset += currSize
            }
        }
        Log.d("AudioPlayer", "Audio data added. Buffer size: ${jitterBuffer.size}")
    }

    fun release() {
        if (isReleased.getAndSet(true)) return

        playbackScope.cancel()

        synchronized(bufferLock) {
            jitterBuffer.clear()
            audioTrack?.stop()
            audioTrack?.release()
            audioTrack = null
        }
        Log.d("AudioPlayer", "AudioPlayer released")
    }

    private fun monitorBufferHealth() {
        playbackScope.launch {
            while (isActive) {
                val bufferSize = jitterBuffer.size
                val playbackHead = audioTrack?.playbackHeadPosition ?: 0
                Log.d("AudioPlayer", "Buffer health: size=$bufferSize, head=$playbackHead")
                delay(1000)
            }
        }
    }
}
