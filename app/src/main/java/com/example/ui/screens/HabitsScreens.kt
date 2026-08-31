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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GridCellState
import com.example.data.model.Habit
import com.example.data.model.HabitCalculationResult
import com.example.data.model.RepeatType
import com.example.ui.components.HabitGrid
import com.example.ui.components.HabitGridMode
import com.example.ui.components.ModernistButton
import com.example.ui.components.ModernistSwitch
import com.example.ui.components.Ruler1dp
import com.example.ui.components.Ruler2dp
import com.example.ui.theme.ArchivoFont
import com.example.ui.theme.BigStatStyle
import com.example.ui.theme.LocalBlocoColors
import com.example.ui.theme.SectionLabelStyle
import com.example.util.HabitCalculations
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HabitsListScreen(
  habits: List<HabitCalculationResult>,
  onOpenHabit: (String) -> Unit,
  onCreateHabit: () -> Unit,
  onOpenStats: () -> Unit,
  onToggleHabit: (String) -> Unit,
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
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 14.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.Bottom
    ) {
      Text(
        text = "Hábitos",
        fontFamily = ArchivoFont,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 34.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.02).sp,
        color = colors.text
      )
      Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Estatísticas →",
          fontFamily = ArchivoFont,
          fontWeight = FontWeight.Bold,
          fontSize = 11.sp,
          color = colors.accentDark,
          modifier = Modifier.clickable(onClick = onOpenStats)
        )
        Text(
          text = "${habits.size} ativos",
          fontFamily = ArchivoFont,
          fontWeight = FontWeight.SemiBold,
          fontSize = 11.sp,
          color = colors.textSecondary
        )
      }
    }

    Ruler2dp()

    // Habit items
    for (habitRes in habits) {
      val habit = habitRes.habit
      val isPaused = habit.isPaused

      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onOpenHabit(habit.id) }
          .padding(16.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Top
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = habit.name,
              fontFamily = ArchivoFont,
              fontWeight = FontWeight.ExtraBold,
              fontSize = 18.sp,
              lineHeight = 20.sp,
              color = if (isPaused) colors.textSecondary else colors.text
            )
            Spacer(modifier = Modifier.height(5.dp))
            val ruleText = when (habit.repeatType) {
              RepeatType.DAYS_OF_WEEK -> "${if (habit.durationDays > 0) "${habit.durationDays} dias · " else ""}todos menos domingo"
              RepeatType.TIMES_PER_WEEK -> "${habit.timesPerWeek}× por semana"
              RepeatType.DAILY -> "Diário · sem fim"
              else -> "Ativo"
            }
            Text(
              text = if (isPaused) "PAUSADO · SEQUÊNCIA GUARDADA (${habit.pausedSavedStreak})" else ruleText.uppercase(),
              style = SectionLabelStyle,
              color = if (habit.durationDays > 0) colors.accentDark else colors.textTertiary
            )
          }

          // Badge (e.g. d62 or 2/3 or d4)
          val badgeText = when (habit.id) {
            "h_corrida" -> "d${habitRes.currentDayNumber}"
            "h_leitura" -> "d${habitRes.currentStreak}"
            "h_academia" -> "2/3"
            else -> if (isPaused) "d${habit.pausedSavedStreak}" else "d${habitRes.currentDayNumber}"
          }

          if (isPaused) {
            Box(
              modifier = Modifier
                .border(1.dp, colors.rulerStrong, RectangleShape)
                .clickable { onToggleHabit(habit.id) }
                .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
              Text(
                text = "Retomar",
                fontFamily = ArchivoFont,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 10.sp,
                color = colors.text
              )
            }
          } else {
            Box(
              modifier = Modifier
                .then(
                  if (habit.durationDays > 0) {
                    Modifier.background(colors.text)
                  } else {
                    Modifier.border(1.dp, colors.rulerStrong, RectangleShape)
                  }
                )
                .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
              Text(
                text = badgeText,
                fontFamily = ArchivoFont,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 11.sp,
                color = if (habit.durationDays > 0) colors.canvas else colors.text
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Grid or Weekly Bar
        if (habit.repeatType == RepeatType.TIMES_PER_WEEK) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
          ) {
            Box(modifier = Modifier.weight(1f).height(24.dp).background(colors.accent))
            Box(modifier = Modifier.weight(1f).height(24.dp).background(colors.accent))
            Box(modifier = Modifier.weight(1f).height(24.dp).border(1.5.dp, colors.text, RectangleShape))
            Box(modifier = Modifier.weight(1f).height(24.dp).border(1.dp, colors.cellOutline, RectangleShape))
            Box(modifier = Modifier.weight(1f).height(24.dp).border(1.dp, colors.cellOutline, RectangleShape))
          }
        } else {
          HabitGrid(
            cells = habitRes.gridCells.take(150),
            mode = HabitGridMode.LIST
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Footer streak info
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "Sequência atual ${HabitCalculations.formatStreakInterval(habitRes.currentStreakStartDay, habitRes.currentStreakEndDay)} (${habitRes.currentStreak} dias)",
            fontFamily = ArchivoFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 10.sp,
            color = colors.textSecondary
          )
          Text(
            text = "Abrir →",
            fontFamily = ArchivoFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 10.sp,
            color = colors.textSecondary
          )
        }
      }

      Ruler1dp()
    }

    // New Habit Button
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      ModernistButton(
        text = "Novo hábito",
        onClick = onCreateHabit,
        modifier = Modifier.fillMaxWidth()
      )
    }

    Spacer(modifier = Modifier.height(40.dp))
  }
}

