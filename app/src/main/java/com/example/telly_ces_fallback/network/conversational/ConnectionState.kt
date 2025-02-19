package com.example.telly_ces_fallback.network.conversational

/**
 * Represents the state of the Main Web Socket.
 */
sealed class ConnectionState {
    object Connected : ConnectionState()
    object Disconnected : ConnectionState()
    object Connecting : ConnectionState()
    data class Closing(val code: Int? = null) : ConnectionState()
    data class Error(val throwable: Throwable) : ConnectionState()
}