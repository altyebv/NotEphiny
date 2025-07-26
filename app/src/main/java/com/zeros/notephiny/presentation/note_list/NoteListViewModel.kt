package com.zeros.notephiny.presentation.note_list


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeros.notephiny.data.model.Note
import com.zeros.notephiny.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteListViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {

    private val _notes = MutableStateFlow<List<Note>>(emptyList())

    val notes: StateFlow<List<Note>> = _notes.asStateFlow()


    private var recentlyDeletedNote: Note? = null

    init {
        getNotes()

        // ⚠️ TEMP: Add a dummy note — remove if you no longer need this
        viewModelScope.launch {
            repository.insertNote(
                Note(
                    title = "Welcome to Notephiny",
                    content = "This is your first note!"
                )
            )
        }
    }

    private fun getNotes() {
        viewModelScope.launch {
            repository.getAllNotes().collect { noteList ->
                _notes.value = noteList
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