@Composable
fun HabitDetailScreen(
  habitRes: HabitCalculationResult?,
  onBack: () -> Unit,
  onToggleToday: () -> Unit,
  onEditRule: () -> Unit,
  modifier: Modifier = Modifier
) {
  val colors = LocalBlocoColors.current
  val scrollState = rememberScrollState()
  if (habitRes == null) return

  val habit = habitRes.habit
  var pauseWithoutStreakLoss by remember { mutableStateOf(false) }
  var reminderActive by remember { mutableStateOf(habit.reminderEnabled) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(colors.canvas)
  ) {
    // Back navigation & Header Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "← Hábitos",
        fontFamily = ArchivoFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        color = colors.textSecondary,
        modifier = Modifier.clickable(onClick = onBack)
      )
      Text(
        text = "HÁBITO",
        fontFamily = ArchivoFont,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 12.sp,
        color = colors.textTertiary
      )
    }

    Ruler2dp()

    Column(
      modifier = Modifier
        .weight(1f)
        .verticalScroll(scrollState)
    ) {
      // Header Title & Rule
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp)
      ) {
        Text(
          text = habit.name,
          fontFamily = ArchivoFont,
          fontWeight = FontWeight.ExtraBold,
          fontSize = 38.sp,
          lineHeight = 37.sp,
          letterSpacing = (-0.03).sp,
          color = colors.text
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "TODOS OS DIAS MENOS DOMINGO · ${habit.durationDays} DIAS",
          style = SectionLabelStyle,
          color = colors.accentDark
        )
      }

      Ruler2dp()

      // 3 Big Stat Boxes
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .height(72.dp)
      ) {
        // Day count
        Column(
          modifier = Modifier
            .weight(1f)
            .padding(12.dp)
        ) {
          Text(text = "d${habitRes.currentDayNumber}", style = BigStatStyle, color = colors.text)
          Spacer(modifier = Modifier.height(4.dp))
          Text(text = "DE ${habitRes.totalDays}", style = SectionLabelStyle, color = colors.textTertiary)
        }
        Box(modifier = Modifier.width(1.dp).fillMaxSize().background(colors.rulerWeak))

        // Streak
        Column(
          modifier = Modifier
            .weight(1f)
            .padding(12.dp)
        ) {
          Text(text = "d${habitRes.currentStreak}", style = BigStatStyle, color = colors.accent)
          Spacer(modifier = Modifier.height(4.dp))
          Text(text = "SEQUÊNCIA", style = SectionLabelStyle, color = colors.textTertiary)
        }
        Box(modifier = Modifier.width(1.dp).fillMaxSize().background(colors.rulerWeak))

        // Consistency %
        Column(
          modifier = Modifier
            .weight(1f)
            .padding(12.dp)
        ) {
          Text(text = "${habitRes.consistencyPercent}%", style = BigStatStyle, color = colors.text)
          Spacer(modifier = Modifier.height(4.dp))
          Text(text = "CONSTÂNCIA", style = SectionLabelStyle, color = colors.textTertiary)
        }
      }

      Ruler2dp()

      // Grid Label Header
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "ABR — SET · UM QUADRADO POR DIA",
          style = SectionLabelStyle,
          color = colors.textTertiary
        )
        Text(
          text = "d1 → d${habitRes.totalDays}",
          fontFamily = ArchivoFont,
          fontWeight = FontWeight.SemiBold,
          fontSize = 10.sp,
          color = colors.textTertiary
        )
      }

      // 12-Column Numbered Grid
      Box(modifier = Modifier.padding(horizontal = 16.dp)) {
        HabitGrid(
          cells = habitRes.gridCells,
          mode = HabitGridMode.DETAIL
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Streak text explanation
      Text(
        text = "Sequência atual: d${habitRes.currentStreakStartDay} → d${habitRes.currentStreakEndDay}, ${habitRes.currentStreak} dias seguidos. Melhor sequência: d${habitRes.bestStreakStartDay} → d${habitRes.bestStreakEndDay}.",
        fontFamily = ArchivoFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        color = colors.text,
        modifier = Modifier.padding(horizontal = 16.dp)
      )

      Spacer(modifier = Modifier.height(14.dp))

      // 5-State Legend
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        LegendItem(color = colors.accent, label = "feito")
        LegendItem(color = colors.gridFail, label = "falhou")
        LegendBorderItem(label = "hoje")
        LegendOutlineItem(label = "a fazer")
      }

      Spacer(modifier = Modifier.height(16.dp))
      Ruler2dp()
      Spacer(modifier = Modifier.height(12.dp))

      // Switch 1: Pause without losing streak
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Pausar sem perder a sequência",
            fontFamily = ArchivoFont,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 13.sp,
            color = colors.text
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Dias pausados não contam como falha.",
            fontFamily = ArchivoFont,
            fontWeight = FontWeight.Normal,
            fontSize = 10.5.sp,
            color = colors.textSecondary
          )
        }
        ModernistSwitch(
          checked = pauseWithoutStreakLoss,
          onCheckedChange = { pauseWithoutStreakLoss = it }
        )
      }

      // Switch 2: Reminder
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Lembrete ${habit.reminderTime}",
          fontFamily = ArchivoFont,
          fontWeight = FontWeight.ExtraBold,
          fontSize = 13.sp,
          color = colors.text
        )
        ModernistSwitch(
          checked = reminderActive,
          onCheckedChange = { reminderActive = it }
        )
      }

      Spacer(modifier = Modifier.height(20.dp))
    }

    // Bottom action row: "Marcar d62" + "Editar regra"
    Ruler2dp()
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp),
      horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
      Box(
        modifier = Modifier
          .weight(1f)
          .background(colors.accent)
          .clickable(onClick = onToggleToday)
          .padding(14.dp),
        contentAlignment = Alignment.CenterStart
      ) {
        Text(
          text = if (habitRes.isDoneToday) "Desmarcar d${habitRes.currentDayNumber}" else "Marcar d${habitRes.currentDayNumber}",
          fontFamily = ArchivoFont,
          fontWeight = FontWeight.ExtraBold,
          fontSize = 13.sp,
          color = Color.White
        )
      }

      Box(
        modifier = Modifier
          .border(1.dp, colors.rulerStrong, RectangleShape)
          .clickable(onClick = onEditRule)
          .padding(14.dp),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "Editar regra",
          fontFamily = ArchivoFont,
          fontWeight = FontWeight.ExtraBold,
          fontSize = 13.sp,
          color = colors.text
        )
      }
    }
  }
}

