package com.zeros.notephiny.presentation.notes


import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column

import androidx.compose.material3.*

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.zeros.notephiny.core.util.Screen
import com.zeros.notephiny.data.model.Note
import com.zeros.notephiny.presentation.components.NotesTop


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(
    navController: NavController,
    viewModel: NoteListViewModel = hiltViewModel()
) {
    val notes by viewModel.filteredNotes.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()

    var noteToDelete by remember { mutableStateOf<Note?>(null) }
    var lastDeletedNote by remember { mutableStateOf<Note?>(null) }
    var showUndoSnackbar by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val categories by viewModel.availableCategories.collectAsState()

    NoteListLayout(
        notes = notes,
        noteToDelete = noteToDelete,
        onDeleteRequest = { note -> noteToDelete = note },
        onNoteClick = { note ->
            navController.navigate(
                Screen.AddEditNote.route + "?noteId=${note.id}&noteColor=${note.color}"
            )
        },
        onFabClick = {
            navController.navigate("add_edit_note")
        },
        snackbarHostState = snackbarHostState,
        searchQuery = searchQuery,
        isSearching = isSearching,
        onSearchQueryChange = viewModel::onSearchQueryChanged,
        onCancelSearch = viewModel::cancelSearch,
        onSearchClick = viewModel::startSearch,
        onCategorySelected = viewModel::selectCategory,
        onOverflowClick = { /* overflow */ },
        selectedCategory = selectedCategory,
        categories = categories
    )

    noteToDelete?.let { note ->
        AlertDialog(
            onDismissRequest = { noteToDelete = null },
            title = { Text("Delete Note") },
            text = { Text("Are you sure you want to delete this note?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteNote(note)
                    lastDeletedNote = note
                    noteToDelete = null
                    showUndoSnackbar = true
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { noteToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    LaunchedEffect(showUndoSnackbar) {
        if (showUndoSnackbar && lastDeletedNote != null) {
            val result = snackbarHostState.showSnackbar(
                message = "Note deleted",
                actionLabel = "Undo",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.restoreNote()
            }
            showUndoSnackbar = false
        }
    }
}



