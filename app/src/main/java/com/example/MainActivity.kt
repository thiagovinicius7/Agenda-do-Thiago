package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import java.time.LocalDate
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.BlocoDatabase
import com.example.data.model.HabitCalculationResult
import com.example.data.model.Note
import com.example.data.model.NoteFormat
import com.example.data.model.NoteWithItems
import com.example.data.repository.BlocoRepository
import com.example.data.repository.CalendarSyncHelper
import com.example.ui.components.Ruler1dp
import com.example.ui.components.Ruler2dp
import com.example.ui.screens.AgendaScreen
import com.example.ui.screens.BillDetailScreen
import com.example.ui.screens.ContasScreen
import com.example.ui.screens.EventCreateScreen
import com.example.ui.screens.EventDetailScreen
import com.example.ui.screens.HabitConcludedScreen
import com.example.ui.screens.HabitCreateScreen
import com.example.ui.screens.HabitDetailScreen
import com.example.ui.screens.HabitsListScreen
import com.example.ui.screens.HojeScreen
import com.example.ui.screens.MuralScreen
import com.example.ui.screens.NoteDetailScreen
import com.example.ui.screens.OfflineScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.theme.ArchivoFont
import com.example.ui.theme.BlocoTheme
import com.example.ui.theme.LocalBlocoColors
import com.example.ui.theme.SectionLabelStyle
import com.example.ui.viewmodel.ActiveOverlay
import com.example.ui.viewmodel.BlocoViewModel
import com.example.ui.viewmodel.BlocoViewModelFactory
import com.example.ui.viewmodel.CalendarViewMode
import com.example.ui.viewmodel.TopSection

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val database = BlocoDatabase.getDatabase(applicationContext)
    val syncHelper = CalendarSyncHelper(applicationContext)
    val repository = BlocoRepository(
      noteDao = database.noteDao(),
      habitDao = database.habitDao(),
      calendarDao = database.calendarDao(),
      syncQueueDao = database.syncQueueDao(),
      billDao = database.billDao(),
      calendarSyncHelper = syncHelper
    )
    val viewModelFactory = BlocoViewModelFactory(repository)

    setContent {
      val viewModel: BlocoViewModel = viewModel(factory = viewModelFactory)
      val uiState by viewModel.uiState.collectAsState()

      BlocoTheme(darkTheme = uiState.isDarkTheme) {
        BlocoApp(
          viewModel = viewModel,
          isDarkTheme = uiState.isDarkTheme,
          onToggleTheme = { viewModel.toggleDarkTheme() }
        )
      }
    }
  }
}

