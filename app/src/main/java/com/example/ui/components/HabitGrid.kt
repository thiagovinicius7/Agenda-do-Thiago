package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GridCellState
import com.example.data.model.HabitGridCell
import com.example.ui.theme.ArchivoFont
import com.example.ui.theme.LocalBlocoColors

enum class HabitGridMode {
  DETAIL,   // 12 cols, 27dp, 3dp gap, labels
  LIST,     // 25 cols, 9dp, 3dp gap, chromatic
  STATS,    // 18 cols, 14dp, 4dp gap, chromatic
  WIDGET,   // 14 cols, 8dp, 2dp gap, chromatic
  PREVIEW   // 25 cols, 9dp, 3dp gap
}

@Composable
fun HabitGrid(
  cells: List<HabitGridCell>,
  mode: HabitGridMode = HabitGridMode.LIST,
  onCellClick: ((HabitGridCell) -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val columns = when (mode) {
    HabitGridMode.DETAIL -> 12
    HabitGridMode.LIST -> 25
    HabitGridMode.STATS -> 18
    HabitGridMode.WIDGET -> 14
    HabitGridMode.PREVIEW -> 25
  }

  val cellSize = when (mode) {
    HabitGridMode.DETAIL -> 27.dp
    HabitGridMode.LIST -> 9.dp
    HabitGridMode.STATS -> 14.dp
    HabitGridMode.WIDGET -> 8.dp
    HabitGridMode.PREVIEW -> 9.dp
  }

  val cellGap = when (mode) {
    HabitGridMode.DETAIL -> 3.dp
    HabitGridMode.LIST -> 3.dp
    HabitGridMode.STATS -> 4.dp
    HabitGridMode.WIDGET -> 2.dp
    HabitGridMode.PREVIEW -> 3.dp
  }

  val showLabels = mode == HabitGridMode.DETAIL

  val chunkedCells = cells.chunked(columns)

  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(cellGap)
  ) {
    for (row in chunkedCells) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(cellGap, Alignment.Start)
      ) {
        for (cell in row) {
          HabitCell(
            cell = cell,
            size = cellSize,
            showLabel = showLabels,
            onClick = onCellClick?.let { { it(cell) } }
          )
        }
      }
    }
  }
}

@Composable
fun HabitCell(
  cell: HabitGridCell,
  size: Dp,
  showLabel: Boolean,
  onClick: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val colors = LocalBlocoColors.current
  val clickableModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier

  when (cell.state) {
    GridCellState.DONE -> {
      Box(
        modifier = modifier
          .size(size)
          .background(colors.accent)
          .then(clickableModifier),
        contentAlignment = Alignment.Center
      ) {
        if (showLabel) {
          Text(
            text = cell.label,
            fontFamily = ArchivoFont,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            color = Color.White,
            textAlign = TextAlign.Center
          )
        }
      }
    }

    GridCellState.FAILED -> {
      Box(
        modifier = modifier
          .size(size)
          .background(colors.gridFail)
          .then(clickableModifier),
        contentAlignment = Alignment.Center
      ) {
        if (showLabel) {
          Text(
            text = cell.label,
            fontFamily = ArchivoFont,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            color = Color.White,
            textAlign = TextAlign.Center
          )
        }
      }
    }

    GridCellState.TODAY -> {
      Box(
        modifier = modifier
          .size(size)
          .border(1.5.dp, colors.text, RectangleShape)
          .background(Color.Transparent)
          .then(clickableModifier),
        contentAlignment = Alignment.Center
      ) {
        if (showLabel) {
          Text(
            text = cell.label,
            fontFamily = ArchivoFont,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 10.sp,
            color = colors.text,
            textAlign = TextAlign.Center
          )
        }
      }
    }

    GridCellState.TODO -> {
      Box(
        modifier = modifier
          .size(size)
          .border(1.dp, colors.cellOutline, RectangleShape)
          .background(Color.Transparent)
          .then(clickableModifier),
        contentAlignment = Alignment.Center
      ) {
        if (showLabel) {
          Text(
            text = cell.label,
            fontFamily = ArchivoFont,
            fontWeight = FontWeight.Normal,
            fontSize = 10.sp,
            color = colors.textTertiary,
            textAlign = TextAlign.Center
          )
        }
      }
    }

    GridCellState.OUTSIDE_RULE -> {
      val hatchBg = if (colors.isDark) Color(0xFF201E1D) else Color(0xFFF3F2F2)
      val hatchLine = if (colors.isDark) Color(0xFF4A4746) else Color(0xFFC9C5C5)

      Canvas(
        modifier = modifier
          .size(size)
          .then(clickableModifier)
      ) {
        drawRect(color = hatchBg)
        draw45DegreeHatch(hatchLine)
      }
    }
  }
}

private fun DrawScope.draw45DegreeHatch(lineColor: Color) {
  val step = 4.dp.toPx()
  val strokeWidth = 1.2.dp.toPx()
  val width = size.width
  val height = size.height

  var x = -height
  while (x < width + height) {
    drawLine(
      color = lineColor,
      start = Offset(x, 0f),
      end = Offset(x + height, height),
      strokeWidth = strokeWidth
    )
    x += step
  }
}
