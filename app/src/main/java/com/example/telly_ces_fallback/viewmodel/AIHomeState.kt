package com.example.telly_ces_fallback.viewmodel

sealed class AIHomeState {
    data object Launching : AIHomeState()
    data object Loading : AIHomeState()
    data object Loaded : AIHomeState()
    data class Error(val message: String) : AIHomeState()
}