package com.fiveminutemirror

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var showMirror by remember { mutableStateOf(false) }
            var hasPermission by remember {
                mutableStateOf(
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                )
            }

            val launcher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { granted ->
                hasPermission = granted
                if (granted) showMirror = true
            }

            if (!showMirror) {
                AppSelectionScreen(
                    onStartMirror = {
                        if (hasPermission) showMirror = true
                        else launcher.launch(Manifest.permission.CAMERA)
                    }
                )
            } else if (hasPermission) {
                MirrorTimerScreen()
            } else {
                CameraPermissionScreen {
                    launcher.launch(Manifest.permission.CAMERA)
                }
            }
        }
    }
}
