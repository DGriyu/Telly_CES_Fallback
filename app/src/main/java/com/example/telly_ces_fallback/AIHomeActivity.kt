package com.example.telly_ces_fallback

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.telly_ces_fallback.ui.screens.AIHomeScreen
import com.example.telly_ces_fallback.viewmodel.AIHomeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AIHomeActivity : ComponentActivity() {
    private val viewModel: AIHomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.connectionState
        enableEdgeToEdge()
        setContent {
            AIHomeScreen(viewModel.conversation)
        }
    }
}