package com.zeros.notephiny.presentation.notes

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
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
import com.zeros.notephiny.presentation.notes.NoteListViewModel.NoteListMode
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import com.zeros.notephiny.presentation.components.DeleteNoteDialog
import com.zeros.notephiny.presentation.components.SearchOverlay
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(
    navController: NavController,
    viewModel: NoteListViewModel = hiltViewModel(),
    setFabClick: ((() -> Unit)?) -> Unit
) {
    val notes by viewModel.filteredNotes.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val categories by viewModel.availableCategories.collectAsState()
    val state by viewModel.uiState.collectAsState()

    var noteToDelete by remember { mutableStateOf<Note?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    BackHandler(enabled = state.mode == NoteListMode.MULTI_SELECT) {
        viewModel.exitMultiSelectMode()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Main content underneath
        NoteListLayout(
            notes = notes,
            noteToDelete = noteToDelete,
            onDeleteRequest = { noteToDelete = it },
            onNoteClick = { note ->
                if (state.mode == NoteListMode.MULTI_SELECT) {
                    viewModel.toggleNoteSelection(note.id!!)
                } else {
                    navController.navigate(
                        Screen.AddEditNote.route + "?noteId=${note.id}&noteColor=${note.color}"
                    )
                }
            },
            snackbarHostState = snackbarHostState,
            searchQuery = searchQuery,
            isSearching = isSearching,
            onSearchQueryChange = viewModel::onSearchQueryChanged,
            onCancelSearch = viewModel::cancelSearch,
            onSearchClick = viewModel::startSearch,
            onCategorySelected = viewModel::selectCategory,
            onOverflowClick = viewModel::onMainMenuAction,
            selectedCategory = selectedCategory,
            categories = categories,
            sortOrder = state.sortOrder,
            selectedNoteIds = state.selectedNoteIds,
            mode = state.mode,
            onCancelMultiSelect = viewModel::exitMultiSelectMode,
            onSelectAll = viewModel::selectAll,
            modifier = Modifier.fillMaxSize()
        )

        // Overlay shown only when searching
        if (isSearching) {
            SearchOverlay(
                query = searchQuery,
                onQueryChange = viewModel::onSearchQueryChanged,
                onCancelClick = viewModel::cancelSearch,
                searchResults = notes,
                onNoteClick = { note ->
                    viewModel.cancelSearch()
                    navController.navigate(
                        Screen.AddEditNote.route + "?noteId=${note.id}&noteColor=${note.color}"
                    )
                }
            )
        }
    }

    // Existing dialog/snackbar/undo logic here (unchanged)
    noteToDelete?.let { note ->
        DeleteNoteDialog(
            noteTitle = note.title,
            onDelete = {
                viewModel.deleteNote(note)
                noteToDelete = null
            },
            onDismiss = {
                noteToDelete = null
            }
        )
    }

    LaunchedEffect(Unit) {
        setFabClick {
            navController.navigate(
                Screen.AddEditNote.route +
                        "?noteId=-1&noteColor=-1&category=" +
                        URLEncoder.encode(selectedCategory, StandardCharsets.UTF_8.toString())
            )
        }
    }
}


