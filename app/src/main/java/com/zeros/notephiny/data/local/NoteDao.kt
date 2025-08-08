package com.zeros.notephiny.data.local

import androidx.room.*
import com.zeros.notephiny.data.model.CategoryCount
import com.zeros.notephiny.data.model.Note
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query("SELECT * FROM notes ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Query("SELECT DISTINCT category FROM notes")
    suspend fun getAllCategories(): List<String>

    @Query("""
    SELECT * FROM notes
    WHERE title LIKE '%' || :query || '%' 
       OR content LIKE '%' || :query || '%'
    ORDER BY isPinned DESC, updatedAt DESC
""")
    suspend fun searchNotesByKeyword(query: String): List<Note>

    @Query("SELECT * FROM notes WHERE (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%') AND category = :category")
    suspend fun searchNotesByKeywordAndCategory(query: String, category: String): List<Note>

    @Query("SELECT * FROM notes WHERE embedding IS NOT NULL AND category = :category")
    suspend fun getNotesWithEmbeddingsByCategory(category: String): List<Note>



    @Query("UPDATE notes SET isPinned = :pinned WHERE id = :noteId")
    suspend fun updatePin(noteId: Int, pinned: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    @Query("SELECT category, COUNT(*) as count FROM notes GROUP BY category")
    suspend fun getNoteCountsByCategory(): List<CategoryCount>

    @Query("SELECT * FROM notes")
    suspend fun getAllNotesOnce(): List<Note>

    @Query("SELECT * FROM notes WHERE embedding IS NOT NULL AND id != :excludeId")
    suspend fun getNotesWithEmbeddingsExcluding(excludeId: Int): List<Note>

    @Query("SELECT * FROM notes WHERE embedding IS NOT NULL")
    suspend fun getNotesWithEmbeddings(): List<Note>


    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteNoteById(noteId: Int)

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: Int): Note?

    @Query("SELECT COUNT(*) FROM notes")
    suspend fun getNotesCount(): Int

    @Query("SELECT * FROM notes WHERE category = :category")
    suspend fun getNotesByCategory(category: String): List<Note>

}