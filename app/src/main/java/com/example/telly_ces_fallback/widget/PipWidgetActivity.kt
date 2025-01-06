package com.example.telly_ces_fallback.widget

import KnowledgeGraphCard
import android.app.PictureInPictureParams
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.telly_ces_fallback.model.knowledge_graph.KnowledgeGraphResponse
import com.example.telly_ces_fallback.model.knowledge_graph.KnowledgeGraphResult
import kotlinx.serialization.json.Json

class PipWidgetActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        val knowledgeGraphData = intent.getStringExtra("event")?.let {
            Json.decodeFromString<KnowledgeGraphResult>(it)
        }

        setContent {
            knowledgeGraphData?.let { KnowledgeGraphCard(it) }
        }
    }
}