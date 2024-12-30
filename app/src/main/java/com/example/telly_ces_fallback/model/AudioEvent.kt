package com.example.telly_ces_fallback.model

sealed class AudioEvent {
    data class Success(val eventId: Long, val audioData: ByteArray, val arrivalTime: Long) : AudioEvent()
    data class Error(val error: Throwable, val rawMessage: String?) : AudioEvent()
}