@Composable
fun HabitCreateScreen(
  onBack: () -> Unit,
  onSaveHabit: (name: String, repeatType: RepeatType, repeatDays: String, durationDays: Int, reminder: String, showInCalendar: Boolean, startDateEpochDay: Long) -> Unit,
  modifier: Modifier = Modifier
) {
  val colors = LocalBlocoColors.current
  val scrollState = rememberScrollState()

  var name by remember { mutableStateOf("Corrida") }
  var repeatType by remember { mutableStateOf(RepeatType.DAYS_OF_WEEK) }
  var selectedDays by remember { mutableStateOf(setOf(1, 2, 3, 4, 5, 6)) } // Mon-Sat
  var durationDays by remember { mutableIntStateOf(150) }
  val today = remember { LocalDate.now() }
  var startDate by remember { mutableStateOf(today) }
  var showInCalendar by remember { mutableStateOf(true) }
  var pauseAllowed by remember { mutableStateOf(false) }
  var reminderTime by remember { mutableStateOf("06:30") }

  val ptBr = remember { Locale("pt", "BR") }
  val dateDisplayFmt = remember { DateTimeFormatter.ofPattern("d 'de' MMMM", ptBr) }
  val shortFmt = remember { DateTimeFormatter.ofPattern("d MMM", ptBr) }

  // Synthetic calculated preview for creation
  val dummyHabit = Habit(
    id = "new",
    name = name,
    repeatType = repeatType,
    repeatDays = selectedDays.joinToString(","),
    durationDays = durationDays,
    startDateEpochDay = startDate.toEpochDay()
  )
  val previewCalc = remember(name, repeatType, selectedDays, durationDays, startDate) {
    HabitCalculations.calculate(dummyHabit, emptyList(), currentEpochDay = startDate.toEpochDay())
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(colors.canvas)
  ) {
    // Header
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 14.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Novo hábito",
        fontFamily = ArchivoFont,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 22.sp,
        color = colors.text
      )
      Text(
        text = "Cancelar",
        fontFamily = ArchivoFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        color = colors.textSecondary,
        modifier = Modifier.clickable(onClick = onBack)
      )
    }

    Ruler2dp()

    Column(
      modifier = Modifier
        .weight(1f)
        .verticalScroll(scrollState)
        .padding(16.dp)
    ) {
      // 1. Name Input
      Text(text = "NOME", style = SectionLabelStyle, color = colors.textTertiary)
      Spacer(modifier = Modifier.height(6.dp))
      BasicTextField(
        value = name,
        onValueChange = { name = it },
        textStyle = TextStyle(
          fontFamily = ArchivoFont,
          fontWeight = FontWeight.ExtraBold,
          fontSize = 22.sp,
          color = colors.text
        ),
        cursorBrush = SolidColor(colors.accent),
        modifier = Modifier.fillMaxWidth()
      )
      Spacer(modifier = Modifier.height(6.dp))
      Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(colors.text))

      Spacer(modifier = Modifier.height(22.dp))

      // 2. Data de Início (START DATE)
      Text(text = "DATA DE INÍCIO", style = SectionLabelStyle, color = colors.textTertiary)
      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        DurationChip(
          label = "Hoje (${today.format(shortFmt)})",
          isSelected = startDate == today
        ) { startDate = today }

        val tomorrow = today.plusDays(1)
        DurationChip(
          label = "Amanhã (${tomorrow.format(shortFmt)})",
          isSelected = startDate == tomorrow
        ) { startDate = tomorrow }

        val nextMonday = today.plusDays(((8 - today.dayOfWeek.value) % 7).toLong().let { if (it == 0L) 7L else it })
        DurationChip(
          label = "Seg (${nextMonday.format(shortFmt)})",
          isSelected = startDate == nextMonday
        ) { startDate = nextMonday }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Fine adjustment for start date
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, colors.rulerStrong, RectangleShape)
          .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          modifier = Modifier
            .border(1.dp, colors.rulerStrong, RectangleShape)
            .clickable { startDate = startDate.minusDays(1) }
            .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
          Text(text = "← -1 dia", fontFamily = ArchivoFont, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = colors.text)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = startDate.format(dateDisplayFmt).uppercase(ptBr),
            fontFamily = ArchivoFont,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 13.sp,
            color = colors.text
          )
          Text(
            text = if (startDate == today) "Hoje" else if (startDate == today.plusDays(1)) "Amanhã" else "Início escolhido",
            fontFamily = ArchivoFont,
            fontSize = 10.sp,
            color = colors.accentDark
          )
        }

        Row(
          modifier = Modifier
            .border(1.dp, colors.rulerStrong, RectangleShape)
            .clickable { startDate = startDate.plusDays(1) }
            .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
          Text(text = "+1 dia →", fontFamily = ArchivoFont, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = colors.text)
        }
      }

      Spacer(modifier = Modifier.height(22.dp))

      // 3. Repetition Rule
      Text(text = "REPETIÇÃO", style = SectionLabelStyle, color = colors.textTertiary)
      Spacer(modifier = Modifier.height(8.dp))

      // 2-Column Repetition Matrix
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(colors.rulerStrong)
          .border(1.dp, colors.rulerStrong, RectangleShape)
      ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
          RepetitionGridItem("Diário", isSelected = repeatType == RepeatType.DAILY, modifier = Modifier.weight(1f)) { repeatType = RepeatType.DAILY }
          RepetitionGridItem("Dias da semana", isSelected = repeatType == RepeatType.DAYS_OF_WEEK, modifier = Modifier.weight(1f)) { repeatType = RepeatType.DAYS_OF_WEEK }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
          RepetitionGridItem("X× por semana", isSelected = repeatType == RepeatType.TIMES_PER_WEEK, modifier = Modifier.weight(1f)) { repeatType = RepeatType.TIMES_PER_WEEK }
          RepetitionGridItem("A cada N dias", isSelected = repeatType == RepeatType.EVERY_N_DAYS, modifier = Modifier.weight(1f)) { repeatType = RepeatType.EVERY_N_DAYS }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
          RepetitionGridItem("Semanal", isSelected = repeatType == RepeatType.WEEKLY, modifier = Modifier.weight(1f)) { repeatType = RepeatType.WEEKLY }
          RepetitionGridItem("Mensal", isSelected = repeatType == RepeatType.MONTHLY, modifier = Modifier.weight(1f)) { repeatType = RepeatType.MONTHLY }
        }
      }

      // S T Q Q S S D Days Selector
      if (repeatType == RepeatType.DAYS_OF_WEEK) {
        Spacer(modifier = Modifier.height(12.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          DayLetterChip("S", 1, selectedDays) { toggleDay(1, selectedDays) { selectedDays = it } }
          DayLetterChip("T", 2, selectedDays) { toggleDay(2, selectedDays) { selectedDays = it } }
          DayLetterChip("Q", 3, selectedDays) { toggleDay(3, selectedDays) { selectedDays = it } }
          DayLetterChip("Q", 4, selectedDays) { toggleDay(4, selectedDays) { selectedDays = it } }
          DayLetterChip("S", 5, selectedDays) { toggleDay(5, selectedDays) { selectedDays = it } }
          DayLetterChip("S", 6, selectedDays) { toggleDay(6, selectedDays) { selectedDays = it } }
          DayLetterChip("D", 7, selectedDays) { toggleDay(7, selectedDays) { selectedDays = it } }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = if (selectedDays.contains(7)) "Todos os dias da semana." else "Todos os dias menos domingo. Domingos não contam como falha.",
          fontFamily = ArchivoFont,
          fontWeight = FontWeight.Normal,
          fontSize = 11.sp,
          color = colors.textSecondary
        )
      }

      Spacer(modifier = Modifier.height(22.dp))

      // 4. Duration Selector
      Text(text = "DURAÇÃO", style = SectionLabelStyle, color = colors.textTertiary)
      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        DurationChip("Sem fim", isSelected = durationDays == 0) { durationDays = 0 }
        DurationChip("40 dias", isSelected = durationDays == 40) { durationDays = 40 }
        DurationChip("150 dias", isSelected = durationDays == 150) { durationDays = 150 }
        DurationChip("Outro", isSelected = durationDays !in listOf(0, 40, 150)) { durationDays = 90 }
      }

      Spacer(modifier = Modifier.height(14.dp))
      val endDate = if (durationDays > 0) startDate.plusDays(durationDays.toLong()) else null
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = "Início: ${startDate.format(shortFmt)} (d1)",
          fontFamily = ArchivoFont,
          fontWeight = FontWeight.SemiBold,
          fontSize = 10.sp,
          color = colors.textSecondary
        )
        Text(
          text = if (endDate != null) "Fim: ${endDate.format(shortFmt)} (d$durationDays)" else "Sem data final",
          fontFamily = ArchivoFont,
          fontWeight = FontWeight.SemiBold,
          fontSize = 10.sp,
          color = colors.textSecondary
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Realtime Preview Grid
      HabitGrid(
        cells = previewCalc.gridCells.take(150),
        mode = HabitGridMode.PREVIEW
      )

      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "${previewCalc.plannedMarksCount} marcações previstas · ${previewCalc.excludedDaysCount} domingos fora da regra.",
        fontFamily = ArchivoFont,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        color = colors.textSecondary
      )

      Spacer(modifier = Modifier.height(20.dp))
      Ruler2dp()
      Spacer(modifier = Modifier.height(14.dp))

      // Toggle: Mostrar na agenda
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(text = "Mostrar na agenda", fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = colors.text)
          Text(text = "Aparece às $reminderTime nos dias da regra.", fontFamily = ArchivoFont, fontWeight = FontWeight.Normal, fontSize = 10.5.sp, color = colors.textSecondary)
        }
        ModernistSwitch(checked = showInCalendar, onCheckedChange = { showInCalendar = it })
      }

      // Toggle: Pausa não quebra sequência
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(text = "Pausa não quebra a sequência", fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = colors.text)
        ModernistSwitch(checked = pauseAllowed, onCheckedChange = { pauseAllowed = it })
      }

      // Reminder button
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(text = "Lembrete", fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = colors.text)
        Box(
          modifier = Modifier
            .border(1.dp, colors.rulerStrong, RectangleShape)
            .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
          Text(text = reminderTime, fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = colors.text)
        }
      }
    }

    Ruler2dp()
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      ModernistButton(
        text = "Criar hábito",
        onClick = {
          onSaveHabit(name, repeatType, selectedDays.joinToString(","), durationDays, reminderTime, showInCalendar, startDate.toEpochDay())
        },
        modifier = Modifier.fillMaxWidth()
      )
    }
  }
}

