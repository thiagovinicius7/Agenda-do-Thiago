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
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

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
      val isCurrentDayToday = (selectedDate == LocalDate.now())

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          when (viewMode) {
            CalendarViewMode.MES -> {
              val monthName = selectedDate.format(DateTimeFormatter.ofPattern("MMMM", Locale("pt", "BR"))).replaceFirstChar { it.uppercase() }
              val yearStr = selectedDate.year.toString()
              Text(
                text = "$monthName\n$yearStr",
                fontFamily = ArchivoFont,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 38.sp,
                lineHeight = 36.sp,
                letterSpacing = (-0.03).sp,
                color = colors.text
              )
            }
            CalendarViewMode.SEMANA -> {
              val dayOfWeek = selectedDate.dayOfWeek.value // 1 (Mon) to 7 (Sun)
              val monday = selectedDate.minusDays((dayOfWeek - 1).toLong())
              val sunday = monday.plusDays(6)
              val weekFormatter = DateTimeFormatter.ofPattern("d MMM", Locale("pt", "BR"))
              val weekRangeStr = "${monday.format(weekFormatter)} — ${sunday.format(weekFormatter)}"
              val weekNumber = selectedDate.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR)

              Text(
                text = weekRangeStr,
                fontFamily = ArchivoFont,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                lineHeight = 24.sp,
                letterSpacing = (-0.02).sp,
                color = colors.text
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "Semana $weekNumber · ${selectedDate.year}",
                fontFamily = ArchivoFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp,
                color = colors.textSecondary
              )
            }
            CalendarViewMode.DIA -> {
              val dayOfWeekName = selectedDate.format(DateTimeFormatter.ofPattern("EEEE", Locale("pt", "BR"))).uppercase()
              val dayAndMonth = selectedDate.format(DateTimeFormatter.ofPattern("d 'de' MMMM", Locale("pt", "BR")))
              val dayEventsCount = events.count { ev ->
                Instant.ofEpochMilli(ev.startEpochMillis).atZone(ZoneId.systemDefault()).toLocalDate() == selectedDate
              }

              Text(text = dayOfWeekName, style = SectionLabelStyle, color = colors.textTertiary)
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = dayAndMonth,
                fontFamily = ArchivoFont,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 30.sp,
                lineHeight = 30.sp,
                letterSpacing = (-0.02).sp,
                color = colors.text
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = if (dayEventsCount == 0) "Nenhum compromisso" else "$dayEventsCount compromisso${if (dayEventsCount > 1) "s" else ""}",
                fontFamily = ArchivoFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp,
                color = colors.textSecondary
              )
            }
          }
        }

        // Period Navigation Controls (Previous, Today, Next)
        Row(
          horizontalArrangement = Arrangement.spacedBy(4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .border(1.dp, colors.rulerStrong, RectangleShape)
              .clickable {
                val prev = when (viewMode) {
                  CalendarViewMode.MES -> selectedDate.minusMonths(1)
                  CalendarViewMode.SEMANA -> selectedDate.minusWeeks(1)
                  CalendarViewMode.DIA -> selectedDate.minusDays(1)
                }
                onSelectDate(prev)
              }
              .padding(horizontal = 10.dp, vertical = 7.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "←",
              fontFamily = ArchivoFont,
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              color = colors.text
            )
          }

          Box(
            modifier = Modifier
              .border(1.dp, if (isCurrentDayToday) colors.accent else colors.rulerStrong, RectangleShape)
              .background(if (isCurrentDayToday) colors.accent.copy(alpha = 0.1f) else Color.Transparent)
              .clickable { onSelectDate(LocalDate.now()) }
              .padding(horizontal = 10.dp, vertical = 7.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "Hoje",
              fontFamily = ArchivoFont,
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp,
              color = if (isCurrentDayToday) colors.accentDark else colors.text
            )
          }

          Box(
            modifier = Modifier
              .border(1.dp, colors.rulerStrong, RectangleShape)
              .clickable {
                val next = when (viewMode) {
                  CalendarViewMode.MES -> selectedDate.plusMonths(1)
                  CalendarViewMode.SEMANA -> selectedDate.plusWeeks(1)
                  CalendarViewMode.DIA -> selectedDate.plusDays(1)
                }
                onSelectDate(next)
              }
              .padding(horizontal = 10.dp, vertical = 7.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "→",
              fontFamily = ArchivoFont,
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              color = colors.text
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
        ViewModeChip("Sincronização", isSelected = false) { onOpenOffline() }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Google Calendar Sync Info Strip
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(colors.track)
          .border(0.5.dp, colors.rulerWeak, RectangleShape)
          .clickable { onOpenOffline() }
          .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(7.dp)
              .background(colors.accent)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Google Agenda · thiagovinicius7@gmail.com",
            fontFamily = ArchivoFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 10.5.sp,
            color = colors.text
          )
        }
        Text(
          text = "↻ Sincronizado",
          fontFamily = ArchivoFont,
          fontWeight = FontWeight.Bold,
          fontSize = 10.sp,
          color = colors.accentDark
        )
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
        AgendaWeekView(
          events = events,
          selectedDate = selectedDate,
          onSelectDate = onSelectDate,
          onCreateEvent = onCreateEvent
        )
      }
      CalendarViewMode.DIA -> {
        AgendaDayView(
          events = events,
          selectedDate = selectedDate,
          onCreateEvent = onCreateEvent
        )
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
  val firstOfMonth = selectedDate.withDayOfMonth(1)
  val daysInMonth = selectedDate.lengthOfMonth()
  // DayOfWeek.MONDAY=1..SUNDAY=7. In Sunday-first header: SUNDAY=0, MONDAY=1..SATURDAY=6
  val startDayOffset = (firstOfMonth.dayOfWeek.value % 7)

  val totalCells = ((startDayOffset + daysInMonth + 6) / 7) * 7
  val monthCells = (0 until totalCells).map { idx ->
    val dayNum = idx - startDayOffset + 1
    if (dayNum in 1..daysInMonth) {
      selectedDate.withDayOfMonth(dayNum)
    } else {
      null
    }
  }

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

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
      val rows = monthCells.chunked(7)
      for (row in rows) {
        Row(modifier = Modifier.fillMaxWidth()) {
          for (cellDate in row) {
            val isSelected = cellDate != null && cellDate == selectedDate
            val dayEvents = if (cellDate != null) {
              events.filter { ev ->
                Instant.ofEpochMilli(ev.startEpochMillis)
                  .atZone(ZoneId.systemDefault())
                  .toLocalDate() == cellDate
              }
            } else {
              emptyList()
            }

            Box(
              modifier = Modifier
                .weight(1f)
                .height(46.dp)
                .border(width = 0.5.dp, color = colors.rulerWeak)
                .background(if (isSelected) colors.text else Color.Transparent)
                .clickable { if (cellDate != null) onSelectDate(cellDate) }
                .padding(top = 5.dp),
              contentAlignment = Alignment.TopCenter
            ) {
              if (cellDate != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Text(
                    text = cellDate.dayOfMonth.toString(),
                    fontFamily = ArchivoFont,
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                    fontSize = 12.sp,
                    color = if (isSelected) colors.canvas else colors.text
                  )
                  Spacer(modifier = Modifier.height(3.dp))
                  if (dayEvents.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                      dayEvents.take(3).forEach { ev ->
                        val dotColor = if (ev.calendarId == "cal_pessoal") {
                          colors.accent
                        } else if (isSelected) {
                          Color.White
                        } else {
                          colors.text
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
      }
    }

    Spacer(modifier = Modifier.height(12.dp))
    Ruler2dp()

    val dayEvents = events.filter { event ->
      val eventDate = Instant.ofEpochMilli(event.startEpochMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
      eventDate == selectedDate
    }.sortedBy { it.startEpochMillis }

    val formattedHeader = selectedDate.format(
      DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale("pt", "BR"))
    ).replaceFirstChar { it.uppercase() }

    // Selected Day Agenda details
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
      ) {
        Text(text = formattedHeader, fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = colors.text)
        Text(
          text = if (dayEvents.isEmpty()) "Nenhum compromisso" else "${dayEvents.size} compromisso${if (dayEvents.size > 1) "s" else ""}",
          fontFamily = ArchivoFont,
          fontWeight = FontWeight.SemiBold,
          fontSize = 10.sp,
          color = colors.textTertiary
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      if (dayEvents.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCreateEvent)
            .padding(vertical = 12.dp)
        ) {
          Text(
            text = "+ Nenhum compromisso neste dia. Toque para adicionar.",
            fontFamily = ArchivoFont,
            fontSize = 12.sp,
            color = colors.textTertiary
          )
        }
      } else {
        dayEvents.forEachIndexed { idx, ev ->
          val timeStr = Instant.ofEpochMilli(ev.startEpochMillis)
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("HH:mm"))

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable(onClick = onCreateEvent)
              .padding(vertical = 10.dp),
            verticalAlignment = Alignment.Top
          ) {
            Text(
              text = timeStr,
              fontFamily = ArchivoFont,
              fontWeight = FontWeight.ExtraBold,
              fontSize = 12.sp,
              color = colors.text,
              modifier = Modifier.width(44.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = ev.title,
                fontFamily = ArchivoFont,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                color = colors.text
              )
              if (ev.attachedNoteTitle != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                  modifier = Modifier
                    .background(colors.postItStudyBg)
                    .padding(horizontal = 9.dp, vertical = 7.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Box(modifier = Modifier.size(9.dp).background(colors.accent))
                  Spacer(modifier = Modifier.width(7.dp))
                  Text(
                    text = "Post-it: “${ev.attachedNoteTitle}”",
                    fontFamily = ArchivoFont,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = colors.accentPostItText
                  )
                }
              }
            }
          }
          if (idx < dayEvents.lastIndex) {
            Ruler1dp()
          }
        }
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
private fun AgendaWeekView(
  events: List<CalendarEvent>,
  selectedDate: LocalDate,
  onSelectDate: (LocalDate) -> Unit,
  onCreateEvent: () -> Unit
) {
  val colors = LocalBlocoColors.current
  val today = LocalDate.now()
  // Week from Monday to Sunday
  val dayOfWeek = selectedDate.dayOfWeek.value // 1 (Mon) to 7 (Sun)
  val monday = selectedDate.minusDays((dayOfWeek - 1).toLong())
  val weekDays = (0..6).map { monday.plusDays(it.toLong()) }
  val dayNames = listOf("SEG", "TER", "QUA", "QUI", "SEX", "SÁB", "DOM")

  Column(modifier = Modifier.fillMaxWidth()) {
    // 7 Days Header
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .border(width = 1.dp, color = colors.rulerWeak)
    ) {
      Box(modifier = Modifier.width(32.dp))
      for (i in 0 until 7) {
        val date = weekDays[i]
        val isSelected = (date == selectedDate)
        val isToday = (date == today)
        Column(
          modifier = Modifier
            .weight(1f)
            .background(if (isSelected) colors.text else if (isToday) colors.track else Color.Transparent)
            .clickable { onSelectDate(date) }
            .padding(vertical = 7.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = dayNames[i],
            fontFamily = ArchivoFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 8.5.sp,
            color = if (isSelected) colors.canvas.copy(alpha = 0.7f) else if (isToday) colors.accentDark else colors.textTertiary
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = date.dayOfMonth.toString(),
            fontFamily = ArchivoFont,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 13.sp,
            color = if (isSelected) colors.canvas else if (isToday) colors.accentDark else colors.text
          )
        }
      }
    }

    // Hour slots 07..21
    val hours = listOf("07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21")
    for (hourStr in hours) {
      val hourInt = hourStr.toIntOrNull() ?: 0

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .height(42.dp)
          .border(width = 0.5.dp, color = colors.rulerWeak)
      ) {
        Text(
          text = hourStr,
          fontFamily = ArchivoFont,
          fontWeight = FontWeight.SemiBold,
          fontSize = 9.sp,
          color = colors.textTertiary,
          modifier = Modifier.width(32.dp).padding(start = 4.dp, top = 4.dp)
        )
        for (i in 0 until 7) {
          val date = weekDays[i]
          val matchingEvent = events.firstOrNull { ev ->
            val evDate = Instant.ofEpochMilli(ev.startEpochMillis)
              .atZone(ZoneId.systemDefault())
              .toLocalDate()
            val evHour = Instant.ofEpochMilli(ev.startEpochMillis)
              .atZone(ZoneId.systemDefault())
              .hour
            evDate == date && evHour == hourInt
          }

          Box(
            modifier = Modifier
              .weight(1f)
              .fillMaxSize()
              .border(0.5.dp, colors.rulerWeak, RectangleShape)
              .clickable { onCreateEvent() }
              .padding(2.dp)
          ) {
            if (matchingEvent != null) {
              val isPessoal = matchingEvent.calendarId == "cal_pessoal"
              Box(
                modifier = Modifier
                  .fillMaxSize()
                  .background(if (isPessoal) colors.accent else colors.text)
                  .padding(2.dp)
              ) {
                Text(
                  text = matchingEvent.title.take(9),
                  fontFamily = ArchivoFont,
                  fontWeight = FontWeight.Bold,
                  fontSize = 8.sp,
                  color = Color.White
                )
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
      Text(text = "thiagovinicius7@gmail.com", fontFamily = ArchivoFont, fontWeight = FontWeight.SemiBold, fontSize = 9.5.sp, color = colors.textTertiary)
    }

    Spacer(modifier = Modifier.height(40.dp))
  }
}

@Composable
private fun AgendaDayView(
  events: List<CalendarEvent>,
  selectedDate: LocalDate,
  onCreateEvent: () -> Unit
) {
  val colors = LocalBlocoColors.current

  val dayEvents = events.filter { event ->
    val eventDate = Instant.ofEpochMilli(event.startEpochMillis)
      .atZone(ZoneId.systemDefault())
      .toLocalDate()
    eventDate == selectedDate
  }

  val hours = listOf("06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22")

  Column(modifier = Modifier.fillMaxWidth()) {
    for (hourStr in hours) {
      val hourInt = hourStr.toIntOrNull() ?: 0
      val hourEvents = dayEvents.filter { ev ->
        val evHour = Instant.ofEpochMilli(ev.startEpochMillis)
          .atZone(ZoneId.systemDefault())
          .hour
        evHour == hourInt
      }

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
          text = hourStr,
          fontFamily = ArchivoFont,
          fontWeight = FontWeight.Bold,
          fontSize = 11.sp,
          color = colors.textTertiary,
          modifier = Modifier.width(36.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
          if (hourEvents.isEmpty()) {
            Text(
              text = "+ toque para adicionar",
              fontFamily = ArchivoFont,
              fontWeight = FontWeight.Normal,
              fontSize = 11.sp,
              color = colors.textTertiary.copy(alpha = 0.5f)
            )
          } else {
            for (ev in hourEvents) {
              val timeFormatted = Instant.ofEpochMilli(ev.startEpochMillis)
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("HH:mm"))

              Row(verticalAlignment = Alignment.Top) {
                Box(modifier = Modifier.width(4.dp).height(28.dp).background(colors.accent))
                Spacer(modifier = Modifier.width(9.dp))
                Column {
                  Text(
                    text = "${ev.title} ($timeFormatted)",
                    fontFamily = ArchivoFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.5.sp,
                    color = colors.text
                  )
                  if (ev.attachedNoteTitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                      text = "Post-it: ${ev.attachedNoteTitle}",
                      fontFamily = ArchivoFont,
                      fontWeight = FontWeight.SemiBold,
                      fontSize = 10.5.sp,
                      color = colors.accentDark
                    )
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
  calendars: List<GoogleCalendar> = emptyList(),
  onBack: () -> Unit,
  onSave: (title: String, calendarId: String, date: LocalDate, hour: Int, minute: Int, duration: Int, noteId: String?, noteTitle: String?, localOnly: Boolean) -> Unit,
  modifier: Modifier = Modifier
) {
  val colors = LocalBlocoColors.current
  val scrollState = rememberScrollState()

  var title by remember { mutableStateOf("") }
  var selectedCalendar by remember {
    mutableStateOf(calendars.firstOrNull { it.isSelected }?.id ?: "cal_pessoal")
  }
  val today = remember { LocalDate.now() }
  var selectedDate by remember { mutableStateOf(today) }
  var selectedHour by remember { mutableStateOf(14) }
  var selectedMinute by remember { mutableStateOf(0) }
  var selectedDuration by remember { mutableStateOf(60) }
  var isAllDay by remember { mutableStateOf(false) }
  var isRepeating by remember { mutableStateOf(false) }
  var hasLocation by remember { mutableStateOf(false) }
  var locationText by remember { mutableStateOf("") }
  var attachedPostIt by remember { mutableStateOf<String?>(null) }

  val dateOptions = listOf(
    Pair("Hoje", today),
    Pair("Amanhã", today.plusDays(1)),
    Pair("+2 dias", today.plusDays(2)),
    Pair("+3 dias", today.plusDays(3)),
    Pair("+4 dias", today.plusDays(4))
  )

  val hourOptions = listOf(8, 9, 10, 11, 14, 15, 16, 18, 19, 20)
  val durationOptions = listOf(Pair("30m", 30), Pair("1h", 60), Pair("1h30", 90), Pair("2h", 120))

  val formattedSelectedDate = selectedDate.format(
    DateTimeFormatter.ofPattern("d 'de' MMMM", Locale("pt", "BR"))
  )
  val timeDisplay = if (isAllDay) {
    "Dia inteiro"
  } else {
    String.format("%02d:%02d — %02d:%02d", selectedHour, selectedMinute, (selectedHour + (selectedDuration / 60)) % 24, (selectedMinute + (selectedDuration % 60)) % 60)
  }

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
      Text(text = "TÍTULO DO COMPROMISSO", style = SectionLabelStyle, color = colors.textTertiary)
      Spacer(modifier = Modifier.height(6.dp))
      BasicTextField(
        value = title,
        onValueChange = { title = it },
        textStyle = TextStyle(fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = colors.text),
        cursorBrush = SolidColor(colors.accent),
        decorationBox = { innerTextField ->
          if (title.isEmpty()) {
            Text(
              text = "Ex: Reunião, Treino, Dentista...",
              fontFamily = ArchivoFont,
              fontWeight = FontWeight.Bold,
              fontSize = 20.sp,
              color = colors.textTertiary.copy(alpha = 0.6f)
            )
          }
          innerTextField()
        },
        modifier = Modifier.fillMaxWidth()
      )
      Spacer(modifier = Modifier.height(6.dp))
      Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(colors.text))

      Spacer(modifier = Modifier.height(20.dp))

      // Date and Time Summary Matrix
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(colors.rulerStrong)
          .border(1.dp, colors.rulerStrong, RectangleShape),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
      ) {
        Column(modifier = Modifier.weight(1f).background(colors.canvas).padding(12.dp)) {
          Text(text = "DATA", style = SectionLabelStyle, color = colors.textTertiary)
          Spacer(modifier = Modifier.height(4.dp))
          Text(text = formattedSelectedDate, fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = colors.text)
        }
        Column(modifier = Modifier.weight(1f).background(colors.canvas).padding(12.dp)) {
          Text(text = "HORÁRIO", style = SectionLabelStyle, color = colors.textTertiary)
          Spacer(modifier = Modifier.height(4.dp))
          Text(text = timeDisplay, fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = colors.accentDark)
        }
      }

      Spacer(modifier = Modifier.height(14.dp))
      Text(text = "SELECIONAR DIA", style = SectionLabelStyle, color = colors.textTertiary)
      Spacer(modifier = Modifier.height(6.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        for ((label, dateVal) in dateOptions) {
          ViewModeChip(label, isSelected = selectedDate == dateVal) {
            selectedDate = dateVal
          }
        }
      }

      if (!isAllDay) {
        Spacer(modifier = Modifier.height(14.dp))
        Text(text = "HORA DE INÍCIO", style = SectionLabelStyle, color = colors.textTertiary)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          for (h in hourOptions.take(5)) {
            val label = String.format("%02d:00", h)
            ViewModeChip(label, isSelected = selectedHour == h) {
              selectedHour = h
            }
          }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          for (h in hourOptions.drop(5)) {
            val label = String.format("%02d:00", h)
            ViewModeChip(label, isSelected = selectedHour == h) {
              selectedHour = h
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))
        Text(text = "DURAÇÃO", style = SectionLabelStyle, color = colors.textTertiary)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          for ((durLabel, durVal) in durationOptions) {
            ViewModeChip(durLabel, isSelected = selectedDuration == durVal) {
              selectedDuration = durVal
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))
      Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        ViewModeChip("Dia inteiro", isSelected = isAllDay) { isAllDay = !isAllDay }
        ViewModeChip("Repetir semanal", isSelected = isRepeating) { isRepeating = !isRepeating }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Salvar em qual calendário
      Text(text = "SALVAR EM QUAL CALENDÁRIO", style = SectionLabelStyle, color = colors.textTertiary)
      Spacer(modifier = Modifier.height(8.dp))
      val displayCalendars = if (calendars.isNotEmpty()) calendars else listOf(
        GoogleCalendar("cal_pessoal", "Pessoal", "thiagovinicius7@gmail.com", "#EC3013", isPrimary = true, isSelected = true),
        GoogleCalendar("cal_trabalho", "Trabalho", "thiagovinicius7@gmail.com", "#201E1D", isPrimary = false, isSelected = true)
      )
      Column(modifier = Modifier.fillMaxWidth().border(1.dp, colors.rulerStrong, RectangleShape)) {
        displayCalendars.forEachIndexed { idx, cal ->
          val color = try {
            Color(android.graphics.Color.parseColor(cal.colorHex))
          } catch (e: Exception) {
            colors.accent
          }
          CalendarOptionRow(
            name = "${cal.name} (Google)",
            email = cal.accountEmail,
            color = color,
            isSelected = selectedCalendar == cal.id,
            onClick = { selectedCalendar = cal.id }
          )
          if (idx < displayCalendars.lastIndex) {
            Ruler1dp()
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Post-it anexado
      Text(text = "ANEXAR POST-IT", style = SectionLabelStyle, color = colors.textTertiary)
      Spacer(modifier = Modifier.height(8.dp))
      if (attachedPostIt != null) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(colors.postItStudyBg)
            .padding(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(modifier = Modifier.size(10.dp).background(colors.accent))
          Spacer(modifier = Modifier.width(11.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(text = attachedPostIt ?: "", fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 12.5.sp, color = colors.text)
            Text(text = "Post-it anexado ao evento", fontFamily = ArchivoFont, fontWeight = FontWeight.Normal, fontSize = 10.5.sp, color = colors.textSecondary)
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
            .clickable { attachedPostIt = "Anotações do compromisso" }
            .padding(12.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(text = "+ Anexar post-it do mural", fontFamily = ArchivoFont, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = colors.accent)
        }
      }

      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "O post-it fica visível no dia e no evento da agenda.",
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
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      ModernistButton(
        text = "Salvar no Google Agenda",
        onClick = {
          val finalTitle = title.ifBlank { "Compromisso" }
          onSave(
            finalTitle,
            selectedCalendar,
            selectedDate,
            if (isAllDay) 0 else selectedHour,
            if (isAllDay) 0 else selectedMinute,
            if (isAllDay) 1440 else selectedDuration,
            if (attachedPostIt != null) "note_attached" else null,
            attachedPostIt,
            false
          )
        },
        modifier = Modifier.weight(1f)
      )
      Box(
        modifier = Modifier
          .border(1.dp, colors.rulerStrong, RectangleShape)
          .clickable {
            val finalTitle = title.ifBlank { "Compromisso" }
            onSave(
              finalTitle,
              selectedCalendar,
              selectedDate,
              if (isAllDay) 0 else selectedHour,
              if (isAllDay) 0 else selectedMinute,
              if (isAllDay) 1440 else selectedDuration,
              if (attachedPostIt != null) "note_attached" else null,
              attachedPostIt,
              true
            )
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
