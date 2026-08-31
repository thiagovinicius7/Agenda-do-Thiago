package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CalendarEvent
import com.example.data.model.GoogleCalendar
import com.example.data.model.SyncQueueItem
import com.example.ui.components.ModernistButton
import com.example.ui.components.Ruler1dp
import com.example.ui.components.Ruler2dp
import com.example.ui.theme.ArchivoFont
import com.example.ui.theme.LocalBlocoColors
import com.example.ui.theme.SectionLabelStyle
import com.example.ui.viewmodel.CalendarViewMode
import java.time.LocalDate

@Composable
fun AgendaScreen(
  events: List<CalendarEvent>,
  viewMode: CalendarViewMode,
  selectedDate: LocalDate,
  onSelectViewMode: (CalendarViewMode) -> Unit,
  onSelectDate: (LocalDate) -> Unit,
  onCreateEvent: () -> Unit,
  onOpenOffline: () -> Unit,
  modifier: Modifier = Modifier
) {
  val colors = LocalBlocoColors.current
  val scrollState = rememberScrollState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(colors.canvas)
      .verticalScroll(scrollState)
  ) {
    // Header
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
      when (viewMode) {
        CalendarViewMode.MES -> {
          Text(
            text = "Setembro\n2026",
            fontFamily = ArchivoFont,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 40.sp,
            lineHeight = 38.sp,
            letterSpacing = (-0.03).sp,
            color = colors.text
          )
        }
        CalendarViewMode.SEMANA -> {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
          ) {
            Text(
              text = "31 ago — 6 set",
              fontFamily = ArchivoFont,
              fontWeight = FontWeight.ExtraBold,
              fontSize = 24.sp,
              lineHeight = 24.sp,
              letterSpacing = (-0.02).sp,
              color = colors.text
            )
            Text(
              text = "Semana 36",
              fontFamily = ArchivoFont,
              fontWeight = FontWeight.SemiBold,
              fontSize = 10.sp,
              color = colors.textSecondary
            )
          }
        }
        CalendarViewMode.DIA -> {
          Text(text = "TERÇA", style = SectionLabelStyle, color = colors.textTertiary)
          Spacer(modifier = Modifier.height(4.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
          ) {
            Text(
              text = "2 de setembro",
              fontFamily = ArchivoFont,
              fontWeight = FontWeight.ExtraBold,
              fontSize = 34.sp,
              lineHeight = 34.sp,
              letterSpacing = (-0.02).sp,
              color = colors.text
            )
            Text(
              text = "3 · 1 post-it",
              fontFamily = ArchivoFont,
              fontWeight = FontWeight.SemiBold,
              fontSize = 10.sp,
              color = colors.textSecondary
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // View Mode Selector Chips (Mês / Semana / Dia / Calendários)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        ViewModeChip("Mês", isSelected = viewMode == CalendarViewMode.MES) { onSelectViewMode(CalendarViewMode.MES) }
        ViewModeChip("Semana", isSelected = viewMode == CalendarViewMode.SEMANA) { onSelectViewMode(CalendarViewMode.SEMANA) }
        ViewModeChip("Dia", isSelected = viewMode == CalendarViewMode.DIA) { onSelectViewMode(CalendarViewMode.DIA) }
        Spacer(modifier = Modifier.weight(1f))
        ViewModeChip("Calendários", isSelected = false) { onOpenOffline() }
      }
    }

    Ruler2dp()

    when (viewMode) {
      CalendarViewMode.MES -> {
        AgendaMonthView(
          events = events,
          selectedDate = selectedDate,
          onSelectDate = onSelectDate,
          onCreateEvent = onCreateEvent
        )
      }
      CalendarViewMode.SEMANA -> {
        AgendaWeekView(events = events, onCreateEvent = onCreateEvent)
      }
      CalendarViewMode.DIA -> {
        AgendaDayView(events = events, onCreateEvent = onCreateEvent)
      }
    }
  }
}

@Composable
private fun AgendaMonthView(
  events: List<CalendarEvent>,
  selectedDate: LocalDate,
  onSelectDate: (LocalDate) -> Unit,
  onCreateEvent: () -> Unit
) {
  val colors = LocalBlocoColors.current

  Column(modifier = Modifier.fillMaxWidth()) {
    // Days of week header (D S T Q Q S S)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      val days = listOf("D", "S", "T", "Q", "Q", "S", "S")
      for (day in days) {
        Text(
          text = day,
          fontFamily = ArchivoFont,
          fontWeight = FontWeight.ExtraBold,
          fontSize = 9.sp,
          color = colors.textTertiary,
          textAlign = androidx.compose.ui.text.style.TextAlign.Center,
          modifier = Modifier.weight(1f)
        )
      }
    }

    // Month Grid (September 2026 has 30 days, 1st is Tuesday)
    val daysData = listOf(
      Pair(0, ""), Pair(1, "k"), Pair(2, "rk"), Pair(3, "k"), Pair(4, "r"), Pair(5, "kk"), Pair(6, ""),
      Pair(7, "g"), Pair(8, "k"), Pair(9, "r"), Pair(10, "kg"), Pair(11, ""), Pair(12, "rk"), Pair(13, "g"),
      Pair(14, ""), Pair(15, "kk"), Pair(16, "r"), Pair(17, "k"), Pair(18, "g"), Pair(19, "rk"), Pair(20, ""),
      Pair(21, ""), Pair(22, "k"), Pair(23, "rg"), Pair(24, "k"), Pair(25, ""), Pair(26, "kr"), Pair(27, "g"),
      Pair(28, ""), Pair(29, "k"), Pair(30, "r"), Pair(0, ""), Pair(0, ""), Pair(0, ""), Pair(0, "")
    )

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
      val rows = daysData.chunked(7)
      for (row in rows) {
        Row(modifier = Modifier.fillMaxWidth()) {
          for ((dayNum, dots) in row) {
            val isSelected = (dayNum == 2)
            Box(
              modifier = Modifier
                .weight(1f)
                .height(46.dp)
                .border(width = 0.5.dp, color = colors.rulerWeak)
                .background(if (isSelected) colors.text else Color.Transparent)
                .clickable { if (dayNum > 0) onSelectDate(LocalDate.of(2026, 9, dayNum)) }
                .padding(top = 5.dp),
              contentAlignment = Alignment.TopCenter
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                  text = if (dayNum > 0) dayNum.toString() else "",
                  fontFamily = ArchivoFont,
                  fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                  fontSize = 12.sp,
                  color = if (isSelected) colors.canvas else colors.text
                )
                Spacer(modifier = Modifier.height(3.dp))
                // Dots for events (r = red, k = black, g = gray)
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                  for (c in dots) {
                    val dotColor = when (c) {
                      'r' -> colors.accent
                      'k' -> if (isSelected) Color.White else colors.text
                      else -> colors.gridFail
                    }
                    Box(modifier = Modifier.size(5.dp).background(dotColor))
                  }
                }
              }
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))
    Ruler2dp()

    // Selected Day Agenda details
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
      ) {
        Text(text = "Terça, 2", fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = colors.text)
        Text(text = "3 compromissos · 1 post-it", fontFamily = ArchivoFont, fontWeight = FontWeight.SemiBold, fontSize = 10.sp, color = colors.textTertiary)
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Event 1
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(text = "08:30", fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = colors.text, modifier = Modifier.width(44.dp))
        Text(text = "Planejamento semanal", fontFamily = ArchivoFont, fontWeight = FontWeight.Normal, fontSize = 13.sp, color = colors.text)
      }
      Ruler1dp()

      // Event 2 (with post-it embed)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 10.dp),
        verticalAlignment = Alignment.Top
      ) {
        Text(text = "14:00", fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = colors.accentDark, modifier = Modifier.width(44.dp))
        Column {
          Text(text = "Prova — Cap. 4", fontFamily = ArchivoFont, fontWeight = FontWeight.Normal, fontSize = 13.sp, color = colors.text)
          Spacer(modifier = Modifier.height(6.dp))
          Row(
            modifier = Modifier
              .background(colors.postItStudyBg)
              .padding(horizontal = 9.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(modifier = Modifier.size(9.dp).background(colors.accent))
            Spacer(modifier = Modifier.width(7.dp))
            Text(text = "Post-it: “Cap. 4 — anotações”", fontFamily = ArchivoFont, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = colors.accentPostItText)
          }
        }
      }
      Ruler1dp()

      // Event 3 (Habit)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(text = "06:30", fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = colors.text, modifier = Modifier.width(44.dp))
        Text(text = "Corrida · hábito d65", fontFamily = ArchivoFont, fontWeight = FontWeight.Normal, fontSize = 13.sp, color = colors.textSecondary)
      }
    }

    // Action buttons
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
      ModernistButton(
        text = "Novo compromisso",
        onClick = onCreateEvent,
        modifier = Modifier.weight(1f)
      )
      Box(
        modifier = Modifier
          .border(1.dp, colors.rulerStrong, RectangleShape)
          .clickable(onClick = onCreateEvent)
          .padding(14.dp),
        contentAlignment = Alignment.Center
      ) {
        Text(text = "Anexar post-it", fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = colors.text)
      }
    }

    Spacer(modifier = Modifier.height(40.dp))
  }
}

