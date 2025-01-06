package com.example.telly_ces_fallback.widget

import KnowledgeGraphCard
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import com.example.telly_ces_fallback.model.knowledge_graph.KnowledgeGraphResult
import kotlinx.serialization.json.Json

class FloatingWidgetService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var floatingComposeView: View
    private var knowledgeGraphData: KnowledgeGraphResult? = null

    override fun onCreate() {
        super.onCreate()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // Create LayoutParams for Floating Widget
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 200
        }
        Log.d("FloatingWidgetService", "onCreate: $knowledgeGraphData")
        floatingComposeView = ComposeView(this).apply {
            setContent {
                knowledgeGraphData?.let { KnowledgeGraphCard(it) }
            }
        }

        windowManager.addView(floatingComposeView, params)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.getStringExtra("event")?.let { jsonData ->
            knowledgeGraphData = Json.decodeFromString<KnowledgeGraphResult>(jsonData)
        }
        return START_STICKY
    }


    override fun onDestroy() {
        super.onDestroy()
        if (::floatingComposeView.isInitialized) {
            windowManager.removeView(floatingComposeView)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}