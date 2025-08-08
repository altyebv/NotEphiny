package com.zeros.notephiny.presentation.notes

import com.zeros.notephiny.data.model.Note
import com.zeros.notephiny.domain.repository.DefaultNoteCategory
import com.zeros.notephiny.presentation.notes.NoteListViewModel.NoteListMode
import com.zeros.notephiny.presentation.notes.NoteListViewModel.SortOrder

data class NoteListUiState(
    val notes: List<Note> = emptyList(),
    val filteredNotes: List<Note> = emptyList(),
    val noteToDelete: Note? = null,
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val selectedCategory: String = DefaultNoteCategory,
    val categories: List<String> = emptyList(),
    val sortOrder: SortOrder = SortOrder.CREATED,
    val mode: NoteListMode = NoteListMode.NORMAL,
    val selectedNoteIds: Set<Int> = emptySet(),
)
