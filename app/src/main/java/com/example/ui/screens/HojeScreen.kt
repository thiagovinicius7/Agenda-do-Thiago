package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CalendarEvent
import com.example.data.model.GoogleCalendar
import com.example.data.model.GridCellState
import com.example.data.model.HabitCalculationResult
import com.example.data.model.NoteWithItems
import com.example.ui.components.ModernistCheckbox
import com.example.ui.components.Ruler2dp
import com.example.ui.theme.ArchivoFont
import com.example.ui.theme.LocalBlocoColors
import com.example.ui.theme.SectionLabelStyle
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HojeScreen(
  habits: List<HabitCalculationResult>,
  notes: List<NoteWithItems>,
  events: List<CalendarEvent> = emptyList(),
  calendars: List<GoogleCalendar> = emptyList(),
  selectedDate: LocalDate = LocalDate.now(),
  onSelectDate: (LocalDate) -> Unit = {},
  onToggleHabit: (String) -> Unit,
  onToggleHabitForDate: (String, Long) -> Unit = { id, _ -> onToggleHabit(id) },
  onOpenHabit: (String) -> Unit,
  onOpenNote: (String) -> Unit,
  onCreateEvent: () -> Unit = {},
  onCreateHabit: () -> Unit = {},
  onCreateNote: () -> Unit = {},
  onToggleCalendar: (String) -> Unit = {},
  onOpenCalendarsDialog: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val colors = LocalBlocoColors.current
  val scrollState = rememberScrollState()
  val today = LocalDate.now()
  val isCurrentToday = (selectedDate == today)

  val selectedDateFormatted = selectedDate.format(
    DateTimeFormatter.ofPattern("EEEE, d 'DE' MMMM", Locale("pt", "BR"))
  ).uppercase()

  val displayTitle = when {
    selectedDate == today -> "Hoje"
    selectedDate == today.minusDays(1) -> "Ontem"
    selectedDate == today.plusDays(1) -> "Amanhã"
    else -> selectedDate.format(DateTimeFormatter.ofPattern("d 'de' MMMM", Locale("pt", "BR")))
  }

  val dateEvents = events.filter { event ->
    val eventDate = Instant.ofEpochMilli(event.startEpochMillis)
      .atZone(ZoneId.systemDefault())
      .toLocalDate()
    eventDate == selectedDate
  }.sortedBy { it.startEpochMillis }

  val targetEpochDay = selectedDate.toEpochDay()

  val doneHabitsCount = habits.count { habitRes ->
    val cell = habitRes.gridCells.find { it.dateEpochDay == targetEpochDay }
    cell?.state == GridCellState.DONE
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(colors.canvas)
      .verticalScroll(scrollState)
  ) {
    // Header with Date Stepper Navigation
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = selectedDateFormatted,
          style = SectionLabelStyle,
          color = colors.textTertiary
        )

        // Date Stepper controls
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Box(
            modifier = Modifier
              .border(1.dp, colors.rulerStrong, RectangleShape)
              .clickable { onSelectDate(selectedDate.minusDays(1)) }
              .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "◀ Anterior",
              fontFamily = ArchivoFont,
              fontWeight = FontWeight.Bold,
              fontSize = 10.sp,
              color = colors.text
            )
          }

          if (!isCurrentToday) {
            Box(
              modifier = Modifier
                .background(colors.accent)
                .clickable { onSelectDate(today) }
                .padding(horizontal = 8.dp, vertical = 4.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "Hoje",
                fontFamily = ArchivoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = Color.White
              )
            }
          }

          Box(
            modifier = Modifier
              .border(1.dp, colors.rulerStrong, RectangleShape)
              .clickable { onSelectDate(selectedDate.plusDays(1)) }
              .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "Próximo ▶",
              fontFamily = ArchivoFont,
              fontWeight = FontWeight.Bold,
              fontSize = 10.sp,
              color = colors.text
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(4.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
      ) {
        Text(
          text = displayTitle,
          fontFamily = ArchivoFont,
          fontWeight = FontWeight.ExtraBold,
          fontSize = 36.sp,
          lineHeight = 38.sp,
          letterSpacing = (-0.03).sp,
          color = colors.text
        )

        if (habits.isNotEmpty()) {
          Text(
            text = "$doneHabitsCount/${habits.size} feitos",
            fontFamily = ArchivoFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            color = colors.accentDark
          )
        }
      }

      // Quick Calendar chips if multiple calendars exist
      if (calendars.size > 1) {
        Spacer(modifier = Modifier.height(10.dp))
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "AGENDAS:",
            style = SectionLabelStyle,
            color = colors.textTertiary,
            fontSize = 9.sp
          )
          calendars.forEach { cal ->
            val isSelected = cal.isSelected
            val calColor = try {
              Color(android.graphics.Color.parseColor(cal.colorHex))
            } catch (e: Exception) {
              colors.accent
            }

            Box(
              modifier = Modifier
                .background(if (isSelected) calColor.copy(alpha = 0.15f) else Color.Transparent)
                .border(
                  width = if (isSelected) 1.5.dp else 1.dp,
                  color = if (isSelected) calColor else colors.rulerWeak,
                  shape = RectangleShape
                )
                .clickable { onToggleCalendar(cal.id) }
                .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(6.dp)
                    .background(calColor)
                )
                Text(
                  text = cal.name,
                  fontFamily = ArchivoFont,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  fontSize = 10.sp,
                  color = if (isSelected) colors.text else colors.textTertiary
                )
              }
            }
          }
          Box(
            modifier = Modifier
              .clickable { onOpenCalendarsDialog() }
              .padding(horizontal = 6.dp, vertical = 3.dp)
          ) {
            Text(
              text = "⚙ Filtrar",
              fontFamily = ArchivoFont,
              fontWeight = FontWeight.SemiBold,
              fontSize = 10.sp,
              color = colors.accentDark
            )
          }
        }
      }
    }

    Ruler2dp()

    // 1. Compromissos
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 14.dp)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (isCurrentToday) "COMPROMISSOS DE HOJE" else "COMPROMISSOS DO DIA",
          style = SectionLabelStyle,
          color = colors.textTertiary
        )
        Text(
          text = "+ Novo",
          fontFamily = ArchivoFont,
          fontWeight = FontWeight.Bold,
          fontSize = 11.sp,
          color = colors.accentDark,
          modifier = Modifier.clickable(onClick = onCreateEvent)
        )
      }

      Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        if (dateEvents.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onCreateEvent() }
              .padding(vertical = 12.dp)
          ) {
            Text(
              text = "+ Nenhum compromisso para este dia. Toque para adicionar.",
              fontFamily = ArchivoFont,
              fontSize = 12.sp,
              color = colors.textTertiary
            )
          }
        } else {
          dateEvents.forEachIndexed { idx, ev ->
            val timeStr = if (ev.isAllDay) {
              "Dia todo"
            } else {
              Instant.ofEpochMilli(ev.startEpochMillis)
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("HH:mm"))
            }

            val cal = calendars.find { it.id == ev.calendarId }
            val calColor = try {
              if (cal != null) Color(android.graphics.Color.parseColor(cal.colorHex)) else colors.accent
            } catch (e: Exception) {
              colors.accent
            }

            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  if (ev.attachedNoteId != null) {
                    onOpenNote(ev.attachedNoteId)
                  } else {
                    onCreateEvent()
                  }
                }
                .padding(vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .width(3.dp)
                  .height(24.dp)
                  .background(calColor)
              )
              Spacer(modifier = Modifier.width(8.dp))

              Text(
                text = timeStr,
                fontFamily = ArchivoFont,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp,
                color = colors.text,
                modifier = Modifier.width(52.dp)
              )
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = ev.title,
                  fontFamily = ArchivoFont,
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 13.sp,
                  color = colors.text
                )
                Row(
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  if (cal != null) {
                    Text(
                      text = cal.name,
                      fontFamily = ArchivoFont,
                      fontSize = 10.sp,
                      color = colors.textTertiary
                    )
                  }
                  if (ev.location != null) {
                    Text(
                      text = "· 📍 ${ev.location}",
                      fontFamily = ArchivoFont,
                      fontSize = 10.sp,
                      color = colors.textTertiary
                    )
                  }
                  if (ev.attachedNoteTitle != null) {
                    Text(
                      text = "· Post-it: ${ev.attachedNoteTitle}",
                      fontFamily = ArchivoFont,
                      fontWeight = FontWeight.SemiBold,
                      fontSize = 10.sp,
                      color = colors.accentDark
                    )
                  }
                }
              }
            }
            if (idx < dateEvents.lastIndex) {
              Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.rulerWeak))
            }
          }
        }
      }
    }

    Ruler2dp()

    // 2. Hábitos
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 14.dp)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (isCurrentToday) "HÁBITOS DE HOJE" else "HÁBITOS NESTE DIA",
          style = SectionLabelStyle,
          color = colors.textTertiary
        )
        Text(
          text = "+ Novo",
          fontFamily = ArchivoFont,
          fontWeight = FontWeight.Bold,
          fontSize = 11.sp,
          color = colors.accentDark,
          modifier = Modifier.clickable(onClick = onCreateHabit)
        )
      }

      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        if (habits.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onCreateHabit() }
              .padding(vertical = 12.dp)
          ) {
            Text(
              text = "+ Nenhum hábito cadastrado. Toque para iniciar um hábito.",
              fontFamily = ArchivoFont,
              fontSize = 12.sp,
              color = colors.textTertiary
            )
          }
        } else {
          for (habitRes in habits) {
            val h = habitRes.habit
            val cellForDay = habitRes.gridCells.find { it.dateEpochDay == targetEpochDay }
            val isDone = cellForDay?.state == GridCellState.DONE
            val metaText = if (habitRes.totalDays > 0) {
              "· d${habitRes.currentDayNumber} de ${habitRes.totalDays} · seq d${habitRes.currentStreak}"
            } else {
              "· seq d${habitRes.currentStreak}"
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically
            ) {
              ModernistCheckbox(
                checked = isDone,
                onCheckedChange = { onToggleHabitForDate(h.id, targetEpochDay) },
                size = 18.dp
              )
              Spacer(modifier = Modifier.width(10.dp))

              Row(
                modifier = Modifier
                  .weight(1f)
                  .clickable { onOpenHabit(h.id) }
                  .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = h.name,
                  fontFamily = ArchivoFont,
                  fontWeight = FontWeight.Medium,
                  fontSize = 13.sp,
                  color = colors.text
                )
                Text(
                  text = " $metaText",
                  fontFamily = ArchivoFont,
                  fontWeight = FontWeight.Normal,
                  fontSize = 13.sp,
                  color = colors.textTertiary
                )
              }
            }
          }
        }
      }
    }

    Ruler2dp()

    // 3. Post-its do dia
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 14.dp, bottom = 24.dp)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "POST-ITS E NOTAS",
          style = SectionLabelStyle,
          color = colors.textTertiary
        )
        Text(
          text = "+ Novo post-it",
          fontFamily = ArchivoFont,
          fontWeight = FontWeight.Bold,
          fontSize = 11.sp,
          color = colors.accentDark,
          modifier = Modifier.clickable(onClick = onCreateNote)
        )
      }

      if (notes.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onCreateNote() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
          Text(
            text = "+ Nenhum post-it criado. Toque para criar uma nota rápida.",
            fontFamily = ArchivoFont,
            fontSize = 12.sp,
            color = colors.textTertiary
          )
        }
      } else {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          val firstNote = notes.getOrNull(0)
          val secondNote = notes.getOrNull(1)

          if (firstNote != null) {
            Box(
              modifier = Modifier
                .weight(1f)
                .height(88.dp)
                .background(colors.postItWorkBg)
                .clickable { onOpenNote(firstNote.note.id) }
                .padding(12.dp)
            ) {
              Column {
                Text(
                  text = "NOTA",
                  style = SectionLabelStyle,
                  color = colors.accentPostItText
                )
                Spacer(modifier = Modifier.height(7.dp))
                Text(
                  text = firstNote.note.title,
                  fontFamily = ArchivoFont,
                  fontWeight = FontWeight.ExtraBold,
                  fontSize = 15.sp,
                  lineHeight = 16.sp,
                  color = colors.text
                )
              }
            }
          }

          if (secondNote != null) {
            Box(
              modifier = Modifier
                .weight(1f)
                .height(88.dp)
                .background(colors.postItPersonalBg)
                .clickable { onOpenNote(secondNote.note.id) }
                .padding(12.dp)
            ) {
              Column {
                Text(
                  text = "NOTA",
                  style = SectionLabelStyle,
                  color = colors.textTertiary
                )
                Spacer(modifier = Modifier.height(7.dp))
                Text(
                  text = secondNote.note.title,
                  fontFamily = ArchivoFont,
                  fontWeight = FontWeight.ExtraBold,
                  fontSize = 15.sp,
                  lineHeight = 16.sp,
                  color = colors.text
                )
              }
            }
          }
        }
      }
    }
  }
}
