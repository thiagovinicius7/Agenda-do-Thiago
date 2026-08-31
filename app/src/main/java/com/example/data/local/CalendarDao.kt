package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CalendarEvent
import com.example.data.model.GoogleCalendar
import com.example.data.model.SyncQueueItem
import kotlinx.coroutines.flow.Flow

@Dao
interface CalendarDao {
  @Query("SELECT * FROM google_calendars")
  fun getAllCalendars(): Flow<List<GoogleCalendar>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCalendars(calendars: List<GoogleCalendar>)

  @Update
  suspend fun updateCalendar(calendar: GoogleCalendar)

  @Query("SELECT * FROM calendar_events ORDER BY startEpochMillis ASC")
  fun getAllEvents(): Flow<List<CalendarEvent>>

  @Query("SELECT * FROM calendar_events WHERE startEpochMillis >= :startMillis AND endEpochMillis <= :endMillis ORDER BY startEpochMillis ASC")
  fun getEventsInRange(startMillis: Long, endMillis: Long): Flow<List<CalendarEvent>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertEvent(event: CalendarEvent)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertEvents(events: List<CalendarEvent>)

  @Update
  suspend fun updateEvent(event: CalendarEvent)

  @Query("DELETE FROM calendar_events WHERE id = :id")
  suspend fun deleteEvent(id: String)

  @Query("SELECT * FROM calendar_events WHERE title LIKE '%' || :query || '%'")
  fun searchEvents(query: String): Flow<List<CalendarEvent>>
}

@Dao
interface SyncQueueDao {
  @Query("SELECT * FROM sync_queue ORDER BY createdAt ASC")
  fun getAllQueueItems(): Flow<List<SyncQueueItem>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertQueueItem(item: SyncQueueItem)

  @Query("DELETE FROM sync_queue WHERE id = :id")
  suspend fun deleteQueueItem(id: String)

  @Query("DELETE FROM sync_queue")
  suspend fun clearQueue()
}
