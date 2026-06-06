package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = AuraNeonPrimary,
    secondary = AuraNeonBlue,
    tertiary = AuraGlowCyan,
    background = AuraDarkBg,
    surface = AuraDarkSurface,
    onBackground = Color.White,
    onSurface = Color.White,
    surfaceVariant = AuraDarkSurfaceVariant
  )

private val LightColorScheme =
  lightColorScheme(
    primary = AuraLightPrimary,
    secondary = AuraLightSecondary,
    tertiary = AuraLightSecondary,
    background = AuraLightBackground,
    surface = AuraLightSurface,
    onBackground = AuraLightOnSurface,
    onSurface = AuraLightOnSurface,
    surfaceVariant = Color(0xFFF0EDFF)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disabling OS-override to protect brand identity
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
