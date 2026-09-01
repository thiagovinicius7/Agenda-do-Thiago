package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.CalendarEvent
import com.example.data.model.Category
import com.example.data.model.GoogleCalendar
import com.example.data.model.Habit
import com.example.data.model.HabitCalculationResult
import com.example.data.model.Note
import com.example.data.model.NoteFormat
import com.example.data.model.NoteItem
import com.example.data.model.NoteWithItems
import com.example.data.model.RepeatType
import com.example.data.model.SyncQueueItem
import com.example.data.repository.BlocoRepository
import com.example.notification.HabitNotificationScheduler
import com.example.util.HabitCalculations
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class TopSection {
  HOJE,
  MURAL,
  AGENDA,
  HABITOS
}

enum class ActiveOverlay {
  NONE,
  NOTE_DETAIL,
  HABIT_DETAIL,
  HABIT_CREATE,
  HABIT_CONCLUDED,
  EVENT_CREATE,
  EVENT_DETAIL,
  SEARCH,
  SETTINGS,
  ONBOARDING,
  OFFLINE,
  STATS
}

enum class CalendarViewMode {
  MES,
  SEMANA,
  DIA
}

enum class DemoMode {
  NORMAL,
  FIRST_USE,
  EMPTY,
  D150_CONCLUDED,
  OFFLINE
}

data class BlocoUiState(
  val currentSection: TopSection = TopSection.HOJE,
  val activeOverlay: ActiveOverlay = ActiveOverlay.NONE,
  val demoMode: DemoMode = DemoMode.NORMAL,
  val isDarkTheme: Boolean = false,
  val selectedNoteId: String? = null,
  val selectedHabitId: String? = "h_corrida",
  val selectedEventId: String? = null,
  val selectedHojeDate: LocalDate = LocalDate.now(),
  val selectedCalendarDate: LocalDate = LocalDate.now(),
  val calendarViewMode: CalendarViewMode = CalendarViewMode.MES,
  val muralCategoryFilter: String = "todos",
  val searchQuery: String = "",
  val searchFilter: String = "tudo", // tudo, notas, agenda, habitos
  val countInDaysNotation: Boolean = true,
  val weekStartSunday: Boolean = true,
  val backgroundSyncEnabled: Boolean = true,
  val habitsInCalendar: Boolean = true
)

class BlocoViewModel(private val repository: BlocoRepository) : ViewModel() {

  private val _uiState = MutableStateFlow(BlocoUiState())
  val uiState: StateFlow<BlocoUiState> = _uiState.asStateFlow()

  val notesWithItems: StateFlow<List<NoteWithItems>> = repository.notesWithItems
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val habitsWithCalculations: StateFlow<List<HabitCalculationResult>> = repository.habitsWithCalculations
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val categories: StateFlow<List<Category>> = repository.categories
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val calendars: StateFlow<List<GoogleCalendar>> = repository.calendars
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val events: StateFlow<List<CalendarEvent>> = repository.events
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val syncQueue: StateFlow<List<SyncQueueItem>> = repository.syncQueue
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  init {
    viewModelScope.launch {
      repository.seedInitialDataIfEmpty()
    }
  }

  fun setSection(section: TopSection) {
    _uiState.value = _uiState.value.copy(currentSection = section, activeOverlay = ActiveOverlay.NONE)
  }

  fun setOverlay(overlay: ActiveOverlay) {
    _uiState.value = _uiState.value.copy(activeOverlay = overlay)
  }

  fun closeOverlay() {
    _uiState.value = _uiState.value.copy(activeOverlay = ActiveOverlay.NONE)
  }

  fun setDemoMode(mode: DemoMode) {
    _uiState.value = when (mode) {
      DemoMode.NORMAL -> _uiState.value.copy(demoMode = mode, currentSection = TopSection.HOJE, activeOverlay = ActiveOverlay.NONE)
      DemoMode.FIRST_USE -> _uiState.value.copy(demoMode = mode, activeOverlay = ActiveOverlay.ONBOARDING)
      DemoMode.EMPTY -> _uiState.value.copy(demoMode = mode, currentSection = TopSection.MURAL, muralCategoryFilter = "vazio", activeOverlay = ActiveOverlay.NONE)
      DemoMode.D150_CONCLUDED -> _uiState.value.copy(demoMode = mode, currentSection = TopSection.HABITOS, activeOverlay = ActiveOverlay.HABIT_CONCLUDED)
      DemoMode.OFFLINE -> _uiState.value.copy(demoMode = mode, currentSection = TopSection.AGENDA, activeOverlay = ActiveOverlay.OFFLINE)
    }
  }

