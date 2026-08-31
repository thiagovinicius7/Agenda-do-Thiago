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
  onToggleHabit: (String) -> Unit,
  onOpenHabit: (String) -> Unit,
  onOpenNote: (String) -> Unit,
  onCreateEvent: () -> Unit = {},
  onCreateHabit: () -> Unit = {},
  onCreateNote: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val colors = LocalBlocoColors.current
  val scrollState = rememberScrollState()
  val today = LocalDate.now()
  val todayFormatted = today.format(
    DateTimeFormatter.ofPattern("EEEE, d 'DE' MMMM", Locale("pt", "BR"))
  ).uppercase()

  val todayEvents = events.filter { event ->
    val eventDate = Instant.ofEpochMilli(event.startEpochMillis)
      .atZone(ZoneId.systemDefault())
      .toLocalDate()
    eventDate == today
  }.sortedBy { it.startEpochMillis }

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
      Text(
        text = todayFormatted,
        style = SectionLabelStyle,
        color = colors.textTertiary
      )
      Spacer(modifier = Modifier.height(4.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
      ) {
        Text(
          text = "Hoje",
          fontFamily = ArchivoFont,
          fontWeight = FontWeight.ExtraBold,
          fontSize = 38.sp,
          lineHeight = 38.sp,
          letterSpacing = (-0.03).sp,
          color = colors.text
        )
        if (habits.isNotEmpty()) {
          Text(
            text = "${habits.count { it.isDoneToday }}/${habits.size} hábitos concluídos",
            fontFamily = ArchivoFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            color = colors.accentDark
          )
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
          text = "COMPROMISSOS",
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
        if (todayEvents.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onCreateEvent() }
              .padding(vertical = 12.dp)
          ) {
            Text(
              text = "+ Nenhum compromisso para hoje. Toque para adicionar.",
              fontFamily = ArchivoFont,
              fontSize = 12.sp,
              color = colors.textTertiary
            )
          }
        } else {
          todayEvents.forEachIndexed { idx, ev ->
            val timeStr = Instant.ofEpochMilli(ev.startEpochMillis)
              .atZone(ZoneId.systemDefault())
              .format(DateTimeFormatter.ofPattern("HH:mm"))

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
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(
                    text = "· Post-it: ${ev.attachedNoteTitle}",
                    fontFamily = ArchivoFont,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.5.sp,
                    color = colors.accentDark
                  )
                }
              }
            }
            if (idx < todayEvents.lastIndex) {
              Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.rulerWeak))
            }
          }
        }
      }
    }

    Ruler2dp()

    // 2. Hábitos de hoje
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
          text = "HÁBITOS DE HOJE",
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
            val isDone = habitRes.isDoneToday
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
                onCheckedChange = { onToggleHabit(h.id) },
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
          text = "POST-ITS DO DIA",
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
