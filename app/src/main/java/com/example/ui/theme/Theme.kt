package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val LightMaterialColorScheme = lightColorScheme(
  primary = AccentRed,
  onPrimary = LightCanvas,
  primaryContainer = PostItWorkBgLight,
  onPrimaryContainer = AccentDarkPostIt,
  secondary = LightText,
  onSecondary = LightCanvas,
  background = LightCanvas,
  onBackground = LightText,
  surface = LightCanvas,
  onSurface = LightText,
  surfaceVariant = PostItPersonalBgLight,
  onSurfaceVariant = LightTextSecondary,
  outline = LightRulerStrong,
  outlineVariant = LightRulerWeak,
  error = AccentRed,
  onError = LightCanvas
)

private val DarkMaterialColorScheme = darkColorScheme(
  primary = AccentRed,
  onPrimary = DarkCanvas,
  primaryContainer = DarkSurfaceTinted,
  onPrimaryContainer = AccentRedLight,
  secondary = DarkText,
  onSecondary = DarkCanvas,
  background = DarkCanvas,
  onBackground = DarkText,
  surface = DarkCanvas,
  onSurface = DarkText,
  surfaceVariant = DarkSurfaceElevated,
  onSurfaceVariant = DarkTextSecondary,
  outline = DarkRulerStrong,
  outlineVariant = DarkRulerWeak,
  error = AccentRed,
  onError = DarkText
)

@Composable
fun BlocoTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit
) {
  val blocoColors = if (darkTheme) LocalBlocoColorsDark else LocalBlocoColorsLight
  val materialScheme = if (darkTheme) DarkMaterialColorScheme else LightMaterialColorScheme

  CompositionLocalProvider(LocalBlocoColors provides blocoColors) {
    MaterialTheme(
      colorScheme = materialScheme,
      typography = BlocoTypography,
      shapes = BlocoShapes,
      content = content
    )
  }
}
