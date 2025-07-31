package com.zeros.notephiny.presentation.notes


import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeros.notephiny.ai.embedder.OnnxEmbedder
import com.zeros.notephiny.core.util.PreferencesManager
import com.zeros.notephiny.data.model.Note
import com.zeros.notephiny.domain.repository.NoteRepository
import com.zeros.notephiny.presentation.components.menus.MainScreenMenu
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
    private val _uiState = MutableStateFlow(NoteListUiState())
    val uiState: StateFlow<NoteListUiState> = _uiState.asStateFlow()


    enum class SortOrder {
        CREATED,
        EDITED
    }

    enum class NoteListMode {
        NORMAL,
        MULTI_SELECT
    }




    private val _availableCategories = MutableStateFlow<List<String>>(emptyList())
    val availableCategories: StateFlow<List<String>> = _availableCategories.asStateFlow()


    val filteredNotes = combine(
        _notes,
        _selectedCategory,
        _searchQuery,
        _uiState
    ) { notes, category, query, uiState ->

        val filtered = notes.filter { note ->
            val matchesCategory = category == "All" || note.category == category
            val matchesQuery = note.title.contains(query, true) || note.content.contains(query, true)
            matchesCategory && matchesQuery
        }

        when (uiState.sortOrder) {
            SortOrder.CREATED -> filtered.sortedByDescending { it.createdAt }
            SortOrder.EDITED -> filtered.sortedByDescending { it.updatedAt }
        }
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())





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

    fun onMainMenuAction(action: MainScreenMenu) {
        when (action) {

            MainScreenMenu.SortByCreated -> {
                _uiState.update { it.copy(sortOrder = SortOrder.CREATED) }
            }
            MainScreenMenu.SortByEdited -> {
                _uiState.update { it.copy(sortOrder = SortOrder.EDITED) }
            }
            MainScreenMenu.Edit -> {
                enterMultiSelectMode()
            }
            MainScreenMenu.Settings -> {
                // TODO: Navigate to settings screen
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

    fun enterMultiSelectMode() {
        _uiState.update { it.copy(mode = NoteListMode.MULTI_SELECT) }
    }

    fun exitMultiSelectMode() {
        _uiState.update {
            it.copy(
                mode = NoteListMode.NORMAL,
                selectedNoteIds = emptySet()
            )
        }
    }

    fun toggleNoteSelection(noteId: Int) {
        _uiState.update { current ->
            val updated = current.selectedNoteIds.toMutableSet()
            if (updated.contains(noteId)) updated.remove(noteId)
            else updated.add(noteId)

            current.copy(selectedNoteIds = updated)
        }
    }


    fun selectAll() {
        val allNoteIds = _uiState.value.notes.mapNotNull { it.id }.toSet()
        _uiState.update { it.copy(selectedNoteIds = allNoteIds) }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedNoteIds = emptySet()) }
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