  fun toggleDarkTheme() {
    _uiState.value = _uiState.value.copy(isDarkTheme = !_uiState.value.isDarkTheme)
  }

  fun setMuralFilter(category: String) {
    _uiState.value = _uiState.value.copy(muralCategoryFilter = category)
  }

  fun setSearchQuery(query: String) {
    _uiState.value = _uiState.value.copy(searchQuery = query)
  }

  fun setSearchFilter(filter: String) {
    _uiState.value = _uiState.value.copy(searchFilter = filter)
  }

  fun setCalendarViewMode(mode: CalendarViewMode) {
    _uiState.value = _uiState.value.copy(calendarViewMode = mode)
  }

  fun selectHojeDate(date: LocalDate) {
    _uiState.value = _uiState.value.copy(selectedHojeDate = date)
  }

  fun selectCalendarDate(date: LocalDate) {
    _uiState.value = _uiState.value.copy(selectedCalendarDate = date)
  }

  fun openNote(noteId: String) {
    _uiState.value = _uiState.value.copy(selectedNoteId = noteId, activeOverlay = ActiveOverlay.NOTE_DETAIL)
  }

  fun openHabit(habitId: String) {
    _uiState.value = _uiState.value.copy(selectedHabitId = habitId, activeOverlay = ActiveOverlay.HABIT_DETAIL)
  }

  fun openHabitCreate() {
    _uiState.value = _uiState.value.copy(selectedHabitId = null, activeOverlay = ActiveOverlay.HABIT_CREATE)
  }

  fun openEvent(eventId: String) {
    _uiState.value = _uiState.value.copy(selectedEventId = eventId, activeOverlay = ActiveOverlay.EVENT_DETAIL)
  }

  fun deleteEvent(eventId: String) {
    viewModelScope.launch {
      repository.deleteEvent(eventId)
      if (_uiState.value.selectedEventId == eventId) {
        closeOverlay()
      }
    }
  }

  fun deleteNote(noteId: String) {
    viewModelScope.launch {
      repository.deleteNote(noteId)
      if (_uiState.value.selectedNoteId == noteId) {
        closeOverlay()
      }
    }
  }

  fun deleteHabit(habitId: String) {
    viewModelScope.launch {
      repository.deleteHabit(habitId)
      if (_uiState.value.selectedHabitId == habitId) {
        closeOverlay()
      }
    }
  }

  fun openNoteForEvent(eventId: String) {
    viewModelScope.launch {
      val noteId = repository.ensureNoteForEvent(eventId)
      openNote(noteId)
    }
  }

  fun createBackup(context: Context, onDone: ((String) -> Unit)? = null) {
    viewModelScope.launch {
      val jsonResult = repository.createBackupJson()
      val prefs = context.getSharedPreferences("bloco_backup_prefs", Context.MODE_PRIVATE)
      prefs.edit()
        .putString("last_backup_json", jsonResult)
        .putLong("last_backup_time", System.currentTimeMillis())
        .apply()

      // Also persist to internal file storage so it survives shared prefs resets
      try {
        val backupDir = java.io.File(context.filesDir, "backups").apply { mkdirs() }
        java.io.File(backupDir, "bloco_t_backup_latest.json").writeText(jsonResult)
        java.io.File(backupDir, "bloco_t_backup_${System.currentTimeMillis()}.json").writeText(jsonResult)
      } catch (_: Exception) {}

      onDone?.invoke(jsonResult)
    }
  }

  fun getLastBackupJson(context: Context): String? {
    val prefs = context.getSharedPreferences("bloco_backup_prefs", Context.MODE_PRIVATE)
    val fromPrefs = prefs.getString("last_backup_json", null)
    if (!fromPrefs.isNullOrBlank()) return fromPrefs

    // Fallback to file on disk
    return try {
      val backupFile = java.io.File(context.filesDir, "backups/bloco_t_backup_latest.json")
      if (backupFile.exists()) backupFile.readText() else null
    } catch (_: Exception) {
      null
    }
  }

