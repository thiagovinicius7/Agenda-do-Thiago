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
import com.example.data.model.HabitGridCell
import com.example.data.model.RepeatType
import com.example.ui.components.HabitGrid
import com.example.ui.components.HabitGridMode
import com.example.ui.components.ModernistButton
import com.example.ui.components.ModernistCheckbox
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
            val ruleText = HabitCalculations.formatHabitRuleDescription(habit)
            Text(
              text = if (isPaused) "PAUSADO · SEQUÊNCIA GUARDADA (${habit.pausedSavedStreak})" else ruleText.uppercase(),
              style = SectionLabelStyle,
              color = if (habit.durationDays > 0) colors.accentDark else colors.textTertiary
            )
          }

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
  onToggleCell: (Long) -> Unit = {},
  onMarkPastDays: (fromEpoch: Long, toEpoch: Long, markDone: Boolean) -> Unit = { _, _, _ -> },
  onUpdateReminder: (String) -> Unit = {},
  onTestNotification: (String) -> Unit = {},
  onEditRule: () -> Unit,
  onDeleteHabit: ((String) -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val colors = LocalBlocoColors.current
  val scrollState = rememberScrollState()
  if (habitRes == null) return

  val habit = habitRes.habit
  var pauseWithoutStreakLoss by remember { mutableStateOf(false) }
  var reminderActive by remember { mutableStateOf(habit.reminderEnabled && habit.reminderTime != "Desativado") }
  var chosenReminderTime by remember { mutableStateOf(habit.reminderTime.ifBlank { "08:00" }) }
  var reminderSavedMessage by remember { mutableStateOf<String?>(null) }
  var showDeleteDialog by remember { mutableStateOf(false) }

  val todayEpoch = HabitCalculations.todayEpochDay()
  val ptBr = remember { Locale("pt", "BR") }
  val shortFmt = remember { DateTimeFormatter.ofPattern("d 'de' MMMM", ptBr) }

  val pastRuleCells: List<HabitGridCell> = habitRes.gridCells.filter { it.dateEpochDay <= todayEpoch && it.state != GridCellState.OUTSIDE_RULE }

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
        val subtitle = HabitCalculations.formatHabitRuleDescription(habit)
        Text(
          text = subtitle,
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
        Column(
          modifier = Modifier
            .weight(1f)
            .padding(12.dp)
        ) {
          Text(text = "d${habitRes.currentDayNumber}", style = BigStatStyle, color = colors.text)
          Spacer(modifier = Modifier.height(4.dp))
          Text(text = if (habitRes.totalDays > 0) "DE ${habitRes.totalDays}" else "DIAS", style = SectionLabelStyle, color = colors.textTertiary)
        }
        Box(modifier = Modifier.width(1.dp).fillMaxSize().background(colors.rulerWeak))

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
          text = "GRADE DE DIAS (TOQUE PARA MARCAR/DESMARCAR)",
          style = SectionLabelStyle,
          color = colors.textTertiary
        )
        Text(
          text = "d1 → d${habitRes.totalDays.coerceAtLeast(habitRes.currentDayNumber)}",
          fontFamily = ArchivoFont,
          fontWeight = FontWeight.SemiBold,
          fontSize = 10.sp,
          color = colors.textTertiary
        )
      }

      // 12-Column Numbered Grid with Click on ANY day
      Box(modifier = Modifier.padding(horizontal = 16.dp)) {
        HabitGrid(
          cells = habitRes.gridCells,
          mode = HabitGridMode.DETAIL,
          onCellClick = { cell -> onToggleCell(cell.dateEpochDay) }
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

      // Histórico de Dias Anteriores & Ações em Massa
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 14.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "MARCAR DIAS ANTERIORES",
            style = SectionLabelStyle,
            color = colors.textTertiary
          )
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
              modifier = Modifier
                .background(colors.accent)
                .clickable {
                  onMarkPastDays(habit.startDateEpochDay, todayEpoch, true)
                }
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(
                text = "✓ Marcar todos até hoje",
                fontFamily = ArchivoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = Color.White
              )
            }
            Box(
              modifier = Modifier
                .border(1.dp, colors.rulerStrong, RectangleShape)
                .clickable {
                  onMarkPastDays(habit.startDateEpochDay, todayEpoch, false)
                }
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(
                text = "✕ Desmarcar todos",
                fontFamily = ArchivoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = colors.text
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(
          text = "Toque em qualquer dia anterior abaixo ou na grade acima para marcar como concluído:",
          fontFamily = ArchivoFont,
          fontSize = 11.sp,
          color = colors.textSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))

        Column(
          modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colors.rulerWeak, RectangleShape)
            .background(colors.canvas)
        ) {
          val recentPast = pastRuleCells.takeLast(14).reversed()
          recentPast.forEachIndexed { index, cell ->
            val date = LocalDate.ofEpochDay(cell.dateEpochDay)
            val isDone = cell.state == GridCellState.DONE
            val dateLabel = if (cell.dateEpochDay == todayEpoch) {
              "Hoje (${date.format(shortFmt)})"
            } else if (cell.dateEpochDay == todayEpoch - 1) {
              "Ontem (${date.format(shortFmt)})"
            } else {
              date.format(shortFmt)
            }

            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleCell(cell.dateEpochDay) }
                .padding(horizontal = 12.dp, vertical = 8.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                ModernistCheckbox(
                  checked = isDone,
                  onCheckedChange = { onToggleCell(cell.dateEpochDay) },
                  size = 18.dp
                )
                Text(
                  text = "d${cell.dayNumber} · $dateLabel",
                  fontFamily = ArchivoFont,
                  fontWeight = if (isDone) FontWeight.Bold else FontWeight.Normal,
                  fontSize = 12.sp,
                  color = if (isDone) colors.text else colors.textSecondary
                )
              }
              Text(
                text = if (isDone) "Concluído" else "Não realizado",
                fontFamily = ArchivoFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp,
                color = if (isDone) colors.accentDark else colors.textTertiary
              )
            }
            if (index < recentPast.lastIndex) {
              Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.rulerWeak))
            }
          }
        }
      }

      Ruler2dp()

      // Seção de Notificação e Escolha de Horário
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 14.dp)
      ) {
        Text(
          text = "NOTIFICAÇÃO E HORÁRIO DO LEMBRETE",
          style = SectionLabelStyle,
          color = colors.textTertiary
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Notificar no celular",
              fontFamily = ArchivoFont,
              fontWeight = FontWeight.ExtraBold,
              fontSize = 13.sp,
              color = colors.text
            )
            Text(
              text = "Dispara alarme sonoro e aviso diário no horário selecionado.",
              fontFamily = ArchivoFont,
              fontSize = 10.5.sp,
              color = colors.textSecondary
            )
          }
          ModernistSwitch(
            checked = reminderActive,
            onCheckedChange = {
              reminderActive = it
              val newTime = if (it) chosenReminderTime else "Desativado"
              onUpdateReminder(newTime)
              reminderSavedMessage = if (it) "Lembrete ativado para às $chosenReminderTime" else "Lembrete desativado"
            }
          )
        }

        if (reminderActive) {
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "ESCOLHA O HORÁRIO DA NOTIFICAÇÃO:",
            style = SectionLabelStyle,
            color = colors.textTertiary,
            fontSize = 9.sp
          )
          Spacer(modifier = Modifier.height(8.dp))

          // Horários predefinidos
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            listOf("06:30", "07:00", "08:00", "09:00", "12:00", "15:00", "18:00", "19:30", "20:00", "21:30").forEach { time ->
              val isSel = (chosenReminderTime == time)
              Box(
                modifier = Modifier
                  .background(if (isSel) colors.text else Color.Transparent)
                  .border(1.dp, if (isSel) colors.text else colors.rulerWeak, RectangleShape)
                  .clickable {
                    chosenReminderTime = time
                    onUpdateReminder(time)
                    reminderSavedMessage = "Horário atualizado para às $time"
                  }
                  .padding(horizontal = 10.dp, vertical = 6.dp)
              ) {
                Text(
                  text = time,
                  fontFamily = ArchivoFont,
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.sp,
                  color = if (isSel) colors.canvas else colors.text
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Ajuste fino do horário (-15 min / +15 min)
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .border(1.dp, colors.rulerWeak, RectangleShape)
              .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .border(1.dp, colors.rulerStrong, RectangleShape)
                .clickable {
                  val newT = adjustMinutes(chosenReminderTime, -15)
                  chosenReminderTime = newT
                  onUpdateReminder(newT)
                  reminderSavedMessage = "Horário ajustado para às $newT"
                }
                .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
              Text(
                text = "◀ -15 min",
                fontFamily = ArchivoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = colors.text
              )
            }

            Text(
              text = "⏰ $chosenReminderTime",
              fontFamily = ArchivoFont,
              fontWeight = FontWeight.ExtraBold,
              fontSize = 16.sp,
              color = colors.text
            )

            Box(
              modifier = Modifier
                .border(1.dp, colors.rulerStrong, RectangleShape)
                .clickable {
                  val newT = adjustMinutes(chosenReminderTime, +15)
                  chosenReminderTime = newT
                  onUpdateReminder(newT)
                  reminderSavedMessage = "Horário ajustado para às $newT"
                }
                .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
              Text(
                text = "+15 min ▶",
                fontFamily = ArchivoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = colors.text
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Botão de testar notificação no celular
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Box(
              modifier = Modifier
                .weight(1f)
                .background(colors.accent)
                .clickable {
                  onTestNotification(habit.name)
                  reminderSavedMessage = "Notificação de teste enviada para o celular!"
                }
                .padding(vertical = 10.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "🔔 Testar Notificação Agora",
                fontFamily = ArchivoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = Color.White
              )
            }
          }

          if (reminderSavedMessage != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "✓ $reminderSavedMessage",
              fontFamily = ArchivoFont,
              fontWeight = FontWeight.SemiBold,
              fontSize = 10.5.sp,
              color = colors.accentDark
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))
    }

    // Bottom action row: "Marcar hoje" + "Editar regra" + "Excluir"
    Ruler2dp()
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
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
          text = if (habitRes.isDoneToday) "Desmarcar d${habitRes.currentDayNumber} (Hoje)" else "Marcar d${habitRes.currentDayNumber} (Hoje)",
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
          text = "Regra",
          fontFamily = ArchivoFont,
          fontWeight = FontWeight.ExtraBold,
          fontSize = 13.sp,
          color = colors.text
        )
      }

      if (onDeleteHabit != null) {
        Box(
          modifier = Modifier
            .border(1.dp, colors.gridFail, RectangleShape)
            .clickable { showDeleteDialog = true }
            .padding(14.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "Excluir",
            fontFamily = ArchivoFont,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 13.sp,
            color = colors.gridFail
          )
        }
      }
    }

    if (showDeleteDialog) {
      androidx.compose.material3.AlertDialog(
        onDismissRequest = { showDeleteDialog = false },
        title = {
          Text(
            text = "EXCLUIR HÁBITO",
            fontFamily = ArchivoFont,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 15.sp,
            color = colors.gridFail
          )
        },
        text = {
          Text(
            text = "Tem certeza que deseja apagar o hábito \"${habit.name}\"? Todo o histórico de dias e marcas será apagado permanentemente.",
            fontFamily = ArchivoFont,
            fontSize = 13.sp,
            color = colors.text
          )
        },
        confirmButton = {
          Text(
            text = "Excluir",
            fontFamily = ArchivoFont,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 13.sp,
            color = colors.gridFail,
            modifier = Modifier.clickable {
              showDeleteDialog = false
              onDeleteHabit?.invoke(habit.id)
            }.padding(8.dp)
          )
        },
        dismissButton = {
          Text(
            text = "Cancelar",
            fontFamily = ArchivoFont,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            modifier = Modifier.clickable { showDeleteDialog = false }.padding(8.dp)
          )
        },
        shape = RectangleShape,
        containerColor = colors.canvas
      )
    }
  }
}

