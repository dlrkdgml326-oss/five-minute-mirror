package com.fiveminutemirror

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawable.toBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun AppSelectionScreen(onStartMirror: () -> Unit) {
    val context = LocalContext.current
    val store = remember { ProtectedAppsStore(context) }
    var selected by remember { mutableStateOf(store.load()) }
    var apps by remember { mutableStateOf<List<AppInfo>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        apps = withContext(Dispatchers.IO) { loadLaunchableApps(context) }
        loading = false
    }

    Scaffold(
        topBar = {
            Column(Modifier.padding(20.dp)) {
                Text("사용을 줄이고 싶은 앱", style = MaterialTheme.typography.headlineSmall)
                Text(
                    "선택한 앱을 열기 전 5분 동안 나와 머뭅니다.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        bottomBar = {
            Button(
                onClick = {
                    store.save(selected)
                    onStartMirror()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("선택 저장 · 거울 테스트")
            }
        }
    ) { padding ->
        if (loading) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                items(apps, key = { it.packageName }) { app ->
                    val checked = app.packageName in selected
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selected = if (checked) selected - app.packageName
                                else selected + app.packageName
                            }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            bitmap = app.icon.toBitmap(96, 96).asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(Modifier.width(14.dp))
                        Text(app.label, modifier = Modifier.weight(1f))
                        Checkbox(
                            checked = checked,
                            onCheckedChange = {
                                selected = if (checked) selected - app.packageName
                                else selected + app.packageName
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun loadLaunchableApps(context: Context): List<AppInfo> {
    val pm = context.packageManager
    val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)

    return pm.queryIntentActivities(launcherIntent, 0)
        .asSequence()
        .filter { it.activityInfo.packageName != context.packageName }
        .map {
            AppInfo(
                label = it.loadLabel(pm).toString(),
                packageName = it.activityInfo.packageName,
                icon = it.loadIcon(pm)
            )
        }
        .distinctBy { it.packageName }
        .sortedBy { it.label.lowercase() }
        .toList()
}
