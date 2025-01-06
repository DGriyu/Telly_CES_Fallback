package com.example.telly_ces_fallback

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.DisposableEffect
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.example.telly_ces_fallback.model.knowledge_graph.KnowledgeGraphResult
import com.example.telly_ces_fallback.ui.screens.AIHomeScreen
import com.example.telly_ces_fallback.viewmodel.AIHomeViewModel
import com.example.telly_ces_fallback.widget.FloatingWidgetService
import com.example.telly_ces_fallback.widget.PipWidgetActivity
import com.teevee.sdk.TellyPartnerSDK
import com.teevee.sdk.components.display.DisplayType
import com.teevee.sdk.components.display.transitionToDisplay
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@AndroidEntryPoint
class AIHomeActivity : ComponentActivity() {
    private val viewModel: AIHomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.connectionState
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Testing Knowledge Graph launch on secondary screen
        /**
        lifecycleScope.launch {
            viewModel.navigation.collect { event ->
                event?.let {
                    Log.d("AIHomeActivity", "Lauch knowledge graph event: $event")
                    val intent = Intent(this@AIHomeActivity, PipWidgetActivity::class.java)
                    ContextCompat.startForegroundService(this@AIHomeActivity, intent)
                    intent.putExtra("event", Json.encodeToString( event))
                    val displayManager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
                    val displays = displayManager.displays


                    val sdk = TellyPartnerSDK.create(this@AIHomeActivity)
                    val options = ActivityOptions.makeBasic()
                    val primaryDisplay = displays[0]
                    options.setLaunchDisplayId(primaryDisplay.displayId)
                    sdk.transitionToDisplay(
                        this@AIHomeActivity,
                        DisplayType.THEATER_DISPLAY,
                        intent,
                        options
                    )
                    sdk.close()
                }
            }
        }**/

        setContent {
            AIHomeScreen(viewModel.conversation, viewModel.uiState)

            DisposableEffect(Unit) {
                onDispose {
                    // cleanup
                }
            }
        }
    }
}
