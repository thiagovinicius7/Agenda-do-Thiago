package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "google_calendars")
data class GoogleCalendar(
  @PrimaryKey val id: String,
  val name: String,
  val accountEmail: String,
  val colorHex: String,
  val isPrimary: Boolean = false,
  val isReadOnly: Boolean = false,
  val isSelected: Boolean = true
)

@Entity(tableName = "calendar_events")
data class CalendarEvent(
  @PrimaryKey val id: String,
  val calendarId: String,
  val title: String,
  val startEpochMillis: Long,
  val endEpochMillis: Long,
  val isAllDay: Boolean = false,
  val location: String? = null,
  val attachedNoteId: String? = null,
  val attachedNoteTitle: String? = null,
  val isLocalOnly: Boolean = false,
  val isPendingSync: Boolean = false,
  val syncAction: String = "NONE", // NONE, CREATE, UPDATE, DELETE
  val syncStatusMessage: String? = null
)

@Entity(tableName = "sync_queue")
data class SyncQueueItem(
  @PrimaryKey val id: String,
  val eventId: String,
  val title: String,
  val targetCalendarId: String,
  val actionDescription: String,
  val timeInfo: String,
  val createdAt: Long = System.currentTimeMillis()
)