@Composable
private fun AgendaWeekView(events: List<CalendarEvent>, onCreateEvent: () -> Unit) {
  val colors = LocalBlocoColors.current

  Column(modifier = Modifier.fillMaxWidth()) {
    // 7 Days Header
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .border(width = 1.dp, color = colors.rulerWeak)
    ) {
      Box(modifier = Modifier.width(32.dp))
      val weekDays = listOf(
        Pair("SEG", "31"), Pair("TER", "1"), Pair("QUA", "2"), Pair("QUI", "3"),
        Pair("SEX", "4"), Pair("SÁB", "5"), Pair("DOM", "6")
      )
      for ((dayStr, numStr) in weekDays) {
        val isToday = (dayStr == "TER")
        Column(
          modifier = Modifier
            .weight(1f)
            .background(if (isToday) colors.text else Color.Transparent)
            .padding(vertical = 7.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(text = dayStr, fontFamily = ArchivoFont, fontWeight = FontWeight.SemiBold, fontSize = 8.5.sp, color = if (isToday) colors.canvas.copy(alpha = 0.7f) else colors.textTertiary)
          Spacer(modifier = Modifier.height(4.dp))
          Text(text = numStr, fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = if (isToday) colors.canvas else colors.text)
        }
      }
    }

    // Habit strip
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .border(width = 1.dp, color = colors.rulerWeak)
        .padding(vertical = 4.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "HÁB",
        style = SectionLabelStyle,
        color = colors.textTertiary,
        modifier = Modifier.width(32.dp).padding(start = 4.dp)
      )
      for (i in 0 until 7) {
        Column(
          modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
          verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
          if (i == 6) {
            // Sunday hatch
            Box(modifier = Modifier.fillMaxWidth().height(17.dp).background(colors.gridFail.copy(alpha = 0.3f)))
          } else if (i < 2) {
            Box(modifier = Modifier.fillMaxWidth().height(7.dp).background(colors.accent))
            Box(modifier = Modifier.fillMaxWidth().height(7.dp).background(if (i == 0) colors.accent else colors.text))
          } else {
            Box(modifier = Modifier.fillMaxWidth().height(7.dp).border(1.dp, colors.cellOutline, RectangleShape))
            Box(modifier = Modifier.fillMaxWidth().height(7.dp).border(1.dp, colors.cellOutline, RectangleShape))
          }
        }
      }
    }

    // Hour slots 07..20
    val hours = listOf("07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20")
    for (hour in hours) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .height(40.dp)
          .border(width = 0.5.dp, color = colors.rulerWeak)
      ) {
        Text(text = hour, fontFamily = ArchivoFont, fontWeight = FontWeight.SemiBold, fontSize = 9.sp, color = colors.textTertiary, modifier = Modifier.width(32.dp).padding(start = 4.dp, top = 4.dp))
        for (col in 1..7) {
          Box(
            modifier = Modifier
              .weight(1f)
              .fillMaxSize()
              .border(0.5.dp, colors.rulerWeak, RectangleShape)
              .padding(2.dp)
          ) {
            if (hour == "09" && col == 2) {
              Box(modifier = Modifier.fillMaxSize().background(colors.text).padding(3.dp)) {
                Text(text = "Daily", fontFamily = ArchivoFont, fontWeight = FontWeight.Bold, fontSize = 8.5.sp, color = Color.White)
              }
            } else if (hour == "14" && col == 3) {
              Box(modifier = Modifier.fillMaxSize().background(colors.accent).padding(3.dp)) {
                Text(text = "Prova", fontFamily = ArchivoFont, fontWeight = FontWeight.Bold, fontSize = 8.5.sp, color = Color.White)
              }
            } else if (hour == "15" && col == 5) {
              Box(modifier = Modifier.fillMaxSize().background(colors.text).padding(3.dp)) {
                Text(text = "Cliente", fontFamily = ArchivoFont, fontWeight = FontWeight.Bold, fontSize = 8.5.sp, color = Color.White)
              }
            }
          }
        }
      }
    }

    // Legend
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      horizontalArrangement = Arrangement.spacedBy(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(9.dp).background(colors.accent))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = "Pessoal", fontFamily = ArchivoFont, fontWeight = FontWeight.SemiBold, fontSize = 9.5.sp)
      }
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(9.dp).background(colors.text))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = "Trabalho", fontFamily = ArchivoFont, fontWeight = FontWeight.SemiBold, fontSize = 9.5.sp)
      }
      Spacer(modifier = Modifier.weight(1f))
      Text(text = "Google · 4 min", fontFamily = ArchivoFont, fontWeight = FontWeight.SemiBold, fontSize = 9.5.sp, color = colors.textTertiary)
    }

    Spacer(modifier = Modifier.height(40.dp))
  }
}

