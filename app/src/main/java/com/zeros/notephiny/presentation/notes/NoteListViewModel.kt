package com.zeros.notephiny.presentation.notes


import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeros.notephiny.ai.embedder.OnnxEmbedder
import com.zeros.notephiny.core.util.PreferencesManager
import com.zeros.notephiny.data.model.Note
import com.zeros.notephiny.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.lang.reflect.Modifier.PRIVATE
import javax.inject.Inject

@HiltViewModel
class NoteListViewModel @Inject constructor(
    private val repository: NoteRepository,
    private val embedder: OnnxEmbedder,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory
    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()
    val availableCategories = _notes
        .map { notes ->
            listOf("All") + notes.map { it.category }.distinct()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    val filteredNotes = combine(_notes, _selectedCategory, _searchQuery) { notes, category, query ->
        notes.filter { note ->
            val matchesCategory = category == "All" || note.category == category
            val matchesQuery = note.title.contains(query, ignoreCase = true) ||
                    note.content.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var recentlyDeletedNote: Note? = null

    init {
        observeNotes() // ✅ handles the logic cleanly now
    }

    private fun observeNotes() {
        viewModelScope.launch {
            repository.getAllNotes().collect { noteList ->
                if (noteList.isEmpty() && !preferencesManager.hasAddedWelcomeNote()) {
                    // ✅ Add welcome note once if DB is empty
                    val welcomeNote = Note(
                        title = "Welcome to Notephiny",
                        content = "This is your first note!",
                        category = "General"
                    )
                    repository.insertNote(welcomeNote)
                    preferencesManager.setWelcomeNoteAdded()
                } else {
                    _notes.value = noteList
                }
            }
        }
    }
    fun startSearch() {
        _isSearching.value = true
    }

    fun cancelSearch() {
        _isSearching.value = false
        _searchQuery.value = ""
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
        filteredNotes
    }


    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun saveNote(title: String, content: String, category: String) {
        viewModelScope.launch {
            try {
                repository.saveNoteWithEmbedding(title, content, category)
            } catch (e: Exception) {
                Log.e("SaveNote", "❌ Failed to save note with embedding: ${e.message}", e)
            }
        }
    }

    fun embedNoteContent(note: Note) {
        viewModelScope.launch {
            try {
                val vector = embedder.embed(note.content)
                Log.d("Embedder", "✅ Embedding for note: ${note.title} = ${vector.joinToString(prefix = "[", postfix = "]")}")
            } catch (e: Exception) {
                Log.e("Embedder", "❌ Embedding failed: ${e.message}", e)
            }
        }
    }

    fun deleteNote(note: Note) {
        Log.d("NoteDelete", "Attempting to delete note with ID: ${note.id}")
        if (note.id == null) {
            Log.e("NoteDelete", "❌ Cannot delete note with null ID")
            return
        }
        viewModelScope.launch {
            recentlyDeletedNote = note
            repository.deleteNoteById(note.id)
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

