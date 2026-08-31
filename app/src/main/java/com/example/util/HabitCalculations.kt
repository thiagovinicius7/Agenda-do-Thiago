package com.example.util

import com.example.data.model.GridCellState
import com.example.data.model.Habit
import com.example.data.model.HabitCalculationResult
import com.example.data.model.HabitGridCell
import com.example.data.model.HabitMark
import com.example.data.model.HabitMarkStatus
import com.example.data.model.RepeatType
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

object HabitCalculations {

  fun toEpochDay(year: Int, month: Int, day: Int): Long {
    return LocalDate.of(year, month, day).toEpochDay()
  }

  fun todayEpochDay(): Long {
    return LocalDate.now().toEpochDay()
  }

  /**
   * Checks whether a specific date (epochDay) is an active/required day according to the habit's repetition rule.
   * Days of week: 1=Mon, 2=Tue, 3=Wed, 4=Thu, 5=Fri, 6=Sat, 7=Sun
   */
  fun isDateInRule(habit: Habit, dateEpochDay: Long, weekStartSunday: Boolean = true): Boolean {
    val date = LocalDate.ofEpochDay(dateEpochDay)
    val dayOfWeek = date.dayOfWeek.value // 1 (Mon) to 7 (Sun)

    return when (habit.repeatType) {
      RepeatType.DAILY -> true
      RepeatType.DAYS_OF_WEEK -> {
        val daysList = habit.repeatDays.split(",").mapNotNull { it.trim().toIntOrNull() }
        daysList.contains(dayOfWeek)
      }
      RepeatType.TIMES_PER_WEEK -> {
        // In X times per week, any day is potentially eligible, but rule applies on weekly aggregation
        true
      }
      RepeatType.EVERY_N_DAYS -> {
        val diff = dateEpochDay - habit.startDateEpochDay
        if (diff < 0) false else (diff % habit.everyNDays.coerceAtLeast(1) == 0L)
      }
      RepeatType.WEEKLY -> {
        dayOfWeek == habit.weeklyDayOfWeek
      }
      RepeatType.MONTHLY -> {
        date.dayOfMonth == habit.monthlyDayOfMonth
      }
    }
  }

  fun isDatePaused(habit: Habit, dateEpochDay: Long): Boolean {
    if (!habit.isPaused && habit.pausedFromEpochDay == null) return false
    val from = habit.pausedFromEpochDay ?: return false
    val until = habit.pausedUntilEpochDay ?: Long.MAX_VALUE
    return dateEpochDay in from..until
  }

