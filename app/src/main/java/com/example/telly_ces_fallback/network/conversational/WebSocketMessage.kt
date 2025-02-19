package com.example.telly_ces_fallback.network.conversational

import com.example.telly_ces_fallback.model.audio.AudioEvent
import com.example.telly_ces_fallback.model.knowledge_graph.KnowledgeGraphResult
import okio.ByteString

sealed class WebSocketMessage {
    data class Text(val content: String) : WebSocketMessage()
    data class KnowledgeGraph(val content: KnowledgeGraphResult) : WebSocketMessage()
    data class Audio(val content: AudioEvent) : WebSocketMessage()
    data class Binary(val content: ByteString) : WebSocketMessage()
    data class Error(val error: Throwable) : WebSocketMessage()
}