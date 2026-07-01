package com.focuslock.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.accessibilityservice.AccessibilityServiceInfo
import android.view.accessibility.AccessibilityManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.focuslock.app.data.*
import com.focuslock.app.ui.*
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FocusTheme {
                val vm: FocusViewModel = viewModel()
                val ctx = LocalContext.current
                LaunchedEffect(Unit) { vm.loadState(ctx); vm.loadApps(ctx) }
                AnimatedContent(targetState = vm.isActive, label = "screen") { active ->
                    if (active) ActiveBlockScreen(vm, ctx)
                    else MainScreen(vm, ctx)
                }
            }
        }
    }
}

@Composable
fun MainScreen(vm: FocusViewModel, ctx: Context) {
    var dur by remember { mutableIntStateOf(25) }
    var showCountdown by remember { mutableStateOf(false) }
    val filtered = remember(vm.apps, vm.searchQuery) {
        if (vm.searchQuery.isEmpty()) vm.apps
        else vm.apps.filter { it.appName.contains(vm.searchQuery, true) }
    }

    if (showCountdown) {
        CountdownOverlay { showCountdown = false; startBlocking(vm, ctx, dur) }
        return
    }

    Scaffold(modifier = Modifier.background(Black), containerColor = Black) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(32.dp))
            Icon(Icons.Rounded.Lock, null, Modifier.size(48.dp).alpha(0.9f), tint = Accent)
            Text("FocusLock", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(24.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                listOf(15, 25, 45, 60).forEach { m ->
                    val sel = dur == m
                    Button(
                        onClick = { dur = m },
                        modifier = Modifier.padding(4.dp).then(if (sel) Modifier.shadow(8.dp, CircleShape) else Modifier),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = if (sel) Accent else Color(0x20FFFFFF))
                    ) { Text("${m}м", color = if (sel) Black else TextPrimary, fontWeight = FontWeight.Bold) }
                }
            }

            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = vm.searchQuery, onValueChange = { vm.searchQuery = it },
                modifier = Modifier.fillMaxWidth().glassCard(),
                placeholder = { Text("Поиск приложений...", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Rounded.Search, null, tint = Accent) },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent),
                singleLine = true
            )

            Spacer(Modifier.height(8.dp))
            LazyColumn(Modifier.weight(1f).fillMaxWidth().glassCard().padding(8.dp)) {
                items(filtered) { app ->
                    val checked = app.packageName in vm.selectedPackages
                    Row(Modifier.fillMaxWidth().clickable { vm.toggleApp(app.packageName) }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(24.dp).clip(CircleShape).background(if (checked) Accent else Color.Transparent).then(if (!checked) Modifier.border(2.dp, TextSecondary, CircleShape) else Modifier), contentAlignment = Alignment.Center) {
                            if (checked) Icon(Icons.Rounded.Check, null, Modifier.size(16.dp), tint = Black)
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(app.appName, color = TextPrimary, fontSize = 15.sp)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    if (!isAccessibilityEnabled(ctx)) {
                        ctx.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    } else if (vm.selectedPackages.isNotEmpty()) {
                        showCountdown = true
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(28.dp)).background(Brush.linearGradient(listOf(Accent, Color(0xFF0099CC)))),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                Text("НАЧАТЬ БЛОКИРОВКУ", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Black)
            }
        }
    }
}

@Composable
fun ActiveBlockScreen(vm: FocusViewModel, ctx: Context) {
    val rem = remember { derivedStateOf { maxOf(0, vm.endTime - System.currentTimeMillis()) } }
    var display by remember { mutableLongStateOf(rem.value) }
    LaunchedEffect(Unit) { while (true) { delay(500); display = rem.value } }
    val mins = display / 60000
    val secs = (display % 60000) / 1000

    Box(Modifier.fillMaxSize().background(Black), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Rounded.Lock, null, Modifier.size(64.dp), tint = Accent)
            Spacer(Modifier.height(24.dp))
            Text("БЛОКИРОВКА АКТИВНА", color = Accent, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(Modifier.height(16.dp))
            Text(String.format("%02d:%02d", mins, secs), fontSize = 56.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(24.dp))
            Text("Заблокировано: ${vm.blockedPackages.size} приложений", color = TextSecondary)
        }
    }
}

@Composable
fun CountdownOverlay(onDone: () -> Unit) {
    var count by remember { mutableIntStateOf(3) }
    LaunchedEffect(Unit) {
        repeat(3) { delay(1000); count-- }
        delay(300)
        onDone()
    }
    Box(Modifier.fillMaxSize().background(Black), contentAlignment = Alignment.Center) {
        Text(count.toString(), fontSize = 120.sp, fontWeight = FontWeight.Bold, color = Accent)
    }
}

fun startBlocking(vm: FocusViewModel, ctx: Context, dur: Int) {
    vm.saveBlocking(ctx, dur)
    ctx.startService(Intent(ctx, BlockingService::class.java))
    ctx.startService(Intent(ctx, OverlayService::class.java))
}

fun isAccessibilityEnabled(ctx: Context): Boolean {
    val am = ctx.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    return am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC).any {
        it.resolveInfo.serviceInfo.packageName == ctx.packageName
    }
}