@Composable
fun BlocoApp(
  viewModel: BlocoViewModel,
  isDarkTheme: Boolean,
  onToggleTheme: () -> Unit
) {
  val context = LocalContext.current
  val colors = LocalBlocoColors.current
  val uiState by viewModel.uiState.collectAsState()

  val notes by viewModel.notesWithItems.collectAsState()
  val habits by viewModel.habitsWithCalculations.collectAsState()
  val events by viewModel.events.collectAsState()
  val calendars by viewModel.calendars.collectAsState()
  val syncQueue by viewModel.syncQueue.collectAsState()
  val bills by viewModel.billsWithStatus.collectAsState()

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
  ) { perms ->
    val granted = perms[Manifest.permission.READ_CALENDAR] == true
    if (granted) {
      viewModel.syncDeviceCalendar("thiagovinicius7@gmail.com")
    }
  }

  LaunchedEffect(Unit) {
    val permissionsToRequest = mutableListOf(
      Manifest.permission.READ_CALENDAR,
      Manifest.permission.WRITE_CALENDAR
    )
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
    }

    val missingPermissions = permissionsToRequest.filter {
      ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
    }

    viewModel.syncDeviceCalendar("thiagovinicius7@gmail.com")

    if (missingPermissions.isNotEmpty()) {
      permissionLauncher.launch(missingPermissions.toTypedArray())
    }
  }

  val sections = remember {
    listOf(
      TopSection.HOJE,
      TopSection.MURAL,
      TopSection.CONTAS,
      TopSection.AGENDA,
      TopSection.HABITOS
    )
  }
  val coroutineScope = rememberCoroutineScope()
  val pagerState = rememberPagerState(
    initialPage = sections.indexOf(uiState.currentSection).coerceAtLeast(0)
  ) { sections.size }

  // Sync pager when top bar tab is clicked
  LaunchedEffect(uiState.currentSection) {
    val targetPage = sections.indexOf(uiState.currentSection)
    if (targetPage in sections.indices && pagerState.currentPage != targetPage) {
      pagerState.animateScrollToPage(targetPage)
    }
  }

  // Sync ViewModel section when user swipes the pager
  LaunchedEffect(pagerState.currentPage) {
    val targetSection = sections[pagerState.currentPage]
    if (uiState.currentSection != targetSection) {
      viewModel.setSection(targetSection)
    }
  }

  // Handle system back button properly
  BackHandler(enabled = uiState.activeOverlay != ActiveOverlay.NONE || uiState.currentSection != TopSection.HOJE) {
    if (uiState.activeOverlay != ActiveOverlay.NONE) {
      viewModel.closeOverlay()
    } else if (uiState.currentSection != TopSection.HOJE) {
      viewModel.setSection(TopSection.HOJE)
    }
  }

  Scaffold(
    modifier = Modifier
      .fillMaxSize()
      .statusBarsPadding()
      .background(colors.canvas),
    containerColor = colors.canvas
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      if (uiState.activeOverlay == ActiveOverlay.NONE) {
        Column(modifier = Modifier.fillMaxSize()) {
          // Top Navigation Bar (HOJE, MURAL, AGENDA, HÁBITOS, ⌕, ⚙)
          BlocoTopTabBar(
            currentSection = uiState.currentSection,
            onSelectSection = { targetSection ->
              viewModel.setSection(targetSection)
              val targetPage = sections.indexOf(targetSection)
              if (targetPage in sections.indices && pagerState.currentPage != targetPage) {
                coroutineScope.launch {
                  pagerState.animateScrollToPage(targetPage)
                }
              }
            },
            onOpenSearch = { viewModel.setOverlay(ActiveOverlay.SEARCH) },
            onOpenSettings = { viewModel.setOverlay(ActiveOverlay.SETTINGS) }
          )

          // Content based on selected Top Section with swipe navigation
          HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
          ) { page ->
            when (sections[page]) {
              TopSection.HOJE -> {
                HojeScreen(
                  habits = habits,
                  notes = notes,
                  events = events,
                  calendars = calendars,
                  bills = bills,
                  selectedDate = uiState.selectedHojeDate,
                  onSelectDate = { viewModel.selectHojeDate(it) },
                  onToggleHabit = { viewModel.toggleHabitDay(it) },
                  onToggleHabitForDate = { id, epoch -> viewModel.toggleHabitDay(id, epoch) },
                  onOpenHabit = { habitId -> viewModel.openHabit(habitId) },
                  onOpenNote = { noteId -> viewModel.openNote(noteId) },
                  onOpenEvent = { eventId -> viewModel.openEvent(eventId) },
                  onOpenBill = { billId -> viewModel.openBill(billId) },
                  onToggleBillPayment = { billStatus -> viewModel.toggleBillPayment(billStatus) },
                  onCreateEvent = { viewModel.setOverlay(ActiveOverlay.EVENT_CREATE) },
                  onCreateHabit = { viewModel.openHabitCreate() },
                  onCreateNote = { viewModel.openNote("") },
                  onCreateBill = { viewModel.openBillCreate() }
                )
              }
              TopSection.MURAL -> {
                MuralScreen(
                  notes = notes,
                  currentFilter = uiState.muralCategoryFilter,
                  onSelectFilter = { viewModel.setMuralFilter(it) },
                  onOpenNote = { noteId -> viewModel.openNote(noteId) },
                  onToggleChecklistItem = { itemId, currentDone ->
                    viewModel.toggleChecklistItem(itemId, currentDone)
                  },
                  onCreateNewNote = {
                    viewModel.openNote("")
                  }
                )
              }
              TopSection.CONTAS -> {
                ContasScreen(
                  bills = bills,
                  currentCategoryFilter = uiState.billsCategoryFilter,
                  onSelectCategoryFilter = { viewModel.setBillsCategoryFilter(it) },
                  onOpenBill = { billId -> viewModel.openBill(billId) },
                  onCreateBill = { viewModel.openBillCreate() },
                  onTogglePayment = { billStatus -> viewModel.toggleBillPayment(billStatus) }
                )
              }
              TopSection.AGENDA -> {
                AgendaScreen(
                  events = events,
                  calendars = calendars,
                  viewMode = uiState.calendarViewMode,
                  selectedDate = uiState.selectedCalendarDate,
                  onSelectViewMode = { viewModel.setCalendarViewMode(it) },
                  onSelectDate = { viewModel.selectCalendarDate(it) },
                  onCreateEvent = { viewModel.setOverlay(ActiveOverlay.EVENT_CREATE) },
                  onOpenEvent = { eventId -> viewModel.openEvent(eventId) },
                  onSyncNow = {
                    val hasPermission = ContextCompat.checkSelfPermission(
                      context,
                      Manifest.permission.READ_CALENDAR
                    ) == PackageManager.PERMISSION_GRANTED
                    if (hasPermission) {
                      viewModel.syncDeviceCalendar("thiagovinicius7@gmail.com")
                    } else {
                      val perms = mutableListOf(
                        Manifest.permission.READ_CALENDAR,
                        Manifest.permission.WRITE_CALENDAR
                      )
                      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        perms.add(Manifest.permission.POST_NOTIFICATIONS)
                      }
                      permissionLauncher.launch(perms.toTypedArray())
                    }
                  }
                )
              }
              TopSection.HABITOS -> {
                HabitsListScreen(
                  habits = habits,
                  onOpenHabit = { habitId -> viewModel.openHabit(habitId) },
                  onCreateHabit = { viewModel.openHabitCreate() },
                  onOpenStats = { viewModel.setOverlay(ActiveOverlay.STATS) },
                  onToggleHabit = { viewModel.toggleHabitDay(it) }
                )
              }
            }
          }
        }
      } else {
        // Sub-screens & Overlays
        when (uiState.activeOverlay) {
          ActiveOverlay.NOTE_DETAIL -> {
            val selectedNoteWithItems = notes.find { it.note.id == uiState.selectedNoteId }
            NoteDetailScreen(
              noteWithItems = selectedNoteWithItems,
              events = events,
              onBack = { viewModel.closeOverlay() },
              onSave = { id, title, body, catId, format, items, eventId, eventSummary, date ->
                viewModel.saveNote(id, title, body, catId, format, items, eventId, eventSummary, date)
              },
              onToggleItem = { itemId, currentDone ->
                viewModel.toggleChecklistItem(itemId, currentDone)
              },
              onDelete = { noteId ->
                viewModel.deleteNote(noteId)
              }
            )
          }
          ActiveOverlay.HABIT_DETAIL -> {
            val habitRes = habits.find { it.habit.id == uiState.selectedHabitId }
            HabitDetailScreen(
              habitRes = habitRes,
              onBack = { viewModel.closeOverlay() },
              onToggleToday = { uiState.selectedHabitId?.let { viewModel.toggleHabitDay(it) } },
              onToggleCell = { epochDay -> uiState.selectedHabitId?.let { viewModel.toggleHabitDay(it, epochDay) } },
              onMarkPastDays = { fromEpoch, toEpoch, markDone ->
                uiState.selectedHabitId?.let { viewModel.markPastHabitDays(it, fromEpoch, toEpoch, markDone) }
              },
              onUpdateReminder = { newTime ->
                if (habitRes != null) {
                  viewModel.updateHabitReminder(context, habitRes.habit.id, habitRes.habit.name, newTime)
                }
              },
              onTestNotification = { habitName ->
                viewModel.testNotification(context, habitName)
              },
              onEditRule = { viewModel.setOverlay(ActiveOverlay.HABIT_CREATE) },
              onDeleteHabit = { habitId ->
                viewModel.deleteHabit(habitId)
              }
            )
          }
          ActiveOverlay.HABIT_CREATE -> {
            val editingHabit = habits.find { it.habit.id == uiState.selectedHabitId }?.habit
            HabitCreateScreen(
              initialHabit = editingHabit,
              onBack = {
                if (editingHabit != null) {
                  viewModel.setOverlay(ActiveOverlay.HABIT_DETAIL)
                } else {
                  viewModel.closeOverlay()
                }
              },
              onSaveHabit = { name, repeatType, repeatDays, timesPerWeek, everyNDays, weeklyDayOfWeek, monthlyDayOfMonth, monthDayStart, monthDayEnd, durationDays, reminder, showInCal, startEpochDay, markPast ->
                viewModel.saveHabit(
                  id = editingHabit?.id,
                  name = name,
                  repeatType = repeatType,
                  repeatDays = repeatDays,
                  timesPerWeek = timesPerWeek,
                  everyNDays = everyNDays,
                  weeklyDayOfWeek = weeklyDayOfWeek,
                  monthlyDayOfMonth = monthlyDayOfMonth,
                  monthDayStart = monthDayStart,
                  monthDayEnd = monthDayEnd,
                  durationDays = durationDays,
                  reminderTime = reminder,
                  showInCalendar = showInCal,
                  startDateEpochDay = startEpochDay,
                  markPastDaysAsDone = markPast,
                  context = context
                )
              },
              onTestNotification = { habitName ->
                viewModel.testNotification(context, habitName)
              }
            )
          }
          ActiveOverlay.HABIT_CONCLUDED -> {
            HabitConcludedScreen(onBack = { viewModel.closeOverlay() })
          }
          ActiveOverlay.EVENT_CREATE -> {
            val defaultDate = uiState.selectedCalendarDate ?: uiState.selectedHojeDate ?: LocalDate.now()
            EventCreateScreen(
              calendars = calendars,
              initialDate = defaultDate,
              onBack = { viewModel.closeOverlay() },
              onSave = { title, calId, date, hour, min, dur, noteId, noteTitle, localOnly ->
                viewModel.saveEvent(title, calId, date, hour, min, dur, noteId, noteTitle, localOnly)
              }
            )
          }
          ActiveOverlay.EVENT_DETAIL -> {
            val selectedEvent = events.find { it.id == uiState.selectedEventId }
            val eventCalendar = calendars.find { it.id == selectedEvent?.calendarId }
            EventDetailScreen(
              event = selectedEvent,
              calendar = eventCalendar,
              onBack = { viewModel.closeOverlay() },
              onOpenNote = { noteId -> viewModel.openNote(noteId) },
              onAttachNote = { eventId -> viewModel.openNoteForEvent(eventId) },
              onDeleteEvent = { eventId -> viewModel.deleteEvent(eventId) },
              onUpdateEvent = { id, title, calId, date, hour, min, dur, noteId, noteTitle ->
                viewModel.updateEvent(id, title, calId, date, hour, min, dur, noteId, noteTitle)
              }
            )
          }
          ActiveOverlay.OFFLINE -> {
            OfflineScreen(
              syncQueue = syncQueue,
              onRetrySync = { viewModel.retrySync() },
              onBack = { viewModel.closeOverlay() }
            )
          }
          ActiveOverlay.SEARCH -> {
            SearchScreen(
              onClose = { viewModel.closeOverlay() },
              onOpenNote = { noteId ->
                viewModel.openNote(noteId)
              }
            )
          }
          ActiveOverlay.SETTINGS -> {
            SettingsScreen(
              isDarkTheme = isDarkTheme,
              onToggleTheme = onToggleTheme,
              onBack = { viewModel.closeOverlay() },
              calendars = calendars,
              onToggleCalendar = { calId -> viewModel.toggleCalendarSelection(calId) },
              onToggleAccount = { email, isSelected -> viewModel.toggleAccountSelection(email, isSelected) },
              onSelectAllCalendars = { isSelected -> viewModel.selectAllCalendars(isSelected) },
              onClearData = { viewModel.clearAllData() },
              onSyncCalendar = {
                val hasPermission = ContextCompat.checkSelfPermission(
                  context,
                  Manifest.permission.READ_CALENDAR
                ) == PackageManager.PERMISSION_GRANTED
                if (hasPermission) {
                  viewModel.syncDeviceCalendar("thiagovinicius7@gmail.com")
                } else {
                  permissionLauncher.launch(
                    arrayOf(
                      Manifest.permission.READ_CALENDAR,
                      Manifest.permission.WRITE_CALENDAR
                    )
                  )
                }
              },
              onCreateBackup = { onDone ->
                viewModel.createBackup(context, onDone)
              },
              onShareBackup = { json ->
                viewModel.shareBackupFile(context, json)
              },
              onGetLastBackupJson = {
                viewModel.getLastBackupJson(context)
              },
              onRestoreLastBackup = { onResult ->
                viewModel.restoreLastBackup(context, onResult)
              },
              onRestoreCustomBackup = { json, onResult ->
                viewModel.restoreFromBackupJson(context, json, onResult)
              },
              lastBackupInfo = viewModel.getLastBackupInfo(context)
            )
          }
          ActiveOverlay.ONBOARDING -> {
            OnboardingScreen(
              onConnect = { viewModel.closeOverlay() },
              onSkip = { viewModel.closeOverlay() }
            )
          }
          ActiveOverlay.STATS -> {
            StatsScreen(onBack = { viewModel.closeOverlay() })
          }
          ActiveOverlay.BILL_CREATE -> {
            BillDetailScreen(
              billWithStatus = null,
              onBack = { viewModel.closeOverlay() },
              onSave = { id, title, amt, isVar, cat, repeatType, dueDayMonth, dueDayWeek, startEpoch, customInterval, remDays, remTime, notes, barcode ->
                viewModel.saveBill(id, title, amt, isVar, cat, repeatType, dueDayMonth, dueDayWeek, startEpoch, customInterval, remDays, remTime, notes, barcode, context)
              },
              onDelete = { /* no-op for create */ },
              onTogglePayment = { /* no-op for create */ },
              onTestNotification = { title, amt ->
                viewModel.testBillNotification(context, title, amt)
              }
            )
          }
          ActiveOverlay.BILL_DETAIL -> {
            val selectedBillWithStatus = bills.find { it.bill.id == uiState.selectedBillId }
            BillDetailScreen(
              billWithStatus = selectedBillWithStatus,
              onBack = { viewModel.closeOverlay() },
              onSave = { id, title, amt, isVar, cat, repeatType, dueDayMonth, dueDayWeek, startEpoch, customInterval, remDays, remTime, notes, barcode ->
                viewModel.saveBill(id, title, amt, isVar, cat, repeatType, dueDayMonth, dueDayWeek, startEpoch, customInterval, remDays, remTime, notes, barcode, context)
              },
              onDelete = { billId ->
                viewModel.deleteBill(billId, context)
              },
              onTogglePayment = { billStatus ->
                viewModel.toggleBillPayment(billStatus)
              },
              onTestNotification = { title, amt ->
                viewModel.testBillNotification(context, title, amt)
              }
            )
          }
          ActiveOverlay.NONE -> {}
        }
      }
    }
  }
}

