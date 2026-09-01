package com.example.data.repository

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import com.example.data.model.CalendarEvent
import com.example.data.model.GoogleCalendar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

class CalendarSyncHelper(private val context: Context) {

  fun hasCalendarPermission(): Boolean {
    val readPermission = ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CALENDAR)
    return readPermission == PackageManager.PERMISSION_GRANTED
  }

  suspend fun fetchDeviceCalendars(): List<GoogleCalendar> = withContext(Dispatchers.IO) {
    if (!hasCalendarPermission()) return@withContext emptyList()
    val calendars = mutableListOf<GoogleCalendar>()
    val uri: Uri = CalendarContract.Calendars.CONTENT_URI
    val projection = arrayOf(
      CalendarContract.Calendars._ID,
      CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
      CalendarContract.Calendars.ACCOUNT_NAME,
      CalendarContract.Calendars.CALENDAR_COLOR,
      CalendarContract.Calendars.IS_PRIMARY,
      CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL,
      CalendarContract.Calendars.VISIBLE
    )

    try {
      val cursor: Cursor? = context.contentResolver.query(
        uri,
        projection,
        null,
        null,
        "${CalendarContract.Calendars.IS_PRIMARY} DESC, ${CalendarContract.Calendars.CALENDAR_DISPLAY_NAME} ASC"
      )
      cursor?.use {
        val idCol = it.getColumnIndex(CalendarContract.Calendars._ID)
        val nameCol = it.getColumnIndex(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)
        val accountCol = it.getColumnIndex(CalendarContract.Calendars.ACCOUNT_NAME)
        val colorCol = it.getColumnIndex(CalendarContract.Calendars.CALENDAR_COLOR)
        val primaryCol = it.getColumnIndex(CalendarContract.Calendars.IS_PRIMARY)
        val accessCol = it.getColumnIndex(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL)

        while (it.moveToNext()) {
          val id = if (idCol >= 0) it.getLong(idCol).toString() else ""
          if (id.isEmpty()) continue
          val name = if (nameCol >= 0) it.getString(nameCol) ?: "Google Agenda" else "Google Agenda"
          val account = if (accountCol >= 0) it.getString(accountCol) ?: "" else ""
          val colorInt = if (colorCol >= 0) it.getInt(colorCol) else 0
          val colorHex = if (colorInt != 0) String.format("#%06X", 0xFFFFFF and colorInt) else "#EC3013"
          val isPrimary = if (primaryCol >= 0) it.getInt(primaryCol) == 1 else false
          val accessLevel = if (accessCol >= 0) it.getInt(accessCol) else CalendarContract.Calendars.CAL_ACCESS_OWNER
          val isReadOnly = accessLevel < CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR

          calendars.add(
            GoogleCalendar(
              id = id,
              name = name,
              accountEmail = account.ifBlank { "Google Calendar" },
              colorHex = colorHex,
              isPrimary = isPrimary,
              isReadOnly = isReadOnly,
              isSelected = true
            )
          )
        }
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
    calendars
  }

  suspend fun fetchDeviceEvents(): List<CalendarEvent> = withContext(Dispatchers.IO) {
    if (!hasCalendarPermission()) return@withContext emptyList()
    val events = mutableListOf<CalendarEvent>()

    val now = Calendar.getInstance()
    now.add(Calendar.MONTH, -6)
    val startWindow = now.timeInMillis
    now.add(Calendar.MONTH, 18)
    val endWindow = now.timeInMillis

    // Query Instances.CONTENT_URI for full instance expansion of recurring & single events across all calendars
    val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
    ContentUris.appendId(builder, startWindow)
    ContentUris.appendId(builder, endWindow)
    val instancesUri = builder.build()

    val projection = arrayOf(
      CalendarContract.Instances._ID,
      CalendarContract.Instances.EVENT_ID,
      CalendarContract.Instances.CALENDAR_ID,
      CalendarContract.Instances.TITLE,
      CalendarContract.Instances.BEGIN,
      CalendarContract.Instances.END,
      CalendarContract.Instances.ALL_DAY,
      CalendarContract.Instances.EVENT_LOCATION,
      CalendarContract.Instances.DESCRIPTION
    )

    try {
      val cursor: Cursor? = context.contentResolver.query(
        instancesUri,
        projection,
        null,
        null,
        "${CalendarContract.Instances.BEGIN} ASC"
      )

      cursor?.use {
        val idCol = it.getColumnIndex(CalendarContract.Instances._ID)
        val eventIdCol = it.getColumnIndex(CalendarContract.Instances.EVENT_ID)
        val calIdCol = it.getColumnIndex(CalendarContract.Instances.CALENDAR_ID)
        val titleCol = it.getColumnIndex(CalendarContract.Instances.TITLE)
        val startCol = it.getColumnIndex(CalendarContract.Instances.BEGIN)
        val endCol = it.getColumnIndex(CalendarContract.Instances.END)
        val allDayCol = it.getColumnIndex(CalendarContract.Instances.ALL_DAY)
        val locCol = it.getColumnIndex(CalendarContract.Instances.EVENT_LOCATION)
        val descCol = it.getColumnIndex(CalendarContract.Instances.DESCRIPTION)

        while (it.moveToNext()) {
          val instanceId = if (idCol >= 0) it.getLong(idCol).toString() else ""
          val eventId = if (eventIdCol >= 0) it.getLong(eventIdCol).toString() else instanceId
          val calId = if (calIdCol >= 0) it.getLong(calIdCol).toString() else ""
          val title = if (titleCol >= 0) it.getString(titleCol) ?: "Sem título" else "Sem título"
          val start = if (startCol >= 0) it.getLong(startCol) else 0L
          val end = if (endCol >= 0 && !it.isNull(endCol)) it.getLong(endCol) else (start + 3600000)
          val allDay = if (allDayCol >= 0) it.getInt(allDayCol) == 1 else false
          val location = if (locCol >= 0) it.getString(locCol) else null
          val desc = if (descCol >= 0) it.getString(descCol) else null

          if (instanceId.isNotEmpty()) {
            events.add(
              CalendarEvent(
                id = "google_${calId}_${instanceId}",
                calendarId = calId,
                title = title,
                startEpochMillis = start,
                endEpochMillis = end,
                isAllDay = allDay,
                location = location,
                attachedNoteId = null,
                attachedNoteTitle = null,
                isLocalOnly = false,
                isPendingSync = false
              )
            )
          }
        }
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }

    // Fallback if Instances query returned nothing
    if (events.isEmpty()) {
      try {
        val eventsUri = CalendarContract.Events.CONTENT_URI
        val eventsProjection = arrayOf(
          CalendarContract.Events._ID,
          CalendarContract.Events.CALENDAR_ID,
          CalendarContract.Events.TITLE,
          CalendarContract.Events.DTSTART,
          CalendarContract.Events.DTEND,
          CalendarContract.Events.ALL_DAY,
          CalendarContract.Events.EVENT_LOCATION,
          CalendarContract.Events.DESCRIPTION
        )
        val selection = "(${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTSTART} <= ?)"
        val selectionArgs = arrayOf(startWindow.toString(), endWindow.toString())

        val cursor: Cursor? = context.contentResolver.query(
          eventsUri,
          eventsProjection,
          selection,
          selectionArgs,
          "${CalendarContract.Events.DTSTART} ASC"
        )
        cursor?.use {
          val idCol = it.getColumnIndex(CalendarContract.Events._ID)
          val calIdCol = it.getColumnIndex(CalendarContract.Events.CALENDAR_ID)
          val titleCol = it.getColumnIndex(CalendarContract.Events.TITLE)
          val startCol = it.getColumnIndex(CalendarContract.Events.DTSTART)
          val endCol = it.getColumnIndex(CalendarContract.Events.DTEND)
          val allDayCol = it.getColumnIndex(CalendarContract.Events.ALL_DAY)
          val locCol = it.getColumnIndex(CalendarContract.Events.EVENT_LOCATION)
          val descCol = it.getColumnIndex(CalendarContract.Events.DESCRIPTION)

          while (it.moveToNext()) {
            val id = if (idCol >= 0) it.getLong(idCol).toString() else ""
            val calId = if (calIdCol >= 0) it.getLong(calIdCol).toString() else ""
            val title = if (titleCol >= 0) it.getString(titleCol) ?: "Sem título" else "Sem título"
            val start = if (startCol >= 0) it.getLong(startCol) else 0L
            val end = if (endCol >= 0 && !it.isNull(endCol)) it.getLong(endCol) else (start + 3600000)
            val allDay = if (allDayCol >= 0) it.getInt(allDayCol) == 1 else false
            val location = if (locCol >= 0) it.getString(locCol) else null
            val desc = if (descCol >= 0) it.getString(descCol) else null

            if (id.isNotEmpty()) {
              events.add(
                CalendarEvent(
                  id = "google_${calId}_$id",
                  calendarId = calId,
                  title = title,
                  startEpochMillis = start,
                  endEpochMillis = end,
                  isAllDay = allDay,
                  location = location,
                  attachedNoteId = null,
                  attachedNoteTitle = null,
                  isLocalOnly = false,
                  isPendingSync = false
                )
              )
            }
          }
        }
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }

    events
  }

  suspend fun insertEventToDevice(
    calendarId: Long,
    title: String,
    startMillis: Long,
    endMillis: Long,
    isAllDay: Boolean,
    location: String? = null,
    description: String? = null
  ): Long? = withContext(Dispatchers.IO) {
    val checkWrite = ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_CALENDAR)
    if (checkWrite != PackageManager.PERMISSION_GRANTED) return@withContext null

    try {
      val values = ContentValues().apply {
        put(CalendarContract.Events.CALENDAR_ID, calendarId)
        put(CalendarContract.Events.TITLE, title)
        put(CalendarContract.Events.DTSTART, startMillis)
        put(CalendarContract.Events.DTEND, endMillis)
        put(CalendarContract.Events.ALL_DAY, if (isAllDay) 1 else 0)
        put(CalendarContract.Events.EVENT_TIMEZONE, java.util.TimeZone.getDefault().id)
        if (!location.isNullOrBlank()) put(CalendarContract.Events.EVENT_LOCATION, location)
        if (!description.isNullOrBlank()) put(CalendarContract.Events.DESCRIPTION, description)
      }
      val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
      uri?.lastPathSegment?.toLongOrNull()
    } catch (e: Exception) {
      e.printStackTrace()
      null
    }
  }
}