@Composable
fun HabitCreateScreen(
  initialHabit: Habit? = null,
  onBack: () -> Unit,
  onSaveHabit: (
    name: String,
    repeatType: RepeatType,
    repeatDays: String,
    timesPerWeek: Int,
    everyNDays: Int,
    weeklyDayOfWeek: Int,
    monthlyDayOfMonth: Int,
    monthDayStart: Int,
    monthDayEnd: Int,
    durationDays: Int,
    reminder: String,
    showInCalendar: Boolean,
    startDateEpochDay: Long,
    markPastDays: Boolean
  ) -> Unit,
  onTestNotification: (String) -> Unit = {},
  modifier: Modifier = Modifier
) {
  val colors = LocalBlocoColors.current
  val scrollState = rememberScrollState()

  val isEditing = initialHabit != null
  var name by remember(initialHabit) { mutableStateOf(initialHabit?.name ?: "Corrida") }
  var repeatType by remember(initialHabit) { mutableStateOf(initialHabit?.repeatType ?: RepeatType.DAYS_OF_WEEK) }
  var selectedDays by remember(initialHabit) {
    mutableStateOf(
      initialHabit?.repeatDays?.split(",")?.mapNotNull { it.trim().toIntOrNull() }?.toSet()?.ifEmpty { setOf(1, 2, 3, 4, 5, 6) }
        ?: setOf(1, 2, 3, 4, 5, 6)
    )
  }
  var timesPerWeek by remember(initialHabit) { mutableIntStateOf(initialHabit?.timesPerWeek ?: 3) }
  var everyNDays by remember(initialHabit) { mutableIntStateOf(initialHabit?.everyNDays ?: 2) }
  var weeklyDayOfWeek by remember(initialHabit) { mutableIntStateOf(initialHabit?.weeklyDayOfWeek ?: 1) }
  var monthlyDayOfMonth by remember(initialHabit) { mutableIntStateOf(initialHabit?.monthlyDayOfMonth ?: 1) }
  var monthDayStart by remember(initialHabit) { mutableIntStateOf(initialHabit?.monthDayStart ?: 9) }
  var monthDayEnd by remember(initialHabit) { mutableIntStateOf(initialHabit?.monthDayEnd ?: 17) }

  var durationDays by remember(initialHabit) { mutableIntStateOf(initialHabit?.durationDays ?: 150) }
  val today = remember { LocalDate.now() }
  var startDate by remember(initialHabit) {
    mutableStateOf(
      initialHabit?.startDateEpochDay?.let { LocalDate.ofEpochDay(it) } ?: today
    )
  }
  var markPastDaysAsDone by remember { mutableStateOf(false) }
  var showInCalendar by remember(initialHabit) { mutableStateOf(initialHabit?.showInCalendar ?: true) }
  var pauseAllowed by remember { mutableStateOf(false) }
  var reminderEnabled by remember(initialHabit) {
    mutableStateOf(
      if (initialHabit != null) (initialHabit.reminderEnabled && initialHabit.reminderTime != "Desativado" && initialHabit.reminderTime.isNotBlank()) else true
    )
  }
  var reminderTime by remember(initialHabit) {
    mutableStateOf(
      if (initialHabit?.reminderTime.isNullOrBlank() || initialHabit?.reminderTime == "Desativado") "08:00" else initialHabit!!.reminderTime
    )
  }
  var testNotificationSent by remember { mutableStateOf(false) }

  val ptBr = remember { Locale("pt", "BR") }
  val dateDisplayFmt = remember { DateTimeFormatter.ofPattern("d 'de' MMMM", ptBr) }
  val shortFmt = remember { DateTimeFormatter.ofPattern("d MMM", ptBr) }

  val isStartInPast = startDate.isBefore(today)

  val dummyHabit = Habit(
    id = initialHabit?.id ?: "new",
    name = name,
    repeatType = repeatType,
    repeatDays = selectedDays.joinToString(","),
    timesPerWeek = timesPerWeek,
    everyNDays = everyNDays,
    weeklyDayOfWeek = weeklyDayOfWeek,
    monthlyDayOfMonth = monthlyDayOfMonth,
    monthDayStart = monthDayStart,
    monthDayEnd = monthDayEnd,
    durationDays = durationDays,
    startDateEpochDay = startDate.toEpochDay()
  )
  val previewCalc = remember(name, repeatType, selectedDays, timesPerWeek, everyNDays, weeklyDayOfWeek, monthlyDayOfMonth, monthDayStart, monthDayEnd, durationDays, startDate) {
    HabitCalculations.calculate(dummyHabit, emptyList(), currentEpochDay = today.toEpochDay())
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
        text = if (isEditing) "Editar hábito" else "Novo hábito",
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

      // 2. Data de Início (START DATE & RETROACTIVE START)
      Text(text = "DATA DE INÍCIO", style = SectionLabelStyle, color = colors.textTertiary)
      Spacer(modifier = Modifier.height(8.dp))

      // Quick choice chips for start date (including past dates)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        DurationChip("Hoje (${today.format(shortFmt)})", isSelected = startDate == today) { startDate = today }
        DurationChip("Ontem (-1d)", isSelected = startDate == today.minusDays(1)) { startDate = today.minusDays(1) }
        DurationChip("-3 dias", isSelected = startDate == today.minusDays(3)) { startDate = today.minusDays(3) }
        DurationChip("-7 dias", isSelected = startDate == today.minusDays(7)) { startDate = today.minusDays(7) }
        DurationChip("-14 dias", isSelected = startDate == today.minusDays(14)) { startDate = today.minusDays(14) }
        DurationChip("-30 dias", isSelected = startDate == today.minusDays(30)) { startDate = today.minusDays(30) }
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
          val diffDays = today.toEpochDay() - startDate.toEpochDay()
          val subtitle = when {
            diffDays == 0L -> "Inicia Hoje"
            diffDays == 1L -> "Iniciou Ontem (1 dia atrás)"
            diffDays > 1L -> "Iniciou há $diffDays dias atrás"
            else -> "Início futuro"
          }
          Text(
            text = subtitle,
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

      // If started in the past, offer auto-marking
      if (isStartInPast) {
        Spacer(modifier = Modifier.height(10.dp))
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(colors.accent.copy(alpha = 0.08f))
            .border(1.dp, colors.accent, RectangleShape)
            .padding(10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Marcar dias anteriores como concluídos",
              fontFamily = ArchivoFont,
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              color = colors.text
            )
            Text(
              text = "Preenche automaticamente os dias desde o início até ontem.",
              fontFamily = ArchivoFont,
              fontSize = 10.sp,
              color = colors.textSecondary
            )
          }
          ModernistCheckbox(
            checked = markPastDaysAsDone,
            onCheckedChange = { markPastDaysAsDone = !markPastDaysAsDone },
            size = 20.dp
          )
        }
      }

      Spacer(modifier = Modifier.height(22.dp))

      // 3. Repetition Rule
      Text(text = "REPETIÇÃO", style = SectionLabelStyle, color = colors.textTertiary)
      Spacer(modifier = Modifier.height(8.dp))

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
          RepetitionGridItem("Novena / Dias do mês", isSelected = repeatType == RepeatType.MONTH_DAYS_RANGE, modifier = Modifier.weight(1f)) { repeatType = RepeatType.MONTH_DAYS_RANGE }
          RepetitionGridItem("Dia fixo no mês", isSelected = repeatType == RepeatType.MONTHLY, modifier = Modifier.weight(1f)) { repeatType = RepeatType.MONTHLY }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
          RepetitionGridItem("Semanal", isSelected = repeatType == RepeatType.WEEKLY, modifier = Modifier.weight(1f)) { repeatType = RepeatType.WEEKLY }
        }
      }

      // Repetition type specific UI controls
      when (repeatType) {
        RepeatType.DAYS_OF_WEEK -> {
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
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            DurationChip("Seg a Sáb (sem Dom)", isSelected = selectedDays == setOf(1, 2, 3, 4, 5, 6)) { selectedDays = setOf(1, 2, 3, 4, 5, 6) }
            DurationChip("Todos os dias", isSelected = selectedDays == setOf(1, 2, 3, 4, 5, 6, 7)) { selectedDays = setOf(1, 2, 3, 4, 5, 6, 7) }
            DurationChip("Seg a Sex (dias úteis)", isSelected = selectedDays == setOf(1, 2, 3, 4, 5)) { selectedDays = setOf(1, 2, 3, 4, 5) }
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = if (selectedDays.contains(7)) "Todos os dias da semana selecionados." else "Dias selecionados ativos. Dias não selecionados não quebram sequência.",
            fontFamily = ArchivoFont,
            fontWeight = FontWeight.Normal,
            fontSize = 11.sp,
            color = colors.textSecondary
          )
        }

        RepeatType.TIMES_PER_WEEK -> {
          Spacer(modifier = Modifier.height(12.dp))
          Text(text = "QUANTAS VEZES POR SEMANA?", style = SectionLabelStyle, color = colors.textTertiary)
          Spacer(modifier = Modifier.height(6.dp))
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            (1..7).forEach { n ->
              DurationChip("${n}× / semana", isSelected = timesPerWeek == n) { timesPerWeek = n }
            }
          }
          Spacer(modifier = Modifier.height(8.dp))
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
                .clickable { if (timesPerWeek > 1) timesPerWeek-- }
                .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Text(text = "− 1 vez", fontFamily = ArchivoFont, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = colors.text)
            }
            Text(
              text = "$timesPerWeek× POR SEMANA",
              fontFamily = ArchivoFont,
              fontWeight = FontWeight.ExtraBold,
              fontSize = 14.sp,
              color = colors.text
            )
            Row(
              modifier = Modifier
                .border(1.dp, colors.rulerStrong, RectangleShape)
                .clickable { if (timesPerWeek < 7) timesPerWeek++ }
                .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Text(text = "+ 1 vez", fontFamily = ArchivoFont, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = colors.text)
            }
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Meta flexível de $timesPerWeek vezes a cada semana (de segunda a domingo).",
            fontFamily = ArchivoFont,
            fontSize = 11.sp,
            color = colors.textSecondary
          )
        }

        RepeatType.EVERY_N_DAYS -> {
          Spacer(modifier = Modifier.height(12.dp))
          Text(text = "INTERVALO DE DIAS (A CADA N DIAS)", style = SectionLabelStyle, color = colors.textTertiary)
          Spacer(modifier = Modifier.height(6.dp))
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            DurationChip("2 dias (dia sim/não)", isSelected = everyNDays == 2) { everyNDays = 2 }
            DurationChip("3 dias", isSelected = everyNDays == 3) { everyNDays = 3 }
            DurationChip("4 dias", isSelected = everyNDays == 4) { everyNDays = 4 }
            DurationChip("5 dias", isSelected = everyNDays == 5) { everyNDays = 5 }
            DurationChip("7 dias (semanal)", isSelected = everyNDays == 7) { everyNDays = 7 }
            DurationChip("10 dias", isSelected = everyNDays == 10) { everyNDays = 10 }
            DurationChip("14 dias (quinzena)", isSelected = everyNDays == 14) { everyNDays = 14 }
            DurationChip("30 dias (mensal)", isSelected = everyNDays == 30) { everyNDays = 30 }
          }
          Spacer(modifier = Modifier.height(8.dp))
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
                .clickable { if (everyNDays > 1) everyNDays-- }
                .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Text(text = "← -1 dia", fontFamily = ArchivoFont, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = colors.text)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = "A CADA $everyNDays DIAS",
                fontFamily = ArchivoFont,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                color = colors.text
              )
              if (everyNDays == 2) {
                Text(
                  text = "Dia sim / Dia não",
                  fontFamily = ArchivoFont,
                  fontSize = 10.sp,
                  color = colors.accentDark
                )
              }
            }
            Row(
              modifier = Modifier
                .border(1.dp, colors.rulerStrong, RectangleShape)
                .clickable { if (everyNDays < 365) everyNDays++ }
                .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Text(text = "+1 dia →", fontFamily = ArchivoFont, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = colors.text)
            }
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "O hábito se repete a cada $everyNDays dias a contar da data de início selecionada (${startDate.format(shortFmt)}).",
            fontFamily = ArchivoFont,
            fontSize = 11.sp,
            color = colors.textSecondary
          )
        }

        RepeatType.MONTH_DAYS_RANGE -> {
          Spacer(modifier = Modifier.height(12.dp))
          Text(text = "INTERVALO DE DIAS DO MÊS (EX: NOVENA)", style = SectionLabelStyle, color = colors.textTertiary)
          Spacer(modifier = Modifier.height(6.dp))
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            DurationChip("Novena (9 a 17)", isSelected = monthDayStart == 9 && monthDayEnd == 17) {
              monthDayStart = 9
              monthDayEnd = 17
            }
            DurationChip("Início do mês (1 a 5)", isSelected = monthDayStart == 1 && monthDayEnd == 5) {
              monthDayStart = 1
              monthDayEnd = 5
            }
            DurationChip("Primeira quinzena (1 a 15)", isSelected = monthDayStart == 1 && monthDayEnd == 15) {
              monthDayStart = 1
              monthDayEnd = 15
            }
            DurationChip("Meio do mês (10 a 20)", isSelected = monthDayStart == 10 && monthDayEnd == 20) {
              monthDayStart = 10
              monthDayEnd = 20
            }
            DurationChip("Final do mês (20 a 30)", isSelected = monthDayStart == 20 && monthDayEnd == 30) {
              monthDayStart = 20
              monthDayEnd = 30
            }
          }
          Spacer(modifier = Modifier.height(10.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            // Dia Inicial
            Column(
              modifier = Modifier
                .weight(1f)
                .border(1.dp, colors.rulerStrong, RectangleShape)
                .padding(8.dp)
            ) {
              Text(text = "DIA INICIAL", style = SectionLabelStyle, color = colors.textTertiary)
              Spacer(modifier = Modifier.height(6.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .border(1.dp, colors.rulerStrong, RectangleShape)
                    .clickable { if (monthDayStart > 1) monthDayStart-- }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                  Text(text = "◀", fontFamily = ArchivoFont, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = colors.text)
                }
                Text(
                  text = "Dia $monthDayStart",
                  fontFamily = ArchivoFont,
                  fontWeight = FontWeight.ExtraBold,
                  fontSize = 14.sp,
                  color = colors.text
                )
                Box(
                  modifier = Modifier
                    .border(1.dp, colors.rulerStrong, RectangleShape)
                    .clickable { if (monthDayStart < 31) monthDayStart++ }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                  Text(text = "▶", fontFamily = ArchivoFont, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = colors.text)
                }
              }
            }

            // Dia Final
            Column(
              modifier = Modifier
                .weight(1f)
                .border(1.dp, colors.rulerStrong, RectangleShape)
                .padding(8.dp)
            ) {
              Text(text = "DIA FINAL", style = SectionLabelStyle, color = colors.textTertiary)
              Spacer(modifier = Modifier.height(6.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .border(1.dp, colors.rulerStrong, RectangleShape)
                    .clickable { if (monthDayEnd > 1) monthDayEnd-- }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                  Text(text = "◀", fontFamily = ArchivoFont, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = colors.text)
                }
                Text(
                  text = "Dia $monthDayEnd",
                  fontFamily = ArchivoFont,
                  fontWeight = FontWeight.ExtraBold,
                  fontSize = 14.sp,
                  color = colors.text
                )
                Box(
                  modifier = Modifier
                    .border(1.dp, colors.rulerStrong, RectangleShape)
                    .clickable { if (monthDayEnd < 31) monthDayEnd++ }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                  Text(text = "▶", fontFamily = ArchivoFont, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = colors.text)
                }
              }
            }
          }
          Spacer(modifier = Modifier.height(6.dp))
          val daysInPeriod = if (monthDayStart <= monthDayEnd) monthDayEnd - monthDayStart + 1 else (31 - monthDayStart + 1 + monthDayEnd)
          Text(
            text = "Ativo todo mês do dia $monthDayStart ao dia $monthDayEnd ($daysInPeriod dias a cada mês, ex: Novena mensal).",
            fontFamily = ArchivoFont,
            fontSize = 11.sp,
            color = colors.accentDark
          )
        }

        RepeatType.MONTHLY -> {
          Spacer(modifier = Modifier.height(12.dp))
          Text(text = "DIA DO MÊS", style = SectionLabelStyle, color = colors.textTertiary)
          Spacer(modifier = Modifier.height(6.dp))
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            listOf(1, 5, 10, 15, 20, 25, 28, 30).forEach { d ->
              DurationChip("Dia $d", isSelected = monthlyDayOfMonth == d) { monthlyDayOfMonth = d }
            }
          }
          Spacer(modifier = Modifier.height(8.dp))
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .border(1.dp, colors.rulerStrong, RectangleShape)
              .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .border(1.dp, colors.rulerStrong, RectangleShape)
                .clickable { if (monthlyDayOfMonth > 1) monthlyDayOfMonth-- }
                .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Text(text = "◀ Dia anterior", fontFamily = ArchivoFont, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = colors.text)
            }
            Text(
              text = "TODO DIA $monthlyDayOfMonth",
              fontFamily = ArchivoFont,
              fontWeight = FontWeight.ExtraBold,
              fontSize = 14.sp,
              color = colors.text
            )
            Box(
              modifier = Modifier
                .border(1.dp, colors.rulerStrong, RectangleShape)
                .clickable { if (monthlyDayOfMonth < 31) monthlyDayOfMonth++ }
                .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Text(text = "Próximo dia ▶", fontFamily = ArchivoFont, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = colors.text)
            }
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "O hábito se repete todo dia $monthlyDayOfMonth de cada mês.",
            fontFamily = ArchivoFont,
            fontSize = 11.sp,
            color = colors.textSecondary
          )
        }

        RepeatType.WEEKLY -> {
          Spacer(modifier = Modifier.height(12.dp))
          Text(text = "DIA DA SEMANA", style = SectionLabelStyle, color = colors.textTertiary)
          Spacer(modifier = Modifier.height(6.dp))
          val weekDays = listOf("Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom")
          val fullNames = listOf("Segunda-feira", "Terça-feira", "Quarta-feira", "Quinta-feira", "Sexta-feira", "Sábado", "Domingo")
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            weekDays.forEachIndexed { idx, dName ->
              val dVal = idx + 1
              val isSel = (weeklyDayOfWeek == dVal)
              Box(
                modifier = Modifier
                  .weight(1f)
                  .background(if (isSel) colors.text else Color.Transparent)
                  .border(1.dp, if (isSel) colors.text else colors.rulerStrong, RectangleShape)
                  .clickable { weeklyDayOfWeek = dVal }
                  .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = dName,
                  fontFamily = ArchivoFont,
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.sp,
                  color = if (isSel) colors.canvas else colors.text
                )
              }
            }
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "O hábito se repete toda ${fullNames.getOrElse(weeklyDayOfWeek - 1) { "semana" }}.",
            fontFamily = ArchivoFont,
            fontSize = 11.sp,
            color = colors.textSecondary
          )
        }

        RepeatType.DAILY -> {
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "O hábito é diário e deve ser cumprido todos os dias.",
            fontFamily = ArchivoFont,
            fontSize = 11.sp,
            color = colors.textSecondary
          )
        }
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

      Spacer(modifier = Modifier.height(20.dp))
      Ruler2dp()
      Spacer(modifier = Modifier.height(14.dp))

      // 5. Notificação e Escolha de Horário
      Text(text = "NOTIFICAÇÃO E HORÁRIO NO CELULAR", style = SectionLabelStyle, color = colors.textTertiary)
      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(text = "Notificar no celular", fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = colors.text)
          Text(text = "Avisa no horário programado para você não esquecer.", fontFamily = ArchivoFont, fontWeight = FontWeight.Normal, fontSize = 10.5.sp, color = colors.textSecondary)
        }
        ModernistSwitch(checked = reminderEnabled, onCheckedChange = { reminderEnabled = it })
      }

      if (reminderEnabled) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          listOf("06:30", "07:00", "08:00", "09:00", "12:00", "15:00", "18:00", "19:30", "20:00", "21:30").forEach { time ->
            val isSel = (reminderTime == time)
            Box(
              modifier = Modifier
                .background(if (isSel) colors.text else Color.Transparent)
                .border(1.dp, if (isSel) colors.text else colors.rulerWeak, RectangleShape)
                .clickable { reminderTime = time }
                .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Text(
                text = time,
                fontFamily = ArchivoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = if (isSel) colors.canvas else colors.text
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Fine adjustment for reminder time
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colors.rulerWeak, RectangleShape)
            .padding(8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .border(1.dp, colors.rulerStrong, RectangleShape)
              .clickable { reminderTime = adjustMinutes(reminderTime, -15) }
              .padding(horizontal = 8.dp, vertical = 6.dp)
          ) {
            Text(text = "◀ -15 min", fontFamily = ArchivoFont, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = colors.text)
          }

          Text(
            text = "⏰ $reminderTime",
            fontFamily = ArchivoFont,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp,
            color = colors.text
          )

          Box(
            modifier = Modifier
              .border(1.dp, colors.rulerStrong, RectangleShape)
              .clickable { reminderTime = adjustMinutes(reminderTime, +15) }
              .padding(horizontal = 8.dp, vertical = 6.dp)
          ) {
            Text(text = "+15 min ▶", fontFamily = ArchivoFont, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = colors.text)
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colors.rulerStrong, RectangleShape)
            .clickable {
              onTestNotification(name)
              testNotificationSent = true
            }
            .padding(vertical = 8.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = if (testNotificationSent) "✓ Notificação de teste disparada!" else "🔔 Testar Notificação Agora",
            fontFamily = ArchivoFont,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = if (testNotificationSent) colors.accentDark else colors.text
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Toggle: Mostrar na agenda
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(text = "Mostrar na agenda", fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = colors.text)
          Text(text = "Aparece às $reminderTime nos dias da regra.", fontFamily = ArchivoFont, fontWeight = FontWeight.Normal, fontSize = 10.5.sp, color = colors.textSecondary)
        }
        ModernistSwitch(checked = showInCalendar, onCheckedChange = { showInCalendar = it })
      }
    }

    Ruler2dp()
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      ModernistButton(
        text = if (isEditing) "Salvar alterações" else "Criar hábito",
        onClick = {
          val finalReminder = if (reminderEnabled) reminderTime else "Desativado"
          onSaveHabit(
            name,
            repeatType,
            selectedDays.joinToString(","),
            timesPerWeek,
            everyNDays,
            weeklyDayOfWeek,
            monthlyDayOfMonth,
            monthDayStart,
            monthDayEnd,
            durationDays,
            finalReminder,
            showInCalendar,
            startDate.toEpochDay(),
            markPastDaysAsDone
          )
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

private fun adjustMinutes(timeStr: String, deltaMinutes: Int): String {
  val parts = timeStr.split(":").mapNotNull { it.trim().toIntOrNull() }
  val hour = parts.getOrNull(0) ?: 8
  val minute = parts.getOrNull(1) ?: 0
  val totalMins = (hour * 60 + minute + deltaMinutes + 1440) % 1440
  val newHour = totalMins / 60
  val newMin = totalMins % 60
  return String.format(Locale.ROOT, "%02d:%02d", newHour, newMin)
}
