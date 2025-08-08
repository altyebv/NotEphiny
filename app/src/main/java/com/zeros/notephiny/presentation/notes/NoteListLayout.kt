package com.zeros.notephiny.presentation.notes


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.zeros.notephiny.data.model.Note
import com.zeros.notephiny.presentation.components.NotesTop
import com.zeros.notephiny.presentation.components.menus.MainScreenMenu
import com.zeros.notephiny.presentation.notes.NoteListViewModel.SortOrder
import  com.zeros.notephiny.presentation.notes.NoteListUiState
import com.zeros.notephiny.presentation.notes.NoteListViewModel.NoteListMode

@Composable
fun NoteListLayout(
    notes: List<Note>,
    noteToDelete: Note?,
    onDeleteRequest: (Note) -> Unit,
    onNoteClick: (Note) -> Unit,
    snackbarHostState: SnackbarHostState,
    searchQuery: String,
    isSearching: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onCancelSearch: () -> Unit,
    onSearchClick: () -> Unit,
    onCategorySelected: (String) -> Unit,
    onOverflowClick: (MainScreenMenu) -> Unit,
    selectedCategory: String,
    categories: List<String>,
    sortOrder: SortOrder,
    selectedNoteIds: Set<Int>,
    mode: NoteListMode,
    onCancelMultiSelect: () -> Unit,
    onSelectAll: () -> Unit,
    modifier: Modifier = Modifier

) {


    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            NotesTop(
                noteCount = notes.size,
                searchQuery = searchQuery,
                isSearching = isSearching,
                onSearchQueryChange = onSearchQueryChange,
                onCancelSearch = onCancelSearch,
                onStartSearch = onSearchClick,
                onCategorySelected = onCategorySelected,
                onOverflowClick = { action ->
                    onOverflowClick(action)
                },
                selectedCategory = selectedCategory,
                categories = categories,
                sortOrder = sortOrder,
                mode = mode,
                onCancelMultiSelect = onCancelMultiSelect,
                selectedNoteIds = selectedNoteIds,
                onSelectAll = onSelectAll,

            )

            NoteListContent(
                notes = notes,
                onNoteClick = onNoteClick,
                onDeleteRequest = onDeleteRequest,
                selectedNoteIds = selectedNoteIds,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}


