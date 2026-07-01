package com.focuslock.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focuslock.app.data.*
import com.focuslock.app.ui.*
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FocusTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF0A0A0F)) {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    val ctx = LocalContext.current
    val apps = remember { getInstalledApps(ctx) }
    var selected by remember { mutableStateOf(setOf("com.android.settings")) }
    var duration by remember { mutableIntStateOf(25) }
    var countdown by remember { mutableIntStateOf(0) }

    if (countdown > 0) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("$countdown", fontSize = 100.sp, color = Accent, fontWeight = FontWeight.Bold)
        }
        LaunchedEffect(countdown) {
            delay(1000)
            if (countdown == 1) {
                val intent = Intent(ctx, BlockingService::class.java).apply {
                    putExtra("blocked", selected.joinToString(","))
                    putExtra("duration", duration)
                }
                ctx.startService(intent)
            } else countdown--
        }
        return
    }

    Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(32.dp))
        Icon(Icons.Rounded.Lock, null, Modifier.size(48.dp), tint = Accent)
        Text("FocusLock", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(Modifier.height(24.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            listOf(15, 25, 45, 60).forEach { m ->
                FilterChip(
                    selected = duration == m,
                    onClick = { duration = m },
                    label = { Text("${m}м") },
                    modifier = Modifier.padding(4.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Accent,
                        selectedLabelColor = Color.Black
                    )
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        LazyColumn(Modifier.weight(1f)) {
            items(apps) { app ->
                val checked = app.packageName in selected
                Row(Modifier.fillMaxWidth().clickable {
                    selected = if (checked) selected - app.packageName else selected + app.packageName
                }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = checked, onCheckedChange = null)
                    Spacer(Modifier.width(8.dp))
                    Text(app.appName, color = Color.White, fontSize = 15.sp)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                if (isAccessibilityServiceEnabled(ctx)) countdown = 3
                else ctx.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Accent)
        ) {
            Text("ЗАБЛОКИРОВАТЬ", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
        }
    }
}
