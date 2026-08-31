package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HabitCalculationResult
import com.example.data.model.NoteWithItems
import com.example.ui.components.HabitGrid
import com.example.ui.components.HabitGridMode
import com.example.ui.components.ModernistButton
import com.example.ui.components.ModernistCheckbox
import com.example.ui.components.ModernistSwitch
import com.example.ui.components.Ruler1dp
import com.example.ui.components.Ruler2dp
import com.example.ui.theme.ArchivoFont
import com.example.ui.theme.BigStatStyle
import com.example.ui.theme.LocalBlocoColors
import com.example.ui.theme.SectionLabelStyle
import com.example.util.HabitCalculations

@Composable
fun SearchScreen(
  onClose: () -> Unit,
  onOpenNote: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val colors = LocalBlocoColors.current
  val scrollState = rememberScrollState()
  var query by remember { mutableStateOf("proposta") }
  var selectedFilter by remember { mutableStateOf("tudo") }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(colors.canvas)
  ) {
    // Search input row
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .border(0.dp, Color.Transparent)
          .padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(text = "⌕", fontFamily = ArchivoFont, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = colors.textTertiary)
        Spacer(modifier = Modifier.width(10.dp))
        BasicTextField(
          value = query,
          onValueChange = { query = it },
          textStyle = TextStyle(fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = colors.text),
          cursorBrush = SolidColor(colors.accent),
          modifier = Modifier.weight(1f)
        )
        Text(
          text = "Fechar",
          fontFamily = ArchivoFont,
          fontWeight = FontWeight.SemiBold,
          fontSize = 11.sp,
          color = colors.textSecondary,
          modifier = Modifier.clickable(onClick = onClose)
        )
      }
      Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(colors.text))

      Spacer(modifier = Modifier.height(12.dp))

      // Filter chips
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        SearchFilterChip("Tudo 6", isSelected = selectedFilter == "tudo") { selectedFilter = "tudo" }
        SearchFilterChip("Notas 3", isSelected = selectedFilter == "notas") { selectedFilter = "notas" }
        SearchFilterChip("Agenda 2", isSelected = selectedFilter == "agenda") { selectedFilter = "agenda" }
        SearchFilterChip("Hábitos 1", isSelected = selectedFilter == "habitos") { selectedFilter = "habitos" }
      }
    }

    Ruler2dp()

    Column(
      modifier = Modifier
        .weight(1f)
        .verticalScroll(scrollState)
    ) {
      // Group 1: Notas e checklists
      if (selectedFilter in listOf("tudo", "notas")) {
        Text(text = "NOTAS E CHECKLISTS", style = SectionLabelStyle, color = colors.textTertiary, modifier = Modifier.padding(16.dp, 14.dp, 16.dp, 8.dp))

        SearchResultRow(
          stripeColor = colors.accent,
          title = "Reunião cliente",
          snippet = "Levar proposta impressa",
          highlightTerm = "proposta",
          meta = "Trabalho · item de checklist",
          onClick = { onOpenNote("n_trabalho_1") }
        )
        Ruler1dp()

        SearchResultRow(
          stripeColor = colors.accent,
          title = "Escopo fase 1",
          snippet = "Duas opções de proposta, prazo curto",
          highlightTerm = "proposta",
          meta = "Trabalho · nota · 28 ago",
          onClick = { onOpenNote("n_trabalho_1") }
        )
        Ruler1dp()

        SearchResultRow(
          stripeColor = colors.gridFail,
          title = "Arquivadas",
          snippet = "1 nota com “proposta”",
          highlightTerm = "proposta",
          meta = "Arquivo",
          onClick = {}
        )
        Ruler1dp()
      }

      // Group 2: Agenda
      if (selectedFilter in listOf("tudo", "agenda")) {
        Text(text = "AGENDA", style = SectionLabelStyle, color = colors.textTertiary, modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 8.dp))

        SearchResultRow(
          stripeColor = colors.text,
          title = "Reunião cliente",
          snippet = "Sex, 30 ago · 15:00 — 16:00",
          highlightTerm = "",
          meta = "Trabalho (Google) · 1 post-it",
          onClick = {}
        )
        Ruler1dp()

        SearchResultRow(
          stripeColor = colors.text,
          title = "Enviar proposta revisada",
          snippet = "Qua, 4 set · 09:00",
          highlightTerm = "",
          meta = "Trabalho (Google)",
          onClick = {}
        )
        Ruler1dp()
      }

      // Group 3: Hábitos
      if (selectedFilter in listOf("tudo", "habitos")) {
        Text(text = "HÁBITOS", style = SectionLabelStyle, color = colors.textTertiary, modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 8.dp))

        SearchResultRow(
          stripeColor = colors.gridFail,
          title = "Escrever proposta diária",
          snippet = "Pausado · sequência guardada d9",
          highlightTerm = "",
          meta = "Hábitos",
          onClick = {}
        )
      }

      Spacer(modifier = Modifier.height(40.dp))
    }
  }
}