@Composable
fun HabitConcludedScreen(
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val colors = LocalBlocoColors.current
  val scrollState = rememberScrollState()

  val dummyConcludedHabit = Habit(
    id = "done",
    name = "Corrida",
    repeatType = RepeatType.DAYS_OF_WEEK,
    repeatDays = "1,2,3,4,5,6",
    durationDays = 150,
    startDateEpochDay = HabitCalculations.todayEpochDay() - 150
  )
  val concludedCalc = remember {
    // Generate full done set
    val marks = (0..150).filter { it % 11 != 7 }.map {
      com.example.data.model.HabitMark("done", HabitCalculations.todayEpochDay() - 150 + it)
    }
    HabitCalculations.calculate(dummyConcludedHabit, marks, currentEpochDay = HabitCalculations.todayEpochDay())
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(colors.canvas)
      .verticalScroll(scrollState)
  ) {
    // Header
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clickable(onClick = onBack)
        .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
      Text(
        text = "← Hábitos",
        fontFamily = ArchivoFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        color = colors.textSecondary
      )
    }

    // Red Celebration Banner
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(colors.accent)
        .padding(20.dp, 16.dp)
    ) {
      Column {
        Text(
          text = "CONCLUÍDO · 27 JAN 2027",
          style = SectionLabelStyle,
          color = Color.White.copy(alpha = 0.85f)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
          text = "Corrida",
          fontFamily = ArchivoFont,
          fontWeight = FontWeight.ExtraBold,
          fontSize = 36.sp,
          color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "150 dias, d1 → d150",
          fontFamily = ArchivoFont,
          fontWeight = FontWeight.ExtraBold,
          fontSize = 14.sp,
          color = Color.White
        )
      }
    }

    Ruler2dp()

    // 3 Stats
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(72.dp)
    ) {
      Column(modifier = Modifier.weight(1f).padding(12.dp)) {
        Text(text = "121", style = BigStatStyle, color = colors.text)
        Text(text = "DE 129 DIAS", style = SectionLabelStyle, color = colors.textTertiary)
      }
      Box(modifier = Modifier.width(1.dp).fillMaxSize().background(colors.rulerWeak))
      Column(modifier = Modifier.weight(1f).padding(12.dp)) {
        Text(text = "d47", style = BigStatStyle, color = colors.accent)
        Text(text = "MELHOR SEQUÊNCIA", style = SectionLabelStyle, color = colors.textTertiary)
      }
      Box(modifier = Modifier.width(1.dp).fillMaxSize().background(colors.rulerWeak))
      Column(modifier = Modifier.weight(1f).padding(12.dp)) {
        Text(text = "94%", style = BigStatStyle, color = colors.text)
        Text(text = "CONSTÂNCIA", style = SectionLabelStyle, color = colors.textTertiary)
      }
    }

    Ruler2dp()

    Column(modifier = Modifier.padding(16.dp)) {
      Text(text = "PERÍODO INTEIRO", style = SectionLabelStyle, color = colors.textTertiary)
      Spacer(modifier = Modifier.height(12.dp))
      HabitGrid(cells = concludedCalc.gridCells, mode = HabitGridMode.PREVIEW)
      Spacer(modifier = Modifier.height(14.dp))
      Text(
        text = "8 falhas, todas isoladas. A sequência mais longa foi de d94 a d140.",
        fontFamily = ArchivoFont,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        color = colors.textSecondary
      )

      Spacer(modifier = Modifier.height(18.dp))
      Ruler2dp()
      Spacer(modifier = Modifier.height(14.dp))

      Text(text = "E AGORA", style = SectionLabelStyle, color = colors.textTertiary)
      Spacer(modifier = Modifier.height(12.dp))

      ActionCard("Repetir por mais 150 dias", "Mesma regra, contagem reinicia em d1.", onClick = onBack)
      Spacer(modifier = Modifier.height(8.dp))
      ActionCard("Continuar sem prazo", "Mantém a sequência atual.", onClick = onBack)
      Spacer(modifier = Modifier.height(8.dp))
      ActionCard("Arquivar", "Sai da lista, histórico fica guardado.", onClick = onBack)
    }
  }
}

