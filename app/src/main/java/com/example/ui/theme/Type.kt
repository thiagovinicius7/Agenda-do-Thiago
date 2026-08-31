package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Archivo-compatible sans-serif styling with strict Modernist weights (400 to 900)
val ArchivoFont = FontFamily.SansSerif

val BlocoTypography = Typography(
  displayLarge = TextStyle(
    fontFamily = ArchivoFont,
    fontWeight = FontWeight.ExtraBold, // 800
    fontSize = 38.sp,
    lineHeight = 38.sp,
    letterSpacing = (-0.03).sp
  ),
  displayMedium = TextStyle(
    fontFamily = ArchivoFont,
    fontWeight = FontWeight.ExtraBold, // 800
    fontSize = 34.sp,
    lineHeight = 34.sp,
    letterSpacing = (-0.02).sp
  ),
  headlineLarge = TextStyle(
    fontFamily = ArchivoFont,
    fontWeight = FontWeight.ExtraBold, // 800
    fontSize = 28.sp,
    lineHeight = 30.sp,
    letterSpacing = (-0.02).sp
  ),
  headlineMedium = TextStyle(
    fontFamily = ArchivoFont,
    fontWeight = FontWeight.ExtraBold, // 800
    fontSize = 22.sp,
    lineHeight = 24.sp,
    letterSpacing = (-0.02).sp
  ),
  headlineSmall = TextStyle(
    fontFamily = ArchivoFont,
    fontWeight = FontWeight.ExtraBold, // 800
    fontSize = 18.sp,
    lineHeight = 20.sp,
    letterSpacing = (-0.01).sp
  ),
  titleLarge = TextStyle(
    fontFamily = ArchivoFont,
    fontWeight = FontWeight.Bold, // 700
    fontSize = 16.sp,
    lineHeight = 20.sp
  ),
  titleMedium = TextStyle(
    fontFamily = ArchivoFont,
    fontWeight = FontWeight.ExtraBold, // 800
    fontSize = 15.sp,
    lineHeight = 18.sp
  ),
  titleSmall = TextStyle(
    fontFamily = ArchivoFont,
    fontWeight = FontWeight.Bold,
    fontSize = 13.sp,
    lineHeight = 16.sp
  ),
  bodyLarge = TextStyle(
    fontFamily = ArchivoFont,
    fontWeight = FontWeight.Normal, // 400
    fontSize = 14.sp,
    lineHeight = 20.sp
  ),
  bodyMedium = TextStyle(
    fontFamily = ArchivoFont,
    fontWeight = FontWeight.Normal, // 400
    fontSize = 13.sp,
    lineHeight = 18.sp
  ),
  bodySmall = TextStyle(
    fontFamily = ArchivoFont,
    fontWeight = FontWeight.Normal, // 400
    fontSize = 12.sp,
    lineHeight = 16.sp
  ),
  labelLarge = TextStyle(
    fontFamily = ArchivoFont,
    fontWeight = FontWeight.ExtraBold, // 800
    fontSize = 11.sp,
    lineHeight = 14.sp
  ),
  labelMedium = TextStyle(
    fontFamily = ArchivoFont,
    fontWeight = FontWeight.SemiBold, // 600
    fontSize = 10.sp,
    lineHeight = 13.sp
  ),
  labelSmall = TextStyle(
    fontFamily = ArchivoFont,
    fontWeight = FontWeight.ExtraBold, // 800
    fontSize = 9.sp,
    lineHeight = 11.sp,
    letterSpacing = 0.16.sp
  )
)

// Specialized TextStyle definitions
val BrandHeaderStyle = TextStyle(
  fontFamily = ArchivoFont,
  fontWeight = FontWeight.Black, // 900
  fontSize = 12.sp,
  lineHeight = 12.sp,
  letterSpacing = 0.2.sp
)

val SectionLabelStyle = TextStyle(
  fontFamily = ArchivoFont,
  fontWeight = FontWeight.ExtraBold, // 800
  fontSize = 9.sp,
  lineHeight = 10.sp,
  letterSpacing = 0.16.sp
)

val CategoryLabelStyle = TextStyle(
  fontFamily = ArchivoFont,
  fontWeight = FontWeight.ExtraBold, // 800
  fontSize = 9.sp,
  lineHeight = 10.sp,
  letterSpacing = 0.14.sp
)

val BigStatStyle = TextStyle(
  fontFamily = ArchivoFont,
  fontWeight = FontWeight.ExtraBold, // 800
  fontSize = 26.sp,
  lineHeight = 26.sp,
  letterSpacing = (-0.02).sp
)