@Composable
fun SettingsScreen(
  isDarkTheme: Boolean,
  onToggleTheme: () -> Unit,
  onBack: () -> Unit,
  onClearData: (() -> Unit)? = null,
  onSyncCalendar: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val colors = LocalBlocoColors.current
  val scrollState = rememberScrollState()

  // User State
  var userName by remember { mutableStateOf("Thiago Vinicius") }
  var userEmail by remember { mutableStateOf("thiagovinicius7@gmail.com") }
  var isGoogleConnected by remember { mutableStateOf(true) }

  // Settings Toggles
  var bgSync by remember { mutableStateOf(true) }
  var habitsInCalendar by remember { mutableStateOf(true) }
  var autoArchive by remember { mutableStateOf(true) }
  var countInDays by remember { mutableStateOf(true) }
  var weekStart by remember { mutableStateOf("Domingo") }
  var lastSyncText by remember { mutableStateOf("agora mesmo") }
  var lastBackupText by remember { mutableStateOf("hoje 06:00") }

  // Categories list
  var categories by remember {
    mutableStateOf(
      listOf(
        Triple("Trabalho", colors.postItWorkBg, colors.accent),
        Triple("Pessoal", colors.postItPersonalBg, colors.text),
        Triple("Estudo", colors.postItStudyBg, colors.gridFail)
      )
    )
  }

  // Calendars
  var activeCalendars by remember {
    mutableStateOf(
      listOf(
        Pair("Pessoal", true),
        Pair("Trabalho", true),
        Pair("Faculdade", false),
        Pair("Feriados no Brasil", true)
      )
    )
  }

  // Dialog States
  var showAccountDialog by remember { mutableStateOf(false) }
  var showDisconnectDialog by remember { mutableStateOf(false) }
  var showCalendarsDialog by remember { mutableStateOf(false) }
  var showNewCategoryDialog by remember { mutableStateOf(false) }
  var showWeekStartDialog by remember { mutableStateOf(false) }
  var showExportDialog by remember { mutableStateOf(false) }
  var showBackupDialog by remember { mutableStateOf(false) }
  var showArchiveDialog by remember { mutableStateOf(false) }
  var showClearDataDialog by remember { mutableStateOf(false) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(colors.canvas)
  ) {
    // Header
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
      Text(
        text = "← Voltar",
        fontFamily = ArchivoFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        color = colors.textSecondary,
        modifier = Modifier.clickable(onClick = onBack)
      )
      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = "Ajustes",
        fontFamily = ArchivoFont,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 30.sp,
        color = colors.text
      )
    }

    Ruler2dp()

    Column(
      modifier = Modifier
        .weight(1f)
        .verticalScroll(scrollState)
    ) {
      // Google Calendar Sync Hero Card
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(colors.canvas)
          .padding(16.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(10.dp)
                .background(if (isGoogleConnected) colors.accent else colors.textTertiary)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "GOOGLE AGENDA",
              fontFamily = ArchivoFont,
              fontWeight = FontWeight.ExtraBold,
              fontSize = 13.sp,
              letterSpacing = 0.5.sp,
              color = colors.text
            )
          }

          Box(
            modifier = Modifier
              .background(if (isGoogleConnected) colors.accent.copy(alpha = 0.15f) else colors.track)
              .border(1.dp, if (isGoogleConnected) colors.accent else colors.rulerWeak, RectangleShape)
              .padding(horizontal = 8.dp, vertical = 3.dp)
          ) {
            Text(
              text = if (isGoogleConnected) "● CONECTADO" else "OFFLINE",
              fontFamily = ArchivoFont,
              fontWeight = FontWeight.Bold,
              fontSize = 10.sp,
              color = if (isGoogleConnected) colors.accentDark else colors.textTertiary
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Account Box
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colors.rulerStrong, RectangleShape)
            .clickable { showAccountDialog = true }
            .padding(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .background(colors.text),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = userName.firstOrNull()?.uppercase() ?: "T",
              fontFamily = ArchivoFont,
              fontWeight = FontWeight.ExtraBold,
              fontSize = 15.sp,
              color = colors.canvas
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = userName,
              fontFamily = ArchivoFont,
              fontWeight = FontWeight.ExtraBold,
              fontSize = 13.sp,
              color = colors.text
            )
            Text(
              text = userEmail,
              fontFamily = ArchivoFont,
              fontWeight = FontWeight.Normal,
              fontSize = 11.sp,
              color = colors.textSecondary
            )
          }
          Text(
            text = "Trocar",
            fontFamily = ArchivoFont,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = colors.accentDark
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Sync & Manage Calendars row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Box(
            modifier = Modifier
              .weight(1f)
              .border(1.dp, colors.rulerStrong, RectangleShape)
              .clickable {
                lastSyncText = "agora mesmo"
                onSyncCalendar?.invoke()
              }
              .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "↻ Sincronizar Agora",
              fontFamily = ArchivoFont,
              fontWeight = FontWeight.Bold,
              fontSize = 11.5.sp,
              color = colors.text
            )
          }

          Box(
            modifier = Modifier
              .weight(1f)
              .border(1.dp, colors.rulerStrong, RectangleShape)
              .clickable { showCalendarsDialog = true }
              .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "Calendários (${activeCalendars.count { it.second }})",
              fontFamily = ArchivoFont,
              fontWeight = FontWeight.Bold,
              fontSize = 11.5.sp,
              color = colors.text
            )
          }
        }
      }

      Ruler2dp()

      // Theme toggle row
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clickable(onClick = onToggleTheme)
          .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(text = "Modo escuro (Modernist Dark)", fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = colors.text)
          Text(text = if (isDarkTheme) "Fundo escuro (#201e1d) ativado" else "Fundo claro (#f3f2f2) ativado", fontFamily = ArchivoFont, fontSize = 10.5.sp, color = colors.textSecondary)
        }
        ModernistSwitch(checked = isDarkTheme, onCheckedChange = { onToggleTheme() })
      }
      Ruler1dp()

      // Group 1: Configurações de Sincronização
      Text(text = "OPÇÕES DE SINCRONIZAÇÃO", style = SectionLabelStyle, color = colors.textTertiary, modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 8.dp))
      
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { showCalendarsDialog = true }
          .padding(horizontal = 16.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(text = "Calendários do Google", fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = colors.text)
          Text(
            text = activeCalendars.filter { it.second }.joinToString(", ") { it.first }.ifEmpty { "Nenhum ativo" },
            fontFamily = ArchivoFont,
            fontSize = 10.5.sp,
            color = colors.textSecondary
          )
        }
        Text(text = "→", fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = colors.text)
      }
      Ruler1dp()
      
      SettingsRowWithSwitch("Sincronizar em segundo plano", null, bgSync) { bgSync = it }
      Ruler1dp()
      SettingsRowWithSwitch("Hábitos na agenda", "Só dentro do app, não no Google.", habitsInCalendar) { habitsInCalendar = it }
      Ruler1dp()
      
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clickable {
            lastSyncText = "agora mesmo"
          }
          .padding(horizontal = 16.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(text = "Última sincronização", fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = colors.text)
          Text(text = "Toque para sincronizar agora", fontFamily = ArchivoFont, fontSize = 10.sp, color = colors.textTertiary)
        }
        Text(text = lastSyncText, fontFamily = ArchivoFont, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = colors.accent)
      }

      Spacer(modifier = Modifier.height(8.dp))
      Ruler2dp()

      // Group 2: Mural
      Text(text = "MURAL", style = SectionLabelStyle, color = colors.textTertiary, modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 8.dp))
      
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { showNewCategoryDialog = true }
          .padding(horizontal = 16.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(text = "Categorias e cores", fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = colors.text)
        Text(text = "→", fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = colors.text)
      }
      Ruler1dp()
      
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        categories.forEach { cat ->
          CategoryPill(cat.first, cat.second, cat.third)
        }
        Box(
          modifier = Modifier
            .border(1.dp, colors.rulerStrong, RectangleShape)
            .clickable { showNewCategoryDialog = true }
            .padding(horizontal = 9.dp, vertical = 6.dp)
        ) {
          Text(text = "+ nova", fontFamily = ArchivoFont, fontSize = 10.5.sp, color = colors.accent)
        }
      }
      Ruler1dp()
      
      SettingsRowWithSwitch("Concluídos vão para o arquivo", null, autoArchive) { autoArchive = it }
      Ruler1dp()
      
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { showArchiveDialog = true }
          .padding(horizontal = 16.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(text = "Arquivo", fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = colors.text)
        Text(text = "31 notas →", fontFamily = ArchivoFont, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = colors.textSecondary)
      }

      Spacer(modifier = Modifier.height(8.dp))
      Ruler2dp()

      // Group 3: Hábitos
      Text(text = "HÁBITOS", style = SectionLabelStyle, color = colors.textTertiary, modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 8.dp))
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { showWeekStartDialog = true }
          .padding(horizontal = 16.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(text = "Início da semana", fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = colors.text)
          Text(text = "Afeta “X× por semana”.", fontFamily = ArchivoFont, fontSize = 10.5.sp, color = colors.textSecondary)
        }
        Box(modifier = Modifier.border(1.dp, colors.rulerStrong, RectangleShape).padding(horizontal = 12.dp, vertical = 8.dp)) {
          Text(text = weekStart, fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = colors.text)
        }
      }
      Ruler1dp()
      SettingsRowWithSwitch("Contar em dias (d62)", "Desligado mostra só a data.", countInDays) { countInDays = it }

      Spacer(modifier = Modifier.height(8.dp))
      Ruler2dp()

      // Group 4: Dados
      Text(text = "DADOS", style = SectionLabelStyle, color = colors.textTertiary, modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 8.dp))
      
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { showExportDialog = true }
          .padding(horizontal = 16.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(text = "Exportar tudo", fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = colors.text)
          Text(text = "Toque para salvar Markdown e CSV", fontFamily = ArchivoFont, fontSize = 10.5.sp, color = colors.textSecondary)
        }
        Text(text = "Markdown, CSV →", fontFamily = ArchivoFont, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = colors.text)
      }
      Ruler1dp()
      
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clickable {
            lastBackupText = "hoje agora"
            showBackupDialog = true
          }
          .padding(horizontal = 16.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(text = "Backup no aparelho", fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = colors.text)
          Text(text = "Salvar cópia completa local", fontFamily = ArchivoFont, fontSize = 10.5.sp, color = colors.textSecondary)
        }
        Text(text = "$lastBackupText →", fontFamily = ArchivoFont, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = colors.textSecondary)
      }
      Ruler1dp()

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { showClearDataDialog = true }
          .padding(horizontal = 16.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(text = "Limpar dados pré-cadastrados", fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = colors.gridFail)
          Text(text = "Apagar post-its, hábitos e eventos de demonstração", fontFamily = ArchivoFont, fontSize = 10.5.sp, color = colors.textSecondary)
        }
        Text(text = "Apagar →", fontFamily = ArchivoFont, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = colors.gridFail)
      }
      Ruler1dp()
      
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clickable {
            if (isGoogleConnected) {
              showDisconnectDialog = true
            } else {
              isGoogleConnected = true
            }
          }
          .padding(horizontal = 16.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (isGoogleConnected) "Desconectar do Google" else "Conectar ao Google",
          fontFamily = ArchivoFont,
          fontWeight = FontWeight.ExtraBold,
          fontSize = 13.sp,
          color = if (isGoogleConnected) colors.accentDark else colors.accent
        )
        Text(
          text = "→",
          fontFamily = ArchivoFont,
          fontWeight = FontWeight.ExtraBold,
          fontSize = 13.sp,
          color = if (isGoogleConnected) colors.accentDark else colors.accent
        )
      }

      Spacer(modifier = Modifier.height(40.dp))
    }
  }

  // --- DIALOGS ---

  // 1. Account Edit Dialog
  if (showAccountDialog) {
    var editName by remember { mutableStateOf(userName) }
    var editEmail by remember { mutableStateOf(userEmail) }

    ModernistSimpleDialog(
      title = "EDITAR CONTA",
      onDismiss = { showAccountDialog = false }
    ) {
      Text("Nome do usuário:", fontFamily = ArchivoFont, fontSize = 11.sp, color = colors.textSecondary)
      Spacer(modifier = Modifier.height(4.dp))
      BasicTextField(
        value = editName,
        onValueChange = { editName = it },
        textStyle = TextStyle(fontFamily = ArchivoFont, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = colors.text),
        modifier = Modifier
          .fillMaxWidth()
          .background(colors.track)
          .padding(10.dp)
      )
      Spacer(modifier = Modifier.height(12.dp))
      Text("E-mail Google:", fontFamily = ArchivoFont, fontSize = 11.sp, color = colors.textSecondary)
      Spacer(modifier = Modifier.height(4.dp))
      BasicTextField(
        value = editEmail,
        onValueChange = { editEmail = it },
        textStyle = TextStyle(fontFamily = ArchivoFont, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = colors.text),
        modifier = Modifier
          .fillMaxWidth()
          .background(colors.track)
          .padding(10.dp)
      )
      Spacer(modifier = Modifier.height(16.dp))
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ModernistButton(
          text = "Cancelar",
          onClick = { showAccountDialog = false },
          isPrimary = false,
          modifier = Modifier.weight(1f)
        )
        ModernistButton(
          text = "Salvar",
          onClick = {
            userName = editName.trim()
            userEmail = editEmail.trim()
            showAccountDialog = false
          },
          modifier = Modifier.weight(1f)
        )
      }
    }
  }

  // 2. Disconnect Dialog
  if (showDisconnectDialog) {
    ModernistSimpleDialog(
      title = "DESCONECTAR CONTA",
      onDismiss = { showDisconnectDialog = false }
    ) {
      Text(
        text = "Deseja desconectar a conta ${userEmail}? Seus post-its e hábitos locais serão mantidos no dispositivo.",
        fontFamily = ArchivoFont,
        fontSize = 12.5.sp,
        color = colors.text
      )
      Spacer(modifier = Modifier.height(16.dp))
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ModernistButton(
          text = "Manter",
          onClick = { showDisconnectDialog = false },
          isPrimary = false,
          modifier = Modifier.weight(1f)
        )
        ModernistButton(
          text = "Desconectar",
          onClick = {
            isGoogleConnected = false
            showDisconnectDialog = false
          },
          modifier = Modifier.weight(1f)
        )
      }
    }
  }

  // 3. Calendars Dialog
  if (showCalendarsDialog) {
    ModernistSimpleDialog(
      title = "CALENDÁRIOS DO GOOGLE",
      onDismiss = { showCalendarsDialog = false }
    ) {
      Text("Selecione quais calendários exibir na Agenda:", fontFamily = ArchivoFont, fontSize = 11.5.sp, color = colors.textSecondary)
      Spacer(modifier = Modifier.height(10.dp))
      Column(modifier = Modifier.fillMaxWidth().border(1.dp, colors.rulerStrong, RectangleShape)) {
        activeCalendars.forEachIndexed { idx, (calName, isChecked) ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable {
                activeCalendars = activeCalendars.toMutableList().also {
                  it[idx] = Pair(calName, !isChecked)
                }
              }
              .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(text = calName, fontFamily = ArchivoFont, fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = colors.text)
            ModernistCheckbox(
              checked = isChecked,
              onCheckedChange = {
                activeCalendars = activeCalendars.toMutableList().also { list ->
                  list[idx] = Pair(calName, !isChecked)
                }
              }
            )
          }
          if (idx < activeCalendars.lastIndex) {
            Ruler1dp()
          }
        }
      }
      Spacer(modifier = Modifier.height(14.dp))
      ModernistButton(
        text = "Pronto",
        onClick = { showCalendarsDialog = false },
        modifier = Modifier.fillMaxWidth()
      )
    }
  }

  // 4. New Category Dialog
  if (showNewCategoryDialog) {
    var newCatName by remember { mutableStateOf("") }
    ModernistSimpleDialog(
      title = "NOVA CATEGORIA",
      onDismiss = { showNewCategoryDialog = false }
    ) {
      Text("Nome da categoria:", fontFamily = ArchivoFont, fontSize = 11.sp, color = colors.textSecondary)
      Spacer(modifier = Modifier.height(4.dp))
      BasicTextField(
        value = newCatName,
        onValueChange = { newCatName = it },
        textStyle = TextStyle(fontFamily = ArchivoFont, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = colors.text),
        modifier = Modifier
          .fillMaxWidth()
          .background(colors.track)
          .padding(10.dp)
      )
      Spacer(modifier = Modifier.height(14.dp))
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ModernistButton(
          text = "Cancelar",
          onClick = { showNewCategoryDialog = false },
          isPrimary = false,
          modifier = Modifier.weight(1f)
        )
        ModernistButton(
          text = "Criar",
          onClick = {
            if (newCatName.isNotBlank()) {
              categories = categories + Triple(newCatName.trim(), colors.postItStudyBg, colors.accent)
            }
            showNewCategoryDialog = false
          },
          modifier = Modifier.weight(1f)
        )
      }
    }
  }

  // 5. Week Start Dialog
  if (showWeekStartDialog) {
    ModernistSimpleDialog(
      title = "INÍCIO DA SEMANA",
      onDismiss = { showWeekStartDialog = false }
    ) {
      listOf("Domingo", "Segunda-feira").forEach { option ->
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable {
              weekStart = option
              showWeekStartDialog = false
            }
            .padding(vertical = 12.dp, horizontal = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(text = option, fontFamily = ArchivoFont, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = colors.text)
          if (weekStart == option) {
            Text(text = "✓", fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = colors.accent)
          }
        }
        Ruler1dp()
      }
    }
  }

  // 6. Export Dialog
  if (showExportDialog) {
    ModernistSimpleDialog(
      title = "EXPORTAR DADOS",
      onDismiss = { showExportDialog = false }
    ) {
      Text(
        text = "Todos os seus post-its (Markdown) e histórico de hábitos (CSV) foram preparados para exportação.",
        fontFamily = ArchivoFont,
        fontSize = 12.5.sp,
        color = colors.text
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "✓ 31 notas em .md\n✓ 4 hábitos em .csv\n✓ Salvo no armazenamento local",
        fontFamily = ArchivoFont,
        fontSize = 11.5.sp,
        color = colors.textSecondary
      )
      Spacer(modifier = Modifier.height(14.dp))
      ModernistButton(
        text = "Concluir",
        onClick = { showExportDialog = false },
        modifier = Modifier.fillMaxWidth()
      )
    }
  }

  // 7. Backup Dialog
  if (showBackupDialog) {
    ModernistSimpleDialog(
      title = "BACKUP REALIZADO",
      onDismiss = { showBackupDialog = false }
    ) {
      Text(
        text = "Cópia de segurança do banco de dados local gerada com sucesso.",
        fontFamily = ArchivoFont,
        fontSize = 12.5.sp,
        color = colors.text
      )
      Spacer(modifier = Modifier.height(14.dp))
      ModernistButton(
        text = "OK",
        onClick = { showBackupDialog = false },
        modifier = Modifier.fillMaxWidth()
      )
    }
  }

  // 8. Archive Dialog
  if (showArchiveDialog) {
    ModernistSimpleDialog(
      title = "ARQUIVO DE NOTAS",
      onDismiss = { showArchiveDialog = false }
    ) {
      Text(
        text = "Você tem 31 notas e checklists concluídos no histórico.",
        fontFamily = ArchivoFont,
        fontSize = 12.sp,
        color = colors.text
      )
      Spacer(modifier = Modifier.height(10.dp))
      Column(modifier = Modifier.fillMaxWidth().border(1.dp, colors.rulerStrong, RectangleShape)) {
        listOf("Proposta Fase 1 (Concluído)", "Compras de Escritório (Concluído)", "Planejamento Trimestral (Concluído)").forEachIndexed { i, title ->
          Row(modifier = Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = title, fontFamily = ArchivoFont, fontSize = 11.5.sp, color = colors.textSecondary)
            Text(text = "Restaurar", fontFamily = ArchivoFont, fontSize = 10.5.sp, color = colors.accentDark, modifier = Modifier.clickable { })
          }
          if (i < 2) Ruler1dp()
        }
      }
      Spacer(modifier = Modifier.height(14.dp))
      ModernistButton(
        text = "Fechar",
        onClick = { showArchiveDialog = false },
        modifier = Modifier.fillMaxWidth()
      )
    }
  }

  // 9. Clear Data Dialog
  if (showClearDataDialog) {
    ModernistSimpleDialog(
      title = "LIMPAR DADOS",
      onDismiss = { showClearDataDialog = false }
    ) {
      Text(
        text = "Deseja apagar todos os post-its, hábitos e eventos de demonstração para deixar o aplicativo 100% limpo com seus dados reais?",
        fontFamily = ArchivoFont,
        fontSize = 12.5.sp,
        color = colors.text
      )
      Spacer(modifier = Modifier.height(16.dp))
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ModernistButton(
          text = "Cancelar",
          onClick = { showClearDataDialog = false },
          isPrimary = false,
          modifier = Modifier.weight(1f)
        )
        ModernistButton(
          text = "Apagar Tudo",
          onClick = {
            onClearData?.invoke()
            showClearDataDialog = false
          },
          modifier = Modifier.weight(1f)
        )
      }
    }
  }
}

