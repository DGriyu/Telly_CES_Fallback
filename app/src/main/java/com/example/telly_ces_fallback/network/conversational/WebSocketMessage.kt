package com.example.telly_ces_fallback.network.conversational

import com.example.telly_ces_fallback.model.AudioEvent
import okio.ByteString

sealed class WebSocketMessage {
    data class Text(val content: String) : WebSocketMessage()
    data class Audio(val content: AudioEvent) : WebSocketMessage()
    data class Binary(val content: ByteString) : WebSocketMessage()
    data class Error(val error: Throwable) : WebSocketMessage()
}