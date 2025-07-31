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
import javax.inject.Inject

@HiltViewModel
class NoteListViewModel @Inject constructor(
    private val repository: NoteRepository,
    private val embedder: OnnxEmbedder,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    var recentlyDeletedNote: Note? = null
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory
    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

//    private val _availableCategories = MutableStateFlow<List<String>>(listOf("All"))
    private val _availableCategories = MutableStateFlow<List<String>>(emptyList())
    val availableCategories: StateFlow<List<String>> = _availableCategories.asStateFlow()

    val filteredNotes = combine(_notes, _selectedCategory, _searchQuery) { notes, category, query ->
        notes.filter { note ->
            val matchesCategory = category == "All" || note.category == category
            val matchesQuery = note.title.contains(query, ignoreCase = true) ||
                    note.content.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        observeNotes()
        fetchAvailableCategories()
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

    private fun fetchAvailableCategories() {
        viewModelScope.launch {
            val dbCategories = repository.getAllCategories()
            val defaultCategories = repository.getDefaultCategories()
            val merged = (dbCategories + defaultCategories).toSet().toList().sorted()
            _availableCategories.value = listOf("All") + merged
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
            fetchAvailableCategories()
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
}