  /**
   * Generates full calculations and grid cells for a habit.
   */
  fun calculate(
    habit: Habit,
    marks: List<HabitMark>,
    currentEpochDay: Long = todayEpochDay(),
    weekStartSunday: Boolean = true
  ): HabitCalculationResult {
    val marksMap = marks.associateBy { it.dateEpochDay }
    val startDate = habit.startDateEpochDay

    val totalDuration = if (habit.durationDays > 0) {
      habit.durationDays
    } else {
      ((currentEpochDay - startDate + 1).coerceAtLeast(1)).toInt()
    }

    val gridEndEpochDay = if (habit.durationDays > 0) {
      startDate + habit.durationDays - 1
    } else {
      currentEpochDay.coerceAtLeast(startDate)
    }

    val cells = mutableListOf<HabitGridCell>()
    var plannedMarksCount = 0
    var excludedDaysCount = 0
    var doneMarksCount = 0
    var eligibleElapsedDays = 0

    var currentRunningStreak = 0
    var bestStreak = 0
    var bestStartDay = 1
    var bestEndDay = 1
    var streakStartIndex = 1

    var activeStreak = 0
    var activeStreakStartDay = 1
    var activeStreakEndDay = 1

    // Evaluate each day in period
    for (dayIdx in 0 until totalDuration) {
      val dayEpoch = startDate + dayIdx
      val dayNumber = dayIdx + 1
      val inRule = isDateInRule(habit, dayEpoch, weekStartSunday)
      val paused = isDatePaused(habit, dayEpoch)
      val mark = marksMap[dayEpoch]
      val isToday = (dayEpoch == currentEpochDay)
      val isPast = (dayEpoch < currentEpochDay)
      val isFuture = (dayEpoch > currentEpochDay)

      if (inRule) {
        plannedMarksCount++
      } else {
        excludedDaysCount++
      }

      val state = when {
        !inRule -> GridCellState.OUTSIDE_RULE
        paused -> GridCellState.OUTSIDE_RULE
        mark?.status == HabitMarkStatus.DONE -> {
          doneMarksCount++
          if (dayEpoch <= currentEpochDay) eligibleElapsedDays++
          GridCellState.DONE
        }
        isToday -> {
          if (mark?.status == HabitMarkStatus.DONE) {
            GridCellState.DONE
          } else {
            eligibleElapsedDays++
            GridCellState.TODAY
          }
        }
        isPast -> {
          eligibleElapsedDays++
          GridCellState.FAILED
        }
        else -> GridCellState.TODO
      }

      val label = if (state == GridCellState.OUTSIDE_RULE) "" else "d$dayNumber"
      cells.add(
        HabitGridCell(
          dayNumber = dayNumber,
          dateEpochDay = dayEpoch,
          state = state,
          label = label,
          isToday = isToday
        )
      )
    }

    // Calculate Streaks (ignoring OUTSIDE_RULE and PAUSED days)
    // Best streak and current active streak
    var tempStreak = 0
    var tempStreakStart = 1

    for (dayIdx in 0 until totalDuration) {
      val dayEpoch = startDate + dayIdx
      val dayNumber = dayIdx + 1
      val inRule = isDateInRule(habit, dayEpoch, weekStartSunday)
      val paused = isDatePaused(habit, dayEpoch)
      val mark = marksMap[dayEpoch]
      val isPastOrToday = (dayEpoch <= currentEpochDay)

      if (!isPastOrToday) break

      if (!inRule || paused) {
        // Outside rule / paused day does NOT break or increase the streak count
        continue
      }

      if (mark?.status == HabitMarkStatus.DONE) {
        if (tempStreak == 0) {
          tempStreakStart = dayNumber
        }
        tempStreak++
        if (tempStreak > bestStreak) {
          bestStreak = tempStreak
          bestStartDay = tempStreakStart
          bestEndDay = dayNumber
        }
      } else {
        // Day in rule failed
        tempStreak = 0
      }
    }

    // Active streak calculation (from current backwards)
    var walkingActiveStreak = 0
    var streakEndDay = 1
    var streakStartDay = 1
    var foundFirstDone = false

    for (dayIdx in (totalDuration - 1) downTo 0) {
      val dayEpoch = startDate + dayIdx
      val dayNumber = dayIdx + 1
      val inRule = isDateInRule(habit, dayEpoch, weekStartSunday)
      val paused = isDatePaused(habit, dayEpoch)
      val mark = marksMap[dayEpoch]
      val isPastOrToday = (dayEpoch <= currentEpochDay)

      if (!isPastOrToday) continue

      if (!inRule || paused) {
        continue
      }

      if (mark?.status == HabitMarkStatus.DONE) {
        if (!foundFirstDone) {
          foundFirstDone = true
          streakEndDay = dayNumber
        }
        walkingActiveStreak++
        streakStartDay = dayNumber
      } else if (dayEpoch == currentEpochDay) {
        // Today not done yet - keep checking previous days
        continue
      } else {
        // Broken streak
        break
      }
    }

    activeStreak = if (walkingActiveStreak > 0) walkingActiveStreak else habit.pausedSavedStreak
    activeStreakStartDay = if (walkingActiveStreak > 0) streakStartDay else 1
    activeStreakEndDay = if (walkingActiveStreak > 0) streakEndDay else 1

    val consistency = if (eligibleElapsedDays > 0) {
      ((doneMarksCount.toDouble() / eligibleElapsedDays.toDouble()) * 100).toInt().coerceIn(0, 100)
    } else {
      0
    }

    val currentDayNumber = ((currentEpochDay - startDate + 1).coerceAtLeast(1)).toInt()
    val isDoneToday = marksMap[currentEpochDay]?.status == HabitMarkStatus.DONE
    val isTodayInRule = isDateInRule(habit, currentEpochDay, weekStartSunday)

    return HabitCalculationResult(
      habit = habit,
      currentDayNumber = currentDayNumber,
      totalDays = totalDuration,
      currentStreak = activeStreak,
      bestStreak = bestStreak.coerceAtLeast(activeStreak),
      currentStreakStartDay = activeStreakStartDay,
      currentStreakEndDay = activeStreakEndDay,
      bestStreakStartDay = bestStartDay,
      bestStreakEndDay = bestEndDay,
      consistencyPercent = consistency,
      doneMarksCount = doneMarksCount,
      plannedMarksCount = plannedMarksCount,
      excludedDaysCount = excludedDaysCount,
      isDoneToday = isDoneToday,
      isTodayInRule = isTodayInRule,
      gridCells = cells
    )
  }

  /**
   * Formats day range string for streak (e.g. "d35 → d62")
   */
  fun formatStreakInterval(startDay: Int, endDay: Int): String {
    return "d$startDay → d$endDay"
  }
}
