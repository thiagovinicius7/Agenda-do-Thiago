package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.BlocoDatabase
import com.example.data.model.HabitCalculationResult
import com.example.data.model.Note
import com.example.data.model.NoteFormat
import com.example.data.model.NoteWithItems
import com.example.data.repository.BlocoRepository
import com.example.ui.components.Ruler2dp
import com.example.ui.screens.AgendaScreen
import com.example.ui.screens.EventCreateScreen
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
    val repository = BlocoRepository(
      noteDao = database.noteDao(),
      habitDao = database.habitDao(),
      calendarDao = database.calendarDao(),
      syncQueueDao = database.syncQueueDao()
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
  val colors = LocalBlocoColors.current
  val uiState by viewModel.uiState.collectAsState()

  val notes by viewModel.notesWithItems.collectAsState()
  val habits by viewModel.habitsWithCalculations.collectAsState()
  val events by viewModel.events.collectAsState()
  val syncQueue by viewModel.syncQueue.collectAsState()

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
            onSelectSection = { viewModel.setSection(it) },
            onOpenSearch = { viewModel.setOverlay(ActiveOverlay.SEARCH) },
            onOpenSettings = { viewModel.setOverlay(ActiveOverlay.SETTINGS) }
          )

          // Content based on selected Top Section
          Box(modifier = Modifier.weight(1f)) {
            when (uiState.currentSection) {
              TopSection.HOJE -> {
                HojeScreen(
                  habits = habits,
                  notes = notes,
                  onToggleHabit = { viewModel.toggleHabitDay(it) },
                  onOpenHabit = { habitId -> viewModel.openHabit(habitId) },
                  onOpenNote = { noteId -> viewModel.openNote(noteId) }
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
              TopSection.AGENDA -> {
                AgendaScreen(
                  events = events,
                  viewMode = uiState.calendarViewMode,
                  selectedDate = uiState.selectedCalendarDate,
                  onSelectViewMode = { viewModel.setCalendarViewMode(it) },
                  onSelectDate = { viewModel.selectCalendarDate(it) },
                  onCreateEvent = { viewModel.setOverlay(ActiveOverlay.EVENT_CREATE) },
                  onOpenOffline = { viewModel.setOverlay(ActiveOverlay.OFFLINE) }
                )
              }
              TopSection.HABITOS -> {
                HabitsListScreen(
                  habits = habits,
                  onOpenHabit = { habitId -> viewModel.openHabit(habitId) },
                  onCreateHabit = { viewModel.setOverlay(ActiveOverlay.HABIT_CREATE) },
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
              onBack = { viewModel.closeOverlay() },
              onSave = { id, title, body, catId, format, items ->
                viewModel.saveNote(id, title, body, catId, format, items)
              },
              onToggleItem = { itemId, currentDone ->
                viewModel.toggleChecklistItem(itemId, currentDone)
              }
            )
          }
          ActiveOverlay.HABIT_DETAIL -> {
            val habitRes = habits.find { it.habit.id == uiState.selectedHabitId }
            HabitDetailScreen(
              habitRes = habitRes,
              onBack = { viewModel.closeOverlay() },
              onToggleToday = { uiState.selectedHabitId?.let { viewModel.toggleHabitDay(it) } },
              onEditRule = { viewModel.setOverlay(ActiveOverlay.HABIT_CREATE) }
            )
          }
          ActiveOverlay.HABIT_CREATE -> {
            HabitCreateScreen(
              onBack = { viewModel.closeOverlay() },
              onSaveHabit = { name, repeatType, repeatDays, durationDays, reminder, showInCal ->
                viewModel.saveHabit(name, repeatType, repeatDays, durationDays, reminder, showInCal)
              }
            )
          }
          ActiveOverlay.HABIT_CONCLUDED -> {
            HabitConcludedScreen(onBack = { viewModel.closeOverlay() })
          }
          ActiveOverlay.EVENT_CREATE -> {
            EventCreateScreen(
              onBack = { viewModel.closeOverlay() },
              onSave = { title, calId, date, hour, min, dur, noteId, noteTitle, localOnly ->
                viewModel.saveEvent(title, calId, date, hour, min, dur, noteId, noteTitle, localOnly)
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
              onBack = { viewModel.closeOverlay() }
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
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(colors.canvas)
        .padding(horizontal = 16.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // 4 Section Tabs
      Row(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
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

      // ⌕ and ⚙ Actions
      Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(32.dp)
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
            .size(32.dp)
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