  fun shareBackupFile(context: Context, jsonString: String): String? {
    return try {
      val cacheBackupDir = java.io.File(context.cacheDir, "shared_backups").apply { mkdirs() }
      val backupFile = java.io.File(cacheBackupDir, "bloco_t_backup.json")
      backupFile.writeText(jsonString)

      val authority = "${context.packageName}.fileprovider"
      val contentUri = androidx.core.content.FileProvider.getUriForFile(context, authority, backupFile)

      val sendIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = "application/json"
        putExtra(android.content.Intent.EXTRA_STREAM, contentUri)
        putExtra(android.content.Intent.EXTRA_SUBJECT, "Backup Bloco T")
        putExtra(android.content.Intent.EXTRA_TEXT, "Backup do Bloco T em formato JSON.")
        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
      }

      val chooser = android.content.Intent.createChooser(sendIntent, "Compartilhar Backup (.json)").apply {
        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(chooser)
      null
    } catch (e: Exception) {
      "Erro ao compartilhar backup: ${e.message}"
    }
  }

  fun restoreLastBackup(context: Context, onResult: (String) -> Unit) {
    val prefs = context.getSharedPreferences("bloco_backup_prefs", Context.MODE_PRIVATE)
    var savedJson = prefs.getString("last_backup_json", null)

    // If SharedPreferences was cleared (e.g. app update/reinstall), try disk files
    if (savedJson.isNullOrBlank()) {
      try {
        val backupDir = java.io.File(context.filesDir, "backups")
        val latestFile = java.io.File(backupDir, "bloco_t_backup_latest.json")
        if (latestFile.exists()) {
          savedJson = latestFile.readText()
        } else if (backupDir.exists()) {
          val newestFile = backupDir.listFiles { f -> f.extension == "json" }?.maxByOrNull { it.lastModified() }
          savedJson = newestFile?.readText()
        }
      } catch (_: Exception) {}
    }

    if (savedJson.isNullOrBlank()) {
      onResult("Nenhum backup encontrado no armazenamento local. Selecione um arquivo (.json) ou cole o texto do backup.")
      return
    }

    viewModelScope.launch {
      try {
        val msg = repository.restoreFromBackupJson(savedJson)
        onResult(msg)
      } catch (e: Exception) {
        onResult("Erro ao restaurar backup: ${e.message}")
      }
    }
  }

  fun restoreFromBackupJson(context: Context, jsonString: String, onResult: (String) -> Unit) {
    viewModelScope.launch {
      try {
        val msg = repository.restoreFromBackupJson(jsonString)
        val prefs = context.getSharedPreferences("bloco_backup_prefs", Context.MODE_PRIVATE)
        prefs.edit()
          .putString("last_backup_json", jsonString)
          .putLong("last_backup_time", System.currentTimeMillis())
          .apply()
        onResult(msg)
      } catch (e: Exception) {
        onResult("Erro no formato do backup: ${e.message}")
      }
    }
  }

  fun getLastBackupInfo(context: Context): String? {
    val prefs = context.getSharedPreferences("bloco_backup_prefs", Context.MODE_PRIVATE)
    val time = prefs.getLong("last_backup_time", 0L)
    if (time == 0L) return null
    val zdt = java.time.Instant.ofEpochMilli(time).atZone(java.time.ZoneId.systemDefault())
    return zdt.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", java.util.Locale("pt", "BR")))
  }

  fun updateEvent(
    id: String,
    title: String,
    calendarId: String,
    date: LocalDate,
    startTimeHour: Int,
    startTimeMinute: Int,
    durationMinutes: Int,
    attachedNoteId: String? = null,
    attachedNoteTitle: String? = null,
    isLocalOnly: Boolean = false
  ) {
    viewModelScope.launch {
      val startEpoch = date.atTime(startTimeHour, startTimeMinute).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
      val event = CalendarEvent(
        id = id,
        calendarId = calendarId,
        title = title.ifBlank { "Compromisso" },
        startEpochMillis = startEpoch,
        endEpochMillis = startEpoch + (durationMinutes * 60 * 1000),
        attachedNoteId = attachedNoteId,
        attachedNoteTitle = attachedNoteTitle,
        isLocalOnly = isLocalOnly,
        isPendingSync = !isLocalOnly
      )
      repository.updateEvent(event)
      closeOverlay()
    }
  }

