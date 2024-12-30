package com.example.telly_ces_fallback.model

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.internal.and

class AudioRecorder(
    private val sampleRate: Int = 16000,
    private val channelConfig: Int = AudioFormat.CHANNEL_IN_MONO,
    private val encodingFormat: Int = AudioFormat.ENCODING_PCM_16BIT,
    private val audioBufferSize: Int = AudioRecord.getMinBufferSize(16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
) {
    companion object {
        data class AudioRecorderState(
            val isRecording: Boolean = false,
            val debugMessages: List<String> = emptyList()
        )
    }

    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _state = MutableStateFlow(AudioRecorderState())
    val state = _state.asStateFlow()

    var onAudioData: ((ByteArray) -> Unit)? = null

    fun startRecording() {
        if (recordingJob != null) {
            Log.i("AudioRecorder","Recording is already in progress.")
            return
        }

        coroutineScope.launch {
            try {
                if (audioRecord == null) {
                    audioRecord = AudioRecord(
                        MediaRecorder.AudioSource.MIC,
                        sampleRate,
                        channelConfig,
                        encodingFormat,
                        audioBufferSize
                    )
                }

                audioRecord?.startRecording()
                _state.value = _state.value.copy(isRecording = true)
                Log.d("AudioRecorder","Started recording")

                val buffer = ShortArray(audioBufferSize / 2)

                recordingJob = coroutineScope.launch {
                    while (isActive) {
                        val readSize = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                        if (readSize > 0) {
                            onAudioData?.invoke(buffer.toByteArray())
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("AudioRecorder", "Failed to start recording", e)
                stopRecording()
            }
        }
    }

    fun stopRecording() {
        recordingJob?.cancel()
        recordingJob = null
        audioRecord?.stop()
        _state.value = _state.value.copy(isRecording = false)
        Log.d("AudioRecorder", "Stopped recording")
    }

    fun release() {
        stopRecording()
        audioRecord?.release()
        audioRecord = null
        Log.d("AudioRecorder", "AudioRecorder resources released")
    }


    private fun addDebugMessage(message: String) {
        val currentMessages = _state.value.debugMessages.toMutableList()
        currentMessages.add(message)
        _state.value = _state.value.copy(debugMessages = currentMessages)
    }

    private fun ShortArray.toByteArray(): ByteArray {
        val byteArray = ByteArray(size * 2)
        forEachIndexed { index, value ->
            byteArray[index * 2] = (value and 0xFF).toByte()
            byteArray[index * 2 + 1] = ((value.toInt() shr 8) and 0xFF).toByte()
        }
        return byteArray
    }
}