@Composable
fun ModernistSimpleDialog(
  title: String,
  onDismiss: () -> Unit,
  content: @Composable () -> Unit
) {
  val colors = LocalBlocoColors.current
  androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(colors.canvas)
        .border(2.dp, colors.text, RectangleShape)
        .padding(16.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(text = title, style = SectionLabelStyle, color = colors.text)
        Text(
          text = "✕",
          fontFamily = ArchivoFont,
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp,
          color = colors.textSecondary,
          modifier = Modifier.clickable(onClick = onDismiss)
        )
      }
      Spacer(modifier = Modifier.height(10.dp))
      Ruler2dp()
      Spacer(modifier = Modifier.height(12.dp))
      content()
    }
  }
}

@Composable
fun OnboardingScreen(
  onConnect: () -> Unit,
  onSkip: () -> Unit,
  modifier: Modifier = Modifier
) {
  val colors = LocalBlocoColors.current
  val scrollState = rememberScrollState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(colors.canvas)
      .verticalScroll(scrollState)
      .padding(16.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(text = "BLOCO", style = SectionLabelStyle, color = colors.text)
      Text(text = "PASSO 2 DE 3", style = SectionLabelStyle, color = colors.textTertiary)
    }

    Spacer(modifier = Modifier.height(10.dp))
    Ruler2dp()
    Spacer(modifier = Modifier.height(18.dp))

    Text(
      text = "Conecte\nsua agenda",
      fontFamily = ArchivoFont,
      fontWeight = FontWeight.ExtraBold,
      fontSize = 40.sp,
      lineHeight = 39.sp,
      letterSpacing = (-0.03).sp,
      color = colors.text
    )

    Spacer(modifier = Modifier.height(16.dp))
    Text(
      text = "O app lê seus compromissos do Google para mostrar o dia junto dos post-its e dos hábitos. Você escolhe quais calendários entram.",
      fontFamily = ArchivoFont,
      fontWeight = FontWeight.Normal,
      fontSize = 14.sp,
      lineHeight = 21.sp,
      color = colors.textSecondary
    )

    Spacer(modifier = Modifier.height(22.dp))
    Ruler2dp()

    // Permission Points
    OnboardingPermissionRow(
      color = colors.accent,
      title = "Ler eventos",
      description = "Título, horário e calendário de origem."
    )
    Ruler1dp()
    OnboardingPermissionRow(
      color = colors.accent,
      title = "Criar eventos",
      description = "Só os que você criar aqui, no calendário que escolher."
    )
    Ruler1dp()
    OnboardingPermissionRow(
      color = colors.text,
      title = "Nada sai do aparelho",
      description = "Post-its e hábitos ficam no dispositivo. Não enviamos suas notas ao Google."
    )

    Spacer(modifier = Modifier.height(22.dp))
    Text(text = "CALENDÁRIOS ENCONTRADOS", style = SectionLabelStyle, color = colors.textTertiary)
    Spacer(modifier = Modifier.height(10.dp))

    Column(modifier = Modifier.fillMaxWidth().border(1.dp, colors.rulerStrong, RectangleShape)) {
      OnboardingCalendarCheckRow("Pessoal", colors.accent, true)
      Ruler1dp()
      OnboardingCalendarCheckRow("Trabalho", colors.text, true)
      Ruler1dp()
      OnboardingCalendarCheckRow("Faculdade", colors.gridFail, false)
      Ruler1dp()
      OnboardingCalendarCheckRow("Feriados no Brasil", colors.switchOffTrack, false)
    }

    Spacer(modifier = Modifier.height(22.dp))
    ModernistButton(
      text = "Conectar thiagovinicius7@gmail.com",
      onClick = onConnect,
      modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(12.dp))
    Text(
      text = "Usar sem agenda por enquanto",
      fontFamily = ArchivoFont,
      fontWeight = FontWeight.SemiBold,
      fontSize = 11.sp,
      color = colors.textSecondary,
      modifier = Modifier.clickable(onClick = onSkip)
    )

    Spacer(modifier = Modifier.height(40.dp))
  }
}

