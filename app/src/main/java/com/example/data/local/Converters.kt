package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.HabitMarkStatus
import com.example.data.model.NoteFormat
import com.example.data.model.RepeatType

class Converters {
  @TypeConverter
  fun fromNoteFormat(format: NoteFormat): String = format.name

  @TypeConverter
  fun toNoteFormat(value: String): NoteFormat = try {
    NoteFormat.valueOf(value)
  } catch (e: Exception) {
    NoteFormat.NOTE
  }

  @TypeConverter
  fun fromRepeatType(type: RepeatType): String = type.name

  @TypeConverter
  fun toRepeatType(value: String): RepeatType = try {
    RepeatType.valueOf(value)
  } catch (e: Exception) {
    RepeatType.DAILY
  }

  @TypeConverter
  fun fromHabitMarkStatus(status: HabitMarkStatus): String = status.name

  @TypeConverter
  fun toHabitMarkStatus(value: String): HabitMarkStatus = try {
    HabitMarkStatus.valueOf(value)
  } catch (e: Exception) {
    HabitMarkStatus.DONE
  }
}
