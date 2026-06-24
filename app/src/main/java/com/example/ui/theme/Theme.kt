package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ElegantDarkColorScheme = darkColorScheme(
  primary = IndigoLight,
  onPrimary = Color.White,
  secondary = IndigoPrimary,
  onSecondary = Color.White,
  tertiary = AmberAccent,
  onTertiary = ElegantDarkBg,
  background = ElegantDarkBg,
  onBackground = ElegantDarkText,
  surface = ElegantDarkSurface,
  onSurface = ElegantDarkText,
  surfaceVariant = ElegantDarkSurface,
  onSurfaceVariant = ElegantDarkMuted,
  outline = ElegantDarkBorder,
  error = RoseError,
  onError = Color.White
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = ElegantDarkColorScheme,
    typography = Typography,
    content = content
  )
}

