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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NoteFormat
import com.example.data.model.NoteWithItems
import com.example.ui.components.ModernistCheckbox
import com.example.ui.components.Ruler1dp
import com.example.ui.components.Ruler2dp
import com.example.ui.theme.ArchivoFont
import com.example.ui.theme.LocalBlocoColors
import com.example.ui.theme.SectionLabelStyle

@Composable
fun NoteDetailScreen(
  noteWithItems: NoteWithItems?,
  onBack: () -> Unit,
  onSave: (id: String?, title: String, body: String, categoryId: String, format: NoteFormat, items: List<String>) -> Unit,
  onToggleItem: (String, Boolean) -> Unit,
  modifier: Modifier = Modifier
) {
  val colors = LocalBlocoColors.current
  val scrollState = rememberScrollState()

  var title by remember(noteWithItems) { mutableStateOf(noteWithItems?.note?.title ?: "Nova nota") }
  var body by remember(noteWithItems) { mutableStateOf(noteWithItems?.note?.body ?: "") }
  var selectedCategory by remember(noteWithItems) { mutableStateOf(noteWithItems?.note?.categoryId ?: "trabalho") }
  var format by remember(noteWithItems) { mutableStateOf(noteWithItems?.note?.format ?: NoteFormat.CHECKLIST) }
  var newItemText by remember { mutableStateOf("") }

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
      Text(
        text = "← Mural",
        fontFamily = ArchivoFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        color = colors.textSecondary,
        modifier = Modifier.clickable(onClick = onBack)
      )
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Salvo 14:32",
          fontFamily = ArchivoFont,
          fontWeight = FontWeight.SemiBold,
          fontSize = 10.sp,
          color = colors.textTertiary
        )
        Box(
          modifier = Modifier
            .size(32.dp)
            .border(1.dp, colors.rulerStrong, RectangleShape),
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

          // Editable Title with Cursor
          BasicTextField(
            value = title,
            onValueChange = { title = it },
            textStyle = TextStyle(
              fontFamily = ArchivoFont,
              fontWeight = FontWeight.ExtraBold,
              fontSize = 28.sp,
              lineHeight = 30.sp,
              letterSpacing = (-0.02).sp,
              color = colors.text
            ),
            cursorBrush = SolidColor(colors.accent),
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = if (format == NoteFormat.CHECKLIST) "CHECKLIST · $doneCount DE $totalItems" else "NOTA",
            style = SectionLabelStyle,
            color = colors.accentPostItText
          )
        }
      }

      Ruler2dp()

      // Checklist items
      Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        for (item in existingItems) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onToggleItem(item.id, item.isDone) }
              .padding(vertical = 12.dp),
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
              .padding(vertical = 12.dp),
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
            .padding(vertical = 12.dp),
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
                  text = "Novo item (digite e aperte enter)",
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
                extraItems.add(newItemText)
                newItemText = ""
              }
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))
        Ruler2dp()
        Spacer(modifier = Modifier.height(14.dp))

        // Attached to Calendar Card
        Text(
          text = "ANEXADO À AGENDA",
          style = SectionLabelStyle,
          color = colors.textTertiary
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colors.rulerStrong, RectangleShape)
            .padding(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .width(4.dp)
              .height(34.dp)
              .background(colors.accent)
          )
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = "Sex, 30 ago · 15:00",
              fontFamily = ArchivoFont,
              fontWeight = FontWeight.ExtraBold,
              fontSize = 12.sp,
              lineHeight = 14.sp,
              color = colors.text
            )
            Text(
              text = "Reunião cliente — Trabalho (Google)",
              fontFamily = ArchivoFont,
              fontWeight = FontWeight.Normal,
              fontSize = 12.sp,
              lineHeight = 15.sp,
              color = colors.textSecondary
            )
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Note Body
        Text(
          text = "NOTA",
          style = SectionLabelStyle,
          color = colors.textTertiary
        )
        Spacer(modifier = Modifier.height(10.dp))
        BasicTextField(
          value = body,
          onValueChange = { body = it },
          textStyle = TextStyle(
            fontFamily = ArchivoFont,
            fontWeight = FontWeight.Normal,
            fontSize = 13.5.sp,
            lineHeight = 20.sp,
            color = colors.text
          ),
          decorationBox = { inner ->
            if (body.isEmpty()) {
              Text(
                text = "Adicionar observações...",
                fontFamily = ArchivoFont,
                fontWeight = FontWeight.Normal,
                fontSize = 13.5.sp,
                color = colors.textTertiary
              )
            }
            inner()
          },
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp)
        )
      }
    }

    // Sticky Bottom Action Bar
    Ruler2dp()
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp),
      horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
      Box(
        modifier = Modifier
          .weight(1f)
          .background(colors.accent)
          .clickable {
            val allItems = existingItems.map { it.text } + extraItems
            onSave(noteWithItems?.note?.id, title, body, selectedCategory, format, allItems)
          }
          .padding(14.dp),
        contentAlignment = Alignment.CenterStart
      ) {
        Text(
          text = "Concluir",
          fontFamily = ArchivoFont,
          fontWeight = FontWeight.ExtraBold,
          fontSize = 13.sp,
          color = Color.White
        )
      }

      Box(
        modifier = Modifier
          .border(1.dp, colors.rulerStrong, RectangleShape)
          .clickable { onBack() }
          .padding(14.dp),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "Fixar",
          fontFamily = ArchivoFont,
          fontWeight = FontWeight.ExtraBold,
          fontSize = 13.sp,
          color = colors.text
        )
      }

      Box(
        modifier = Modifier
          .border(1.dp, colors.rulerStrong, RectangleShape)
          .clickable { onBack() }
          .padding(14.dp),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "Arquivar",
          fontFamily = ArchivoFont,
          fontWeight = FontWeight.ExtraBold,
          fontSize = 13.sp,
          color = colors.text
        )
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
