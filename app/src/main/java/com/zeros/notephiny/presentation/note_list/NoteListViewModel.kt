package com.zeros.notephiny.presentation.note_list


import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeros.notephiny.ai.embedder.OnnxEmbedder
import com.zeros.notephiny.data.model.Note
import com.zeros.notephiny.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteListViewModel @Inject constructor(
    private val repository: NoteRepository,
    private val embedder: OnnxEmbedder
) : ViewModel() {

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    private var recentlyDeletedNote: Note? = null

    init {
        getNotes()
        seedNotes()

        // ⚠️ TEMP: Add a dummy note — remove if no longer needed
        viewModelScope.launch {
            repository.insertNote(
                Note(
                    title = "Welcome to Notephiny",
                    content = "This is your first note!"
                )
            )
        }
    }
    // ⚠️ Dev-only: Seeding sample notes for testing purposes
    fun seedNotes() {
        viewModelScope.launch {
            try {
                repository.seedTestNotes()
                Log.d("Seed", "✅ Test notes seeded successfully")
            } catch (e: Exception) {
                Log.e("Seed", "❌ Failed to seed notes: ${e.message}", e)
            }
        }
    }

    private fun getNotes() {
        viewModelScope.launch {
            repository.getAllNotes().collect { noteList ->
                _notes.value = noteList
            }
        }
    }

    fun saveNote(title: String, content: String) {
        viewModelScope.launch {
            try {
                repository.saveNoteWithEmbedding(title, content)
            } catch (e: Exception) {
                Log.e("SaveNote", " Failed to save note with embedding: ${e.message}", e)
            }
        }
    }


    fun embedNoteContent(note: Note) {
        viewModelScope.launch {
            try {
                val vector = embedder.embed(note.content)
                Log.d(
                    "Embedder",
                    "✅ Embedding for note: ${note.title} = ${vector.joinToString(prefix = "[", postfix = "]")}"
                )
            } catch (e: Exception) {
                Log.e("Embedder", "❌ Embedding failed: ${e.message}", e)
            }
        }
    }



    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.deleteNote(note)
            recentlyDeletedNote = note
        }
    }

    fun restoreNote() {
        viewModelScope.launch {
            recentlyDeletedNote?.let {
                repository.insertNote(it)
                recentlyDeletedNote = null
            }
        }
    }
}