@Composable
fun StatsScreen(
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val colors = LocalBlocoColors.current
  val scrollState = rememberScrollState()
  var period by remember { mutableStateOf("90d") }

  val dummyCalc = remember {
    val h = com.example.data.model.Habit("all", "All", durationDays = 90, startDateEpochDay = HabitCalculations.todayEpochDay() - 90)
    val marks = (0..90).filter { it % 9 != 3 }.map { com.example.data.model.HabitMark("all", HabitCalculations.todayEpochDay() - 90 + it) }
    HabitCalculations.calculate(h, marks)
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(colors.canvas)
      .verticalScroll(scrollState)
  ) {
    // Header
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
      Text(text = "← Hábitos", fontFamily = ArchivoFont, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = colors.textSecondary, modifier = Modifier.clickable(onClick = onBack))
      Spacer(modifier = Modifier.height(10.dp))
      Text(text = "Estatísticas", fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 30.sp, color = colors.text)
      Spacer(modifier = Modifier.height(12.dp))

      Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        SearchFilterChip("30 dias", isSelected = period == "30d") { period = "30d" }
        SearchFilterChip("90 dias", isSelected = period == "90d") { period = "90d" }
        SearchFilterChip("Tudo", isSelected = period == "tudo") { period = "tudo" }
      }
    }

    Ruler2dp()

    // 3 Big Stats
    Row(modifier = Modifier.fillMaxWidth().height(72.dp)) {
      Column(modifier = Modifier.weight(1f).padding(12.dp)) {
        Text(text = "81%", style = BigStatStyle, color = colors.text)
        Text(text = "MÉDIA GERAL", style = SectionLabelStyle, color = colors.textTertiary)
      }
      Box(modifier = Modifier.width(1.dp).fillMaxSize().background(colors.rulerWeak))
      Column(modifier = Modifier.weight(1f).padding(12.dp)) {
        Text(text = "d28", style = BigStatStyle, color = colors.accent)
        Text(text = "MAIOR ATIVA", style = SectionLabelStyle, color = colors.textTertiary)
      }
      Box(modifier = Modifier.width(1.dp).fillMaxSize().background(colors.rulerWeak))
      Column(modifier = Modifier.weight(1f).padding(12.dp)) {
        Text(text = "216", style = BigStatStyle, color = colors.text)
        Text(text = "MARCAÇÕES", style = SectionLabelStyle, color = colors.textTertiary)
      }
    }

    Ruler2dp()

    // Constância por hábito
    Column(modifier = Modifier.padding(16.dp)) {
      Text(text = "CONSTÂNCIA POR HÁBITO", style = SectionLabelStyle, color = colors.textTertiary)
      Spacer(modifier = Modifier.height(10.dp))

      HabitConsistencyBar("Ler 20 páginas", 96, colors.accent)
      Ruler1dp()
      HabitConsistencyBar("Corrida", 86, colors.accent)
      Ruler1dp()
      HabitConsistencyBar("Academia", 71, colors.text)
      Ruler1dp()
      HabitConsistencyBar("Meditar · pausado", 54, colors.gridFail, isMuted = true)
    }

    Ruler2dp()

    // Por dia da semana chart
    Column(modifier = Modifier.padding(16.dp)) {
      Text(text = "POR DIA DA SEMANA", style = SectionLabelStyle, color = colors.textTertiary)
      Spacer(modifier = Modifier.height(12.dp))

      Row(
        modifier = Modifier.fillMaxWidth().height(110.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Bottom
      ) {
        DayOfWeekBar("SEG", 92, colors.accent, modifier = Modifier.weight(1f))
        DayOfWeekBar("TER", 88, colors.accent, modifier = Modifier.weight(1f))
        DayOfWeekBar("QUA", 64, colors.gridFail, modifier = Modifier.weight(1f))
        DayOfWeekBar("QUI", 79, colors.accent, modifier = Modifier.weight(1f))
        DayOfWeekBar("SEX", 85, colors.accent, modifier = Modifier.weight(1f))
        DayOfWeekBar("SÁB", 76, colors.accent, modifier = Modifier.weight(1f))
        DayOfWeekBar("DOM", 0, colors.switchOffTrack, isSunday = true, modifier = Modifier.weight(1f))
      }

      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = "Quarta é o dia mais fraco: 64%. Domingo está fora da regra da maioria dos hábitos.",
        fontFamily = ArchivoFont,
        fontWeight = FontWeight.Normal,
        fontSize = 11.5.sp,
        color = colors.textSecondary
      )
    }

    Ruler2dp()

    // Últimos 90 dias todos juntos
    Column(modifier = Modifier.padding(16.dp)) {
      Text(text = "ÚLTIMOS 90 DIAS · TODOS JUNTOS", style = SectionLabelStyle, color = colors.textTertiary)
      Spacer(modifier = Modifier.height(12.dp))
      HabitGrid(cells = dummyCalc.gridCells.take(90), mode = HabitGridMode.STATS)
    }

    Spacer(modifier = Modifier.height(40.dp))
  }
}

