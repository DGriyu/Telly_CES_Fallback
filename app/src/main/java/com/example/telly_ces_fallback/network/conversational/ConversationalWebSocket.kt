package com.example.telly_ces_fallback.network.conversational

import android.util.Base64
import android.util.Log
import com.example.telly_ces_fallback.model.audio.AudioEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class ConversationalWebSocket(
    private val apiKey: String,
    private val agentId: String
) : WebSocketClient {

    private var webSocket: WebSocket? = null
    private var conversationId: String? = null

    private val client = OkHttpClient.Builder()
        .readTimeout(30, TimeUnit.SECONDS)
        .connectTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        })
        .build()

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    override val connectionState: Flow<ConnectionState> = _connectionState

    private val _messages = Channel<WebSocketMessage>(capacity = Channel.BUFFERED)
    override val messages: Flow<WebSocketMessage> = _messages.receiveAsFlow()

    private val _audioMessage = Channel<AudioEvent>(capacity = Channel.BUFFERED)
    override val audioMessage: Flow<AudioEvent> = _audioMessage.receiveAsFlow()

    private val _ping = Channel<Long>()
    override val ping: Flow<Long> = _ping.receiveAsFlow()

    override fun connect() {
        if (_connectionState.value is ConnectionState.Connected) return
        System.currentTimeMillis()

        Log.i("ConversationalWebSocket","Getting signed URL for WebSocket connection")
                Log.d("ConversationalWebSocket","API Key length: ${apiKey.length}, first 5 chars: ${apiKey.take(5)}")

        val signedUrlRequest = Request.Builder()
            .url("https://api.elevenlabs.io/v1/convai/conversation/get_signed_url?agent_id=$agentId")
            .addHeader("xi-api-key", apiKey)
            .get()
            .build()
        _connectionState.value = ConnectionState.Connecting
        client.newCall(signedUrlRequest).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                _connectionState.value = ConnectionState.Error(e)
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val responseBody = response.body?.string()
                    Log.d("ConversationalWebSocket","Signed URL response: $responseBody")

                    if (!response.isSuccessful || responseBody == null) {
                        val errorMessage = "Failed to get signed URL: ${response.code}. Response body: $responseBody"
                        Log.e("ConversationalWebSocket", errorMessage)
                        _connectionState.value = ConnectionState.Error(IOException(errorMessage))
                        return
                    }

                    val signedUrl = JSONObject(responseBody).getString("signed_url")
                    connectWebSocket(signedUrl)
                } catch (e: Exception) {
                    Log.e("ConversationalWebSocket", "Error processing signed URL response")
                    _connectionState.value = ConnectionState.Error(e)
                }
            }
        })
    }

    private fun connectWebSocket(signedUrl: String) {
        val request = Request.Builder()
            .url(signedUrl)
            .build()

        _connectionState.value = ConnectionState.Connecting
        Log.i("ConversationalWebSocket","Connecting to WebSocket with signed URL")

        webSocket = client.newWebSocket(request, ConversationalWebSocketListener(
            onMessageReceived = { message ->
                _messages.trySend(message)
                                },
            onAudioReceived = { audioData ->
                _audioMessage.trySend(audioData)
            },
            onPingReceived =  { ping -> _ping.trySend(ping) },
            conversationId,
            onConnectionStateChange = { state ->
                _connectionState.update {  state }
            }
        )
        )
    }

    override  fun sendAudio(audioData: ByteArray) {
        if (_connectionState.value !is ConnectionState.Connected) {
            Log.w("ConversationalWebSocket","Attempting to send audio while not connected")
            return
        }
        try {
            val base64Audio = Base64.encodeToString(audioData, Base64.NO_WRAP)
            val message = JSONObject().apply {
                put("user_audio_chunk", base64Audio)
            }.toString()

            webSocket?.send(message)
           Log.d("ConversationalWebSocket","Sent audio data: ${audioData.size} bytes")
        } catch (e: Exception) {
            Log.e("ConversationalWebSocket", "Error sending audio data", e)
        }
    }

    override fun disconnect() {
        try {
            webSocket?.close(1000, "User initiated disconnect")
            webSocket = null
            conversationId = null
            _connectionState.value = ConnectionState.Disconnected
            Log.d("ConversationalWebSocket", "WebSocket disconnected by user")
        } catch (e: Exception) {
            Log.e("ConversationalWebSocket", "Error during disconnect", e)
        }
    }

    fun testApiKey(callback: (Boolean, String) -> Unit) {
        val request = Request.Builder()
            .url("https://api.elevenlabs.io/v1/user/subscription")
            .addHeader("xi-api-key", apiKey)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, "Network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                callback(response.isSuccessful,
                    if (response.isSuccessful) "API key valid"
                    else "API key invalid: ${response.code}, $responseBody")
            }
        })
    }
}

interface WebSocketClient {
    fun connect()
    fun disconnect()
    fun sendAudio(audioData: ByteArray)
    val messages: Flow<WebSocketMessage>
    val audioMessage: Flow<AudioEvent>
    val ping: Flow<Long>
    val connectionState: Flow<ConnectionState>
}