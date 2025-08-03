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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import com.zeros.notephiny.presentation.components.DeleteNoteDialog
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
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    var noteToDelete by remember { mutableStateOf<Note?>(null) }
    var lastDeletedNote by remember { mutableStateOf<Note?>(null) }
    var showUndoSnackbar by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val categories by viewModel.availableCategories.collectAsState()
    val state by viewModel.uiState.collectAsState()

    BackHandler(enabled = state.mode == NoteListMode.MULTI_SELECT) {
        viewModel.exitMultiSelectMode()
    }

    NoteListLayout(
        notes = notes,
        noteToDelete = noteToDelete,
        onDeleteRequest = { note -> noteToDelete = note },
        onNoteClick = { note ->
            if (state.mode == NoteListMode.MULTI_SELECT) {
                viewModel.toggleNoteSelection(note.id!!)
            } else {
                navController.navigate(
                    Screen.AddEditNote.route + "?noteId=${note.id}&noteColor=${note.color}"
                )
            }
        },
//        onFabClick = {
//            navController.navigate(
//                Screen.AddEditNote.route +
//                        "?noteId=-1&noteColor=-1&category=${URLEncoder.encode(selectedCategory, StandardCharsets.UTF_8.toString())
//                        }"
//            )
//        },
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
        onSelectAll = { viewModel.selectAll() }
    )
    LaunchedEffect(Unit) {
        Log.d("NoteListScreen", "LaunchedEffect triggered")
        setFabClick {
            navController.navigate(
                Screen.AddEditNote.route +
                        "?noteId=-1&noteColor=-1&category=${
                            URLEncoder.encode(
                                selectedCategory,
                                StandardCharsets.UTF_8.toString()
                            )
                        }"
            )
        }
    }

    noteToDelete?.let { note ->
        DeleteNoteDialog(
            noteTitle = note.title,
            onDelete = {
                viewModel.deleteNote(note)
                lastDeletedNote = note
                noteToDelete = null
                showUndoSnackbar = true
            },
            onDismiss = {
                noteToDelete = null
            }
        )
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    LaunchedEffect(currentRoute) {
        if (currentRoute == Screen.NoteList.route) {
            setFabClick {
                navController.navigate(
                    Screen.AddEditNote.route +
                            "?noteId=-1&noteColor=-1&category=" +
                            URLEncoder.encode(selectedCategory, StandardCharsets.UTF_8.toString())
                )
            }
        } else {
            setFabClick(null)
        }
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