  fun toggleAccountSelection(accountEmail: String, isSelected: Boolean) {
    viewModelScope.launch {
      repository.toggleAccountSelection(accountEmail, isSelected)
    }
  }

  fun toggleChecklistItem(itemId: String, currentDone: Boolean) {
    viewModelScope.launch {
      repository.toggleNoteItem(itemId, currentDone)
    }
  }

  fun toggleHabitDay(habitId: String, dateEpochDay: Long = HabitCalculations.todayEpochDay()) {
    viewModelScope.launch {
      repository.toggleHabitDay(habitId, dateEpochDay)
    }
  }

  fun markPastHabitDays(habitId: String, fromEpochDay: Long, toEpochDay: Long, markAsDone: Boolean = true) {
    viewModelScope.launch {
      repository.markPastHabitDays(habitId, fromEpochDay, toEpochDay, markAsDone)
    }
  }

  fun saveNote(
    id: String?,
    title: String,
    body: String,
    categoryId: String,
    format: NoteFormat,
    items: List<String>,
    attachedEventId: String? = null,
    attachedEventSummary: String? = null,
    attachedDate: String? = null
  ) {
    viewModelScope.launch {
      val noteId = id?.ifBlank { null } ?: "note_${System.currentTimeMillis()}"
      val note = Note(
        id = noteId,
        title = title.ifBlank { "Nova nota" },
        body = body,
        categoryId = categoryId,
        format = format,
        attachedEventId = attachedEventId,
        attachedEventSummary = attachedEventSummary,
        attachedDate = attachedDate,
        updatedAt = System.currentTimeMillis()
      )
      val noteItems = items.filter { it.isNotBlank() }.mapIndexed { idx, text ->
        NoteItem(
          id = "${noteId}_item_$idx",
          noteId = noteId,
          text = text,
          isDone = false,
          orderIndex = idx
        )
      }
      repository.insertNote(note, noteItems)
      closeOverlay()
    }
  }

  fun saveHabit(
    id: String? = null,
    name: String,
    repeatType: RepeatType,
    repeatDays: String,
    durationDays: Int,
    reminderTime: String,
    showInCalendar: Boolean,
    startDateEpochDay: Long = HabitCalculations.todayEpochDay(),
    markPastDaysAsDone: Boolean = false,
    context: Context? = null
  ) {
    viewModelScope.launch {
      val isExisting = !id.isNullOrBlank()
      val habitId = if (isExisting) id!! else "habit_${System.currentTimeMillis()}"
      val habitName = name.ifBlank { "Novo hábito" }

      val existingHabits = repository.activeHabits.first()
      val existingHabit = existingHabits.find { it.id == habitId }

      val habit = if (existingHabit != null) {
        existingHabit.copy(
          name = habitName,
          repeatType = repeatType,
          repeatDays = repeatDays,
          durationDays = durationDays,
          startDateEpochDay = startDateEpochDay,
          reminderTime = reminderTime,
          showInCalendar = showInCalendar
        )
      } else {
        Habit(
          id = habitId,
          name = habitName,
          repeatType = repeatType,
          repeatDays = repeatDays,
          durationDays = durationDays,
          startDateEpochDay = startDateEpochDay,
          reminderTime = reminderTime,
          showInCalendar = showInCalendar
        )
      }

      if (isExisting && existingHabit != null) {
        repository.updateHabit(habit)
      } else {
        repository.insertHabit(habit)
      }

      // If user selected a start date in the past and opted to mark past days as done
      val todayEpoch = HabitCalculations.todayEpochDay()
      if (markPastDaysAsDone && startDateEpochDay < todayEpoch) {
        repository.markPastHabitDays(habitId, startDateEpochDay, todayEpoch - 1, true)
      }

      // Schedule or cancel notification alarm if reminder is set and context is provided
      if (context != null) {
        if (reminderTime.isNotBlank() && reminderTime != "Desativado") {
          HabitNotificationScheduler.scheduleHabitReminder(
            context = context,
            habitId = habitId,
            habitName = habitName,
            reminderTime = reminderTime
          )
        } else {
          HabitNotificationScheduler.cancelHabitReminder(context, habitId)
        }
      }

      if (isExisting) {
        _uiState.value = _uiState.value.copy(activeOverlay = ActiveOverlay.HABIT_DETAIL, selectedHabitId = habitId)
      } else {
        closeOverlay()
      }
    }
  }

