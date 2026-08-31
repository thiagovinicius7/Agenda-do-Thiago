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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NoteWithItems
import com.example.ui.components.ModernistCheckbox
import com.example.ui.components.Ruler1dp
import com.example.ui.components.Ruler2dp
import com.example.ui.theme.ArchivoFont
import com.example.ui.theme.LocalBlocoColors
import com.example.ui.theme.SectionLabelStyle

@Composable
fun MuralScreen(
  notes: List<NoteWithItems>,
  currentFilter: String,
  onSelectFilter: (String) -> Unit,
  onOpenNote: (String) -> Unit,
  onToggleChecklistItem: (String, Boolean) -> Unit,
  onCreateNewNote: () -> Unit,
  modifier: Modifier = Modifier
) {
  val colors = LocalBlocoColors.current
  val scrollState = rememberScrollState()

  val filteredNotes = when (currentFilter.lowercase()) {
    "todos" -> notes
    "vazio" -> emptyList()
    else -> notes.filter { it.note.categoryId.equals(currentFilter, ignoreCase = true) }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(colors.canvas)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(scrollState)
    ) {
      // Header
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
      ) {
        Column {
          Text(
            text = "Mural",
            fontFamily = ArchivoFont,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 34.sp,
            lineHeight = 34.sp,
            letterSpacing = (-0.02).sp,
            color = colors.text
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "${notes.size} notas no total",
            fontFamily = ArchivoFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            color = colors.textSecondary
          )
        }
        Text(
          text = "+ Novo post-it",
          fontFamily = ArchivoFont,
          fontWeight = FontWeight.Bold,
          fontSize = 12.sp,
          color = colors.accentDark,
          modifier = Modifier.clickable(onClick = onCreateNewNote)
        )
      }

      Ruler2dp()

      // Category filter chips
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        MuralFilterChip(
          label = "Todos",
          isSelected = currentFilter == "todos",
          onClick = { onSelectFilter("todos") }
        )
        MuralFilterChip(
          label = "Trabalho",
          isSelected = currentFilter == "trabalho",
          onClick = { onSelectFilter("trabalho") }
        )
        MuralFilterChip(
          label = "Pessoal",
          isSelected = currentFilter == "pessoal",
          onClick = { onSelectFilter("pessoal") }
        )
        MuralFilterChip(
          label = "Estudo",
          isSelected = currentFilter == "estudo",
          onClick = { onSelectFilter("estudo") }
        )
      }

      Ruler1dp()

      if (filteredNotes.isEmpty()) {
        // Mural Vazio (Empty state)
        MuralEmptyState(onCreateNote = onCreateNewNote)
      } else {
        // 2-Column Post-It Grid
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        ) {
          val chunked = filteredNotes.chunked(2)
          for (rowNotes in chunked) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              for (noteWithItems in rowNotes) {
                Box(modifier = Modifier.weight(1f)) {
                  PostItCard(
                    noteWithItems = noteWithItems,
                    onOpenNote = { onOpenNote(noteWithItems.note.id) },
                    onToggleItem = onToggleChecklistItem
                  )
                }
              }
              if (rowNotes.size == 1) {
                Spacer(modifier = Modifier.weight(1f))
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(80.dp))
    }

    // FAB square 56x56 in bottom right
    Box(
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(16.dp)
        .size(56.dp)
        .shadow(elevation = 4.dp, shape = RectangleShape)
        .background(colors.accent)
        .clickable(onClick = onCreateNewNote),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = "+",
        fontFamily = ArchivoFont,
        fontWeight = FontWeight.Normal,
        fontSize = 30.sp,
        color = Color.White
      )
    }
  }
}

@Composable
fun PostItCard(
  noteWithItems: NoteWithItems,
  onOpenNote: () -> Unit,
  onToggleItem: (String, Boolean) -> Unit,
  modifier: Modifier = Modifier
) {
  val colors = LocalBlocoColors.current
  val note = noteWithItems.note
  val isPinned = note.isPinned

  val bg = when {
    isPinned -> colors.accent
    note.categoryId.equals("trabalho", true) -> colors.postItWorkBg
    note.categoryId.equals("pessoal", true) -> colors.postItPersonalBg
    note.categoryId.equals("estudo", true) -> colors.postItStudyBg
    else -> colors.postItHomeBg
  }

  val categoryLabelColor = when {
    isPinned -> Color.White.copy(alpha = 0.82f)
    note.categoryId.equals("trabalho", true) -> colors.accentPostItText
    note.categoryId.equals("pessoal", true) -> colors.textTertiary
    note.categoryId.equals("estudo", true) -> colors.accentPostItText
    else -> colors.textTertiary
  }

  val textColor = if (isPinned) Color.White else colors.text

  Box(
    modifier = modifier
      .fillMaxWidth()
      .shadow(elevation = 1.dp, shape = RectangleShape)
      .background(bg)
      .padding(12.dp)
  ) {
    Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // Category & Pinned Tag
      Text(
        text = if (isPinned) "IDEIA · FIXADO" else note.categoryId.uppercase(),
        style = SectionLabelStyle,
        color = categoryLabelColor
      )

      // Title (clickable to open full note)
      Text(
        text = note.title,
        fontFamily = ArchivoFont,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 15.sp,
        lineHeight = 17.sp,
        color = textColor,
        modifier = Modifier.clickable(onClick = onOpenNote)
      )

      // Checklist preview or Note text
      if (noteWithItems.items.isNotEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          for (item in noteWithItems.items.take(3)) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleItem(item.id, item.isDone) },
              verticalAlignment = Alignment.CenterVertically
            ) {
              ModernistCheckbox(
                checked = item.isDone,
                onCheckedChange = { onToggleItem(item.id, item.isDone) },
                size = 13.dp
              )
              Spacer(modifier = Modifier.width(7.dp))
              Text(
                text = item.text,
                fontFamily = ArchivoFont,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 14.sp,
                color = textColor,
                textDecoration = if (item.isDone) TextDecoration.LineThrough else TextDecoration.None
              )
            }
          }
        }
      } else if (note.body.isNotBlank()) {
        Text(
          text = note.body,
          fontFamily = ArchivoFont,
          fontWeight = FontWeight.Normal,
          fontSize = 12.sp,
          lineHeight = 15.sp,
          color = textColor.copy(alpha = 0.85f),
          maxLines = 3
        )
      }

      // Footer metadata
      val footerMeta = when {
        note.attachedDate != null -> note.attachedDate
        note.attachedEventSummary != null -> note.attachedEventSummary
        noteWithItems.items.isNotEmpty() -> {
          val doneCount = noteWithItems.items.count { it.isDone }
          "$doneCount/${noteWithItems.items.size}"
        }
        else -> null
      }

      if (footerMeta != null) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = footerMeta,
          fontFamily = ArchivoFont,
          fontWeight = FontWeight.SemiBold,
          fontSize = 9.5.sp,
          color = if (isPinned) Color.White.copy(alpha = 0.85f) else categoryLabelColor
        )
      }
    }
  }
}

