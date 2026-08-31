package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ArchivoFont
import com.example.ui.theme.BrandHeaderStyle
import com.example.ui.theme.LocalBlocoColors
import com.example.ui.viewmodel.DemoMode
import com.example.ui.viewmodel.TopSection

@Composable
fun Ruler2dp(modifier: Modifier = Modifier) {
  val colors = LocalBlocoColors.current
  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(2.dp)
      .background(colors.rulerStrong)
  )
}

@Composable
fun Ruler1dp(modifier: Modifier = Modifier) {
  val colors = LocalBlocoColors.current
  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(1.dp)
      .background(colors.rulerWeak)
  )
}

@Composable
fun VerticalRuler1dp(modifier: Modifier = Modifier) {
  val colors = LocalBlocoColors.current
  Box(
    modifier = modifier
      .width(1.dp)
      .background(colors.rulerWeak)
  )
}

@Composable
fun ModernistCheckbox(
  checked: Boolean,
  onCheckedChange: () -> Unit,
  modifier: Modifier = Modifier,
  size: Dp = 18.dp
) {
  val colors = LocalBlocoColors.current
  Box(
    modifier = modifier
      .size(size)
      .then(
        if (checked) {
          Modifier.background(colors.accent)
        } else {
          Modifier
            .border(1.5.dp, colors.text, RectangleShape)
            .background(Color.Transparent)
        }
      )
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onCheckedChange
      ),
    contentAlignment = Alignment.Center
  ) {
    if (checked) {
      Text(
        text = "✓",
        color = Color.White,
        fontSize = (size.value * 0.65).sp,
        fontWeight = FontWeight.ExtraBold,
        fontFamily = ArchivoFont,
        textAlign = TextAlign.Center
      )
    }
  }
}

@Composable
fun ModernistSwitch(
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier
) {
  val colors = LocalBlocoColors.current
  val trackColor = if (checked) colors.accent else colors.switchOffTrack
  val thumbColor = if (checked) Color.White else colors.text

  Box(
    modifier = modifier
      .size(width = 44.dp, height = 24.dp)
      .background(trackColor)
      .clickable { onCheckedChange(!checked) }
      .padding(2.dp),
    contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart
  ) {
    Box(
      modifier = Modifier
        .size(20.dp)
        .background(thumbColor)
    )
  }
}

@Composable
fun ModernistButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  isPrimary: Boolean = true,
  enabled: Boolean = true
) {
  val colors = LocalBlocoColors.current
  val bg = if (isPrimary) colors.accent else Color.Transparent
  val textColor = if (isPrimary) Color.White else colors.text
  val borderModifier = if (!isPrimary) Modifier.border(1.dp, colors.rulerStrong, RectangleShape) else Modifier

  Box(
    modifier = modifier
      .then(borderModifier)
      .background(bg)
      .clickable(enabled = enabled, onClick = onClick)
      .padding(horizontal = 14.dp, vertical = 14.dp),
    contentAlignment = Alignment.CenterStart
  ) {
    Text(
      text = text,
      color = textColor,
      fontFamily = ArchivoFont,
      fontWeight = FontWeight.ExtraBold,
      fontSize = 13.sp,
      textAlign = TextAlign.Start
    )
  }
}