  fun updateHabitReminder(
    context: Context,
    habitId: String,
    habitName: String,
    newReminderTime: String
  ) {
    viewModelScope.launch {
      val habits = repository.activeHabits.first()
      val habit = habits.find { it.id == habitId } ?: return@launch
      val updated = habit.copy(reminderTime = newReminderTime)
      repository.updateHabit(updated)

      if (newReminderTime.isNotBlank() && newReminderTime != "Desativado") {
        HabitNotificationScheduler.scheduleHabitReminder(context, habitId, habitName, newReminderTime)
      } else {
        HabitNotificationScheduler.cancelHabitReminder(context, habitId)
      }
    }
  }

  fun testNotification(context: Context, habitName: String) {
    HabitNotificationScheduler.sendTestNotificationNow(context, habitName)
  }

  fun saveEvent(
    title: String,
    calendarId: String,
    date: LocalDate,
    startTimeHour: Int,
    startTimeMinute: Int,
    durationMinutes: Int,
    attachedNoteId: String?,
    attachedNoteTitle: String?,
    isLocalOnly: Boolean
  ) {
    viewModelScope.launch {
      val startEpoch = date.atTime(startTimeHour, startTimeMinute).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
      val event = CalendarEvent(
        id = "event_${System.currentTimeMillis()}",
        calendarId = calendarId,
        title = title.ifBlank { "Novo compromisso" },
        startEpochMillis = startEpoch,
        endEpochMillis = startEpoch + (durationMinutes * 60 * 1000),
        attachedNoteId = attachedNoteId,
        attachedNoteTitle = attachedNoteTitle,
        isLocalOnly = isLocalOnly,
        isPendingSync = !isLocalOnly
      )
      repository.insertEvent(event)
      closeOverlay()
    }
  }

  fun toggleSetting(key: String) {
    when (key) {
      "countInDays" -> _uiState.value = _uiState.value.copy(countInDaysNotation = !_uiState.value.countInDaysNotation)
      "bgSync" -> _uiState.value = _uiState.value.copy(backgroundSyncEnabled = !_uiState.value.backgroundSyncEnabled)
      "habitsInCalendar" -> _uiState.value = _uiState.value.copy(habitsInCalendar = !_uiState.value.habitsInCalendar)
      "weekStart" -> _uiState.value = _uiState.value.copy(weekStartSunday = !_uiState.value.weekStartSunday)
    }
  }

  fun toggleCalendarSelection(calendarId: String) {
    viewModelScope.launch {
      repository.toggleCalendarSelection(calendarId)
    }
  }

  fun updateCalendarSelection(calendarId: String, isSelected: Boolean) {
    viewModelScope.launch {
      repository.updateCalendarSelection(calendarId, isSelected)
    }
  }

  fun selectAllCalendars(isSelected: Boolean) {
    viewModelScope.launch {
      repository.selectAllCalendars(isSelected)
    }
  }

  fun clearAllData() {
    viewModelScope.launch {
      repository.clearAllPreloadedData()
      repository.seedInitialDataIfEmpty()
    }
  }

  fun syncDeviceCalendar(userEmail: String? = "thiagovinicius7@gmail.com") {
    viewModelScope.launch {
      repository.syncWithDeviceCalendar(userEmail)
    }
  }

  fun retrySync() {
    viewModelScope.launch {
      repository.clearSyncQueue()
      repository.syncWithDeviceCalendar("thiagovinicius7@gmail.com")
    }
  }
}

class BlocoViewModelFactory(private val repository: BlocoRepository) : androidx.lifecycle.ViewModelProvider.Factory {
  @Suppress("UNCHECKED_CAST")
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(BlocoViewModel::class.java)) {
      return BlocoViewModel(repository) as T
    }
    throw IllegalArgumentException("Unknown ViewModel class")
  }
}
