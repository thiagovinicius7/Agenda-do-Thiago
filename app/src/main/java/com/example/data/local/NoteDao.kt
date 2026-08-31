package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Category
import com.example.data.model.Note
import com.example.data.model.NoteItem
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
  @Query("SELECT * FROM categories ORDER BY `order` ASC")
  fun getAllCategories(): Flow<List<Category>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCategory(category: Category)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCategories(categories: List<Category>)

  @Query("SELECT * FROM notes WHERE isArchived = 0 ORDER BY isPinned DESC, createdAt DESC")
  fun getActiveNotes(): Flow<List<Note>>

  @Query("SELECT * FROM notes WHERE isArchived = 1 ORDER BY updatedAt DESC")
  fun getArchivedNotes(): Flow<List<Note>>

  @Query("SELECT * FROM notes WHERE id = :id")
  suspend fun getNoteById(id: String): Note?

  @Query("SELECT * FROM notes WHERE id = :id")
  fun observeNoteById(id: String): Flow<Note?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertNote(note: Note)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertNotes(notes: List<Note>)

  @Update
  suspend fun updateNote(note: Note)

  @Query("DELETE FROM notes WHERE id = :id")
  suspend fun deleteNoteById(id: String)

  @Query("SELECT * FROM note_items WHERE noteId = :noteId ORDER BY orderIndex ASC")
  fun getItemsForNote(noteId: String): Flow<List<NoteItem>>

  @Query("SELECT * FROM note_items")
  fun getAllNoteItems(): Flow<List<NoteItem>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertNoteItem(item: NoteItem)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertNoteItems(items: List<NoteItem>)

  @Update
  suspend fun updateNoteItem(item: NoteItem)

  @Query("DELETE FROM note_items WHERE id = :id")
  suspend fun deleteNoteItem(id: String)

  @Query("DELETE FROM note_items WHERE noteId = :noteId")
  suspend fun deleteItemsForNote(noteId: String)

  @Query("SELECT * FROM notes WHERE title LIKE '%' || :query || '%' OR body LIKE '%' || :query || '%'")
  fun searchNotes(query: String): Flow<List<Note>>

  @Query("DELETE FROM notes")
  suspend fun clearAllNotes()

  @Query("DELETE FROM note_items")
  suspend fun clearAllNoteItems()
}