@Composable
fun BlocoTopTabBar(
  currentSection: TopSection,
  onSelectSection: (TopSection) -> Unit,
  onOpenSearch: () -> Unit,
  onOpenSettings: () -> Unit,
  modifier: Modifier = Modifier
) {
  val colors = LocalBlocoColors.current

  Column(modifier = modifier.fillMaxWidth()) {
    // App Brand Title Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(colors.canvas)
        .padding(horizontal = 16.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(10.dp)
            .background(colors.accent)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "BLOCO T",
          fontFamily = ArchivoFont,
          fontWeight = FontWeight.ExtraBold,
          fontSize = 14.sp,
          letterSpacing = 0.5.sp,
          color = colors.text
        )
      }

      // ⌕ and ⚙ Actions
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .border(1.dp, colors.rulerWeak, RectangleShape)
            .clickable(onClick = onOpenSearch)
            .testTag("btn_search"),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "⌕",
            fontFamily = ArchivoFont,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = colors.text
          )
        }
        Box(
          modifier = Modifier
            .size(36.dp)
            .border(1.dp, colors.rulerWeak, RectangleShape)
            .clickable(onClick = onOpenSettings)
            .testTag("btn_settings"),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "⚙",
            fontFamily = ArchivoFont,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = colors.text
          )
        }
      }
    }

    Ruler1dp()

    // Section Tabs Row (HOJE, MURAL, CONTAS, AGENDA, HÁBITOS)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(colors.canvas)
        .horizontalScroll(rememberScrollState())
        .padding(horizontal = 16.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.spacedBy(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      TopTabItem(
        title = "HOJE",
        isSelected = currentSection == TopSection.HOJE,
        testTag = "tab_hoje",
        onClick = { onSelectSection(TopSection.HOJE) }
      )
      TopTabItem(
        title = "MURAL",
        isSelected = currentSection == TopSection.MURAL,
        testTag = "tab_mural",
        onClick = { onSelectSection(TopSection.MURAL) }
      )
      TopTabItem(
        title = "CONTAS",
        isSelected = currentSection == TopSection.CONTAS,
        testTag = "tab_contas",
        onClick = { onSelectSection(TopSection.CONTAS) }
      )
      TopTabItem(
        title = "AGENDA",
        isSelected = currentSection == TopSection.AGENDA,
        testTag = "tab_agenda",
        onClick = { onSelectSection(TopSection.AGENDA) }
      )
      TopTabItem(
        title = "HÁBITOS",
        isSelected = currentSection == TopSection.HABITOS,
        testTag = "tab_habitos",
        onClick = { onSelectSection(TopSection.HABITOS) }
      )
    }

    Ruler2dp()
  }
}

@Composable
private fun TopTabItem(
  title: String,
  isSelected: Boolean,
  testTag: String,
  onClick: () -> Unit
) {
  val colors = LocalBlocoColors.current
  Column(
    modifier = Modifier
      .clickable(onClick = onClick)
      .testTag(testTag)
      .padding(vertical = 4.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      text = title,
      style = SectionLabelStyle,
      color = if (isSelected) colors.text else colors.textTertiary
    )
    Spacer(modifier = Modifier.height(4.dp))
    Box(
      modifier = Modifier
        .width(if (isSelected) 24.dp else 0.dp)
        .height(2.dp)
        .background(if (isSelected) colors.accent else Color.Transparent)
    )
  }
}
