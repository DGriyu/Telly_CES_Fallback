package com.example.telly_ces_fallback.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.telly_ces_fallback.network.conversational.ConnectionState
import com.example.telly_ces_fallback.repository.ConversationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AIHomeViewModel  @Inject constructor(
    private val repository: ConversationRepository
) : ViewModel() {

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _conversation = MutableStateFlow<List<String>>(emptyList())
    val conversation: StateFlow<List<String>> = _conversation.asStateFlow()

    init {
        repository.connectWebSocket()
        repository.initializeAudioTrack()
        observeWebSocketEvents()
        observeMessages()
        observeConnectionState()
    }

    private fun observeWebSocketEvents() {
        connectionState.onEach { event ->
            when (event) {
                is ConnectionState.Connecting -> {
                    Log.d("ConversationalRepository", "WebSocket connecting")
                }
                is ConnectionState.Error -> {
                    Log.e("ConversationalRepository", "WebSocket error: ${event.throwable}, event: ${event.throwable}")

                }
                is ConnectionState.Closing -> {
                    Log.d("ConversationalRepository", "WebSocket closing")

                }
                is ConnectionState.Connected -> {
                    repository.startRecording()
                    Log.d("ConversationalRepository",   "WebSocket connected")
                }
                is ConnectionState.Disconnected -> {
                    Log.d("ConversationalRepository", "WebSocket disconnected")
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun observeMessages() {
        viewModelScope.launch {
            repository.messages.collect { message ->
                _conversation.update { currentMessages ->
                    currentMessages + message
                }
            }
        }
    }

    private fun observeConnectionState() {
        viewModelScope.launch {
            repository.connectionState.collect { state ->
                _connectionState.value = state
            }
        }
    }


    override fun onCleared() {
        super.onCleared()
        repository.cleanup()
    }
}