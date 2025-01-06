package com.example.telly_ces_fallback.repository

import android.util.Log
import com.example.telly_ces_fallback.model.audio.AudioEvent
import com.example.telly_ces_fallback.model.audio.AudioPlayer
import com.example.telly_ces_fallback.model.audio.AudioRecorder
import com.example.telly_ces_fallback.model.knowledge_graph.KnowledgeGraphResult
import com.example.telly_ces_fallback.network.conversational.ConversationalWebSocket
import com.example.telly_ces_fallback.network.conversational.WebSocketMessage
import com.example.telly_ces_fallback.network.conversational.ConnectionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConversationRepository @Inject constructor(
    private val webSocket: ConversationalWebSocket,
    private val audioPlayer: AudioPlayer,
    private val audioRecorder: AudioRecorder,
) {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val connectionState: Flow<ConnectionState> = webSocket.connectionState
    val recordingState: StateFlow<AudioRecorder.Companion.AudioRecorderState> = audioRecorder.state

    private val audioMessages: Flow<AudioEvent> = webSocket.audioMessage

    val messages: Flow<String> = webSocket.messages.map { message ->
            when (message) {
                is WebSocketMessage.Text -> message.content
                is WebSocketMessage.Error -> "Error: ${message.error.message}"
                else -> "Unhandled message type"
            }
        }
    val knowledgeGraphResult: Flow<KnowledgeGraphResult> = webSocket.graphQuery


    // Websocket
    fun connectWebSocket() = repositoryScope.launch { webSocket.connect() }
    fun disconnectWebSocket() = repositoryScope.launch { webSocket.disconnect() }
    fun sendRecordedAudio(audioData: ByteArray) = webSocket.sendAudio(audioData)

    // Audio Player
    fun initializeAudioTrack() {
        repositoryScope.launch {
            try {
                audioPlayer.initializeAudioTrack()
                Log.d("ConversationalRepository", "Audio track initialized successfully")
            } catch (e: Exception) {
                Log.e("ConversationalRepository", "Failed to initialize audio track", e)
            }
        }

        repositoryScope.launch {
            try {
                audioMessages.collect { audioEvent ->
                    Log.d("ConversationalRepository", "Converstational audio received")
                    when (audioEvent) {
                        is AudioEvent.Success -> {
                            Log.d("ConversationalRepository", "Audio data sent to player")
                            audioPlayer.playAudioData(audioEvent)
                        }
                        is AudioEvent.Error -> throw audioEvent.error
                    }
                }
            } catch (e: Exception) {
                Log.e("ConversationalRepository", "Error collecting audio messages", e)
            }
        }
    }

    private fun releaseAudioPlayer() = repositoryScope.launch { audioPlayer.release() }

    fun startRecording() {
        audioRecorder.startRecording()
        audioRecorder.onAudioData = { audioData -> sendRecordedAudio(audioData) }
       // adjustBuffer()
    }
    private fun stopRecording() = audioRecorder.stopRecording()
    private fun releaseAudioRecorder() = audioRecorder.release()

    fun cleanup() {
        stopRecording()
        disconnectWebSocket()
        releaseAudioRecorder()
        releaseAudioPlayer()
        repositoryScope.cancel()
    }
}