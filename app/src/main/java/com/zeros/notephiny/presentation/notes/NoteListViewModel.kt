package com.zeros.notephiny.presentation.notes


import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeros.notephiny.ai.embedder.OnnxEmbedder
import com.zeros.notephiny.core.util.CategoryProvider
import com.zeros.notephiny.core.util.PreferencesManager
import com.zeros.notephiny.data.model.CategoryGroup
import com.zeros.notephiny.data.model.CategoryItem
import com.zeros.notephiny.data.model.Note
import com.zeros.notephiny.domain.repository.NoteRepository
import com.zeros.notephiny.presentation.components.menus.MainScreenMenu
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteListViewModel @Inject constructor(
    private val repository: NoteRepository,
    private val preferencesManager: PreferencesManager,
    private val categoryProvider: CategoryProvider
) : ViewModel() {

    // ------------------------
    // Public state & flows (kept for compatibility)
    // ------------------------
    var recentlyDeletedNote: Note? = null

    // query + ui flows the UI already uses
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    // UI state object kept for other properties (sortOrder, mode, selectedNoteIds, etc.)
    private val _uiState = MutableStateFlow(NoteListUiState())
    val uiState: StateFlow<NoteListUiState> = _uiState.asStateFlow()

    private val _availableCategories = MutableStateFlow<List<String>>(emptyList())
    val availableCategories: StateFlow<List<String>> = _availableCategories.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private val _categoryItems = MutableStateFlow<List<CategoryItem>>(emptyList())
    val categoryItems: StateFlow<List<CategoryItem>> = _categoryItems.asStateFlow()

    // ------------------------
    // Internal sources
    // ------------------------
    // live list of all notes (keeps track of DB updates)
    private val _allNotes = MutableStateFlow<List<Note>>(emptyList())
    val allNotes: StateFlow<List<Note>> = _allNotes.asStateFlow()

    // search results (semantic + keyword). UI will display this when isSearching == true
    private val _searchResults = MutableStateFlow<List<Note>>(emptyList())
    private val searchResults: StateFlow<List<Note>> = _searchResults.asStateFlow()

    // Combined "what to show" flow that UI will collect from (keeps previous name filteredNotes)
    val filteredNotes: StateFlow<List<Note>>

    // debounce/search job
    private var searchJob: Job? = null

    // ------------------------
    // Sealed types & enums (kept)
    // ------------------------
    sealed class NavigationEvent {
        object GoToSettings : NavigationEvent()
    }

    enum class SortOrder {
        CREATED,
        EDITED
    }

    enum class NoteListMode {
        NORMAL,
        MULTI_SELECT
    }

    // ------------------------
    // Init
    // ------------------------
    init {
        // build filteredNotes by combining sources:
        filteredNotes = combine(
            _allNotes,
            searchResults,
            _selectedCategory,
            _isSearching,
            _uiState
        ) { all, search, selectedCategory, isSearching, uiState ->
            val base = if (isSearching) search else all

            // category filter
            val categoryFiltered = if (selectedCategory == "All") base
            else base.filter { it.category == selectedCategory }

            // sorting
            val sorted = when (uiState.sortOrder) {
                SortOrder.CREATED -> categoryFiltered.sortedWith(
                    compareByDescending<Note> { it.isPinned }.thenByDescending { it.createdAt }
                )
                SortOrder.EDITED -> categoryFiltered.sortedWith(
                    compareByDescending<Note> { it.isPinned }.thenByDescending { it.updatedAt }
                )
            }

            sorted
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // start observers
        observeNotes()
        fetchAvailableCategories()
        fetchCategoryItems()
    }

    // ------------------------
    // Observers & helpers
    // ------------------------
    private fun fetchCategoryItems() {
        viewModelScope.launch {
            val structured = categoryProvider.getStructuredCategories()
            _categoryItems.value = structured
        }
    }

    private fun fetchAvailableCategories() {
        viewModelScope.launch {
            val dbCategories = repository.getAllCategories()
            val defaultCategories = repository.getDefaultCategories()
            val merged = (dbCategories + defaultCategories).toSet().toList().sorted()
            _availableCategories.value = listOf("All") + merged.filter { it != "All" }
        }
    }

    private fun observeNotes() {
        viewModelScope.launch {
            repository.getAllNotes().collect { noteList ->
                if (noteList.isEmpty() && !preferencesManager.hasAddedWelcomeNote()) {
                    val welcomeNote = Note(
                        title = "Welcome to Notephiny",
                        content = "This is your first note!",
                        category = "General"
                    )
                    repository.insertNote(welcomeNote)
                    preferencesManager.setWelcomeNoteAdded()
                } else {
                    val sortedNotes = noteList.sortedWith(
                        compareByDescending<Note> { it.isPinned }.thenByDescending { it.updatedAt }
                    )

                    // update both the local allNotes container and the UI state notes (keeps compatibility)
                    _allNotes.value = sortedNotes
                    _uiState.update { it.copy(notes = sortedNotes) }
                }
            }
        }
    }

    // ------------------------
    // Public actions (search + category)
    // ------------------------
    fun startSearch() {
        _isSearching.value = true
        _uiState.update { it.copy(isSearching = true) } // keep uiState consistent
    }

    fun cancelSearch() {
        _isSearching.value = false
        _searchQuery.value = ""
        // clear results and keep uiState consistent
        _searchResults.value = emptyList()
        _uiState.update { it.copy(isSearching = false, searchQuery = "", filteredNotes = emptyList()) }
    }

    /**
     * Called when search query changes from UI.
     * Debounces rapid updates and triggers repository.searchNotes(...) in background.
     */

    fun onSearchQueryChanged(query: String) {
        val trimmed = query.trim()
        _searchQuery.value = query
        _uiState.update { it.copy(searchQuery = query) }

        // Cancel previous debounce job
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            // Debounce to avoid excessive embedding calls
            delay(300L)

            // Avoid searching for very short/blank queries
            if (trimmed.length < 3) {
                _searchResults.value = emptyList()
                _uiState.update { it.copy(filteredNotes = emptyList()) }
                return@launch
            }

            try {
                // Perform hybrid search without category filtering
                val results = repository.searchNotes(
                    query = trimmed,
                    topK = 50,
                    similarityThreshold = 0.75
                )

                _searchResults.value = results
                _uiState.update { it.copy(filteredNotes = results) }

            } catch (t: Throwable) {
                _searchResults.value = emptyList()
                _uiState.update { it.copy(filteredNotes = emptyList()) }
            }
        }
    }




    fun selectCategory(category: String) {
        _selectedCategory.value = category

        // If searching, re-trigger search with new category filter
        if (_isSearching.value && _searchQuery.value.isNotBlank()) {
            onSearchQueryChanged(_searchQuery.value)
        }
    }


    // ------------------------
    // Note operations (kept from original)
    // ------------------------
    fun onMainMenuAction(action: MainScreenMenu) {
        when (action) {
            MainScreenMenu.SortByCreated -> _uiState.update { it.copy(sortOrder = SortOrder.CREATED) }
            MainScreenMenu.SortByEdited -> _uiState.update { it.copy(sortOrder = SortOrder.EDITED) }
            MainScreenMenu.Edit -> enterMultiSelectMode()
            MainScreenMenu.Settings -> viewModelScope.launch { _navigationEvent.emit(NavigationEvent.GoToSettings) }
        }
    }

    fun deleteNote(note: Note) {
        if (note.id == null) {
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
        _uiState.update { it.copy(mode = NoteListMode.NORMAL, selectedNoteIds = emptySet()) }
    }

    fun toggleNoteSelection(noteId: Int) {
        _uiState.update { current ->
            val updated = current.selectedNoteIds.toMutableSet().apply {
                if (contains(noteId)) remove(noteId) else add(noteId)
            }
            current.copy(selectedNoteIds = updated)
        }
    }

    fun selectAll() {
        val allNoteIds = _uiState.value.notes.mapNotNull { it.id }.toSet()
        _uiState.update { it.copy(selectedNoteIds = allNoteIds) }
    }
}


