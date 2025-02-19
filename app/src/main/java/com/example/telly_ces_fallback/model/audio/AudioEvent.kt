package com.example.telly_ces_fallback.model.audio

sealed class AudioEvent {
    data class Success(val eventId: Long, val audioData: ByteArray, val arrivalTime: Long, val interuption: Boolean = false) : AudioEvent()
    data class Error(val error: Throwable, val rawMessage: String?) : AudioEvent()
}