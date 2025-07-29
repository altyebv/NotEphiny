package com.zeros.notephiny.presentation.notes

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.zeros.notephiny.core.util.Screen
import com.zeros.notephiny.data.model.Note
import com.zeros.notephiny.presentation.components.CategoryChips
import com.zeros.notephiny.presentation.components.NotesTop
import com.zeros.notephiny.presentation.components.NotesTopBar
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
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
)  {
    val categories = listOf("All") + notes.map { it.category }.distinct()

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
                categories = categories
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


