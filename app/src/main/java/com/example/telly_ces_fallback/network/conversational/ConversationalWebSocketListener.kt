package com.example.telly_ces_fallback.network.conversational

import android.util.Base64
import android.util.Log
import com.example.telly_ces_fallback.model.audio.AudioEvent
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import org.json.JSONObject

class ConversationalWebSocketListener(
    private val onMessageReceived: (WebSocketMessage) -> Unit,
    private val onAudioReceived: (AudioEvent) -> Unit,
    private val onPingReceived: (Long) -> Unit,
    private var conversationId: String?,
    private val onConnectionStateChange: (ConnectionState) -> Unit
): WebSocketListener() {

    var pongTime = System.currentTimeMillis()

    override fun onOpen(webSocket: WebSocket, response: Response) {
        Log.i("ConversationalWebSocketListener", "WebSocket opened with response code: ${response.code}")
        onConnectionStateChange(ConnectionState.Connected)
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        try {
            val json = JSONObject(text)
            when (json.getString("type")) {
                "conversation_initiation_metadata" -> {
                    onConnectionStateChange(ConnectionState.Connected)
                    val metadata = json.getJSONObject("conversation_initiation_metadata_event")
                    val newConversationId = metadata.getString("conversation_id")
                    val audioFormat = metadata.getString("agent_output_audio_format")
                    if (conversationId != newConversationId) {
                        conversationId = newConversationId
                        Log.d("ConversationalWebSocketListener","Conversation initialized with ID: $conversationId, audio format: $audioFormat")
                    }
                }
                "ping" -> {
                    val pingEvent = json.getJSONObject("ping_event")
                    val eventId = pingEvent.getLong("event_id")
                    val pingTime = System.currentTimeMillis()
                    val rtt = pingTime - pongTime
                    Log.d("ConversationalWebSocket", "Got ping in: $rtt")
                    onPingReceived(rtt)
                    sendPong(eventId, webSocket)
                    pongTime = System.currentTimeMillis()
                }
                /**"interruption" -> {
                    val interuptionEvent = json.getJSONObject("interruption_event")
                    val eventId = interuptionEvent.getLong("event_id")
                    Log.d("ConversationalWebSocketListener", "Received interruption: $eventId")
                    onMessageReceived(WebSocketMessage.Audio(AudioEvent.Success(eventId, ByteArray(0), true)))
                }**/
                "user_transcript" -> {
                    val transcriptEvent = json.getJSONObject("user_transcription_event")
                    val transcript = transcriptEvent.getString("user_transcript")
                    Log.d("ConversationalWebSocketListener", "Forwarding transcript to UI: $transcript")
                    onMessageReceived(WebSocketMessage.Text(transcript))
                }
                "agent_response" -> {
                    val responseEvent = json.getJSONObject("agent_response_event")
                    val response = responseEvent.getString("agent_response")
                    Log.d("ConversationalWebSocketListener", "Received agent response: $response")
                    onMessageReceived(WebSocketMessage.Text(response))
                }
                "audio" -> {
                    Log.d("ConversationalWebSocketListener", "Received audio event from agent")
                    val audioData = handleAudioMessage(text)
                    onAudioReceived(audioData)
                }
                else -> {
                    //Log.d("ConversationalWebSocketListener", "Received unhandled message type: ${json.getString("type")}")
                }
            }
        } catch (e: Exception) {
            onMessageReceived(WebSocketMessage.Error(e))
            Log.e("ConversationalWebSocketListener", "Error processing message: ${text.take(200)}...", e)
        }
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        Log.d("ConversationalWebSocketListener", "Received binary message of size: ${bytes.size}")
        onMessageReceived(WebSocketMessage.Binary(bytes))
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Log.e("ConversationalWebSocketListener", "WebSocket failure", t)
        if (response != null) {
            Log.e("ConversationalWebSocketListener", "Response code: ${response.code}, message: ${response.message}")
            response.body?.string()?.let { body ->
                Log.e("ConversationalWebSocketListener", "Response body: $body")
            }
        }
        onMessageReceived(WebSocketMessage.Error(t))
        onConnectionStateChange(ConnectionState.Error(t))
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        Log.d(
            "ConversationalWebSocketListener",
            "WebSocket closing with code: $code, reason: $reason"
        )
        onConnectionStateChange(ConnectionState.Closing)
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        Log.d("ConversationalWebSocketListener", "WebSocket closed with code: $code, reason: $reason")
        onConnectionStateChange(ConnectionState.Disconnected)
    }

    private fun sendPong(eventId: Long, webSocket: WebSocket?) {
        val pongMessage = JSONObject().apply {
            put("type", "pong")
            put("event_id", eventId)
        }.toString()
        webSocket?.send(pongMessage)
        Log.d("ConversationalWebSocket", "Sent pong message for event: $eventId")
    }

    private fun handleAudioMessage(message: String): AudioEvent {
        try {
            val jsonObject = JSONObject(message)
            val audioEvent = jsonObject.getJSONObject("audio_event")
            val audioBase64 = audioEvent.getString("audio_base_64")
            val eventId = audioEvent.getLong("event_id")

            Log.d("ConversationalWebSocketListener", "Processing audio event #$eventId")
            val audioData = Base64.decode(audioBase64, Base64.DEFAULT)
            if (audioData.isEmpty()) {
                Log.w("ConversationalWebSocketListener", "Received empty audio data for event #$eventId")
            }

            Log.d("ConversationalWebSocketListener", "Successfully forwarded audio event #$eventId to player (${audioData.size} bytes)", )
            return AudioEvent.Success(eventId, audioData, System.currentTimeMillis())

        } catch (e: Exception) {
            Log.e(
                "ConversationalWebSocketListener",
                "Error handling audio message: ${e.message}",
                e
            )
            try {
                Log.e(
                    "ConversationalWebSocketListener",
                    "Raw message preview: ${message.take(100)}...",
                    e
                )
            } catch (e2: Exception) {
                Log.e("ConversationalWebSocketListener", "Could not log raw message", e2)
            }
            return AudioEvent.Error(e, message)
        }
    }
}