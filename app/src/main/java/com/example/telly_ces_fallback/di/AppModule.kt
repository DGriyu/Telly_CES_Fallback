package com.example.telly_ces_fallback.di

import com.example.telly_ces_fallback.BuildConfig
import com.example.telly_ces_fallback.model.audio.AudioPlayer
import com.example.telly_ces_fallback.model.audio.AudioRecorder
import com.example.telly_ces_fallback.network.conversational.ConversationalWebSocket
import com.example.telly_ces_fallback.repository.ConversationRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideMainWebSocket(): ConversationalWebSocket {
        return ConversationalWebSocket(
            apiKey = BuildConfig.ELEVEN_LABS_API_KEY,
            agentId = BuildConfig.ELEVEN_LABS_AGENT_ID
        )
    }

    @Provides
    @Singleton
    fun provideAudioRecorder(): AudioRecorder {
        return AudioRecorder()
    }

    @Provides
    @Singleton
    fun provideConversationRepository(
        webSocket: ConversationalWebSocket
    ): ConversationRepository {
        return ConversationRepository(webSocket, AudioPlayer(), AudioRecorder())
    }
}