@Composable
private fun AgendaDayView(events: List<CalendarEvent>, onCreateEvent: () -> Unit) {
  val colors = LocalBlocoColors.current

  Column(modifier = Modifier.fillMaxWidth()) {
    val dayRows = listOf(
      Pair("06", listOf(Pair("Corrida · hábito d65", true))),
      Pair("07", emptyList()),
      Pair("08", listOf(Pair("Planejamento semanal (08:30 — 09:00)", false))),
      Pair("09", emptyList()),
      Pair("10", emptyList()),
      Pair("11", emptyList()),
      Pair("12", emptyList()),
      Pair("13", emptyList()),
      Pair("14", listOf(Pair("Prova — Cap. 4 (14:00 — 15:00)", false))),
      Pair("15", emptyList()),
      Pair("16", emptyList()),
      Pair("17", emptyList()),
      Pair("18", emptyList()),
      Pair("19", listOf(Pair("Aula de inglês (19:30 — 20:30)", false))),
      Pair("20", emptyList())
    )

    for ((hour, items) in dayRows) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clickable(onClick = onCreateEvent)
          .padding(vertical = 6.dp)
          .border(0.5.dp, colors.rulerWeak, RectangleShape)
          .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.Top
      ) {
        Text(
          text = hour,
          fontFamily = ArchivoFont,
          fontWeight = FontWeight.Bold,
          fontSize = 11.sp,
          color = colors.textTertiary,
          modifier = Modifier.width(36.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
          if (items.isEmpty()) {
            Text(
              text = "+ toque para adicionar",
              fontFamily = ArchivoFont,
              fontWeight = FontWeight.Normal,
              fontSize = 11.sp,
              color = colors.textTertiary.copy(alpha = 0.5f)
            )
          }
          for ((itemTitle, isHabit) in items) {
            if (isHabit) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(14.dp).border(1.5.dp, colors.text, RectangleShape))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = itemTitle, fontFamily = ArchivoFont, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = colors.textSecondary)
              }
            } else {
              Row(verticalAlignment = Alignment.Top) {
                Box(modifier = Modifier.width(4.dp).height(32.dp).background(colors.accent))
                Spacer(modifier = Modifier.width(9.dp))
                Column {
                  Text(text = itemTitle, fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 12.5.sp, color = colors.text)
                  if (itemTitle.contains("Prova")) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Post-it: Cap. 4 — anotações", fontFamily = ArchivoFont, fontWeight = FontWeight.SemiBold, fontSize = 10.5.sp, color = colors.accentDark)
                  }
                }
              }
            }
          }
        }
      }
    }

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
      ModernistButton(text = "Novo compromisso", onClick = onCreateEvent, modifier = Modifier.weight(1f))
      Box(
        modifier = Modifier
          .border(1.dp, colors.rulerStrong, RectangleShape)
          .clickable(onClick = onCreateEvent)
          .padding(14.dp),
        contentAlignment = Alignment.Center
      ) {
        Text(text = "Anexar post-it", fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = colors.text)
      }
    }
    Spacer(modifier = Modifier.height(40.dp))
  }
}