@Composable
private fun HabitConsistencyBar(name: String, percent: Int, barColor: Color, isMuted: Boolean = false) {
  val colors = LocalBlocoColors.current
  Column(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
      Text(text = name, fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = if (isMuted) colors.textTertiary else colors.text)
      Text(text = "$percent%", fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = if (isMuted) colors.textTertiary else colors.text)
    }
    Spacer(modifier = Modifier.height(8.dp))
    Box(modifier = Modifier.fillMaxWidth().height(9.dp).background(colors.track)) {
      Box(modifier = Modifier.fillMaxWidth(percent / 100f).height(9.dp).background(barColor))
    }
  }
}

@Composable
private fun DayOfWeekBar(label: String, percent: Int, barColor: Color, isSunday: Boolean = false, modifier: Modifier = Modifier) {
  val colors = LocalBlocoColors.current
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Bottom
  ) {
    Text(text = if (isSunday) "—" else "$percent%", fontFamily = ArchivoFont, fontWeight = FontWeight.Bold, fontSize = 9.sp, color = colors.textSecondary)
    Spacer(modifier = Modifier.height(4.dp))
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(if (isSunday) 17.dp else (percent * 0.72).dp.coerceAtLeast(4.dp))
        .background(barColor)
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(text = label, fontFamily = ArchivoFont, fontWeight = FontWeight.Bold, fontSize = 8.5.sp, color = colors.textTertiary)
  }
}

