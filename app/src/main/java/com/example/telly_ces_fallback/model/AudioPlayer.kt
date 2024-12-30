package com.example.telly_ces_fallback.model

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
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
import kotlinx.coroutines.withContext
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean

class AudioPlayer(
    private val sampleRate: Int = 16000,
    private val channelConfig: Int = AudioFormat.CHANNEL_OUT_MONO,
    private val encodingFormat: Int = AudioFormat.ENCODING_PCM_16BIT
) {
    private var audioTrack: AudioTrack? = null
    private var globalEventId = 0L

    private val chunkSize: Int = 960

    private val jitterBuffer = LinkedBlockingQueue<AudioEvent.Success>()
    private val bufferLock = Object()

    private var baseBufferSize = 0
    private var bufferSize = 0
    private val desiredPlayoutDelayMs = 60

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
            bufferSize = baseBufferSize * 12
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

            Log.d("AudioPlayer", "AudioTrack initialized and started")
        } catch (e: Exception) {
            release()
            Log.e("AudioPlayer", "Failed to initialize AudioTrack: ${e.message}", e)
        }
    }

    private fun processAudioBuffer() {
        playbackJob?.cancel()

        playbackJob = playbackScope.launch {
            while (isActive && !isReleased.get()) {
                withContext(Dispatchers.IO) {
                    var wroteSomething = false

                    synchronized(bufferLock) {
                        var total = 0
                        val head = jitterBuffer.peek()
                        if (head != null) {
                            val now = System.currentTimeMillis()
                            val scheduledPlayTime = head.arrivalTime + desiredPlayoutDelayMs
                            if (now >= scheduledPlayTime) {
                                while (jitterBuffer.isNotEmpty()) {
                                    val packet = jitterBuffer.poll() ?: break
                                    val written = processPacket(packet.audioData)
                                    if (written > 0) {
                                        wroteSomething = true
                                        Log.d(
                                            "AudioPlayer",
                                            "AudioTrack write with chunkId: ${packet.audioData.size}"
                                        )
                                    } else if (written == 0) {
                                        Log.d(
                                            "AudioPlayer",
                                            "No bytes written this pass (buffer possibly full)."
                                        )
                                        break
                                    }
                                }
                            }
                        }
                    }
                    if (!wroteSomething) {
                        delay(5)
                    } else {
                        delay(30)
                    }
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
            val result = track.write(data, offset, data.size - offset)
            if (result <= 0) {
                // Typically indicates an error or that the buffer is full in non-blocking mode
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

    fun adjustBufferSize(rtt: Long) {
        /*
        val newBufferSize = when {
            rtt < 100 -> baseBufferSize * 8   // Low latency
            rtt in 100..300 -> baseBufferSize * 12 // Medium latency
            else -> baseBufferSize * 300      // High latency
        }

        synchronized(bufferLock) {
            if (newBufferSize != bufferSize) {
                bufferSize = newBufferSize
                // Note: setBufferSizeInFrames() might not always be respected
                audioTrack?.setBufferSizeInFrames(bufferSize / 2)
                Log.d("AudioPlayer", "Buffer size adjusted: $bufferSize")
            }
        }
        */
    }

    fun playAudioData(audioData: AudioEvent.Success) {
        Log.d("AudioPlayer", "Successfully wrote ${audioTrack?.playState} bytes to AudioTrack")
        if (audioData.audioData.isEmpty()) {
            Log.w("AudioPlayer", "Attempting to play empty audio data")
            return
        }

        Log.d("AudioPlayer", "Splitting and enqueueing ${audioData.audioData.size} bytes to AudioTrack")
        globalEventId++
        val localId = (audioData.eventId + globalEventId) * 10000
        synchronized(bufferLock) {
            var offset = 0
            while (offset < audioData.audioData.size) {
                val remaining = audioData.audioData.size - offset
                val currentChunkSize = minOf(chunkSize, remaining)
                val chunk = ByteArray(chunkSize)
                System.arraycopy(audioData.audioData, offset, chunk, 0, currentChunkSize)

                // Create new audio event for each chunk
                val chunkEvent = AudioEvent.Success(
                    eventId = localId + (offset / chunkSize),
                    audioData = chunk,
                    arrivalTime = audioData.arrivalTime
                )

                jitterBuffer.offer(chunkEvent)
                offset += currentChunkSize
            }
        }
        Log.d("AudioPlayer", "Audio data added to jitter buffer. Buffer size: ${jitterBuffer.size}")
    }

    fun release() {
        // Guard against double-release
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
}