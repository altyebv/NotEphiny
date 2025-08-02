package com.zeros.notephiny.presentation.add_edit_note

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zeros.notephiny.core.util.formatDateTime
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.zIndex
import com.zeros.notephiny.data.model.Note
import com.zeros.notephiny.presentation.components.CategorySelectorUI
import com.zeros.notephiny.presentation.components.DeleteNoteDialog
import com.zeros.notephiny.presentation.components.HighlightedText
import com.zeros.notephiny.presentation.components.SearchBarRow
import com.zeros.notephiny.presentation.components.menus.GenericDropdownMenu
import com.zeros.notephiny.presentation.components.menus.MenuAction
import com.zeros.notephiny.presentation.components.menus.NoteScreenMenu
import com.zeros.notephiny.presentation.components.menus.label
import com.zeros.notephiny.presentation.notes.NoteListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteScreen(
    viewModel: AddEditNoteViewModel = hiltViewModel(),
    listViewModel: NoteListViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onNoteSaved: () -> Unit = {},
    modifier: Modifier = Modifier,
    category: String
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val timestamp = uiState.updatedAt ?: uiState.createdAt ?: System.currentTimeMillis()
    val formattedDate = formatDateTime(timestamp)
    val category = uiState.category
    val keyboardController = LocalSoftwareKeyboardController.current
    val noteToDelete = viewModel.getOriginalNote()



    LaunchedEffect(Unit) {
        viewModel.fetchNotebookGroups()
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
    ) {
        NoteTopSection(
            isEdited = uiState.isEdited,
            dateTime = formattedDate,
            category = category,
            onBack = onBack,
            onSaveClick = {
                viewModel.saveNote(
                    onSuccess = {
                        keyboardController?.hide()
                        onNoteSaved()
                    },
                    onError = { message ->
                        Log.e("NoteSave", message)
                    }
                )
            },
            onMenuAction = viewModel::onMenuAction,
            isPinned = uiState.isPinned,
            isFindMode = uiState.isFindMode,
            findQuery = uiState.findQuery,
            onQueryChange = viewModel::onFindQueryChanged,
            onCancelClick = viewModel::exitFindMode
        )

        Spacer(Modifier.height(16.dp))

        Box(modifier = Modifier.weight(1f)) {
            NoteEditorSection(
                title = uiState.title,
                onTitleChange = viewModel::onTitleChange,
                content = uiState.content,
                onContentChange = viewModel::onContentChange,
                modifier = Modifier.fillMaxSize(),
                isFindMode = uiState.isFindMode,
                findQuery = uiState.findQuery
            )

            if (uiState.isFindMode && uiState.findQuery.isBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.25f),
                                    Color.Transparent
                                )
                            )
                        )
                        .blur(12.dp) // Apply blur effect
                        .zIndex(1f)
                )
            }
        }
        if (uiState.showDeleteDialog && noteToDelete != null) {
            DeleteNoteDialog(
                noteTitle = noteToDelete.title,
                onDelete = {
                    listViewModel.deleteNote(noteToDelete)
                    viewModel.onDeleteDialogDismissed()
                    onBack()
                },
                onDismiss = {
                    viewModel.onDeleteDialogDismissed()
                }
            )
        }
        if (uiState.showMoveNotebookSheet) {
            CategorySelectorUI(
                userNotebooks = viewModel.userNotebooks.value,
                specialNotebooks = viewModel.appNotebooks.value,
                onNotebookClick = { selected ->
                    viewModel.moveNoteToCategory(selected.name) {
                        // Optional: close the sheet or navigate up
//                        navController.popBackStack()
                    }
                }
                ,
                onCreateNew = {
                    // Handle creation if needed
                }
            )
        }



    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun NoteTopSection(
    isEdited: Boolean,
    dateTime: String,
    category: String,
    onBack: () -> Unit,
    onSaveClick: () -> Unit,
    isFindMode: Boolean,
    findQuery: String,
    onMenuAction: (NoteScreenMenu) -> Unit,
    isPinned: Boolean,
    onQueryChange: (String) -> Unit,
    onCancelClick: () -> Unit
) {
    var isMenuExpanded by remember { mutableStateOf(false) }
    var isShareSheetVisible by remember { mutableStateOf(false) }
    var showCategorySelector by remember { mutableStateOf(false) }


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp)
    ) {
        if (isFindMode) {
            SearchBarRow(
                query = findQuery,
                onQueryChange = onQueryChange,
                onCancelClick = onCancelClick,
            )
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    AnimatedContent(
                        targetState = isEdited,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "NoteTopActionTransition"
                    ) { editing ->
                        Row {
                            if (!editing) {
                                IconButton(onClick = { isShareSheetVisible = true }) {
                                    Icon(Icons.Default.Share, contentDescription = "Share")
                                }
                            }

                            Box {
                                IconButton(onClick = { isMenuExpanded = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "More")
                                }

                                NoteDropdownMenu(
                                    expanded = isMenuExpanded,
                                    onDismiss = { isMenuExpanded = false },
                                    onMenuItemClick = { action ->
                                        isMenuExpanded = false
                                        onMenuAction(action)
                                    },
                                    isPinned = isPinned
                                )
                            }

                            if (editing) {
                                IconButton(onClick = onSaveClick) {
                                    Icon(Icons.Default.Check, contentDescription = "Save")
                                }
                            }
                        }
                    }
                }
            }
        }


        Text(
            text = "$dateTime | $category",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    }

    if (isShareSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = { isShareSheetVisible = false }
        ) {
            Text(
                text = "Share Options (Coming Soon)",
                modifier = Modifier.padding(24.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

}



@Composable
fun NoteEditorSection(
    title: String,
    onTitleChange: (String) -> Unit,
    content: String,
    onContentChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isFindMode: Boolean = false,
    findQuery: String = "",

) {
    Column(modifier = modifier.padding(top = 16.dp)) {

        BasicTextField(
            value = title,
            onValueChange = onTitleChange,
            textStyle = MaterialTheme.typography.headlineSmall.copy(
                color = MaterialTheme.colorScheme.onBackground
            ),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                if (title.isEmpty()) {
                    Text(
                        text = "Heading",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                        )
                    )
                }
                innerTextField()
            }
        )

        Spacer(Modifier.height(12.dp))

        BasicTextField(
            value = content,
            onValueChange = onContentChange,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onBackground
            ),
            modifier = Modifier
                .fillMaxSize()
                .weight(1f, fill = true),
            decorationBox = { innerTextField ->
                if (content.isEmpty()) {
                    Text(
                        text = "Start typing...",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                        )
                    )
                } else if (isFindMode && findQuery.isNotBlank()) {
                    HighlightedText(
                        text = content,
                        query = findQuery,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                } else {
                    innerTextField()
                }
            }
        )

    }
}
@Composable
fun NoteDropdownMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onMenuItemClick: (NoteScreenMenu) -> Unit,
    isPinned: Boolean
) {
    GenericDropdownMenu(
        expanded = expanded,
        onDismiss = onDismiss,
        actions = NoteScreenMenu.values().map { action ->
            val label = when (action) {
                NoteScreenMenu.Pin -> if (isPinned) "Unpin" else "Pin"
                else -> action.label()
            }

            MenuAction(
                label = label,
                onClick = { onMenuItemClick(action) }
            )
        }
    )
}








