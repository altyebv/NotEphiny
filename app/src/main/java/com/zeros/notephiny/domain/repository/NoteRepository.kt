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

    fun getAllNotes(): Flow<List<Note>> = dao.getAllNotes()

    suspend fun insertNote(note: Note) = dao.insertNote(note)

    suspend fun deleteNoteById(noteId: Int) = dao.deleteNoteById(noteId)


    suspend fun getNoteById(id: Int): Note? = dao.getNoteById(id)

    suspend fun getNotesCount(): Int {
        return dao.getNotesCount()
    }

    suspend fun saveNoteWithEmbedding(title: String, content: String, category: String) {
        val text = "$title $content $category"
        val embedding = embedder.embed(text).toList()
        val note = Note(
            title = title,
            content = content,
            category = category,
            embedding = embedding
        )
        dao.insertNote(note)
    }


}