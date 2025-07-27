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

    suspend fun deleteNote(note: Note) = dao.deleteNote(note)

    suspend fun getNoteById(id: Int): Note? = dao.getNoteById(id)

    suspend fun saveNoteWithEmbedding(title: String, content: String) {
        val text = "$title $content"
        val embedding = embedder.embed(text).toList()
        val note = Note(
            title = title,
            content = content,
            embedding = embedding
        )
        dao.insertNote(note)
    }

    suspend fun seedTestNotes() {
        val sampleNotes = listOf(
            "Meeting Notes" to "Discuss project milestones and assign tasks.",
            "Shopping List" to "Eggs, milk, bread, and peanut butter.",
            "Quote" to "The only limit to our realization of tomorrow is our doubts of today.",
            "Workout Plan" to "30 mins cardio, strength training, and stretching.",
            "Recipe" to "Boil pasta, add sauce, and mix with cheese."
        )

        sampleNotes.forEach { (title, content) ->
            saveNoteWithEmbedding(title, content)
        }
    }
}