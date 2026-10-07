package com.fiveminutemirror

import android.app.*
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*

class ProtectionMonitorService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var monitor: ProtectionMonitor
    private var lastProtected: String? = null

    override fun onCreate() {
        super.onCreate()
        monitor = ProtectionMonitor(this)
        createChannels()
        startForeground(
            1001,
            NotificationCompat.Builder(this, CHANNEL_MONITOR)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle("5분 거울 보호 중")
                .setContentText("선택한 앱의 실행을 기기에서 확인하고 있습니다.")
                .setOngoing(true)
                .build()
        )

        scope.launch {
            while (isActive) {
                val detected = monitor.protectedForegroundApp()
                if (detected != null && detected != lastProtected) {
                    showMirrorPrompt(detected)
                    lastProtected = detected
                } else if (detected == null) {
                    lastProtected = null
                }
                delay(1000)
            }
        }
    }

    private fun showMirrorPrompt(packageName: String) {
        val launch = Intent(this, MirrorGateActivity::class.java)
            .putExtra(MirrorGateActivity.EXTRA_TARGET_PACKAGE, packageName)

        val pending = PendingIntent.getActivity(
            this,
            packageName.hashCode(),
            launch,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val label = runCatching {
            packageManager.getApplicationLabel(
                packageManager.getApplicationInfo(packageName, 0)
            ).toString()
        }.getOrDefault("선택한 앱")

        val notification = NotificationCompat.Builder(this, CHANNEL_GATE)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("$label 사용 전 5분")
            .setContentText("거울 시간을 시작하려면 눌러주세요.")
            .setContentIntent(pending)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
            .notify(2000 + packageName.hashCode(), notification)
    }

    private fun createChannels() {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_MONITOR,
                "보호 기능",
                NotificationManager.IMPORTANCE_LOW
            )
        )
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_GATE,
                "5분 거울 시작",
                NotificationManager.IMPORTANCE_HIGH
            )
        )
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val CHANNEL_MONITOR = "protection_monitor"
        const val CHANNEL_GATE = "mirror_gate"
    }
}
