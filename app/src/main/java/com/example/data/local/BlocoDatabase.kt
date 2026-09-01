package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.model.Bill
import com.example.data.model.BillPayment
import com.example.data.model.CalendarEvent
import com.example.data.model.Category
import com.example.data.model.GoogleCalendar
import com.example.data.model.Habit
import com.example.data.model.HabitMark
import com.example.data.model.Note
import com.example.data.model.NoteItem
import com.example.data.model.SyncQueueItem

@Database(
  entities = [
    Category::class,
    Note::class,
    NoteItem::class,
    Habit::class,
    HabitMark::class,
    GoogleCalendar::class,
    CalendarEvent::class,
    SyncQueueItem::class,
    Bill::class,
    BillPayment::class
  ],
  version = 2,
  exportSchema = false
)
@TypeConverters(Converters::class)
abstract class BlocoDatabase : RoomDatabase() {
  abstract fun noteDao(): NoteDao
  abstract fun habitDao(): HabitDao
  abstract fun calendarDao(): CalendarDao
  abstract fun syncQueueDao(): SyncQueueDao
  abstract fun billDao(): BillDao

  companion object {
    @Volatile
    private var INSTANCE: BlocoDatabase? = null

    fun getDatabase(context: Context): BlocoDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          BlocoDatabase::class.java,
          "bloco_database"
        ).fallbackToDestructiveMigration().build()
        INSTANCE = instance
        instance
      }
    }
  }
}
