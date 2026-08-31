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
      calendarDao.insertEvents(deviceEvents)
      return deviceEvents.size
    } else {
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
    val noteBody = event?.attachedNoteTitle ?: event?.location?.let { "Local: $it" } ?: ""

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
          attachedNoteTitle = event.attachedNoteTitle ?: event.title
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
    val root = org.json.JSONObject(jsonString.trim())

    // Clear existing data before restoring
    clearAllPreloadedData()

    var restoredNotesCount = 0
    var restoredHabitsCount = 0
    var restoredEventsCount = 0

    // Restore Categories
    if (root.has("categories")) {
      val catsArr = root.getJSONArray("categories")
      val catList = mutableListOf<Category>()
      for (i in 0 until catsArr.length()) {
        val obj = catsArr.getJSONObject(i)
        catList.add(
          Category(
            id = obj.getString("id"),
            name = obj.getString("name"),
            colorHex = obj.optString("colorHex", "#EC3013"),
            order = obj.optInt("order", i)
          )
        )
      }
      if (catList.isNotEmpty()) noteDao.insertCategories(catList)
    }

    // Restore Notes
    if (root.has("notes")) {
      val notesArr = root.getJSONArray("notes")
      val noteList = mutableListOf<Note>()
      for (i in 0 until notesArr.length()) {
        val obj = notesArr.getJSONObject(i)
        val fmtStr = obj.optString("format", NoteFormat.NOTE.name)
        val fmt = try { NoteFormat.valueOf(fmtStr) } catch (e: Exception) { NoteFormat.NOTE }
        noteList.add(
          Note(
            id = obj.getString("id"),
            title = obj.getString("title"),
            body = obj.optString("body", ""),
            categoryId = obj.optString("categoryId", "trabalho"),
            format = fmt,
            isPinned = obj.optBoolean("isPinned", false),
            isArchived = obj.optBoolean("isArchived", false),
            attachedEventId = obj.optString("attachedEventId").ifBlank { null },
            attachedEventSummary = obj.optString("attachedEventSummary").ifBlank { null },
            attachedDate = obj.optString("attachedDate").ifBlank { null },
            createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
            updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
          )
        )
      }
      if (noteList.isNotEmpty()) {
        noteDao.insertNotes(noteList)
        restoredNotesCount = noteList.size
      }
    }

    // Restore Note Items
    if (root.has("noteItems")) {
      val itemsArr = root.getJSONArray("noteItems")
      val itemList = mutableListOf<NoteItem>()
      for (i in 0 until itemsArr.length()) {
        val obj = itemsArr.getJSONObject(i)
        itemList.add(
          NoteItem(
            id = obj.getString("id"),
            noteId = obj.getString("noteId"),
            text = obj.getString("text"),
            isDone = obj.optBoolean("isDone", false),
            orderIndex = obj.optInt("orderIndex", i)
          )
        )
      }
      if (itemList.isNotEmpty()) noteDao.insertNoteItems(itemList)
    }

    // Restore Habits
    if (root.has("habits")) {
      val habitsArr = root.getJSONArray("habits")
      val habitList = mutableListOf<Habit>()
      for (i in 0 until habitsArr.length()) {
        val obj = habitsArr.getJSONObject(i)
        val repType = try { RepeatType.valueOf(obj.optString("repeatType", RepeatType.DAILY.name)) } catch (e: Exception) { RepeatType.DAILY }
        habitList.add(
          Habit(
            id = obj.getString("id"),
            name = obj.getString("name"),
            repeatType = repType,
            repeatDays = obj.optString("repeatDays", "1,2,3,4,5,6"),
            durationDays = obj.optInt("durationDays", 0),
            startDateEpochDay = obj.optLong("startDateEpochDay", HabitCalculations.todayEpochDay()),
            isArchived = obj.optBoolean("isArchived", false),
            reminderTime = obj.optString("reminderTime", "Desativado"),
            showInCalendar = obj.optBoolean("showInCalendar", true),
            pausedSavedStreak = obj.optInt("pausedSavedStreak", 0),
            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
          )
        )
      }
      if (habitList.isNotEmpty()) {
        habitDao.insertHabits(habitList)
        restoredHabitsCount = habitList.size
      }
    }

    // Restore Habit Marks
    if (root.has("habitMarks")) {
      val marksArr = root.getJSONArray("habitMarks")
      val markList = mutableListOf<HabitMark>()
      for (i in 0 until marksArr.length()) {
        val obj = marksArr.getJSONObject(i)
        val stat = try { HabitMarkStatus.valueOf(obj.optString("status", HabitMarkStatus.DONE.name)) } catch (e: Exception) { HabitMarkStatus.DONE }
        markList.add(
          HabitMark(
            habitId = obj.getString("habitId"),
            dateEpochDay = obj.getLong("dateEpochDay"),
            status = stat
          )
        )
      }
      if (markList.isNotEmpty()) habitDao.insertMarks(markList)
    }

    // Restore Calendars
    if (root.has("calendars")) {
      val calsArr = root.getJSONArray("calendars")
      val calList = mutableListOf<GoogleCalendar>()
      for (i in 0 until calsArr.length()) {
        val obj = calsArr.getJSONObject(i)
        calList.add(
          GoogleCalendar(
            id = obj.getString("id"),
            name = obj.getString("name"),
            accountEmail = obj.optString("accountEmail", "thiagovinicius7@gmail.com"),
            colorHex = obj.optString("colorHex", "#EC3013"),
            isPrimary = obj.optBoolean("isPrimary", false),
            isReadOnly = obj.optBoolean("isReadOnly", false),
            isSelected = obj.optBoolean("isSelected", true)
          )
        )
      }
      if (calList.isNotEmpty()) calendarDao.insertCalendars(calList)
    }

    // Restore Events
    if (root.has("events")) {
      val eventsArr = root.getJSONArray("events")
      val eventList = mutableListOf<CalendarEvent>()
      for (i in 0 until eventsArr.length()) {
        val obj = eventsArr.getJSONObject(i)
        eventList.add(
          CalendarEvent(
            id = obj.getString("id"),
            calendarId = obj.optString("calendarId", ""),
            title = obj.getString("title"),
            startEpochMillis = obj.getLong("startEpochMillis"),
            endEpochMillis = obj.optLong("endEpochMillis", obj.getLong("startEpochMillis") + 3600000),
            isAllDay = obj.optBoolean("isAllDay", false),
            location = obj.optString("location").ifBlank { null },
            attachedNoteId = obj.optString("attachedNoteId").ifBlank { null },
            attachedNoteTitle = obj.optString("attachedNoteTitle").ifBlank { null },
            isLocalOnly = obj.optBoolean("isLocalOnly", false),
            isPendingSync = false
          )
        )
      }
      if (eventList.isNotEmpty()) {
        calendarDao.insertEvents(eventList)
        restoredEventsCount = eventList.size
      }
    }

    return "Backup restaurado com sucesso! ($restoredNotesCount notas, $restoredHabitsCount hábitos, $restoredEventsCount eventos)"
  }

  // Initial categories setup and cleanup of mock data
  suspend fun seedInitialDataIfEmpty() {
    // Always purge previous mock events
    calendarDao.deleteMockEvents()

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
