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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.text.style.TextDecoration
import android.content.Context
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CalendarEvent
import com.example.data.model.NoteFormat
import com.example.data.model.NoteWithItems
import com.example.ui.components.ModernistCheckbox
import com.example.ui.components.Ruler1dp
import com.example.ui.components.Ruler2dp
import com.example.ui.theme.ArchivoFont
import com.example.ui.theme.LocalBlocoColors
import com.example.ui.theme.SectionLabelStyle
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun NoteDetailScreen(
  noteWithItems: NoteWithItems?,
  events: List<CalendarEvent> = emptyList(),
  onBack: () -> Unit,
  onSave: (id: String?, title: String, body: String, categoryId: String, format: NoteFormat, items: List<String>, attachedEventId: String?, attachedEventSummary: String?, attachedDate: String?) -> Unit,
  onToggleItem: (String, Boolean) -> Unit,
  onDelete: ((String) -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val colors = LocalBlocoColors.current
  val context = LocalContext.current
  val scrollState = rememberScrollState()

  var title by remember(noteWithItems) { mutableStateOf(noteWithItems?.note?.title ?: "") }
  var body by remember(noteWithItems) { mutableStateOf(noteWithItems?.note?.body ?: "") }
  var selectedCategory by remember(noteWithItems) { mutableStateOf(noteWithItems?.note?.categoryId ?: "trabalho") }
  var format by remember(noteWithItems) { mutableStateOf(noteWithItems?.note?.format ?: NoteFormat.NOTE) }
  var isPinned by remember(noteWithItems) { mutableStateOf(noteWithItems?.note?.isPinned ?: false) }
  var newItemText by remember { mutableStateOf("") }
  var showMoreMenu by remember { mutableStateOf(false) }
  var showEventPicker by remember { mutableStateOf(false) }
  var showDeleteConfirmation by remember { mutableStateOf(false) }

  // Attached event state (null by default for free unlinked notes)
  var attachedEventId by remember(noteWithItems) { mutableStateOf(noteWithItems?.note?.attachedEventId) }
  var attachedEventSummary by remember(noteWithItems) { mutableStateOf(noteWithItems?.note?.attachedEventSummary) }
  var attachedDate by remember(noteWithItems) { mutableStateOf(noteWithItems?.note?.attachedDate) }

  val existingItems = noteWithItems?.items ?: emptyList()
  val extraItems = remember { mutableStateListOf<String>() }

  val headerBg = when (selectedCategory.lowercase()) {
    "trabalho" -> colors.postItWorkBg
    "pessoal" -> colors.postItPersonalBg
    "estudo" -> colors.postItStudyBg
    else -> colors.postItHomeBg
  }

  val totalItems = existingItems.size + extraItems.size
  val doneCount = existingItems.count { it.isDone }

  fun formatNoteForWhatsApp(): String {
    val sb = StringBuilder()
    sb.append("📌 *${title.ifBlank { "Bloco T · Nota" }}*\n")
    sb.append("🏷️ Categoria: ${selectedCategory.replaceFirstChar { it.uppercase() }}\n")
    if (!attachedDate.isNullOrBlank()) {
      sb.append("📅 Data: $attachedDate\n")
    }
    if (!attachedEventSummary.isNullOrBlank()) {
      sb.append("📍 Compromisso: $attachedEventSummary\n")
    }
    sb.append("\n")
    if (body.isNotBlank()) {
      sb.append("${body.trim()}\n\n")
    }
    val allItemsList = existingItems.map { Pair(it.text, it.isDone) } + extraItems.map { Pair(it, false) }
    if (allItemsList.isNotEmpty()) {
      sb.append("*Checklist:*\n")
      allItemsList.forEach { (text, isDone) ->
        val check = if (isDone) "☑️" else "◻️"
        sb.append("$check $text\n")
      }
      sb.append("\n")
    }
    sb.append("— _Compartilhado via Bloco T_")
    return sb.toString()
  }

  fun shareOnWhatsApp() {
    val textToShare = formatNoteForWhatsApp()
    try {
      val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, textToShare)
        setPackage("com.whatsapp")
      }
      context.startActivity(intent)
    } catch (e: Exception) {
      val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, textToShare)
      }
      context.startActivity(Intent.createChooser(fallbackIntent, "Compartilhar nota"))
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(colors.canvas)
  ) {
    // Top Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = "← Mural",
          fontFamily = ArchivoFont,
          fontWeight = FontWeight.SemiBold,
          fontSize = 11.sp,
          color = colors.textSecondary,
          modifier = Modifier.clickable(onClick = onBack)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
          text = if (format == NoteFormat.CHECKLIST) "CHECKLIST" else "POST-IT / NOTA",
          fontFamily = ArchivoFont,
          fontWeight = FontWeight.ExtraBold,
          fontSize = 12.sp,
          color = colors.text
        )
      }
      Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // WhatsApp Share button in top bar
        Box(
          modifier = Modifier
            .border(1.dp, Color(0xFF25D366), RectangleShape)
            .background(Color(0xFF25D366).copy(alpha = 0.12f))
            .clickable { shareOnWhatsApp() }
            .padding(horizontal = 8.dp, vertical = 6.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "💬 WhatsApp",
            fontFamily = ArchivoFont,
            fontWeight = FontWeight.Bold,
            fontSize = 10.5.sp,
            color = colors.text
          )
        }

        Box(
          modifier = Modifier
            .size(32.dp)
            .border(1.dp, colors.rulerStrong, RectangleShape)
            .clickable { showMoreMenu = true },
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "⋯",
            fontFamily = ArchivoFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            color = colors.text
          )
        }
      }
    }

    if (showMoreMenu) {
      AlertDialog(
        onDismissRequest = { showMoreMenu = false },
        title = {
          Text(text = "Opções do post-it", fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
        },
        text = {
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
              text = "💬 Compartilhar formatado no WhatsApp",
              fontFamily = ArchivoFont,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = Color(0xFF1E7E34),
              modifier = Modifier.fillMaxWidth().clickable {
                showMoreMenu = false
                shareOnWhatsApp()
              }.padding(vertical = 6.dp)
            )
            Text(
              text = if (isPinned) "Desafixar do topo" else "Fixar no topo",
              fontFamily = ArchivoFont,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              modifier = Modifier.fillMaxWidth().clickable {
                isPinned = !isPinned
                showMoreMenu = false
              }.padding(vertical = 6.dp)
            )
            Text(
              text = "Mudar para ${if (format == NoteFormat.CHECKLIST) "Texto simples" else "Checklist"}",
              fontFamily = ArchivoFont,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              modifier = Modifier.fillMaxWidth().clickable {
                format = if (format == NoteFormat.CHECKLIST) NoteFormat.NOTE else NoteFormat.CHECKLIST
                showMoreMenu = false
              }.padding(vertical = 6.dp)
            )
            if (attachedEventId != null) {
              Text(
                text = "✕ Desvincular de evento da agenda",
                fontFamily = ArchivoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = colors.accentDark,
                modifier = Modifier.fillMaxWidth().clickable {
                  attachedEventId = null
                  attachedEventSummary = null
                  attachedDate = null
                  showMoreMenu = false
                }.padding(vertical = 6.dp)
              )
            }
            if (noteWithItems != null && onDelete != null) {
              Text(
                text = "🗑️ Excluir post-it",
                fontFamily = ArchivoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = colors.gridFail,
                modifier = Modifier.fillMaxWidth().clickable {
                  showMoreMenu = false
                  showDeleteConfirmation = true
                }.padding(vertical = 6.dp)
              )
            }
          }
        },
        confirmButton = {
          Text(
            text = "Fechar",
            fontFamily = ArchivoFont,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            modifier = Modifier.clickable { showMoreMenu = false }.padding(8.dp)
          )
        },
        shape = RectangleShape,
        containerColor = colors.canvas
      )
    }

    if (showDeleteConfirmation) {
      AlertDialog(
        onDismissRequest = { showDeleteConfirmation = false },
        title = {
          Text(
            text = "EXCLUIR POST-IT",
            fontFamily = ArchivoFont,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 15.sp,
            color = colors.gridFail
          )
        },
        text = {
          Text(
            text = "Tem certeza que deseja apagar \"${title.ifBlank { "esta nota" }}\"? Esta ação não pode ser desfeita.",
            fontFamily = ArchivoFont,
            fontSize = 13.sp,
            color = colors.text
          )
        },
        confirmButton = {
          Text(
            text = "Excluir",
            fontFamily = ArchivoFont,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 13.sp,
            color = colors.gridFail,
            modifier = Modifier.clickable {
              showDeleteConfirmation = false
              noteWithItems?.note?.id?.let { onDelete?.invoke(it) }
            }.padding(8.dp)
          )
        },
        dismissButton = {
          Text(
            text = "Cancelar",
            fontFamily = ArchivoFont,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            modifier = Modifier.clickable { showDeleteConfirmation = false }.padding(8.dp)
          )
        },
        shape = RectangleShape,
        containerColor = colors.canvas
      )
    }

    // Event Picker Dialog
    if (showEventPicker) {
      AlertDialog(
        onDismissRequest = { showEventPicker = false },
        title = {
          Text(text = "Vincular a compromisso", fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
        },
        text = {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .heightIn(max = 340.dp)
              .verticalScroll(rememberScrollState())
          ) {
            Text(
              text = "Selecione um evento da sua agenda ou mantenha livre:",
              fontFamily = ArchivoFont,
              fontSize = 12.sp,
              color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Option 1: No event (Free Note)
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, colors.rulerStrong, RectangleShape)
                .clickable {
                  attachedEventId = null
                  attachedEventSummary = null
                  attachedDate = null
                  showEventPicker = false
                }
                .padding(10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "✕ Sem vínculo (Nota Livre / Post-it Solto)",
                fontFamily = ArchivoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = colors.text
              )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (events.isEmpty()) {
              Text(
                text = "Nenhum evento encontrado na agenda.",
                fontFamily = ArchivoFont,
                fontSize = 11.5.sp,
                color = colors.textTertiary,
                modifier = Modifier.padding(vertical = 8.dp)
              )
            } else {
              events.forEach { ev ->
                val timeStr = remember(ev.startEpochMillis) {
                  val zdt = Instant.ofEpochMilli(ev.startEpochMillis).atZone(ZoneId.systemDefault())
                  val fmt = DateTimeFormatter.ofPattern("EEE, d MMM · HH:mm", Locale("pt", "BR"))
                  zdt.format(fmt)
                }
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .border(1.dp, if (attachedEventId == ev.id) colors.accent else colors.rulerWeak, RectangleShape)
                    .background(if (attachedEventId == ev.id) colors.accent.copy(alpha = 0.08f) else Color.Transparent)
                    .clickable {
                      attachedEventId = ev.id
                      attachedEventSummary = ev.title
                      attachedDate = timeStr
                      showEventPicker = false
                    }
                    .padding(10.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Box(modifier = Modifier.size(6.dp).background(colors.accent))
                  Spacer(modifier = Modifier.width(8.dp))
                  Column {
                    Text(text = ev.title, fontFamily = ArchivoFont, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = colors.text)
                    Text(text = timeStr, fontFamily = ArchivoFont, fontSize = 10.sp, color = colors.textSecondary)
                  }
                }
              }
            }
          }
        },
        confirmButton = {
          Text(
            text = "Cancelar",
            fontFamily = ArchivoFont,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            modifier = Modifier.clickable { showEventPicker = false }.padding(8.dp)
          )
        },
        shape = RectangleShape,
        containerColor = colors.canvas
      )
    }

    Ruler2dp()

    Column(
      modifier = Modifier
        .weight(1f)
        .verticalScroll(scrollState)
    ) {
      // Category Header Banner
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(headerBg)
          .padding(16.dp)
      ) {
        Column {
          // Category selector chips
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            CategorySelectorChip("Trabalho", isSelected = selectedCategory == "trabalho") { selectedCategory = "trabalho" }
            CategorySelectorChip("Pessoal", isSelected = selectedCategory == "pessoal") { selectedCategory = "pessoal" }
            CategorySelectorChip("Estudo", isSelected = selectedCategory == "estudo") { selectedCategory = "estudo" }
            CategorySelectorChip("Casa", isSelected = selectedCategory == "casa") { selectedCategory = "casa" }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Format selector (Texto / Checklist)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Box(
              modifier = Modifier
                .border(1.dp, if (format == NoteFormat.NOTE) colors.text else colors.rulerStrong, RectangleShape)
                .background(if (format == NoteFormat.NOTE) colors.text else Color.Transparent)
                .clickable { format = NoteFormat.NOTE }
                .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
              Text(
                text = "Texto livre",
                fontFamily = ArchivoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = if (format == NoteFormat.NOTE) colors.canvas else colors.text
              )
            }

            Box(
              modifier = Modifier
                .border(1.dp, if (format == NoteFormat.CHECKLIST) colors.text else colors.rulerStrong, RectangleShape)
                .background(if (format == NoteFormat.CHECKLIST) colors.text else Color.Transparent)
                .clickable { format = NoteFormat.CHECKLIST }
                .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
              Text(
                text = "Checklist",
                fontFamily = ArchivoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = if (format == NoteFormat.CHECKLIST) colors.canvas else colors.text
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Editable Title with Cursor & Placeholder
          BasicTextField(
            value = title,
            onValueChange = { title = it },
            textStyle = TextStyle(
              fontFamily = ArchivoFont,
              fontWeight = FontWeight.ExtraBold,
              fontSize = 24.sp,
              lineHeight = 28.sp,
              letterSpacing = (-0.02).sp,
              color = colors.text
            ),
            decorationBox = { innerTextField ->
              if (title.isEmpty()) {
                Text(
                  text = "Título do post-it / nota...",
                  fontFamily = ArchivoFont,
                  fontWeight = FontWeight.ExtraBold,
                  fontSize = 24.sp,
                  color = colors.textSecondary.copy(alpha = 0.6f)
                )
              }
              innerTextField()
            },
            cursorBrush = SolidColor(colors.accent),
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = if (format == NoteFormat.CHECKLIST) "CHECKLIST · $doneCount DE $totalItems CONCLUÍDOS" else "POST-IT LIVRE",
            style = SectionLabelStyle,
            color = colors.accentPostItText
          )
        }
      }

      Ruler2dp()

      // Format: Checklist items
      if (format == NoteFormat.CHECKLIST) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
          for (item in existingItems) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleItem(item.id, item.isDone) }
                .padding(vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              ModernistCheckbox(
                checked = item.isDone,
                onCheckedChange = { onToggleItem(item.id, item.isDone) },
                size = 20.dp
              )
              Spacer(modifier = Modifier.width(12.dp))
              Text(
                text = item.text,
                fontFamily = ArchivoFont,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                color = colors.text,
                textDecoration = if (item.isDone) TextDecoration.LineThrough else TextDecoration.None
              )
            }
            Ruler1dp()
          }

          for (text in extraItems) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              ModernistCheckbox(
                checked = false,
                onCheckedChange = {},
                size = 20.dp
              )
              Spacer(modifier = Modifier.width(12.dp))
              Text(
                text = text,
                fontFamily = ArchivoFont,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                color = colors.text
              )
            }
            Ruler1dp()
          }

          // Add new item row
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(20.dp)
                .border(1.5.dp, colors.rulerStrong, RectangleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            BasicTextField(
              value = newItemText,
              onValueChange = { newItemText = it },
              textStyle = TextStyle(
                fontFamily = ArchivoFont,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = colors.text
              ),
              decorationBox = { innerTextField ->
                if (newItemText.isEmpty()) {
                  Text(
                    text = "Novo item da lista...",
                    fontFamily = ArchivoFont,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = colors.textTertiary
                  )
                }
                innerTextField()
              },
              modifier = Modifier.weight(1f)
            )
            if (newItemText.isNotBlank()) {
              Text(
                text = "+ Adicionar",
                fontFamily = ArchivoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = colors.accent,
                modifier = Modifier.clickable {
                  extraItems.add(newItemText.trim())
                  newItemText = ""
                }
              )
            }
          }
        }
      }

      // Note Body / Observações
      Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(
          text = if (format == NoteFormat.CHECKLIST) "OBSERVAÇÕES / TEXTO" else "CONTEÚDO DA NOTA",
          style = SectionLabelStyle,
          color = colors.textTertiary
        )
        Spacer(modifier = Modifier.height(8.dp))
        BasicTextField(
          value = body,
          onValueChange = { body = it },
          textStyle = TextStyle(
            fontFamily = ArchivoFont,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 21.sp,
            color = colors.text
          ),
          decorationBox = { inner ->
            if (body.isEmpty()) {
              Text(
                text = "Escreva seus pensamentos, detalhes ou observações livremente...",
                fontFamily = ArchivoFont,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = colors.textTertiary
              )
            }
            inner()
          },
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp)
            .background(colors.track)
            .padding(12.dp)
        )
      }

      Ruler1dp()

      // Dynamic Attached to Calendar Section
      Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "VÍNCULO COM A AGENDA",
            style = SectionLabelStyle,
            color = colors.textTertiary
          )
          Text(
            text = if (attachedEventId != null) "Alterar" else "+ Vincular a evento",
            fontFamily = ArchivoFont,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = colors.accentDark,
            modifier = Modifier.clickable { showEventPicker = true }
          )
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (attachedEventId != null) {
          // Has attached event
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .border(1.dp, colors.accent, RectangleShape)
              .background(colors.accent.copy(alpha = 0.05f))
              .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
              Box(
                modifier = Modifier
                  .width(4.dp)
                  .height(34.dp)
                  .background(colors.accent)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = attachedDate ?: "Data agendada",
                  fontFamily = ArchivoFont,
                  fontWeight = FontWeight.ExtraBold,
                  fontSize = 11.5.sp,
                  color = colors.text
                )
                Text(
                  text = attachedEventSummary ?: "Compromisso vinculado",
                  fontFamily = ArchivoFont,
                  fontWeight = FontWeight.Normal,
                  fontSize = 12.sp,
                  color = colors.textSecondary
                )
              }
            }
            Text(
              text = "✕ Desvincular",
              fontFamily = ArchivoFont,
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp,
              color = colors.accentDark,
              modifier = Modifier
                .clickable {
                  attachedEventId = null
                  attachedEventSummary = null
                  attachedDate = null
                }
                .padding(4.dp)
            )
          }
        } else {
          // Free note / No event
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .border(1.dp, colors.rulerWeak, RectangleShape)
              .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Nota livre (sem evento vinculado na agenda)",
              fontFamily = ArchivoFont,
              fontSize = 12.sp,
              color = colors.textSecondary
            )
            Text(
              text = "+ Vincular",
              fontFamily = ArchivoFont,
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp,
              color = colors.accentDark,
              modifier = Modifier.clickable { showEventPicker = true }
            )
          }
        }
      }
    }

    // Sticky Bottom Action Bar
    Ruler2dp()
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Box(
        modifier = Modifier
          .weight(1f)
          .background(colors.accent)
          .clickable {
            val allItems = existingItems.map { it.text } + extraItems
            onSave(
              noteWithItems?.note?.id,
              title.ifBlank { "Sem título" },
              body,
              selectedCategory,
              format,
              allItems,
              attachedEventId,
              attachedEventSummary,
              attachedDate
            )
          }
          .padding(14.dp),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "Salvar post-it",
          fontFamily = ArchivoFont,
          fontWeight = FontWeight.ExtraBold,
          fontSize = 13.sp,
          color = Color.White
        )
      }

      Box(
        modifier = Modifier
          .then(if (isPinned) Modifier.background(colors.text) else Modifier.border(1.dp, colors.rulerStrong, RectangleShape))
          .clickable { isPinned = !isPinned }
          .padding(14.dp),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = if (isPinned) "Fixado ✓" else "Fixar",
          fontFamily = ArchivoFont,
          fontWeight = FontWeight.ExtraBold,
          fontSize = 13.sp,
          color = if (isPinned) colors.canvas else colors.text
        )
      }

      if (noteWithItems != null && onDelete != null) {
        Box(
          modifier = Modifier
            .border(1.dp, colors.gridFail, RectangleShape)
            .clickable { showDeleteConfirmation = true }
            .padding(14.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "Excluir",
            fontFamily = ArchivoFont,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 13.sp,
            color = colors.gridFail
          )
        }
      }
    }
  }
}

@Composable
fun CategorySelectorChip(
  label: String,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  val colors = LocalBlocoColors.current
  val bg = if (isSelected) colors.text else Color.Transparent
  val textColor = if (isSelected) colors.canvas else colors.text
  val borderModifier = if (!isSelected) Modifier.border(1.dp, colors.rulerStrong, RectangleShape) else Modifier

  Box(
    modifier = Modifier
      .then(borderModifier)
      .background(bg)
      .clickable(onClick = onClick)
      .padding(horizontal = 10.dp, vertical = 7.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      fontFamily = ArchivoFont,
      fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
      fontSize = 10.sp,
      color = textColor
    )
  }
}