@Composable
fun TopNavigationIndex(
  currentSection: TopSection,
  onSelectSection: (TopSection) -> Unit,
  onOpenSearch: () -> Unit,
  onOpenSettings: () -> Unit,
  demoMode: DemoMode,
  onSelectDemoMode: (DemoMode) -> Unit,
  statusText: String = "Google · 4 min",
  modifier: Modifier = Modifier
) {
  val colors = LocalBlocoColors.current

  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(colors.canvas)
  ) {
    // Top Bar: BLOCO brand header + Search & Settings icons
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "BLOCO",
        style = BrandHeaderStyle,
        color = colors.text
      )

      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = statusText,
          fontFamily = ArchivoFont,
          fontWeight = FontWeight.SemiBold,
          fontSize = 10.sp,
          color = colors.textTertiary,
          modifier = Modifier.padding(end = 4.dp)
        )

        // Search Button 32x32
        Box(
          modifier = Modifier
            .size(32.dp)
            .border(1.dp, colors.rulerStrong, RectangleShape)
            .clickable(onClick = onOpenSearch),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "⌕",
            fontFamily = ArchivoFont,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = colors.text
          )
        }

        // Settings Button 32x32
        Box(
          modifier = Modifier
            .size(32.dp)
            .border(1.dp, colors.rulerStrong, RectangleShape)
            .clickable(onClick = onOpenSettings),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "⚙",
            fontFamily = ArchivoFont,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = colors.text
          )
        }
      }
    }

    // Demo Mode Selector Row for quick interactive verification
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 2.dp)
        .padding(bottom = 6.dp),
      horizontalArrangement = Arrangement.spacedBy(4.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "DEMO",
        fontFamily = ArchivoFont,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 8.5.sp,
        letterSpacing = 0.14.sp,
        color = colors.textTertiary,
        modifier = Modifier.padding(end = 4.dp)
      )

      DemoChip("Normal", isSelected = demoMode == DemoMode.NORMAL) { onSelectDemoMode(DemoMode.NORMAL) }
      DemoChip("1º uso", isSelected = demoMode == DemoMode.FIRST_USE) { onSelectDemoMode(DemoMode.FIRST_USE) }
      DemoChip("Vazio", isSelected = demoMode == DemoMode.EMPTY) { onSelectDemoMode(DemoMode.EMPTY) }
      DemoChip("d150", isSelected = demoMode == DemoMode.D150_CONCLUDED) { onSelectDemoMode(DemoMode.D150_CONCLUDED) }
      DemoChip("Offline", isSelected = demoMode == DemoMode.OFFLINE) { onSelectDemoMode(DemoMode.OFFLINE) }
    }

    // 4 Top Sections Index (2px rulers above and below, 1px internal dividers)
    Ruler2dp()
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(38.dp)
    ) {
      SectionTabItem(
        label = "01 Hoje",
        isSelected = currentSection == TopSection.HOJE,
        onClick = { onSelectSection(TopSection.HOJE) },
        modifier = Modifier.weight(1f)
      )
      VerticalRuler1dp()
      SectionTabItem(
        label = "02 Mural",
        isSelected = currentSection == TopSection.MURAL,
        onClick = { onSelectSection(TopSection.MURAL) },
        modifier = Modifier.weight(1f)
      )
      VerticalRuler1dp()
      SectionTabItem(
        label = "03 Agenda",
        isSelected = currentSection == TopSection.AGENDA,
        onClick = { onSelectSection(TopSection.AGENDA) },
        modifier = Modifier.weight(1f)
      )
      VerticalRuler1dp()
      SectionTabItem(
        label = "04 Hábitos",
        isSelected = currentSection == TopSection.HABITOS,
        onClick = { onSelectSection(TopSection.HABITOS) },
        modifier = Modifier.weight(1f)
      )
    }
    Ruler2dp()
  }
}

@Composable
private fun SectionTabItem(
  label: String,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val colors = LocalBlocoColors.current
  val bg = if (isSelected) colors.accent else Color.Transparent
  val textColor = if (isSelected) Color.White else colors.textTertiary

  Box(
    modifier = modifier
      .background(bg)
      .clickable(onClick = onClick)
      .padding(horizontal = 8.dp, vertical = 11.dp),
    contentAlignment = Alignment.CenterStart
  ) {
    Text(
      text = label,
      fontFamily = ArchivoFont,
      fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
      fontSize = 11.sp,
      lineHeight = 11.sp,
      color = textColor,
      maxLines = 1
    )
  }
}

@Composable
private fun DemoChip(
  label: String,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  val colors = LocalBlocoColors.current
  Box(
    modifier = Modifier
      .border(1.dp, if (isSelected) colors.accent else colors.rulerWeak, RectangleShape)
      .background(if (isSelected) colors.accent.copy(alpha = 0.12f) else Color.Transparent)
      .clickable(onClick = onClick)
      .padding(horizontal = 6.dp, vertical = 4.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      fontFamily = ArchivoFont,
      fontWeight = FontWeight.Bold,
      fontSize = 9.sp,
      color = if (isSelected) colors.accent else colors.textSecondary
    )
  }
}
