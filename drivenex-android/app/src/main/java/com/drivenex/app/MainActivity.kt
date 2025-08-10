package com.drivenex.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.drivenex.app.ui.theme.DriveNexTheme

class MainActivity : ComponentActivity() {

    private lateinit var locationManager: LocationManager
    private lateinit var biometricAuthManager: BiometricAuthManager
    private lateinit var notificationManager: DriveNexNotificationManager
    private lateinit var emergencyManager: EmergencyManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locationManager = LocationManager(this)
        biometricAuthManager = BiometricAuthManager(this)
        notificationManager = DriveNexNotificationManager(this)
        emergencyManager = EmergencyManager(this)

        setContent {
            DriveNexTheme {
                DriveNexApp()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        locationManager.stopLocationUpdates()
    }
}