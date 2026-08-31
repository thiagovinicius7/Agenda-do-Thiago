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
  private val syncQueueDao: SyncQueueDao
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
  val events: Flow<List<CalendarEvent>> = calendarDao.getAllEvents()
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

  // Calendar actions
  suspend fun insertEvent(event: CalendarEvent) {
    calendarDao.insertEvent(event)
    if (event.isPendingSync) {
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

  suspend fun clearSyncQueue() {
    syncQueueDao.clearQueue()
  }

  // Initial demo seed
  suspend fun seedInitialDataIfEmpty() {
    val existingCats = categories.first()
    if (existingCats.isNotEmpty()) return

    val defaultCategories = listOf(
      Category("trabalho", "Trabalho", "#EC3013", 0),
      Category("pessoal", "Pessoal", "#201E1D", 1),
      Category("estudo", "Estudo", "#9B9797", 2),
      Category("casa", "Casa", "#7A685D", 3)
    )
    noteDao.insertCategories(defaultCategories)

    // Seed Notes matching design prototype
    val note1 = Note(
      id = "n_trabalho_1",
      title = "Reunião cliente",
      body = "Eles pediram um prazo mais curto. Levar duas opções de escopo e deixar claro o que sai da primeira fase.",
      categoryId = "trabalho",
      format = NoteFormat.CHECKLIST,
      isPinned = false,
      attachedEventId = "ev_2",
      attachedEventSummary = "Sex, 30 ago · 15:00 — Trabalho (Google)",
      attachedDate = "hoje 15h"
    )
    val note1Items = listOf(
      NoteItem("n1_1", "n_trabalho_1", "Revisar números", isDone = true, orderIndex = 0),
      NoteItem("n1_2", "n_trabalho_1", "Levar proposta impressa", isDone = false, orderIndex = 1),
      NoteItem("n1_3", "n_trabalho_1", "Confirmar sala 2", isDone = false, orderIndex = 2),
      NoteItem("n1_4", "n_trabalho_1", "Enviar ata depois", isDone = false, orderIndex = 3)
    )

    val note2 = Note(
      id = "n_pessoal_1",
      title = "Mercado",
      body = "Compras da semana.",
      categoryId = "pessoal",
      format = NoteFormat.CHECKLIST,
      isPinned = false
    )
    val note2Items = listOf(
      NoteItem("n2_1", "n_pessoal_1", "Café", isDone = false, orderIndex = 0),
      NoteItem("n2_2", "n_pessoal_1", "Arroz, feijão", isDone = false, orderIndex = 1),
      NoteItem("n2_3", "n_pessoal_1", "Sabão", isDone = false, orderIndex = 2)
    )

    val note3 = Note(
      id = "n_ideia_1",
      title = "Nome do curso: “Rotina Curta”",
      body = "Estrutura do curso em módulos de 5 minutos focados em consistência diária.",
      categoryId = "trabalho",
      format = NoteFormat.NOTE,
      isPinned = true,
      attachedDate = "Nota · 28 ago"
    )

    val note4 = Note(
      id = "n_estudo_1",
      title = "Cap. 4 — anotações",
      body = "Revisar exercícios 12 a 20 antes da prova.",
      categoryId = "estudo",
      format = NoteFormat.NOTE,
      isPinned = false,
      attachedDate = "Anexado a 2 set"
    )

    noteDao.insertNote(note1)
    noteDao.insertNoteItems(note1Items)
    noteDao.insertNote(note2)
    noteDao.insertNoteItems(note2Items)
    noteDao.insertNote(note3)
    noteDao.insertNote(note4)

    // Seed Habits
    val todayEpoch = HabitCalculations.todayEpochDay()
    val startDateCorrida = todayEpoch - 61 // day 62 today

    val habitCorrida = Habit(
      id = "h_corrida",
      name = "Corrida",
      repeatType = RepeatType.DAYS_OF_WEEK,
      repeatDays = "1,2,3,4,5,6", // Mon-Sat (excluding Sun=7)
      durationDays = 150,
      startDateEpochDay = startDateCorrida,
      showInCalendar = true,
      reminderTime = "06:30",
      reminderEnabled = true,
      pauseAllowed = true
    )

    val habitLeitura = Habit(
      id = "h_leitura",
      name = "Ler 20 páginas",
      repeatType = RepeatType.DAILY,
      durationDays = 0, // sem fim
      startDateEpochDay = todayEpoch - 89,
      reminderTime = "21:00",
      reminderEnabled = true
    )

    val habitAcademia = Habit(
      id = "h_academia",
      name = "Academia",
      repeatType = RepeatType.TIMES_PER_WEEK,
      timesPerWeek = 3,
      durationDays = 0,
      startDateEpochDay = todayEpoch - 30
    )

    val habitMeditar = Habit(
      id = "h_meditar",
      name = "Meditar",
      repeatType = RepeatType.DAILY,
      durationDays = 0,
      startDateEpochDay = todayEpoch - 60,
      isPaused = true,
      pausedSavedStreak = 23
    )

    habitDao.insertHabits(listOf(habitCorrida, habitLeitura, habitAcademia, habitMeditar))

    // Seed realistic habit marks for Corrida (61 days marked, with streak d35->d62)
    val marks = mutableListOf<HabitMark>()
    for (d in 0 until 61) {
      val dayEpoch = startDateCorrida + d
      val inRule = HabitCalculations.isDateInRule(habitCorrida, dayEpoch)
      if (inRule) {
        // sporadic early fail around day 7 or 18, but streak from day 35 onward unbroken
        val isFail = (d < 34 && (d % 11 == 7))
        if (!isFail) {
          marks.add(HabitMark(habitCorrida.id, dayEpoch, HabitMarkStatus.DONE))
        }
      }
    }

    // Leitura marks (86 of 90 done)
    for (d in 0 until 90) {
      val dayEpoch = todayEpoch - 89 + d
      if (d != 10 && d != 33 && d != 55 && d != 85) {
        marks.add(HabitMark(habitLeitura.id, dayEpoch, HabitMarkStatus.DONE))
      }
    }

    habitDao.insertMarks(marks)

    // Seed Google Calendars & Events
    val calPersonal = GoogleCalendar("cal_pessoal", "Pessoal", "ana@gmail.com", "#EC3013", isPrimary = true)
    val calWork = GoogleCalendar("cal_trabalho", "Trabalho", "ana@empresa.com", "#201E1D")
    val calCollege = GoogleCalendar("cal_faculdade", "Faculdade", "Somente leitura", "#9B9797", isReadOnly = true)
    calendarDao.insertCalendars(listOf(calPersonal, calWork, calCollege))

    val event1 = CalendarEvent(
      id = "ev_1",
      calendarId = "cal_trabalho",
      title = "Daily do time",
      startEpochMillis = System.currentTimeMillis(),
      endEpochMillis = System.currentTimeMillis() + 1800000
    )
    val event2 = CalendarEvent(
      id = "ev_2",
      calendarId = "cal_trabalho",
      title = "Reunião cliente",
      startEpochMillis = System.currentTimeMillis() + 7200000,
      endEpochMillis = System.currentTimeMillis() + 10800000,
      attachedNoteId = "n_trabalho_1",
      attachedNoteTitle = "Reunião cliente"
    )
    val event3 = CalendarEvent(
      id = "ev_3",
      calendarId = "cal_pessoal",
      title = "Aula de inglês",
      startEpochMillis = System.currentTimeMillis() + 18000000,
      endEpochMillis = System.currentTimeMillis() + 21600000
    )
    calendarDao.insertEvents(listOf(event1, event2, event3))
  }
}
