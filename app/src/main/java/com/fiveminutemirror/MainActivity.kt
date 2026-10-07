package com.fiveminutemirror

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.content.Intent
import androidx.core.content.ContextCompat as CoreContextCompat
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat

private enum class SetupStep { APP_SELECTION, USAGE_ACCESS, MIRROR }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var step by remember { mutableStateOf(SetupStep.APP_SELECTION) }
            var refreshUsageAccess by remember { mutableIntStateOf(0) }
            val usageAllowed = remember(refreshUsageAccess) { hasUsageAccess(this) }

            var cameraAllowed by remember {
                mutableStateOf(
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                )
            }

            val cameraLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { granted ->
                cameraAllowed = granted
                if (granted) step = SetupStep.MIRROR
            }

            when (step) {
                SetupStep.APP_SELECTION -> AppSelectionScreen {
                    step = if (usageAllowed) SetupStep.MIRROR else SetupStep.USAGE_ACCESS
                }

                SetupStep.USAGE_ACCESS -> UsageAccessScreen {
                    refreshUsageAccess++
                    if (hasUsageAccess(this)) {
                        CoreContextCompat.startForegroundService(
                            this,
                            Intent(this, ProtectionMonitorService::class.java)
                        )
                        if (cameraAllowed) step = SetupStep.MIRROR
                        else cameraLauncher.launch(Manifest.permission.CAMERA)
                    }
                }

                SetupStep.MIRROR -> {
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
