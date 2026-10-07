package com.fiveminutemirror

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat

class MirrorGateActivity : ComponentActivity() {
    companion object {
        const val EXTRA_TARGET_PACKAGE = "target_package"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val target = intent.getStringExtra(EXTRA_TARGET_PACKAGE)
        if (target.isNullOrBlank()) {
            finish()
            return
        }

        setContent {
            var cameraAllowed by remember {
                mutableStateOf(
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                )
            }
            val launcher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { cameraAllowed = it }

            if (cameraAllowed) {
                MirrorTimerScreen(
                    onUseApp = {
                        packageManager.getLaunchIntentForPackage(target)?.let {
                            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(it)
                        }
                        finish()
                    },
                    onStop = { finish() }
                )
            } else {
                CameraPermissionScreen { launcher.launch(Manifest.permission.CAMERA) }
            }
        }
    }
}
