package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val TacticalDarkColorScheme = darkColorScheme(
  primary = SignalRed,
  onPrimary = PureWhite,
  primaryContainer = SignalRedDark,
  onPrimaryContainer = PureWhite,
  secondary = TelemetryCyan,
  onSecondary = Slate950,
  secondaryContainer = Slate850,
  onSecondaryContainer = TelemetryCyan,
  tertiary = SignalAmber,
  onTertiary = Slate950,
  tertiaryContainer = SignalAmberDark,
  onTertiaryContainer = PureWhite,
  background = Slate950,
  onBackground = Slate100,
  surface = Slate900,
  onSurface = Slate100,
  surfaceVariant = Slate850,
  onSurfaceVariant = Slate300,
  outline = Slate700,
  outlineVariant = Slate800,
  error = SignalRed,
  onError = PureWhite
)

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = TacticalDarkColorScheme,
    typography = Typography,
    content = content
  )
}
