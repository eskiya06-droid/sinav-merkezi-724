package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.graphics.Color

private val CustomLightColorScheme =
  lightColorScheme(
    primary = BrightCyan,
    secondary = DeepViolet,
    tertiary = NeonPurple,
    background = MidnightDark,
    surface = SlateBlueCard,
    onPrimary = Color.White,
    onSecondary = TextLight,
    onBackground = TextLight,
    onSurface = TextLight,
    outline = DarkBorder
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Disable dynamic colors to preserve branded Polish theme
  content: @Composable () -> Unit,
) {
  val colorScheme = CustomLightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
