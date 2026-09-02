package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class RepeatType {
  DAILY,
  DAYS_OF_WEEK,
  TIMES_PER_WEEK,
  EVERY_N_DAYS,
  WEEKLY,
  MONTHLY,
  MONTH_DAYS_RANGE
}

enum class HabitMarkStatus {
  DONE,
  FAILED,
  PAUSED
}

@Entity(tableName = "habits")
data class Habit(
  @PrimaryKey val id: String,
  val name: String,
  val repeatType: RepeatType = RepeatType.DAILY,
  // For DAYS_OF_WEEK: comma-separated days (1=Mon..7=Sun)
  val repeatDays: String = "1,2,3,4,5,6", // Mon-Sat by default
  val timesPerWeek: Int = 3,
  val everyNDays: Int = 2,
  val weeklyDayOfWeek: Int = 1, // 1=Mon..7=Sun
  val monthlyDayOfMonth: Int = 1, // 1..31
  val monthDayStart: Int = 9, // ex: Novena (dia 9 ao 17 de cada mês)
  val monthDayEnd: Int = 17,
  val durationDays: Int = 0, // 0 = sem fim, 40 = 40 dias, 150 = 150 dias
  val startDateEpochDay: Long = 0L, // LocalDate.toEpochDay()
  val showInCalendar: Boolean = true,
  val reminderTime: String = "06:30", // e.g. "06:30"
  val reminderEnabled: Boolean = true,
  val pauseAllowed: Boolean = true,
  val isPaused: Boolean = false,
  val pausedFromEpochDay: Long? = null,
  val pausedUntilEpochDay: Long? = null,
  val pausedSavedStreak: Int = 0,
  val isArchived: Boolean = false,
  val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "habit_marks", primaryKeys = ["habitId", "dateEpochDay"])
data class HabitMark(
  val habitId: String,
  val dateEpochDay: Long,
  val status: HabitMarkStatus = HabitMarkStatus.DONE
)

enum class GridCellState {
  DONE,         // Feito: Solid Red #EC3013, white text
  FAILED,       // Falhou: Neutral #9B9797 (light) / #6B6767 (dark)
  TODAY,        // Hoje: Transparent, 1.5px solid border, bold number
  TODO,         // A fazer: Transparent, 1px outline
  OUTSIDE_RULE  // Fora da regra: 45deg diagonal hatch lines
}

data class HabitGridCell(
  val dayNumber: Int,         // e.g. 1..150 (d1..d150)
  val dateEpochDay: Long,
  val state: GridCellState,
  val label: String,          // e.g. "d1", "d62", or "" for outside rule
  val isToday: Boolean = false
)

data class HabitCalculationResult(
  val habit: Habit,
  val currentDayNumber: Int,        // e.g. 62
  val totalDays: Int,               // e.g. 150 or elapsed
  val currentStreak: Int,           // e.g. 28
  val bestStreak: Int,              // e.g. 47
  val currentStreakStartDay: Int,   // e.g. 35
  val currentStreakEndDay: Int,     // e.g. 62
  val bestStreakStartDay: Int,      // e.g. 3
  val bestStreakEndDay: Int,        // e.g. 41
  val consistencyPercent: Int,      // e.g. 86
  val doneMarksCount: Int,          // e.g. 61 or 121
  val plannedMarksCount: Int,       // e.g. 129
  val excludedDaysCount: Int,       // e.g. 21
  val isDoneToday: Boolean,
  val isTodayInRule: Boolean,
  val gridCells: List<HabitGridCell>
)