@Composable
fun EventCreateScreen(
  onBack: () -> Unit,
  onSave: (title: String, calendarId: String, date: LocalDate, hour: Int, minute: Int, duration: Int, noteId: String?, noteTitle: String?, localOnly: Boolean) -> Unit,
  modifier: Modifier = Modifier
) {
  val colors = LocalBlocoColors.current
  val scrollState = rememberScrollState()

  var title by remember { mutableStateOf("Novo compromisso") }
  var selectedCalendar by remember { mutableStateOf("cal_pessoal") }
  var selectedDate by remember { mutableStateOf(LocalDate.of(2026, 9, 2)) }
  var isAllDay by remember { mutableStateOf(false) }
  var isRepeating by remember { mutableStateOf(false) }
  var hasLocation by remember { mutableStateOf(false) }
  var attachedPostIt by remember { mutableStateOf<String?>("Cap. 4 — anotações") }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(colors.canvas)
  ) {
    // Top Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 14.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(text = "Novo compromisso", fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = colors.text)
      Text(text = "Cancelar", fontFamily = ArchivoFont, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = colors.textSecondary, modifier = Modifier.clickable(onClick = onBack))
    }

    Ruler2dp()

    Column(
      modifier = Modifier
        .weight(1f)
        .verticalScroll(scrollState)
        .padding(16.dp)
    ) {
      Text(text = "TÍTULO", style = SectionLabelStyle, color = colors.textTertiary)
      Spacer(modifier = Modifier.height(6.dp))
      BasicTextField(
        value = title,
        onValueChange = { title = it },
        textStyle = TextStyle(fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = colors.text),
        cursorBrush = SolidColor(colors.accent),
        modifier = Modifier.fillMaxWidth()
      )
      Spacer(modifier = Modifier.height(6.dp))
      Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(colors.text))

      Spacer(modifier = Modifier.height(22.dp))

      // Date and Time 2-Column Matrix
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(colors.rulerStrong)
          .border(1.dp, colors.rulerStrong, RectangleShape),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
      ) {
        Column(modifier = Modifier.weight(1f).background(colors.canvas).padding(12.dp)) {
          Text(text = "DATA", style = SectionLabelStyle, color = colors.textTertiary)
          Spacer(modifier = Modifier.height(7.dp))
          Text(text = "2 set 2026", fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = colors.text)
        }
        Column(modifier = Modifier.weight(1f).background(colors.canvas).padding(12.dp)) {
          Text(text = "HORÁRIO", style = SectionLabelStyle, color = colors.textTertiary)
          Spacer(modifier = Modifier.height(7.dp))
          Text(text = if (isAllDay) "Dia inteiro" else "14:00 — 15:00", fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = colors.text)
        }
      }

      Spacer(modifier = Modifier.height(12.dp))
      Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        ViewModeChip("Dia inteiro", isSelected = isAllDay) { isAllDay = !isAllDay }
        ViewModeChip("Repetir", isSelected = isRepeating) { isRepeating = !isRepeating }
        ViewModeChip("Local", isSelected = hasLocation) { hasLocation = !hasLocation }
      }

      Spacer(modifier = Modifier.height(22.dp))

      // Salvar em qual calendário
      Text(text = "SALVAR EM QUAL CALENDÁRIO", style = SectionLabelStyle, color = colors.textTertiary)
      Spacer(modifier = Modifier.height(10.dp))
      Column(modifier = Modifier.fillMaxWidth().border(1.dp, colors.rulerStrong, RectangleShape)) {
        CalendarOptionRow(
          name = "Pessoal",
          email = "thiagovinicius7@gmail.com",
          color = colors.accent,
          isSelected = selectedCalendar == "cal_pessoal",
          onClick = { selectedCalendar = "cal_pessoal" }
        )
        Ruler1dp()
        CalendarOptionRow(
          name = "Trabalho",
          email = "thiago@empresa.com",
          color = colors.text,
          isSelected = selectedCalendar == "cal_trabalho",
          onClick = { selectedCalendar = "cal_trabalho" }
        )
        Ruler1dp()
        CalendarOptionRow(
          name = "Faculdade",
          email = "Somente leitura",
          color = colors.gridFail,
          isSelected = false,
          isBlocked = true,
          onClick = {}
        )
      }

      Spacer(modifier = Modifier.height(22.dp))

      // Post-it anexado
      Text(text = "POST-IT ANEXADO", style = SectionLabelStyle, color = colors.textTertiary)
      Spacer(modifier = Modifier.height(10.dp))
      if (attachedPostIt != null) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(colors.postItStudyBg)
            .padding(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(modifier = Modifier.size(34.dp).background(colors.postItWorkBg))
          Spacer(modifier = Modifier.width(11.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(text = attachedPostIt ?: "", fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 12.5.sp, color = colors.text)
            Text(text = "Estudo · checklist 3 itens", fontFamily = ArchivoFont, fontWeight = FontWeight.Normal, fontSize = 10.5.sp, color = colors.textSecondary)
          }
          Text(
            text = "Remover",
            fontFamily = ArchivoFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            color = colors.accentDark,
            modifier = Modifier.clickable { attachedPostIt = null }
          )
        }
      } else {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colors.rulerWeak, RectangleShape)
            .clickable { attachedPostIt = "Cap. 4 — anotações" }
            .padding(14.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(text = "+ Anexar post-it do mural", fontFamily = ArchivoFont, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = colors.accent)
        }
      }

      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = "O post-it fica visível no dia e no evento. Removê-lo aqui não apaga a nota do mural.",
        fontFamily = ArchivoFont,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        color = colors.textSecondary
      )
    }

    Ruler2dp()
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
      ModernistButton(
        text = "Salvar no Google",
        onClick = {
          onSave(title, selectedCalendar, selectedDate, 14, 0, 60, if (attachedPostIt != null) "n_estudo_1" else null, attachedPostIt, false)
        },
        modifier = Modifier.weight(1f)
      )
      Box(
        modifier = Modifier
          .border(1.dp, colors.rulerStrong, RectangleShape)
          .clickable {
            onSave(title, selectedCalendar, selectedDate, 14, 0, 60, if (attachedPostIt != null) "n_estudo_1" else null, attachedPostIt, true)
          }
          .padding(14.dp),
        contentAlignment = Alignment.Center
      ) {
        Text(text = "Só no app", fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = colors.text)
      }
    }
  }
}