@Composable
private fun ActionCard(title: String, subtitle: String, onClick: () -> Unit) {
  val colors = LocalBlocoColors.current
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .border(1.dp, colors.rulerStrong, RectangleShape)
      .clickable(onClick = onClick)
      .padding(14.dp, 12.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(text = title, fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = colors.text)
      Spacer(modifier = Modifier.height(4.dp))
      Text(text = subtitle, fontFamily = ArchivoFont, fontWeight = FontWeight.Normal, fontSize = 10.5.sp, color = colors.textSecondary)
    }
    Text(text = "→", fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = colors.text)
  }
}

@Composable
private fun LegendItem(color: Color, label: String) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Box(modifier = Modifier.size(11.dp).background(color))
    Spacer(modifier = Modifier.width(6.dp))
    Text(text = label, fontFamily = ArchivoFont, fontWeight = FontWeight.Normal, fontSize = 10.sp)
  }
}

@Composable
private fun LegendBorderItem(label: String) {
  val colors = LocalBlocoColors.current
  Row(verticalAlignment = Alignment.CenterVertically) {
    Box(modifier = Modifier.size(11.dp).border(1.5.dp, colors.text, RectangleShape))
    Spacer(modifier = Modifier.width(6.dp))
    Text(text = label, fontFamily = ArchivoFont, fontWeight = FontWeight.Normal, fontSize = 10.sp)
  }
}

