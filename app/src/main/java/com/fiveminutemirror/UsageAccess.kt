package com.fiveminutemirror

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Process
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

fun hasUsageAccess(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = appOps.checkOpNoThrow(
        AppOpsManager.OPSTR_GET_USAGE_STATS,
        Process.myUid(),
        context.packageName
    )
    return mode == AppOpsManager.MODE_ALLOWED
}

@Composable
fun UsageAccessScreen(onRecheck: () -> Unit) {
    val context = LocalContext.current
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("사용정보 접근 허용", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        Text(
            "5분 거울은 내가 선택한 앱이 실행됐는지 확인하기 위해 Android의 사용정보 접근 권한을 사용합니다. " +
                "화면 내용, 비밀번호, 메시지는 읽지 않습니다. 앱 사용 감지는 기기 안에서만 처리합니다."
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("사용정보 접근 설정 열기")
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = onRecheck,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("권한 허용 확인")
        }
    }
}
