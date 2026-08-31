package com.example.data.repository

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
      CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL
    )

    try {
      val cursor: Cursor? = context.contentResolver.query(
        uri,
        projection,
        null,
        null,
        null
      )
      cursor?.use {
        val idCol = it.getColumnIndex(CalendarContract.Calendars._ID)
        val nameCol = it.getColumnIndex(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)
        val accountCol = it.getColumnIndex(CalendarContract.Calendars.ACCOUNT_NAME)
        val colorCol = it.getColumnIndex(CalendarContract.Calendars.CALENDAR_COLOR)
        val primaryCol = it.getColumnIndex(CalendarContract.Calendars.IS_PRIMARY)
        val accessCol = it.getColumnIndex(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL)

        while (it.moveToNext()) {
          val id = if (idCol >= 0) it.getLong(idCol).toString() else System.currentTimeMillis().toString()
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
              accountEmail = account,
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
    now.add(Calendar.MONTH, -3)
    val startWindow = now.timeInMillis
    now.add(Calendar.MONTH, 12)
    val endWindow = now.timeInMillis

    val uri: Uri = CalendarContract.Events.CONTENT_URI
    val projection = arrayOf(
      CalendarContract.Events._ID,
      CalendarContract.Events.CALENDAR_ID,
      CalendarContract.Events.TITLE,
      CalendarContract.Events.DTSTART,
      CalendarContract.Events.DTEND,
      CalendarContract.Events.ALL_DAY,
      CalendarContract.Events.EVENT_LOCATION,
      CalendarContract.Events.DESCRIPTION
    )

    try {
      val selection = "(${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTSTART} <= ?)"
      val selectionArgs = arrayOf(startWindow.toString(), endWindow.toString())

      val cursor: Cursor? = context.contentResolver.query(
        uri,
        projection,
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

        while (it.moveToNext()) {
          val id = if (idCol >= 0) it.getLong(idCol).toString() else ""
          val calId = if (calIdCol >= 0) it.getLong(calIdCol).toString() else ""
          val title = if (titleCol >= 0) it.getString(titleCol) ?: "Sem título" else "Sem título"
          val start = if (startCol >= 0) it.getLong(startCol) else 0L
          val end = if (endCol >= 0 && !it.isNull(endCol)) it.getLong(endCol) else (start + 3600000)
          val allDay = if (allDayCol >= 0) it.getInt(allDayCol) == 1 else false
          val location = if (locCol >= 0) it.getString(locCol) else null

          if (id.isNotEmpty()) {
            events.add(
              CalendarEvent(
                id = "google_$id",
                calendarId = calId,
                title = title,
                startEpochMillis = start,
                endEpochMillis = end,
                isAllDay = allDay,
                location = location,
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
