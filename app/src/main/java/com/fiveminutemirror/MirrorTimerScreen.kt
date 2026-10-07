package com.fiveminutemirror

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Matrix
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.delay
import java.util.concurrent.Executors
import kotlin.math.max

private const val TOTAL_SECONDS = 5 * 60

@Composable
fun MirrorTimerScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var facePresent by remember { mutableStateOf(false) }
    var remainingSeconds by remember { mutableIntStateOf(TOTAL_SECONDS) }
    var completed by remember { mutableStateOf(false) }

    LaunchedEffect(facePresent, completed) {
        while (facePresent && !completed && remainingSeconds > 0) {
            delay(1000)
            remainingSeconds = max(0, remainingSeconds - 1)
            if (remainingSeconds == 0) completed = true
        }
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                createMirrorPreview(
                    context = ctx,
                    lifecycleOwner = lifecycleOwner,
                    onFacePresenceChanged = { detected -> facePresent = detected }
                )
            }
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Text(
                text = formatTime(remainingSeconds),
                color = Color.White,
                fontSize = 56.sp,
                modifier = Modifier.align(Alignment.TopCenter)
            )

            Text(
                text = when {
                    completed -> "5분이 지났습니다."
                    facePresent -> "그대로 있어도 됩니다."
                    else -> "얼굴이 화면 안에 있어야 시간이 흐릅니다."
                },
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 36.dp)
            )
        }

        if (completed) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.72f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "5분이 지났습니다.",
                        color = Color.White,
                        fontSize = 28.sp
                    )
                    Spacer(Modifier.height(20.dp))
                    Button(onClick = {
                        remainingSeconds = TOTAL_SECONDS
                        completed = false
                    }) {
                        Text("다시 시작")
                    }
                }
            }
        }
    }
}

@SuppressLint("UnsafeOptInUsageError")
@androidx.annotation.OptIn(ExperimentalGetImage::class)
private fun createMirrorPreview(
    context: Context,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    onFacePresenceChanged: (Boolean) -> Unit
): PreviewView {
    val previewView = PreviewView(context).apply {
        scaleType = PreviewView.ScaleType.FILL_CENTER
        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        scaleX = -1f
    }

    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    val executor = Executors.newSingleThreadExecutor()

    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        val detector = FaceDetection.getClient(
            FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .build()
        )

        val analysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        analysis.setAnalyzer(executor) { imageProxy ->
            val mediaImage = imageProxy.image
            if (mediaImage == null) {
                imageProxy.close()
                return@setAnalyzer
            }

            val image = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )

            detector.process(image)
                .addOnSuccessListener { faces ->
                    onFacePresenceChanged(faces.isNotEmpty())
                }
                .addOnFailureListener {
                    onFacePresenceChanged(false)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_FRONT_CAMERA,
                preview,
                analysis
            )
        } catch (_: Exception) {
            onFacePresenceChanged(false)
        }
    }, ContextCompat.getMainExecutor(context))

    return previewView
}

private fun formatTime(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

@Composable
fun CameraPermissionScreen(onRequestPermission: () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                "5분 거울을 사용하려면 카메라 권한이 필요합니다.",
                color = Color.White
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = onRequestPermission) {
                Text("카메라 권한 허용")
            }
        }
    }
}
