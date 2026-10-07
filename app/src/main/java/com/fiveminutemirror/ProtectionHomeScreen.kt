package com.fiveminutemirror

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

@Composable
fun ProtectionHomeScreen(
    onEditApps: () -> Unit,
    onMirrorTest: () -> Unit
) {
    val context = LocalContext.current
    val state = remember { ProtectionStateStore(context) }
    var enabled by remember { mutableStateOf(state.enabled) }
    val selectedCount = remember { ProtectedAppsStore(context).load().size }

    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("5분 거울", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(8.dp))
        Text("사용을 줄이고 싶은 앱 $selectedCount개")
        Spacer(Modifier.height(28.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("보호 기능", style = MaterialTheme.typography.titleMedium)
                Text(if (enabled) "선택한 앱을 확인하고 있습니다." else "현재 꺼져 있습니다.")
            }
            Switch(
                checked = enabled,
                onCheckedChange = { turnOn ->
                    enabled = turnOn
                    state.enabled = turnOn
                    if (turnOn) {
                        ContextCompat.startForegroundService(
                            context,
                            Intent(context, ProtectionMonitorService::class.java)
                        )
                    } else {
                        context.stopService(Intent(context, ProtectionMonitorService::class.java))
                        state.clearAllowance()
                    }
                }
            )
        }

        Spacer(Modifier.height(28.dp))
        Button(onClick = onEditApps, modifier = Modifier.fillMaxWidth()) {
            Text("보호할 앱 변경")
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onMirrorTest, modifier = Modifier.fillMaxWidth()) {
            Text("5분 거울 테스트")
        }
    }
}
