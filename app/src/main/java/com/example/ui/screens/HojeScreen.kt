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
import com.example.data.model.HabitCalculationResult
import com.example.data.model.NoteWithItems
import com.example.ui.components.ModernistCheckbox
import com.example.ui.components.Ruler2dp
import com.example.ui.theme.ArchivoFont
import com.example.ui.theme.LocalBlocoColors
import com.example.ui.theme.SectionLabelStyle
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HojeScreen(
  habits: List<HabitCalculationResult>,
  notes: List<NoteWithItems>,
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
        text = "SEXTA, 30 DE AGOSTO",
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
        Text(
          text = "${habits.count { it.isDoneToday }}/${habits.size} hábitos concluídos",
          fontFamily = ArchivoFont,
          fontWeight = FontWeight.SemiBold,
          fontSize = 11.sp,
          color = colors.accentDark
        )
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
        // Event 1: 09:00 Daily
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onCreateEvent() }
            .padding(vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "09:00",
            fontFamily = ArchivoFont,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 12.sp,
            color = colors.text,
            modifier = Modifier.width(44.dp)
          )
          Text(
            text = "Daily do time",
            fontFamily = ArchivoFont,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            color = colors.text
          )
        }
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.rulerWeak))

        // Event 2: 15:00 Reunião cliente (highlighted next appointment)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { notes.firstOrNull()?.note?.id?.let { onOpenNote(it) } ?: onCreateEvent() }
            .padding(vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "15:00",
            fontFamily = ArchivoFont,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 12.sp,
            color = colors.accentDark,
            modifier = Modifier.width(44.dp)
          )
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "Reunião cliente ",
              fontFamily = ArchivoFont,
              fontWeight = FontWeight.Normal,
              fontSize = 13.sp,
              color = colors.text
            )
            Text(
              text = "· 1 post-it anexado →",
              fontFamily = ArchivoFont,
              fontWeight = FontWeight.SemiBold,
              fontSize = 12.sp,
              color = colors.accentDark
            )
          }
        }
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.rulerWeak))

        // Event 3: 19:30 Aula de inglês
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onCreateEvent() }
            .padding(vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "19:30",
            fontFamily = ArchivoFont,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 12.sp,
            color = colors.text,
            modifier = Modifier.width(44.dp)
          )
          Text(
            text = "Aula de inglês",
            fontFamily = ArchivoFont,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            color = colors.text
          )
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
        for (habitRes in habits) {
          val h = habitRes.habit
          val isDone = habitRes.isDoneToday
          val metaText = when (h.id) {
            "h_corrida" -> "· d${habitRes.currentDayNumber} de ${habitRes.totalDays} · sequência d${habitRes.currentStreak}"
            "h_leitura" -> "· sequência d${habitRes.currentStreak}"
            "h_academia" -> "· 2 de 3 na semana"
            else -> if (h.isPaused) "· pausado" else "· ativo"
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Checkbox target (marks day only)
            ModernistCheckbox(
              checked = isDone,
              onCheckedChange = { onToggleHabit(h.id) },
              size = 18.dp
            )
            Spacer(modifier = Modifier.width(10.dp))

            // Habit Name target (opens habit detail)
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

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Post-it Trabalho
        Box(
          modifier = Modifier
            .weight(1f)
            .height(88.dp)
            .background(colors.postItWorkBg)
            .clickable { notes.firstOrNull()?.note?.id?.let { onOpenNote(it) } ?: onCreateNote() }
            .padding(12.dp)
        ) {
          Column {
            Text(
              text = "TRABALHO",
              style = SectionLabelStyle,
              color = colors.accentPostItText
            )
            Spacer(modifier = Modifier.height(7.dp))
            Text(
              text = notes.firstOrNull()?.note?.title ?: "Levar proposta",
              fontFamily = ArchivoFont,
              fontWeight = FontWeight.ExtraBold,
              fontSize = 15.sp,
              lineHeight = 16.sp,
              color = colors.text
            )
          }
        }

        // Post-it Pessoal
        Box(
          modifier = Modifier
            .weight(1f)
            .height(88.dp)
            .background(colors.postItPersonalBg)
            .clickable { notes.getOrNull(1)?.note?.id?.let { onOpenNote(it) } ?: onCreateNote() }
            .padding(12.dp)
        ) {
          Column {
            Text(
              text = "PESSOAL",
              style = SectionLabelStyle,
              color = colors.textTertiary
            )
            Spacer(modifier = Modifier.height(7.dp))
            Text(
              text = notes.getOrNull(1)?.note?.title ?: "Mercado",
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
