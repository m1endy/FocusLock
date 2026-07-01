package com.focuslock.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val Black = Color(0xFF0A0A0F)
val Accent = Color(0xFF00D2FF)
val CardBg = Color(0x1AFFFFFF)
val CardBorder = Brush.linearGradient(listOf(Color(0x40FFFFFF), Color(0x1A00D2FF)))
val TextPrimary = Color(0xFFE0E0E0)
val TextSecondary = Color(0xFF909090)

private val DarkColors = darkColorScheme(primary = Accent, background = Black, surface = Black, onSurface = TextPrimary)

@Composable
fun FocusTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColors, typography = Typography(bodyLarge = TextStyle(color = TextPrimary, fontSize = 16.sp)), content = content)
}

fun Modifier.glassCard(): Modifier = this
    .clip(RoundedCornerShape(16.dp))
    .background(CardBg)
    .border(2.dp, Brush.linearGradient(listOf(Color(0x40FFFFFF), Color(0x1A00D2FF))), RoundedCornerShape(16.dp))
