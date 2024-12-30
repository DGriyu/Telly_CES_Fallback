package com.example.telly_ces_fallback

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint


/**
 * Launches main AI activity on the second display
 */

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val displayManager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        val displays = displayManager.displays

        if (displays.size > 1) {
            Log.i("AIHomeActivity","Launching AIHomeActivity on secondary display")
            val secondDisplay = displays[1] // Target the second display
            val intent = Intent(this, AIHomeActivity::class.java)
            val options = ActivityOptions.makeBasic()
            options.setLaunchDisplayId(secondDisplay.displayId)
            startActivity(intent, options.toBundle())
        } else {
            Log.i("AIHomeActivity","Second Display not found. Launching AIHomeActivity on Primary Display")
            // Fallback if no secondary display is available
            startActivity(Intent(this, AIHomeActivity::class.java))
        }
        finish()
    }
}