@Composable
private fun SearchResultRow(
  stripeColor: Color,
  title: String,
  snippet: String,
  highlightTerm: String,
  meta: String,
  onClick: () -> Unit
) {
  val colors = LocalBlocoColors.current
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(16.dp, 12.dp),
    verticalAlignment = Alignment.Top
  ) {
    Box(modifier = Modifier.width(5.dp).height(44.dp).background(stripeColor))
    Spacer(modifier = Modifier.width(12.dp))
    Column {
      Text(text = title, fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = colors.text)
      Spacer(modifier = Modifier.height(4.dp))
      Text(text = snippet, fontFamily = ArchivoFont, fontWeight = FontWeight.Normal, fontSize = 12.sp, color = colors.textSecondary)
      Spacer(modifier = Modifier.height(6.dp))
      Text(text = meta, fontFamily = ArchivoFont, fontWeight = FontWeight.SemiBold, fontSize = 9.5.sp, color = colors.textTertiary)
    }
  }
}

@Composable
private fun SearchFilterChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
  val colors = LocalBlocoColors.current
  val bg = if (isSelected) colors.text else Color.Transparent
  val textColor = if (isSelected) colors.canvas else colors.text
  val borderModifier = if (!isSelected) Modifier.border(1.dp, colors.rulerStrong, RectangleShape) else Modifier

  Box(
    modifier = Modifier
      .then(borderModifier)
      .background(bg)
      .clickable(onClick = onClick)
      .padding(horizontal = 11.dp, vertical = 8.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(text = label, fontFamily = ArchivoFont, fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold, fontSize = 10.sp, color = textColor)
  }
}

