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

@Composable
fun NoteListLayout(
    notes: List<Note>,
    noteToDelete: Note?,
    onDeleteRequest: (Note) -> Unit,
    onNoteClick: (Note) -> Unit,
    onFabClick: () -> Unit,
    snackbarHostState: SnackbarHostState,
    searchQuery: String,
    isSearching: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onCancelSearch: () -> Unit,
    onSearchClick: () -> Unit,
    onCategorySelected: (String) -> Unit,
    onOverflowClick: () -> Unit,
    selectedCategory: String,
    categories: List<String>
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = onFabClick) {
                Icon(Icons.Default.Add, contentDescription = "Add Note")
            }
        }
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
                onOverflowClick = onOverflowClick,
                selectedCategory = selectedCategory,
                categories = categories // ✅ this stays
            )

            NoteListContent(
                notes = notes,
                onNoteClick = onNoteClick,
                onDeleteRequest = onDeleteRequest,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}



//@Preview(showBackground = true)
//@Composable
//fun NoteListLayoutPreview() {
//    val mockNotes = listOf(
//        Note(id = 1, title = "Note 1", content = "This is note 1"),
//        Note(id = 2, title = "Note 2", content = "This is note 2"),
//    )
//
//    NoteListLayout(
//        notes = mockNotes,
//        noteToDelete = null,
//        onDeleteRequest = {},
//        onNoteClick = {},
//        onFabClick = {},
//        snackbarHostState = remember { SnackbarHostState() },
//    )
//}