@Composable
fun OfflineScreen(
  syncQueue: List<SyncQueueItem>,
  onRetrySync: () -> Unit,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val colors = LocalBlocoColors.current
  val scrollState = rememberScrollState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(colors.canvas)
      .verticalScroll(scrollState)
  ) {
    // Top Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 14.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = "← Voltar",
          fontFamily = ArchivoFont,
          fontWeight = FontWeight.SemiBold,
          fontSize = 11.sp,
          color = colors.textSecondary,
          modifier = Modifier.clickable(onClick = onBack)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
          text = "MODO OFFLINE",
          fontFamily = ArchivoFont,
          fontWeight = FontWeight.ExtraBold,
          fontSize = 12.sp,
          color = colors.text
        )
      }
      Text(
        text = "Desconectado",
        fontFamily = ArchivoFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 10.sp,
        color = colors.accentDark
      )
    }

    Ruler2dp()

    // Offline Banner
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(colors.postItWorkBg)
        .padding(14.dp, 16.dp)
    ) {
      Row(verticalAlignment = Alignment.Top) {
        Box(modifier = Modifier.size(10.dp).background(colors.accent).padding(top = 4.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Text(text = "Sem conexão com o Google", fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = colors.text)
          Spacer(modifier = Modifier.height(4.dp))
          Text(text = "Mostrando a agenda como estava às 08:12. Post-its e hábitos continuam funcionando normalmente.", fontFamily = ArchivoFont, fontWeight = FontWeight.Normal, fontSize = 11.5.sp, color = colors.textSecondary)
        }
      }
    }

    Ruler2dp()

    // Cached Schedule at 62% opacity
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Text(text = "Sexta, 30", fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 30.sp, color = colors.text)
      Text(text = "CÓPIA LOCAL · 3 COMPROMISSOS", style = SectionLabelStyle, color = colors.textTertiary)
      Spacer(modifier = Modifier.height(10.dp))

      Column(modifier = Modifier.alpha(0.62f)) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
          Text(text = "09:00", fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, modifier = Modifier.width(44.dp))
          Text(text = "Daily do time", fontFamily = ArchivoFont, fontSize = 13.sp)
        }
        Ruler1dp()
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
          Text(text = "15:00", fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, modifier = Modifier.width(44.dp))
          Text(text = "Reunião cliente", fontFamily = ArchivoFont, fontSize = 13.sp)
        }
        Ruler1dp()
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
          Text(text = "19:30", fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, modifier = Modifier.width(44.dp))
          Text(text = "Aula de inglês", fontFamily = ArchivoFont, fontSize = 13.sp)
        }
      }
    }

    Ruler2dp()

    // Sync queue
    Column(modifier = Modifier.padding(16.dp)) {
      Text(text = "ESPERANDO SINCRONIZAR · 2", style = SectionLabelStyle, color = colors.textTertiary)
      Spacer(modifier = Modifier.height(10.dp))

      // Queue Item 1
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, colors.rulerStrong, RectangleShape)
          .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(modifier = Modifier.width(4.dp).height(32.dp).background(colors.accent))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(text = "Prova — Cap. 4", fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 12.5.sp, color = colors.text)
          Text(text = "2 set · 14:00 · criado offline", fontFamily = ArchivoFont, fontWeight = FontWeight.Normal, fontSize = 10.5.sp, color = colors.textSecondary)
        }
        Text(text = "na fila", fontFamily = ArchivoFont, fontWeight = FontWeight.SemiBold, fontSize = 10.sp, color = colors.textTertiary)
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Queue Item 2
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, colors.rulerStrong, RectangleShape)
          .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(modifier = Modifier.width(4.dp).height(32.dp).background(colors.text))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(text = "Daily do time", fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 12.5.sp, color = colors.text)
          Text(text = "Movido para 09:30", fontFamily = ArchivoFont, fontWeight = FontWeight.Normal, fontSize = 10.5.sp, color = colors.textSecondary)
        }
        Text(text = "na fila", fontFamily = ArchivoFont, fontWeight = FontWeight.SemiBold, fontSize = 10.sp, color = colors.textTertiary)
      }

      Spacer(modifier = Modifier.height(12.dp))
      Text(
        text = "Ao voltar a conexão, a fila sobe sozinha. Nada é apagado.",
        fontFamily = ArchivoFont,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        color = colors.textSecondary
      )
    }

    Spacer(modifier = Modifier.height(12.dp))
    Ruler2dp()
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
      ModernistButton(text = "Tentar sincronizar", onClick = onRetrySync, modifier = Modifier.weight(1f))
      Box(
        modifier = Modifier
          .border(1.dp, colors.rulerStrong, RectangleShape)
          .clickable(onClick = onBack)
          .padding(14.dp),
        contentAlignment = Alignment.Center
      ) {
        Text(text = "Voltar", fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = colors.text)
      }
    }
  }
}

