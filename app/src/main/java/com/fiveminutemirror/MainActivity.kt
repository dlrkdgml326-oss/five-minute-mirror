package com.fiveminutemirror

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat

private enum class Screen { HOME, APP_SELECTION, USAGE_ACCESS, MIRROR }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val hasSelections = remember { ProtectedAppsStore(this).load().isNotEmpty() }
            var screen by remember {
                mutableStateOf(if (hasSelections) Screen.HOME else Screen.APP_SELECTION)
            }
            var cameraAllowed by remember {
                mutableStateOf(
                    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                        PackageManager.PERMISSION_GRANTED
                )
            }

            val cameraLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { granted ->
                cameraAllowed = granted
                if (granted) screen = Screen.MIRROR
            }

            val notificationLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { }

            fun ensureNotifications() {
                if (Build.VERSION.SDK_INT >= 33 &&
                    ContextCompat.checkSelfPermission(
                        this, Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            when (screen) {
                Screen.HOME -> ProtectionHomeScreen(
                    onEditApps = { screen = Screen.APP_SELECTION },
                    onMirrorTest = {
                        if (cameraAllowed) screen = Screen.MIRROR
                        else cameraLauncher.launch(Manifest.permission.CAMERA)
                    }
                )

                Screen.APP_SELECTION -> AppSelectionScreen {
                    screen = if (hasUsageAccess(this)) {
                        ensureNotifications()
                        ProtectionStateStore(this).enabled = true
                        ContextCompat.startForegroundService(
                            this,
                            Intent(this, ProtectionMonitorService::class.java)
                        )
                        Screen.HOME
                    } else Screen.USAGE_ACCESS
                }

                Screen.USAGE_ACCESS -> UsageAccessScreen {
                    if (hasUsageAccess(this)) {
                        ensureNotifications()
                        ProtectionStateStore(this).enabled = true
                        ContextCompat.startForegroundService(
                            this,
                            Intent(this, ProtectionMonitorService::class.java)
                        )
                        screen = Screen.HOME
                    }
                }

                Screen.MIRROR -> {
                    if (cameraAllowed) {
                        MirrorTimerScreen()
                    } else {
                        CameraPermissionScreen {
                            cameraLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
                }
            }
        }
    }
}