@Composable
private fun LegendOutlineItem(label: String) {
  val colors = LocalBlocoColors.current
  Row(verticalAlignment = Alignment.CenterVertically) {
    Box(modifier = Modifier.size(11.dp).border(1.dp, colors.cellOutline, RectangleShape))
    Spacer(modifier = Modifier.width(6.dp))
    Text(text = label, fontFamily = ArchivoFont, fontWeight = FontWeight.Normal, fontSize = 10.sp)
  }
}

@Composable
private fun RepetitionGridItem(
  label: String,
  isSelected: Boolean,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  val colors = LocalBlocoColors.current
  val bg = if (isSelected) colors.text else colors.canvas
  val textColor = if (isSelected) colors.canvas else colors.text

  Box(
    modifier = modifier
      .background(bg)
      .clickable(onClick = onClick)
      .padding(12.dp),
    contentAlignment = Alignment.CenterStart
  ) {
    Text(
      text = label,
      fontFamily = ArchivoFont,
      fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
      fontSize = 12.sp,
      color = textColor
    )
  }
}

@Composable
private fun DayLetterChip(
  letter: String,
  dayIndex: Int,
  selectedDays: Set<Int>,
  onClick: () -> Unit
) {
  val colors = LocalBlocoColors.current
  val isSelected = selectedDays.contains(dayIndex)
  val bg = if (isSelected) colors.accent else Color.Transparent
  val textColor = if (isSelected) Color.White else colors.textTertiary
  val borderModifier = if (!isSelected) Modifier.border(1.dp, colors.rulerStrong, RectangleShape) else Modifier

  Box(
    modifier = Modifier
      .then(borderModifier)
      .background(bg)
      .clickable(onClick = onClick)
      .padding(vertical = 12.dp, horizontal = 12.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = letter,
      fontFamily = ArchivoFont,
      fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
      fontSize = 12.sp,
      color = textColor
    )
  }
}

private fun toggleDay(day: Int, current: Set<Int>, onUpdate: (Set<Int>) -> Unit) {
  if (current.contains(day)) {
    if (current.size > 1) onUpdate(current - day)
  } else {
    onUpdate(current + day)
  }
}

@Composable
private fun DurationChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
  val colors = LocalBlocoColors.current
  val bg = if (isSelected) colors.text else Color.Transparent
  val textColor = if (isSelected) colors.canvas else colors.text
  val borderModifier = if (!isSelected) Modifier.border(1.dp, colors.rulerStrong, RectangleShape) else Modifier

  Box(
    modifier = Modifier
      .then(borderModifier)
      .background(bg)
      .clickable(onClick = onClick)
      .padding(horizontal = 13.dp, vertical = 11.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      fontFamily = ArchivoFont,
      fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
      fontSize = 11.sp,
      color = textColor
    )
  }
}
