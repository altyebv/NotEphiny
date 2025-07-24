package com.zeros.notephiny.domain.repository

import com.zeros.notephiny.data.local.NoteDao
import com.zeros.notephiny.data.model.Note
import kotlinx.coroutines.flow.Flow

class NoteRepository(private val dao: NoteDao) {

    fun getAllNotes(): Flow<List<Note>> = dao.getAllNotes()

    suspend fun insertNote(note: Note) = dao.insertNote(note)

    suspend fun deleteNote(note: Note) = dao.deleteNote(note)

    suspend fun getNoteById(id: Int): Note? = dao.getNoteById(id)
}