@Composable
private fun CalendarOptionRow(
  name: String,
  email: String,
  color: Color,
  isSelected: Boolean,
  isBlocked: Boolean = false,
  onClick: () -> Unit
) {
  val colors = LocalBlocoColors.current
  val bg = if (isSelected) colors.postItStudyBg else Color.Transparent

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(bg)
      .clickable(enabled = !isBlocked, onClick = onClick)
      .padding(horizontal = 12.dp, vertical = 13.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(modifier = Modifier.size(14.dp).background(color))
    Spacer(modifier = Modifier.width(11.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(text = name, fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 12.5.sp, color = colors.text)
      Text(text = email, fontFamily = ArchivoFont, fontWeight = FontWeight.Normal, fontSize = 10.5.sp, color = colors.textSecondary)
    }
    if (isSelected) {
      Text(text = "✓", fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = colors.accentDark)
    } else if (isBlocked) {
      Text(text = "bloqueado", fontFamily = ArchivoFont, fontWeight = FontWeight.SemiBold, fontSize = 10.sp, color = colors.textTertiary)
    }
  }
}

@Composable
private fun ViewModeChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
  val colors = LocalBlocoColors.current
  val bg = if (isSelected) colors.text else Color.Transparent
  val textColor = if (isSelected) colors.canvas else colors.text
  val borderModifier = if (!isSelected) Modifier.border(1.dp, colors.rulerStrong, RectangleShape) else Modifier

  Box(
    modifier = Modifier
      .then(borderModifier)
      .background(bg)
      .clickable(onClick = onClick)
      .padding(horizontal = 11.dp, vertical = 8.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(text = label, fontFamily = ArchivoFont, fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold, fontSize = 10.sp, color = textColor)
  }
}
