package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Habit
import com.example.data.model.HabitMark
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
  @Query("SELECT * FROM habits WHERE isArchived = 0 ORDER BY createdAt ASC")
  fun getActiveHabits(): Flow<List<Habit>>

  @Query("SELECT * FROM habits WHERE isArchived = 1 ORDER BY createdAt ASC")
  fun getArchivedHabits(): Flow<List<Habit>>

  @Query("SELECT * FROM habits WHERE id = :id")
  suspend fun getHabitById(id: String): Habit?

  @Query("SELECT * FROM habits WHERE id = :id")
  fun observeHabitById(id: String): Flow<Habit?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertHabit(habit: Habit)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertHabits(habits: List<Habit>)

  @Update
  suspend fun updateHabit(habit: Habit)

  @Query("DELETE FROM habits WHERE id = :id")
  suspend fun deleteHabit(id: String)

  @Query("SELECT * FROM habit_marks WHERE habitId = :habitId ORDER BY dateEpochDay ASC")
  fun getMarksForHabit(habitId: String): Flow<List<HabitMark>>

  @Query("SELECT * FROM habit_marks")
  fun getAllHabitMarks(): Flow<List<HabitMark>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMark(mark: HabitMark)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMarks(marks: List<HabitMark>)

  @Query("DELETE FROM habit_marks WHERE habitId = :habitId AND dateEpochDay = :dateEpochDay")
  suspend fun deleteMark(habitId: String, dateEpochDay: Long)

  @Query("SELECT * FROM habits WHERE name LIKE '%' || :query || '%'")
  fun searchHabits(query: String): Flow<List<Habit>>
}