@Composable
private fun SettingsRowWithNav(title: String, subtitle: String?) {
  val colors = LocalBlocoColors.current
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 13.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column {
      Text(text = title, fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = colors.text)
      if (subtitle != null) {
        Text(text = subtitle, fontFamily = ArchivoFont, fontSize = 10.5.sp, color = colors.textSecondary)
      }
    }
    Text(text = "→", fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = colors.text)
  }
}

@Composable
private fun SettingsRowWithSwitch(title: String, subtitle: String?, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
  val colors = LocalBlocoColors.current
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 13.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(text = title, fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = colors.text)
      if (subtitle != null) {
        Text(text = subtitle, fontFamily = ArchivoFont, fontSize = 10.5.sp, color = colors.textSecondary)
      }
    }
    ModernistSwitch(checked = checked, onCheckedChange = onCheckedChange)
  }
}

@Composable
private fun CategoryPill(name: String, bg: Color, barColor: Color) {
  Row(
    modifier = Modifier.background(bg).padding(horizontal = 9.dp, vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(modifier = Modifier.size(9.dp).background(barColor))
    Spacer(modifier = Modifier.width(6.dp))
    Text(text = name, fontFamily = ArchivoFont, fontWeight = FontWeight.SemiBold, fontSize = 10.5.sp)
  }
}

@Composable
private fun OnboardingPermissionRow(color: Color, title: String, description: String) {
  val colors = LocalBlocoColors.current
  Row(
    modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
    verticalAlignment = Alignment.Top
  ) {
    Box(modifier = Modifier.size(10.dp).background(color).padding(top = 4.dp))
    Spacer(modifier = Modifier.width(12.dp))
    Column {
      Text(text = title, fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = colors.text)
      Spacer(modifier = Modifier.height(3.dp))
      Text(text = description, fontFamily = ArchivoFont, fontWeight = FontWeight.Normal, fontSize = 11.5.sp, color = colors.textSecondary)
    }
  }
}

@Composable
private fun OnboardingCalendarCheckRow(name: String, color: Color, isChecked: Boolean) {
  val colors = LocalBlocoColors.current
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(if (isChecked) colors.postItStudyBg else Color.Transparent)
      .padding(horizontal = 12.dp, vertical = 13.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(modifier = Modifier.size(14.dp).background(color))
    Spacer(modifier = Modifier.width(11.dp))
    Text(text = name, fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 12.5.sp, color = colors.text, modifier = Modifier.weight(1f))
    if (isChecked) {
      Box(modifier = Modifier.size(18.dp).background(colors.accent), contentAlignment = Alignment.Center) {
        Text(text = "✓", color = Color.White, fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
      }
    } else {
      Box(modifier = Modifier.size(18.dp).border(1.5.dp, colors.text, RectangleShape))
    }
  }
}
