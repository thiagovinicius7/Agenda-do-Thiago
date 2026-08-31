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
  suspend fun deleteHabit(habitId: String) = habitDao.deleteHabit(habitId)

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
              name = "Pessoal",
              accountEmail = email,
              colorHex = "#EC3013",
              isPrimary = true,
              isSelected = true
            ),
            GoogleCalendar(
              id = "cal_trabalho",
              name = "Trabalho",
              accountEmail = email,
              colorHex = "#201E1D",
              isPrimary = false,
              isSelected = true
            ),
            GoogleCalendar(
              id = "cal_faculdade",
              name = "Faculdade / Cursos",
              accountEmail = email,
              colorHex = "#3277DB",
              isPrimary = false,
              isSelected = true
            ),
            GoogleCalendar(
              id = "cal_feriados",
              name = "Feriados no Brasil",
              accountEmail = "pt.brazilian#holiday@group.v.calendar.google.com",
              colorHex = "#529E72",
              isPrimary = false,
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
      seedDynamicGoogleEvents(email)
      return events.first().size
    }
  }

  suspend fun seedDynamicGoogleEvents(email: String = "thiagovinicius7@gmail.com") {
    val today = LocalDate.now()
    val zone = java.time.ZoneId.systemDefault()

    fun epochOf(date: LocalDate, hour: Int, minute: Int): Long {
      return date.atTime(hour, minute).atZone(zone).toInstant().toEpochMilli()
    }

    val sampleEvents = mutableListOf<CalendarEvent>()

    // Today's events
    sampleEvents.add(
      CalendarEvent(
        id = "gcal_today_1",
        calendarId = "cal_trabalho",
        title = "Reunião de alinhamento e planejamento",
        startEpochMillis = epochOf(today, 9, 0),
        endEpochMillis = epochOf(today, 10, 0),
        isAllDay = false,
        location = "Google Meet",
        isLocalOnly = false,
        isPendingSync = false
      )
    )
    sampleEvents.add(
      CalendarEvent(
        id = "gcal_today_2",
        calendarId = "cal_trabalho",
        title = "Revisão de propostas e entregas",
        startEpochMillis = epochOf(today, 14, 30),
        endEpochMillis = epochOf(today, 15, 30),
        isAllDay = false,
        location = "Sala de reuniões",
        attachedNoteTitle = "Proposta comercial",
        isLocalOnly = false,
        isPendingSync = false
      )
    )
    sampleEvents.add(
      CalendarEvent(
        id = "gcal_today_3",
        calendarId = "cal_pessoal",
        title = "Treino e corrida no parque",
        startEpochMillis = epochOf(today, 18, 0),
        endEpochMillis = epochOf(today, 19, 0),
        isAllDay = false,
        location = "Parque",
        isLocalOnly = false,
        isPendingSync = false
      )
    )

    // Tomorrow (+1)
    sampleEvents.add(
      CalendarEvent(
        id = "gcal_d1_1",
        calendarId = "cal_trabalho",
        title = "Apresentação de resultados e métricas",
        startEpochMillis = epochOf(today.plusDays(1), 10, 30),
        endEpochMillis = epochOf(today.plusDays(1), 11, 30),
        isAllDay = false,
        location = "Google Meet",
        isLocalOnly = false,
        isPendingSync = false
      )
    )
    sampleEvents.add(
      CalendarEvent(
        id = "gcal_d1_2",
        calendarId = "cal_pessoal",
        title = "Dentista — consulta de rotina",
        startEpochMillis = epochOf(today.plusDays(1), 16, 0),
        endEpochMillis = epochOf(today.plusDays(1), 17, 0),
        isAllDay = false,
        location = "Centro Clínico",
        isLocalOnly = false,
        isPendingSync = false
      )
    )

    // +2 days
    sampleEvents.add(
      CalendarEvent(
        id = "gcal_d2_1",
        calendarId = "cal_faculdade",
        title = "Aula Magna e Seminário Integrador",
        startEpochMillis = epochOf(today.plusDays(2), 19, 0),
        endEpochMillis = epochOf(today.plusDays(2), 21, 30),
        isAllDay = false,
        location = "Auditório Central",
        isLocalOnly = false,
        isPendingSync = false
      )
    )

    // +3 days
    sampleEvents.add(
      CalendarEvent(
        id = "gcal_d3_1",
        calendarId = "cal_trabalho",
        title = "Sprint Review e Retrospectiva",
        startEpochMillis = epochOf(today.plusDays(3), 15, 0),
        endEpochMillis = epochOf(today.plusDays(3), 16, 30),
        isAllDay = false,
        location = "Google Meet",
        isLocalOnly = false,
        isPendingSync = false
      )
    )

    // +5 days
    sampleEvents.add(
      CalendarEvent(
        id = "gcal_d5_1",
        calendarId = "cal_pessoal",
        title = "Almoço de família e aniversário",
        startEpochMillis = epochOf(today.plusDays(5), 12, 30),
        endEpochMillis = epochOf(today.plusDays(5), 15, 0),
        isAllDay = false,
        location = "Restaurante",
        isLocalOnly = false,
        isPendingSync = false
      )
    )

    // +7 days (Independência do Brasil / Feriado)
    sampleEvents.add(
      CalendarEvent(
        id = "gcal_holiday_1",
        calendarId = "cal_feriados",
        title = "Independência do Brasil",
        startEpochMillis = epochOf(today.plusDays(7), 8, 0),
        endEpochMillis = epochOf(today.plusDays(7), 18, 0),
        isAllDay = true,
        location = "Brasil",
        isLocalOnly = false,
        isPendingSync = false
      )
    )

    // +10 days
    sampleEvents.add(
      CalendarEvent(
        id = "gcal_d10_1",
        calendarId = "cal_trabalho",
        title = "Workshop de Design System e Arquitetura",
        startEpochMillis = epochOf(today.plusDays(10), 14, 0),
        endEpochMillis = epochOf(today.plusDays(10), 16, 0),
        isAllDay = false,
        location = "Google Meet",
        isLocalOnly = false,
        isPendingSync = false
      )
    )

    // +14 days
    sampleEvents.add(
      CalendarEvent(
        id = "gcal_d14_1",
        calendarId = "cal_pessoal",
        title = "Consulta médica e exames periódicos",
        startEpochMillis = epochOf(today.plusDays(14), 8, 30),
        endEpochMillis = epochOf(today.plusDays(14), 10, 0),
        isAllDay = false,
        location = "Hospital Santa Clara",
        isLocalOnly = false,
        isPendingSync = false
      )
    )

    // +20 days
    sampleEvents.add(
      CalendarEvent(
        id = "gcal_d20_1",
        calendarId = "cal_faculdade",
        title = "Entrega final de Artigo e Projeto",
        startEpochMillis = epochOf(today.plusDays(20), 23, 59),
        endEpochMillis = epochOf(today.plusDays(20), 23, 59),
        isAllDay = true,
        location = "Portal Acadêmico",
        isLocalOnly = false,
        isPendingSync = false
      )
    )

    calendarDao.insertEvents(sampleEvents)
  }

  // Initial categories setup without fake mock data
  suspend fun seedInitialDataIfEmpty() {
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
