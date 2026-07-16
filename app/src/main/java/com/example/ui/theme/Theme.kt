package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = NeonBlue,
    secondary = NeonCyan,
    tertiary = NeonAqua,
    background = MidnightDark,
    surface = CardSurfaceDark,
    onPrimary = MidnightDark,
    onSecondary = MidnightDark,
    onTertiary = MidnightDark,
    onBackground = TextWhite,
    onSurface = TextWhite,
    error = NeonRed
  )

private val LightColorScheme =
  lightColorScheme(
    primary = Color(0xFF00838F),
    secondary = Color(0xFF0097A7),
    tertiary = Color(0xFF0288D1),
    background = Color(0xFFF4F6F9),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1A1F26),
    onSurface = Color(0xFF1A1F26),
    error = Color(0xFFD32F2F)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Default to Dark UI as requested
  dynamicColor: Boolean = false, // Disable dynamic colors so our premium neon identity stays intact
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
