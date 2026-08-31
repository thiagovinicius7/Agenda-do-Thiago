package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class NoteFormat {
  NOTE,
  CHECKLIST
}

@Entity(tableName = "categories")
data class Category(
  @PrimaryKey val id: String,
  val name: String,
  val colorHex: String,
  val order: Int = 0
)

@Entity(tableName = "notes")
data class Note(
  @PrimaryKey val id: String,
  val title: String,
  val body: String = "",
  val categoryId: String = "trabalho",
  val format: NoteFormat = NoteFormat.NOTE,
  val isPinned: Boolean = false,
  val isArchived: Boolean = false,
  val attachedEventId: String? = null,
  val attachedEventSummary: String? = null,
  val attachedDate: String? = null, // e.g. "2 set", "hoje 15h"
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "note_items")
data class NoteItem(
  @PrimaryKey val id: String,
  val noteId: String,
  val text: String,
  val isDone: Boolean = false,
  val orderIndex: Int = 0
)

data class NoteWithItems(
  val note: Note,
  val items: List<NoteItem> = emptyList(),
  val category: Category? = null
)
