package com.zeros.notephiny.domain.repository

import com.zeros.notephiny.data.local.NoteDao
import com.zeros.notephiny.data.model.Note
import com.zeros.notephiny.ai.embedder.OnnxEmbedder

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NoteRepository @Inject constructor(
    private val dao: NoteDao,
    private val embedder: OnnxEmbedder
) {

    fun getDefaultCategories(): List<String> = DefaultCategories

    fun getAllNotes(): Flow<List<Note>> = dao.getAllNotes()

    suspend fun insertNote(note: Note) = dao.insertNote(note)

    suspend fun getAllOrDefaultCategories(): List<String> {
        val fromDb = dao.getAllCategories()
        return if (fromDb.isEmpty()) DefaultCategories else (fromDb + DefaultCategories).distinct()
    }


    suspend fun deleteNoteById(noteId: Int) = dao.deleteNoteById(noteId)

    suspend fun getAllCategories(): List<String> = dao.getAllCategories()

    suspend fun getNoteById(id: Int): Note? = dao.getNoteById(id)

    suspend fun getNotesCount(): Int {
        return dao.getNotesCount()
    }

    suspend fun saveNoteWithEmbedding(
        id: Int? = null,
        title: String,
        content: String,
        category: String,
        color: Int,
        createdAt: Long? = null,
        updatedAt: Long = System.currentTimeMillis()
    ) {
        val text = "$title $content $category"
        val embedding = embedder.embed(text).toList()
        val note = Note(
            id = id,
            title = title,
            content = content,
            category = category,
            embedding = embedding,
            color = color,
            createdAt = createdAt ?: System.currentTimeMillis(),
            updatedAt = updatedAt
        )
        dao.insertNote(note)
    }


}