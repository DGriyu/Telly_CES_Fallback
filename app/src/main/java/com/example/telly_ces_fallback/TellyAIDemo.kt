package com.example.telly_ces_fallback

import android.app.Application
import android.content.Context
import android.os.Process
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class TellyAIDemo : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()

        if (isMainProcess()) {
            initializeApp()
        }
    }

    private fun initializeApp() {
        instance = this

        if (BuildConfig.DEBUG) {

        }

        initializeDependencies()
    }

    private fun initializeDependencies() {
        applicationScope.launch {
            // Initialize background dependencies here
            Log.d("TellyAIDemo", "Dependencies initialized")
        }
    }

    private fun isMainProcess(): Boolean {
        val pid = Process.myPid()
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        for (processInfo in activityManager.runningAppProcesses ?: emptyList()) {
            if (processInfo.pid == pid) {
                return packageName == processInfo.processName
            }
        }
        return false
    }

    companion object {
        @Volatile
        private var instance: TellyAIDemo? = null

        fun getInstance(): TellyAIDemo {
            return instance ?: throw IllegalStateException(
                "Application not initialized. Ensure it is declared in the AndroidManifest.xml."
            )
        }
    }
}