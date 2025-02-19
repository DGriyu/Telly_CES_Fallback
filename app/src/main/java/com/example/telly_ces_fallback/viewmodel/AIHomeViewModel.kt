package com.example.telly_ces_fallback.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.telly_ces_fallback.model.knowledge_graph.KnowledgeGraphResult
import com.example.telly_ces_fallback.network.conversational.ConnectionState
import com.example.telly_ces_fallback.repository.ConversationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AIHomeViewModel  @Inject constructor(
    private val repository: ConversationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AIHomeState>(AIHomeState.Launching)
    val uiState: StateFlow<AIHomeState> = _uiState.asStateFlow()

    private val _navigation = MutableStateFlow<KnowledgeGraphResult?>(null)
    val navigation: StateFlow<KnowledgeGraphResult?> = _navigation.asStateFlow()


    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Connecting)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _conversation = MutableStateFlow<List<String>>(emptyList())
    val conversation: StateFlow<List<String>> = _conversation.asStateFlow()

    init {
        _uiState.update { AIHomeState.Launching }
        repository.initializeAudioTrack()
        viewModelScope.launch {
            delay(2800)
            _uiState.update { AIHomeState.Loading }
            repository.connectWebSocket()
            observeWebSocketEvents()
            observeMessages()
            observeKnowledgeGraphResults()
        }
    }

    private fun observeKnowledgeGraphResults() {
        viewModelScope.launch {
            repository.knowledgeGraphResult.collect { result ->
                _navigation.emit( result )
            }
        }
    }

    private fun observeWebSocketEvents() {
        viewModelScope.launch {
            repository.connectionState.collect { event ->
                when (event) {
                    is ConnectionState.Connecting -> {
                        _uiState.update { AIHomeState.Loading }
                        Log.d("AIHomeViewModel", "WebSocket connecting")
                    }

                    is ConnectionState.Error -> {
                        _uiState.update {
                            AIHomeState.Error(
                                event.throwable.message ?: "Unknown error"
                            )
                        }
                        retryConnection()
                        Log.e(
                            "AIHomeViewModel",
                            "WebSocket error: ${event.throwable}, event: ${event.throwable}"
                        )

                    }

                    is ConnectionState.Closing -> {
                        if (event.code == 1002) {
                            _uiState.update { AIHomeState.Error("WebSocket Closing, reconnecting...") }
                            retryConnection()
                        } else if (event.code == 1011) {
                            _uiState.update { AIHomeState.Error("WebSocket Closing Time Limit Reached") }
                        } else {
                            _uiState.update { AIHomeState.Error("WebSocket Closing") }
                            Log.d("AIHomeViewModel", "WebSocket closing")
                        }

                    }

                    is ConnectionState.Connected -> {
                        repository.startRecording()
                        _uiState.emit(AIHomeState.Loaded)
                        Log.d("AIHomeViewModel", "WebSocket connected")
                    }

                    is ConnectionState.Disconnected -> {
                        _uiState.update { AIHomeState.Error("WebSocket disconnected") }
                        retryConnection()
                        Log.d("AIHomeViewModel", "WebSocket disconnected")
                    }
                }
            }
        }
    }

    private fun  retryConnection() {
        viewModelScope.launch {
            var retryCount = 0
            val maxRetries = 5
            val delayTime = 100L

            while (retryCount < maxRetries) {
                try {
                    Log.d("AIHomeViewModel", "Retrying WebSocket connection, attempt: ${retryCount + 1}")
                    _uiState.update { AIHomeState.Loading }
                    repository.connectWebSocket() // Implement this method in your repository
                    repository.resetAudioTrack()
                    break
                } catch (e: Exception) {
                    retryCount++
                    Log.e("AIHomeViewModel", "Retry failed: ${e.message}, attempt: $retryCount")
                    if (retryCount == maxRetries) {
                        Log.e("AIHomeViewModel", "Max retry attempts reached")
                        _uiState.update { AIHomeState.Error("Max retries reached. Could not reconnect.") }
                    } else {
                        delay(delayTime)
                    }
                }
            }
        }
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


    override fun onCleared() {
        super.onCleared()
        repository.cleanup()
        viewModelScope.cancel()
    }
}