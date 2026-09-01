package com.example.data.repository

import com.example.data.local.CalendarDao
import com.example.data.local.HabitDao
import com.example.data.local.NoteDao
import com.example.data.local.SyncQueueDao
import com.example.data.model.CalendarEvent
import com.example.data.model.Category
import com.example.data.model.GoogleCalendar
import com.example.data.model.Habit
import com.example.data.model.HabitCalculationResult
import com.example.data.model.HabitMark
import com.example.data.model.HabitMarkStatus
import com.example.data.model.Note
import com.example.data.model.NoteFormat
import com.example.data.model.NoteItem
import com.example.data.model.NoteWithItems
import com.example.data.model.RepeatType
import com.example.data.model.SyncQueueItem
import com.example.util.HabitCalculations
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate

class BlocoRepository(
  private val noteDao: NoteDao,
  private val habitDao: HabitDao,
  private val calendarDao: CalendarDao,
  private val syncQueueDao: SyncQueueDao,
  private val calendarSyncHelper: CalendarSyncHelper? = null
) {

  val categories: Flow<List<Category>> = noteDao.getAllCategories()
  val activeNotes: Flow<List<Note>> = noteDao.getActiveNotes()
  val archivedNotes: Flow<List<Note>> = noteDao.getArchivedNotes()
  val allNoteItems: Flow<List<NoteItem>> = noteDao.getAllNoteItems()

  val notesWithItems: Flow<List<NoteWithItems>> = combine(
    activeNotes,
    allNoteItems,
    categories
  ) { notes, items, cats ->
    val itemsByNote = items.groupBy { it.noteId }
    val catMap = cats.associateBy { it.id }
    notes.map { note ->
      NoteWithItems(
        note = note,
        items = itemsByNote[note.id] ?: emptyList(),
        category = catMap[note.categoryId]
      )
    }
  }

  val activeHabits: Flow<List<Habit>> = habitDao.getActiveHabits()
  val archivedHabits: Flow<List<Habit>> = habitDao.getArchivedHabits()
  val allHabitMarks: Flow<List<HabitMark>> = habitDao.getAllHabitMarks()

  val habitsWithCalculations: Flow<List<HabitCalculationResult>> = combine(
    activeHabits,
    allHabitMarks
  ) { habits, marks ->
    val marksByHabit = marks.groupBy { it.habitId }
    habits.map { habit ->
      HabitCalculations.calculate(
        habit = habit,
        marks = marksByHabit[habit.id] ?: emptyList()
      )
    }
  }

  val calendars: Flow<List<GoogleCalendar>> = calendarDao.getAllCalendars()
  val allEvents: Flow<List<CalendarEvent>> = calendarDao.getAllEvents()
  val events: Flow<List<CalendarEvent>> = combine(
    allEvents,
    calendars
  ) { eventList, calList ->
    if (calList.isEmpty()) {
      eventList
    } else {
      val selectedIds = calList.filter { it.isSelected }.map { it.id }.toSet()
      // Show events belonging to selected calendars, or local events without calId
      eventList.filter { it.calendarId.isEmpty() || it.calendarId in selectedIds }
    }
  }
  val syncQueue: Flow<List<SyncQueueItem>> = syncQueueDao.getAllQueueItems()

  // Note actions
  suspend fun insertNote(note: Note, items: List<NoteItem> = emptyList()) {
    noteDao.insertNote(note)
    if (items.isNotEmpty()) {
      noteDao.insertNoteItems(items)
    }
  }

  suspend fun updateNote(note: Note) = noteDao.updateNote(note)
  suspend fun deleteNote(id: String) {
    noteDao.deleteNoteById(id)
    noteDao.deleteItemsForNote(id)
  }

  suspend fun toggleNoteItem(itemId: String, currentDone: Boolean) {
    val items = allNoteItems.first()
    val item = items.find { it.id == itemId } ?: return
    noteDao.updateNoteItem(item.copy(isDone = !currentDone))
  }

  suspend fun insertNoteItem(item: NoteItem) = noteDao.insertNoteItem(item)
  suspend fun deleteNoteItem(itemId: String) = noteDao.deleteNoteItem(itemId)

  // Habit actions
  suspend fun insertHabit(habit: Habit) = habitDao.insertHabit(habit)
  suspend fun updateHabit(habit: Habit) = habitDao.updateHabit(habit)
  suspend fun deleteHabit(habitId: String) {
    habitDao.deleteHabit(habitId)
    habitDao.deleteMarksForHabit(habitId)
  }

  suspend fun toggleHabitDay(habitId: String, dateEpochDay: Long = HabitCalculations.todayEpochDay()) {
    val marks = allHabitMarks.first()
    val mark = marks.find { it.habitId == habitId && it.dateEpochDay == dateEpochDay }
    if (mark != null) {
      habitDao.deleteMark(habitId, dateEpochDay)
    } else {
      habitDao.insertMark(HabitMark(habitId = habitId, dateEpochDay = dateEpochDay, status = HabitMarkStatus.DONE))
    }
  }

  suspend fun markPastHabitDays(habitId: String, fromEpochDay: Long, toEpochDay: Long, markAsDone: Boolean = true) {
    val habits = activeHabits.first()
    val habit = habits.find { it.id == habitId } ?: return
    val currentMarks = allHabitMarks.first().filter { it.habitId == habitId }.associateBy { it.dateEpochDay }
    val newMarks = mutableListOf<HabitMark>()

    for (epochDay in fromEpochDay..toEpochDay) {
      if (HabitCalculations.isDateInRule(habit, epochDay)) {
        if (markAsDone) {
          if (!currentMarks.containsKey(epochDay)) {
            newMarks.add(HabitMark(habitId = habitId, dateEpochDay = epochDay, status = HabitMarkStatus.DONE))
          }
        } else {
          habitDao.deleteMark(habitId, epochDay)
        }
      }
    }
    if (newMarks.isNotEmpty()) {
      habitDao.insertMarks(newMarks)
    }
  }

  // Calendar actions
  suspend fun insertEvent(event: CalendarEvent) {
    calendarDao.insertEvent(event)
    val helper = calendarSyncHelper
    if (helper != null && helper.hasCalendarPermission()) {
      val calIdLong = event.calendarId.toLongOrNull() ?: 1L
      helper.insertEventToDevice(
        calendarId = calIdLong,
        title = event.title,
        startMillis = event.startEpochMillis,
        endMillis = event.endEpochMillis,
        isAllDay = event.isAllDay,
        location = event.location,
        description = event.attachedNoteTitle
      )
    } else if (event.isPendingSync) {
      syncQueueDao.insertQueueItem(
        SyncQueueItem(
          id = "sync_${System.currentTimeMillis()}",
          eventId = event.id,
          title = event.title,
          targetCalendarId = event.calendarId,
          actionDescription = "Criado offline",
          timeInfo = "${LocalDate.now()} · criado offline"
        )
      )
    }
  }

  suspend fun updateEvent(event: CalendarEvent) {
    calendarDao.updateEvent(event)
  }

  suspend fun deleteEvent(eventId: String) {
    calendarDao.deleteEvent(eventId)
    syncQueueDao.deleteQueueItem("sync_$eventId")
  }

  suspend fun updateCalendarSelection(calendarId: String, isSelected: Boolean) {
    val currentCalendars = calendars.first()
    val cal = currentCalendars.find { it.id == calendarId } ?: return
    calendarDao.updateCalendar(cal.copy(isSelected = isSelected))
  }

  suspend fun toggleCalendarSelection(calendarId: String) {
    val currentCalendars = calendars.first()
    val cal = currentCalendars.find { it.id == calendarId } ?: return
    calendarDao.updateCalendar(cal.copy(isSelected = !cal.isSelected))
  }

  suspend fun toggleAccountSelection(accountEmail: String, isSelected: Boolean) {
    val currentCalendars = calendars.first()
    val updated = currentCalendars.map {
      if (it.accountEmail == accountEmail) it.copy(isSelected = isSelected) else it
    }
    calendarDao.insertCalendars(updated)
  }

  suspend fun selectAllCalendars(isSelected: Boolean) {
    val currentCalendars = calendars.first()
    val updated = currentCalendars.map { it.copy(isSelected = isSelected) }
    calendarDao.insertCalendars(updated)
  }

  suspend fun clearSyncQueue() {
    syncQueueDao.clearQueue()
  }

  suspend fun clearAllPreloadedData() {
    noteDao.clearAllNotes()
    noteDao.clearAllNoteItems()
    habitDao.clearAllHabits()
    habitDao.clearAllHabitMarks()
    calendarDao.clearAllEvents()
    calendarDao.clearAllCalendars()
    syncQueueDao.clearQueue()
  }

  suspend fun syncWithDeviceCalendar(userEmail: String? = null): Int {
    val email = userEmail ?: "thiagovinicius7@gmail.com"
    val helper = calendarSyncHelper

    // Purge any older mock events
    calendarDao.deleteMockEvents()

    val deviceCalendars = helper?.fetchDeviceCalendars() ?: emptyList()
    if (deviceCalendars.isNotEmpty()) {
      val existingCalendars = calendars.first().associateBy { it.id }
      val mergedCalendars = deviceCalendars.map { devCal ->
        val existing = existingCalendars[devCal.id]
        if (existing != null) {
          devCal.copy(isSelected = existing.isSelected)
        } else {
          devCal.copy(isSelected = true)
        }
      }
      calendarDao.insertCalendars(mergedCalendars)
    } else {
      val existing = calendars.first()
      if (existing.isEmpty()) {
        calendarDao.insertCalendars(
          listOf(
            GoogleCalendar(
              id = "cal_pessoal",
              name = "Minha Agenda",
              accountEmail = email,
              colorHex = "#EC3013",
              isPrimary = true,
              isSelected = true
            )
          )
        )
      }
    }

    val deviceEvents = helper?.fetchDeviceEvents() ?: emptyList()
    if (deviceEvents.isNotEmpty()) {
      val existingEvents = allEvents.first().associateBy { it.id }
      val mergedEvents = deviceEvents.map { devEv ->
        val existing = existingEvents[devEv.id]
        if (existing != null && !existing.attachedNoteId.isNullOrBlank()) {
          devEv.copy(
            attachedNoteId = existing.attachedNoteId,
            attachedNoteTitle = existing.attachedNoteTitle
          )
        } else {
          devEv.copy(
            attachedNoteId = null,
            attachedNoteTitle = null
          )
        }
      }
      calendarDao.insertEvents(mergedEvents)
      calendarDao.clearOrphanedNoteTitles()
      calendarDao.clearEmptyNoteTitles()
      return deviceEvents.size
    } else {
      calendarDao.clearOrphanedNoteTitles()
      calendarDao.clearEmptyNoteTitles()
      return events.first().size
    }
  }

  suspend fun ensureNoteForEvent(eventId: String): String {
    val allEvs = allEvents.first()
    val event = allEvs.find { it.id == eventId }

    if (event != null && !event.attachedNoteId.isNullOrBlank()) {
      val existingNote = noteDao.getNoteById(event.attachedNoteId)
      if (existingNote != null) {
        return existingNote.id
      }
    }

    // If event exists or user tapped an event with a description or note
    val noteId = "note_ev_${System.currentTimeMillis()}"
    val noteTitle = event?.title?.let { "Post-it: $it" } ?: "Post-it do compromisso"
    val noteBody = event?.location?.let { "Local: $it" } ?: ""

    val newNote = Note(
      id = noteId,
      title = noteTitle,
      body = noteBody,
      categoryId = "trabalho",
      format = NoteFormat.NOTE,
      attachedEventId = event?.id,
      attachedEventSummary = event?.title,
      attachedDate = event?.let {
        val zdt = java.time.Instant.ofEpochMilli(it.startEpochMillis).atZone(java.time.ZoneId.systemDefault())
        zdt.format(java.time.format.DateTimeFormatter.ofPattern("EEE, d MMM · HH:mm", java.util.Locale("pt", "BR")))
      },
      updatedAt = System.currentTimeMillis()
    )

    noteDao.insertNote(newNote)

    if (event != null) {
      calendarDao.updateEvent(
        event.copy(
          attachedNoteId = noteId,
          attachedNoteTitle = noteTitle
        )
      )
    }

    return noteId
  }

  suspend fun createBackupJson(): String {
    val cats = categories.first()
    val notes = activeNotes.first()
    val items = allNoteItems.first()
    val habits = activeHabits.first()
    val marks = allHabitMarks.first()
    val evs = allEvents.first()
    val cals = calendars.first()

    val root = org.json.JSONObject()
    root.put("version", 1)
    root.put("timestamp", System.currentTimeMillis())
    root.put("appName", "Bloco T")

    // Categories
    val catsArr = org.json.JSONArray()
    cats.forEach { c ->
      val obj = org.json.JSONObject()
      obj.put("id", c.id)
      obj.put("name", c.name)
      obj.put("colorHex", c.colorHex)
      obj.put("order", c.order)
      catsArr.put(obj)
    }
    root.put("categories", catsArr)

    // Notes
    val notesArr = org.json.JSONArray()
    notes.forEach { n ->
      val obj = org.json.JSONObject()
      obj.put("id", n.id)
      obj.put("title", n.title)
      obj.put("body", n.body)
      obj.put("categoryId", n.categoryId)
      obj.put("format", n.format.name)
      obj.put("isPinned", n.isPinned)
      obj.put("isArchived", n.isArchived)
      obj.put("attachedEventId", n.attachedEventId ?: "")
      obj.put("attachedEventSummary", n.attachedEventSummary ?: "")
      obj.put("attachedDate", n.attachedDate ?: "")
      obj.put("createdAt", n.createdAt)
      obj.put("updatedAt", n.updatedAt)
      notesArr.put(obj)
    }
    root.put("notes", notesArr)

    // Note Items
    val itemsArr = org.json.JSONArray()
    items.forEach { itm ->
      val obj = org.json.JSONObject()
      obj.put("id", itm.id)
      obj.put("noteId", itm.noteId)
      obj.put("text", itm.text)
      obj.put("isDone", itm.isDone)
      obj.put("orderIndex", itm.orderIndex)
      itemsArr.put(obj)
    }
    root.put("noteItems", itemsArr)

    // Habits
    val habitsArr = org.json.JSONArray()
    habits.forEach { h ->
      val obj = org.json.JSONObject()
      obj.put("id", h.id)
      obj.put("name", h.name)
      obj.put("repeatType", h.repeatType.name)
      obj.put("repeatDays", h.repeatDays)
      obj.put("durationDays", h.durationDays)
      obj.put("startDateEpochDay", h.startDateEpochDay)
      obj.put("isArchived", h.isArchived)
      obj.put("reminderTime", h.reminderTime)
      obj.put("showInCalendar", h.showInCalendar)
      obj.put("pausedSavedStreak", h.pausedSavedStreak)
      obj.put("createdAt", h.createdAt)
      habitsArr.put(obj)
    }
    root.put("habits", habitsArr)

    // Habit Marks
    val marksArr = org.json.JSONArray()
    marks.forEach { m ->
      val obj = org.json.JSONObject()
      obj.put("habitId", m.habitId)
      obj.put("dateEpochDay", m.dateEpochDay)
      obj.put("status", m.status.name)
      marksArr.put(obj)
    }
    root.put("habitMarks", marksArr)

    // Events
    val eventsArr = org.json.JSONArray()
    evs.forEach { ev ->
      val obj = org.json.JSONObject()
      obj.put("id", ev.id)
      obj.put("calendarId", ev.calendarId)
      obj.put("title", ev.title)
      obj.put("startEpochMillis", ev.startEpochMillis)
      obj.put("endEpochMillis", ev.endEpochMillis)
      obj.put("isAllDay", ev.isAllDay)
      obj.put("location", ev.location ?: "")
      obj.put("attachedNoteId", ev.attachedNoteId ?: "")
      obj.put("attachedNoteTitle", ev.attachedNoteTitle ?: "")
      obj.put("isLocalOnly", ev.isLocalOnly)
      eventsArr.put(obj)
    }
    root.put("events", eventsArr)

    // Calendars
    val calsArr = org.json.JSONArray()
    cals.forEach { cal ->
      val obj = org.json.JSONObject()
      obj.put("id", cal.id)
      obj.put("name", cal.name)
      obj.put("accountEmail", cal.accountEmail)
      obj.put("colorHex", cal.colorHex)
      obj.put("isPrimary", cal.isPrimary)
      obj.put("isReadOnly", cal.isReadOnly)
      obj.put("isSelected", cal.isSelected)
      calsArr.put(obj)
    }
    root.put("calendars", calsArr)

    return root.toString(2)
  }

  suspend fun restoreFromBackupJson(jsonString: String): String {
    var cleanJson = jsonString.trim()
    // Remove BOM if present
    if (cleanJson.startsWith("\uFEFF")) {
      cleanJson = cleanJson.substring(1).trim()
    }
    // Remove markdown code fences if pasted with ```json or ```
    if (cleanJson.startsWith("```json")) {
      cleanJson = cleanJson.removePrefix("```json").trim()
    } else if (cleanJson.startsWith("```")) {
      cleanJson = cleanJson.removePrefix("```").trim()
    }
    if (cleanJson.endsWith("```")) {
      cleanJson = cleanJson.removeSuffix("```").trim()
    }

    if (cleanJson.isBlank()) {
      throw IllegalArgumentException("O texto do backup está vazio.")
    }

    val root = try {
      org.json.JSONObject(cleanJson)
    } catch (e: Exception) {
      // Check if it's a raw array
      try {
        val arr = org.json.JSONArray(cleanJson)
        val obj = org.json.JSONObject()
        obj.put("notes", arr)
        obj
      } catch (e2: Exception) {
        throw IllegalArgumentException("Formato JSON inválido: ${e.message}")
      }
    }

    // Step 1: Parse all data into memory safely before touching database
    val catList = mutableListOf<Category>()
    val noteList = mutableListOf<Note>()
    val itemList = mutableListOf<NoteItem>()
    val habitList = mutableListOf<Habit>()
    val markList = mutableListOf<HabitMark>()
    val calList = mutableListOf<GoogleCalendar>()
    val eventList = mutableListOf<CalendarEvent>()

    // Helper functions for safe type conversion
    fun safeLong(obj: org.json.JSONObject, key: String, defaultVal: Long): Long {
      if (!obj.has(key) || obj.isNull(key)) return defaultVal
      return try {
        obj.optLong(key, defaultVal)
      } catch (e: Exception) {
        obj.optString(key).toLongOrNull() ?: defaultVal
      }
    }

    fun safeInt(obj: org.json.JSONObject, key: String, defaultVal: Int): Int {
      if (!obj.has(key) || obj.isNull(key)) return defaultVal
      return try {
        obj.optInt(key, defaultVal)
      } catch (e: Exception) {
        obj.optString(key).toIntOrNull() ?: defaultVal
      }
    }

    // Parse Categories
    val catsArr = root.optJSONArray("categories") ?: root.optJSONArray("category_list")
    if (catsArr != null) {
      for (i in 0 until catsArr.length()) {
        try {
          val obj = catsArr.getJSONObject(i)
          val id = obj.optString("id", "cat_$i").ifBlank { "cat_$i" }
          val name = obj.optString("name", "Categoria $i").ifBlank { "Categoria $i" }
          val color = obj.optString("colorHex", "#EC3013").ifBlank { "#EC3013" }
          val order = safeInt(obj, "order", i)
          catList.add(Category(id = id, name = name, colorHex = color, order = order))
        } catch (_: Exception) {}
      }
    }

    // Parse Notes
    val notesArr = root.optJSONArray("notes") ?: root.optJSONArray("note_list") ?: root.optJSONArray("postits")
    if (notesArr != null) {
      for (i in 0 until notesArr.length()) {
        try {
          val obj = notesArr.getJSONObject(i)
          val id = obj.optString("id", "note_${System.currentTimeMillis()}_$i").ifBlank { "note_${System.currentTimeMillis()}_$i" }
          val title = obj.optString("title", "")
          val body = obj.optString("body", "")
          val catId = obj.optString("categoryId", "trabalho").ifBlank { "trabalho" }
          val fmtStr = obj.optString("format", NoteFormat.NOTE.name)
          val fmt = try { NoteFormat.valueOf(fmtStr) } catch (e: Exception) { NoteFormat.NOTE }
          val isPinned = obj.optBoolean("isPinned", false)
          val isArchived = obj.optBoolean("isArchived", false)
          val attachedEventId = obj.optString("attachedEventId").ifBlank { null }
          val attachedEventSummary = obj.optString("attachedEventSummary").ifBlank { null }
          val attachedDate = obj.optString("attachedDate").ifBlank { null }
          val createdAt = safeLong(obj, "createdAt", System.currentTimeMillis())
          val updatedAt = safeLong(obj, "updatedAt", System.currentTimeMillis())

          noteList.add(
            Note(
              id = id,
              title = title,
              body = body,
              categoryId = catId,
              format = fmt,
              isPinned = isPinned,
              isArchived = isArchived,
              attachedEventId = attachedEventId,
              attachedEventSummary = attachedEventSummary,
              attachedDate = attachedDate,
              createdAt = createdAt,
              updatedAt = updatedAt
            )
          )
        } catch (_: Exception) {}
      }
    }

    // Parse Note Items
    val itemsArr = root.optJSONArray("noteItems") ?: root.optJSONArray("note_items") ?: root.optJSONArray("items")
    if (itemsArr != null) {
      for (i in 0 until itemsArr.length()) {
        try {
          val obj = itemsArr.getJSONObject(i)
          val id = obj.optString("id", "item_$i").ifBlank { "item_$i" }
          val noteId = obj.optString("noteId", "").ifBlank { obj.optString("note_id", "") }
          val text = obj.optString("text", "")
          val isDone = obj.optBoolean("isDone", false) || obj.optBoolean("is_done", false)
          val orderIdx = safeInt(obj, "orderIndex", i)
          if (noteId.isNotBlank()) {
            itemList.add(
              NoteItem(
                id = id,
                noteId = noteId,
                text = text,
                isDone = isDone,
                orderIndex = orderIdx
              )
            )
          }
        } catch (_: Exception) {}
      }
    }

    // Parse Habits
    val habitsArr = root.optJSONArray("habits") ?: root.optJSONArray("habit_list")
    if (habitsArr != null) {
      for (i in 0 until habitsArr.length()) {
        try {
          val obj = habitsArr.getJSONObject(i)
          val id = obj.optString("id", "habit_$i").ifBlank { "habit_$i" }
          val name = obj.optString("name", "Hábito $i").ifBlank { "Hábito $i" }
          val repTypeStr = obj.optString("repeatType", RepeatType.DAILY.name)
          val repType = try { RepeatType.valueOf(repTypeStr) } catch (e: Exception) { RepeatType.DAILY }
          val repDays = obj.optString("repeatDays", "1,2,3,4,5,6").ifBlank { "1,2,3,4,5,6" }
          val durationDays = safeInt(obj, "durationDays", 0)
          val startEpoch = safeLong(obj, "startDateEpochDay", HabitCalculations.todayEpochDay())
          val isArchived = obj.optBoolean("isArchived", false)
          val reminderTime = obj.optString("reminderTime", "Desativado").ifBlank { "Desativado" }
          val showInCal = obj.optBoolean("showInCalendar", true)
          val savedStreak = safeInt(obj, "pausedSavedStreak", 0)
          val createdAt = safeLong(obj, "createdAt", System.currentTimeMillis())

          habitList.add(
            Habit(
              id = id,
              name = name,
              repeatType = repType,
              repeatDays = repDays,
              durationDays = durationDays,
              startDateEpochDay = startEpoch,
              isArchived = isArchived,
              reminderTime = reminderTime,
              showInCalendar = showInCal,
              pausedSavedStreak = savedStreak,
              createdAt = createdAt
            )
          )
        } catch (_: Exception) {}
      }
    }

    // Parse Habit Marks
    val marksArr = root.optJSONArray("habitMarks") ?: root.optJSONArray("habit_marks") ?: root.optJSONArray("marks")
    if (marksArr != null) {
      for (i in 0 until marksArr.length()) {
        try {
          val obj = marksArr.getJSONObject(i)
          val habitId = obj.optString("habitId", "").ifBlank { obj.optString("habit_id", "") }
          val epochDay = safeLong(obj, "dateEpochDay", 0L)
          val statStr = obj.optString("status", HabitMarkStatus.DONE.name)
          val stat = try { HabitMarkStatus.valueOf(statStr) } catch (e: Exception) { HabitMarkStatus.DONE }
          if (habitId.isNotBlank() && epochDay != 0L) {
            markList.add(
              HabitMark(
                habitId = habitId,
                dateEpochDay = epochDay,
                status = stat
              )
            )
          }
        } catch (_: Exception) {}
      }
    }

    // Parse Calendars
    val calsArr = root.optJSONArray("calendars") ?: root.optJSONArray("calendar_list")
    if (calsArr != null) {
      for (i in 0 until calsArr.length()) {
        try {
          val obj = calsArr.getJSONObject(i)
          val id = obj.optString("id", "cal_$i").ifBlank { "cal_$i" }
          val name = obj.optString("name", "Agenda $i").ifBlank { "Agenda $i" }
          val email = obj.optString("accountEmail", "thiagovinicius7@gmail.com")
          val color = obj.optString("colorHex", "#EC3013").ifBlank { "#EC3013" }
          val isPrimary = obj.optBoolean("isPrimary", false)
          val isReadOnly = obj.optBoolean("isReadOnly", false)
          val isSelected = obj.optBoolean("isSelected", true)

          calList.add(
            GoogleCalendar(
              id = id,
              name = name,
              accountEmail = email,
              colorHex = color,
              isPrimary = isPrimary,
              isReadOnly = isReadOnly,
              isSelected = isSelected
            )
          )
        } catch (_: Exception) {}
      }
    }

    // Parse Events
    val eventsArr = root.optJSONArray("events") ?: root.optJSONArray("event_list") ?: root.optJSONArray("calendar_events")
    if (eventsArr != null) {
      for (i in 0 until eventsArr.length()) {
        try {
          val obj = eventsArr.getJSONObject(i)
          val id = obj.optString("id", "event_$i").ifBlank { "event_$i" }
          val calId = obj.optString("calendarId", "").ifBlank { obj.optString("calendar_id", "") }
          val title = obj.optString("title", "Compromisso").ifBlank { "Compromisso" }
          val startEpoch = safeLong(obj, "startEpochMillis", System.currentTimeMillis())
          val endEpoch = safeLong(obj, "endEpochMillis", startEpoch + 3600000)
          val isAllDay = obj.optBoolean("isAllDay", false)
          val location = obj.optString("location").ifBlank { null }
          val attachedNoteId = obj.optString("attachedNoteId").ifBlank { null }
          val attachedNoteTitle = obj.optString("attachedNoteTitle").ifBlank { null }
          val isLocalOnly = obj.optBoolean("isLocalOnly", false)

          eventList.add(
            CalendarEvent(
              id = id,
              calendarId = calId,
              title = title,
              startEpochMillis = startEpoch,
              endEpochMillis = endEpoch,
              isAllDay = isAllDay,
              location = location,
              attachedNoteId = attachedNoteId,
              attachedNoteTitle = attachedNoteTitle,
              isLocalOnly = isLocalOnly,
              isPendingSync = false
            )
          )
        } catch (_: Exception) {}
      }
    }

    // Ensure we actually found something to restore or default setup
    val totalItems = catList.size + noteList.size + habitList.size + eventList.size
    if (totalItems == 0 && root.length() == 0) {
      throw IllegalArgumentException("O backup fornecido não contém dados válidos de notas, hábitos ou agenda.")
    }

    // Step 2: Now that everything parsed cleanly, apply to database
    clearAllPreloadedData()

    if (catList.isNotEmpty()) noteDao.insertCategories(catList)
    if (noteList.isNotEmpty()) noteDao.insertNotes(noteList)
    if (itemList.isNotEmpty()) noteDao.insertNoteItems(itemList)
    if (habitList.isNotEmpty()) habitDao.insertHabits(habitList)
    if (markList.isNotEmpty()) habitDao.insertMarks(markList)
    if (calList.isNotEmpty()) calendarDao.insertCalendars(calList)
    if (eventList.isNotEmpty()) calendarDao.insertEvents(eventList)

    return "Backup restaurado com sucesso! (${noteList.size} notas, ${habitList.size} hábitos, ${eventList.size} compromissos)"
  }

  // Initial categories setup and cleanup of mock data
  suspend fun seedInitialDataIfEmpty() {
    // Always purge previous mock events and legacy orphaned note badges
    calendarDao.deleteMockEvents()
    calendarDao.clearOrphanedNoteTitles()
    calendarDao.clearEmptyNoteTitles()

    val existingCats = categories.first()
    if (existingCats.isEmpty()) {
      val defaultCategories = listOf(
        Category("trabalho", "Trabalho", "#EC3013", 0),
        Category("pessoal", "Pessoal", "#201E1D", 1),
        Category("estudo", "Estudo", "#9B9797", 2),
        Category("casa", "Casa", "#7A685D", 3)
      )
      noteDao.insertCategories(defaultCategories)
    }
  }
}
