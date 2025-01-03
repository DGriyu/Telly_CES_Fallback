package com.example.telly_ces_fallback

import android.app.ActivityOptions
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.teevee.sdk.TellyPartnerSDK
import com.teevee.sdk.components.display.DisplayType
import com.teevee.sdk.components.display.transitionToDisplay
import dagger.hilt.android.AndroidEntryPoint


/**
 * Launches main AI activity on the second display
 */

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED) {
            Log.d("MainActivity", "Record Audio permission is already granted. Proceed with launching AIHomeActivity")
            launchAIHomeActivity()
        } else {
            Log.d("MainActivity", "Record Audio Permission not granted. Request the permission")
            requestMicrophonePermission()
        }
    }

    private fun launchAIHomeActivity() {
        val displayManager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        val displays = displayManager.displays

        // Check if a second display is available
        if (displays.size < 2) {
            Toast.makeText(this, "No second display found.", Toast.LENGTH_SHORT).show()
            return
        }

        val sdk = TellyPartnerSDK.create(this)

        val intent = Intent(this, AIHomeActivity::class.java)
        val options = ActivityOptions.makeBasic()
        val secondDisplay = displays[1]
        options.setLaunchDisplayId(secondDisplay.displayId)

        sdk.transitionToDisplay(
            this,
            DisplayType.SMART_DISPLAY,
            intent,
            options
        )

        // Finish MainActivity after launching the new activity
        finish()
        sdk.close()
    }


    private fun requestMicrophonePermission() {
        // Show rationale if needed
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.RECORD_AUDIO)) {
            // Show an explanation to the user asynchronously
            AlertDialog.Builder(this)
                .setTitle("Microphone Permission Needed")
                .setMessage("This app requires access to your microphone to function properly.")
                .setPositiveButton("OK") { dialog, _ ->
                    // Request the permission after explanation
                    requestPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    // User declined. Handle accordingly
                    handlePermissionDenied()
                    dialog.dismiss()
                }
                .create()
                .show()
        } else {
            // No explanation needed; request the permission directly
            requestPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Proceed with launching AIHomeActivity
                launchAIHomeActivity()
            } else {
                // Permission denied. Handle accordingly
                handlePermissionDenied()
            }
        }


    private fun handlePermissionDenied() {
        // Inform the user that the app cannot function without the permission
        Log.d("MainActivity", "Record Audio Permission Denied")
        AlertDialog.Builder(this)
            .setTitle("Permission Denied")
            .setMessage("Without microphone access, the app cannot function properly. Please grant the permission in settings.")
            .setPositiveButton("Open Settings") { dialog, _ ->
                // Open app settings for the user to manually grant permissions
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
                dialog.dismiss()
            }
            .setNegativeButton("Exit") { dialog, _ ->
                finish()
                dialog.dismiss()
            }
            .create()
            .show()
    }

}