@Composable
fun MuralFilterChip(
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
      .padding(horizontal = 12.dp, vertical = 8.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      fontFamily = ArchivoFont,
      fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
      fontSize = 11.sp,
      color = textColor
    )
  }
}

@Composable
fun MuralEmptyState(onCreateNote: () -> Unit) {
  val colors = LocalBlocoColors.current

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 22.dp)
  ) {
    Text(
      text = "Mural vazio",
      fontFamily = ArchivoFont,
      fontWeight = FontWeight.ExtraBold,
      fontSize = 34.sp,
      lineHeight = 35.sp,
      letterSpacing = (-0.03).sp,
      color = colors.text
    )
    Spacer(modifier = Modifier.height(14.dp))
    Text(
      text = "Comece com uma nota solta ou uma lista de itens. A categoria define a cor do post-it.",
      fontFamily = ArchivoFont,
      fontWeight = FontWeight.Normal,
      fontSize = 14.sp,
      lineHeight = 21.sp,
      color = colors.textSecondary
    )

    Spacer(modifier = Modifier.height(22.dp))

    // Format choices: Nota / Checklist
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // Option 1: Nota
      Box(
        modifier = Modifier
          .weight(1f)
          .height(150.dp)
          .background(colors.postItWorkBg)
          .clickable(onClick = onCreateNote)
          .padding(14.dp)
      ) {
        Column(modifier = Modifier.fillMaxSize()) {
          Text(text = "FORMATO", style = SectionLabelStyle, color = colors.accentPostItText)
          Spacer(modifier = Modifier.height(9.dp))
          Text(text = "Nota", fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = colors.text)
          Spacer(modifier = Modifier.height(7.dp))
          Text(text = "Texto corrido, sem caixas.", fontFamily = ArchivoFont, fontWeight = FontWeight.Normal, fontSize = 11.5.sp, color = colors.textSecondary)
          Spacer(modifier = Modifier.weight(1f))
          Text(text = "Criar →", fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = colors.accentPostItText)
        }
      }

      // Option 2: Checklist
      Box(
        modifier = Modifier
          .weight(1f)
          .height(150.dp)
          .background(colors.postItPersonalBg)
          .clickable(onClick = onCreateNote)
          .padding(14.dp)
      ) {
        Column(modifier = Modifier.fillMaxSize()) {
          Text(text = "FORMATO", style = SectionLabelStyle, color = colors.textTertiary)
          Spacer(modifier = Modifier.height(9.dp))
          Text(text = "Checklist", fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = colors.text)
          Spacer(modifier = Modifier.height(7.dp))
          Text(text = "Itens que dá pra marcar.", fontFamily = ArchivoFont, fontWeight = FontWeight.Normal, fontSize = 11.5.sp, color = colors.textSecondary)
          Spacer(modifier = Modifier.weight(1f))
          Text(text = "Criar →", fontFamily = ArchivoFont, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = colors.text)
        }
      }
    }

    Spacer(modifier = Modifier.height(22.dp))
    Ruler2dp()
    Spacer(modifier = Modifier.height(14.dp))

    Text(
      text = "CATEGORIAS SUGERIDAS",
      style = SectionLabelStyle,
      color = colors.textTertiary
    )
    Spacer(modifier = Modifier.height(10.dp))
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      MuralFilterChip("Trabalho", isSelected = false, onClick = onCreateNote)
      MuralFilterChip("Pessoal", isSelected = false, onClick = onCreateNote)
      MuralFilterChip("Estudo", isSelected = false, onClick = onCreateNote)
      MuralFilterChip("Casa", isSelected = false, onClick = onCreateNote)
    }

    Spacer(modifier = Modifier.height(22.dp))
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(colors.accent)
        .clickable(onClick = onCreateNote)
        .padding(14.dp)
    ) {
      Text(
        text = "Criar primeiro post-it",
        fontFamily = ArchivoFont,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 13.sp,
        color = Color.White
      )
    }
  }
}
