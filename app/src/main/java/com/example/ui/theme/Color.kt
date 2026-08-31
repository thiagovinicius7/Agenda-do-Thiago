package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Modernist Design Tokens - Light Palette
val LightCanvas = Color(0xFFF3F2F2)
val LightText = Color(0xFF201E1D)
val LightTextSecondary = Color(0x8C201E1D) // ~55%
val LightTextTertiary = Color(0x80201E1D)  // ~50%
val LightRulerStrong = Color(0x66201E1D)  // 40% ~ 2px ruler
val LightRulerWeak = Color(0x29201E1D)    // 16% ~ 1px ruler
val LightCellOutline = Color(0x2E201E1D)  // 18%

val AccentRed = Color(0xFFEC3013)
val AccentDark = Color(0xFFAE1800)
val AccentDarkPostIt = Color(0xFF7C1405)

// Post-it Colors - Light Mode
val PostItWorkBgLight = Color(0xFFFFE0D9)
val PostItWorkTextLight = Color(0xFF7C1405)
val PostItWorkBar = Color(0xFFEC3013)

val PostItPersonalBgLight = Color(0xFFEAE7E7)
val PostItPersonalTextLight = Color(0x8C201E1D)
val PostItPersonalBar = Color(0xFF201E1D)

val PostItStudyBgLight = Color(0xFFFFF2EF)
val PostItStudyTextLight = Color(0xFF7C1405)
val PostItStudyBar = Color(0xFF9B9797)

val PostItHomeBgLight = Color(0xFFF0EBE6)
val PostItHomeTextLight = Color(0xFF5A4A42)
val PostItHomeBar = Color(0xFF7A685D)

val GridFailLight = Color(0xFF9B9797)
val TrackLight = Color(0xFFEAE7E7)
val TrackSwitchOff = Color(0xFFD7D3D3)

// Modernist Design Tokens - Dark Palette
val DarkCanvas = Color(0xFF201E1D)
val DarkSurfaceElevated = Color(0xFF2C2A29)
val DarkSurfaceTinted = Color(0xFF3A2320)
val DarkText = Color(0xFFF3F2F2)
val DarkTextSecondary = Color(0x8CF3F2F2) // ~55%
val DarkTextTertiary = Color(0x66F3F2F2)
val DarkRulerStrong = Color(0x6BF3F2F2)  // 42%
val DarkRulerWeak = Color(0x33F3F2F2)    // 20%
val DarkCellOutline = Color(0x38F3F2F2)  // 22%

val AccentRedLight = Color(0xFFFF8B74)
val AccentRedNumber = Color(0xFFFF5C3D)
val GridFailDark = Color(0xFF6B6767)

@Immutable
data class BlocoColors(
  val isDark: Boolean,
  val canvas: Color,
  val surface: Color,
  val surfaceElevated: Color,
  val text: Color,
  val textSecondary: Color,
  val textTertiary: Color,
  val rulerStrong: Color,
  val rulerWeak: Color,
  val cellOutline: Color,
  val accent: Color,
  val accentDark: Color,
  val accentLight: Color,
  val accentPostItText: Color,
  val gridFail: Color,
  val track: Color,
  val switchOffTrack: Color,
  val postItWorkBg: Color,
  val postItWorkBar: Color,
  val postItPersonalBg: Color,
  val postItPersonalBar: Color,
  val postItStudyBg: Color,
  val postItStudyBar: Color,
  val postItHomeBg: Color,
  val postItHomeBar: Color,
)

val LocalBlocoColorsLight = BlocoColors(
  isDark = false,
  canvas = LightCanvas,
  surface = Color.White,
  surfaceElevated = Color(0xFFF9F8F8),
  text = LightText,
  textSecondary = LightTextSecondary,
  textTertiary = LightTextTertiary,
  rulerStrong = LightRulerStrong,
  rulerWeak = LightRulerWeak,
  cellOutline = LightCellOutline,
  accent = AccentRed,
  accentDark = AccentDark,
  accentLight = AccentRedLight,
  accentPostItText = AccentDarkPostIt,
  gridFail = GridFailLight,
  track = TrackLight,
  switchOffTrack = TrackSwitchOff,
  postItWorkBg = PostItWorkBgLight,
  postItWorkBar = PostItWorkBar,
  postItPersonalBg = PostItPersonalBgLight,
  postItPersonalBar = PostItPersonalBar,
  postItStudyBg = PostItStudyBgLight,
  postItStudyBar = PostItStudyBar,
  postItHomeBg = PostItHomeBgLight,
  postItHomeBar = PostItHomeBar,
)

val LocalBlocoColorsDark = BlocoColors(
  isDark = true,
  canvas = DarkCanvas,
  surface = DarkSurfaceElevated,
  surfaceElevated = DarkSurfaceTinted,
  text = DarkText,
  textSecondary = DarkTextSecondary,
  textTertiary = DarkTextTertiary,
  rulerStrong = DarkRulerStrong,
  rulerWeak = DarkRulerWeak,
  cellOutline = DarkCellOutline,
  accent = AccentRed,
  accentDark = AccentRedLight,
  accentLight = AccentRedNumber,
  accentPostItText = AccentRedLight,
  gridFail = GridFailDark,
  track = Color(0xFF333130),
  switchOffTrack = Color(0xFF4A4746),
  postItWorkBg = Color(0xFF3A2320),
  postItWorkBar = AccentRed,
  postItPersonalBg = Color(0xFF2C2A29),
  postItPersonalBar = Color(0xFF9B9797),
  postItStudyBg = Color(0xFF2C2A29),
  postItStudyBar = Color(0xFF6B6767),
  postItHomeBg = Color(0xFF302824),
  postItHomeBar = Color(0xFF7A685D),
)

val LocalBlocoColors = staticCompositionLocalOf { LocalBlocoColorsLight }
