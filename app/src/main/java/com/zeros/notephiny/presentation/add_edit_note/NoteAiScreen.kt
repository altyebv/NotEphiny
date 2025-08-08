package com.zeros.notephiny.presentation.add_edit_note

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.zeros.notephiny.core.util.formatDateTime
import com.zeros.notephiny.presentation.components.HighlightedText
import com.zeros.notephiny.presentation.components.SearchBarRow
import com.zeros.notephiny.presentation.components.menus.NoteScreenMenu
import com.zeros.notephiny.presentation.icons.Pin
import com.zeros.notephiny.presentation.icons.Stars
import com.zeros.notephiny.testing_features.NoteDropdownMenu
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteAIScreen(
    viewModel: AddEditNoteViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onNoteSaved: () -> Unit = {},
    setFabClick: ((() -> Unit)?) -> Unit,
    category: String,
    navController: NavHostController
) {
    val uiState by viewModel.uiState.collectAsState()
    val timestamp = uiState.updatedAt ?: uiState.createdAt ?: System.currentTimeMillis()
    val formattedDate = formatDateTime(timestamp)
    val keyboardController = LocalSoftwareKeyboardController.current

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    // Sheet open/close handling
    LaunchedEffect(uiState.showRelated) {
        if (uiState.showRelated) {
            sheetState.show()
        } else {
            sheetState.hide()
        }
    }

    LaunchedEffect(Unit) {
        setFabClick(null)
    }


    Scaffold(
        bottomBar = {
            BottomBarSection(
                showRelated = uiState.showRelated,
                showHighlights = uiState.highlightActions,
                onToggleRelated = {
                    viewModel.toggleShowRelated()
                },
                onToggleHighlight = { viewModel.toggleHighlightMode() },
                onDetectActions = { viewModel.detectActionsFromText() },
                onTogglePin = {viewModel.togglePin(note = uiState.toNote())}
            )
        }
    ) { innerPadding ->

        // Main content (always full screen)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TopBarSection(
                category = category,
                isEdited = uiState.isEdited,
                dateTime = formattedDate,
                onBack = onBack,
                onSaveClick = {
                    viewModel.saveNote(
                        onSuccess = {
                            keyboardController?.hide()
                            onNoteSaved()
                        },
                        onError = { message -> Log.e("NoteSave", message) }
                    )
                },
                onMenuAction = viewModel::onMenuAction,
                isPinned = uiState.isPinned,
                isFindMode = uiState.isFindMode,
                findQuery = uiState.findQuery,
                onQueryChange = viewModel::onFindQueryChanged,
                onCancelClick = viewModel::exitFindMode,
            )

            NoteEditorSection(
                title = uiState.title,
                onTitleChange = viewModel::onTitleChange,
                content = uiState.content,
                onContentChange = viewModel::onContentChange,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .weight(1f),
                isFindMode = uiState.isFindMode,
                findQuery = uiState.findQuery,
                extractedActions = uiState.extractedActions,
                highlightActions = uiState.highlightActions,
                viewModel = viewModel
            )
        }

        // Bottom sheet for related notes
        if (uiState.showRelated) {
            ModalBottomSheet(
                onDismissRequest = {
                    viewModel.toggleShowRelated()
                },
                sheetState = sheetState
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Related Notes", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    if (uiState.relatedNotes.isNotEmpty()) {
                        RelatedNotesSection(
                            relatedNotes = uiState.relatedNotes,
                            onNoteClick = { note ->
                                coroutineScope.launch {
                                    sheetState.hide()
                                    viewModel.toggleShowRelated()
                                }
                                navController.navigate("note/${note.id}")
                            }
                        )
                    } else {
                        Text(
                            text = "No related notes found.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarSection(
    category: String,
    isEdited: Boolean,
    dateTime: String,
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
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
                    .padding(horizontal = 8.dp),
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

        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "$dateTime | $category",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (isPinned) {
                Icon(
                    imageVector = Pin,
                    contentDescription = "Pinned",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

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
    content: String,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    isFindMode: Boolean = false,
    findQuery: String = "",
    modifier: Modifier = Modifier,
    extractedActions: List<String>,
    highlightActions: Boolean,
    viewModel: AddEditNoteViewModel
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val editorHeight = screenHeight * 0.4f

    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    var selectedAction by remember { mutableStateOf<String?>(null) }
    var actionPosition by remember { mutableStateOf(Offset.Zero) }
    var showMenu by remember { mutableStateOf(false) }




    Column(
        modifier = modifier
            .padding(top = 4.dp)
            .height(editorHeight)
    ) {
        // Title
        BasicTextField(
            value = title,
            onValueChange = onTitleChange,
            textStyle = MaterialTheme.typography.headlineSmall.copy(
                color = MaterialTheme.colorScheme.onBackground
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            decorationBox = { innerTextField ->
                Box {
                    if (title.isEmpty()) {
                        Text(
                            text = "Heading",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                            )
                        )
                    }
                    innerTextField()
                }
            }
        )

        Spacer(Modifier.height(12.dp))

        // Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            BasicTextField(
                value = content,
                onValueChange = onContentChange,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onBackground
                ),
                modifier = Modifier
                    .fillMaxSize(),
                onTextLayout = { layoutResult = it },
                decorationBox = { innerTextField ->
                    when {
                        content.isEmpty() -> {
                            Text(
                                text = "Start typing...",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                                )
                            )
                        }

                        isFindMode && findQuery.isNotBlank() -> {
                            HighlightedText(
                                text = content,
                                query = findQuery,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            )
                        }

                        highlightActions -> {
                            HighlightedActionsText(
                                text = content,
                                actions = extractedActions,
                                style = MaterialTheme.typography.bodyLarge,
                                onActionClick = { action, offset ->
                                    selectedAction = action
                                    actionPosition = offset
                                    showMenu = true
                                }
                            )
                        }

                        else -> innerTextField()
                    }
                }
            )

            if (showMenu && selectedAction != null) {
                val fixedOffset = Offset(x = 0f, y = 0f)
                ActionPopupMenu(
                    visible = showMenu,
                    position = fixedOffset,
                    actionText = selectedAction ?: "",
                    onAddTodo = {
                        viewModel.deriveTodosFromCurrentNote()
                        showMenu = false
                    },
                    onDismiss = {
                        showMenu = false
                    }
                )
            }
        }
    }
}



@Composable
fun BottomBarSection(
    showRelated: Boolean,
    showHighlights: Boolean,
    onToggleRelated: () -> Unit,
    onToggleHighlight: () -> Unit,
    onDetectActions: () -> Unit,
    onTogglePin: () -> Unit
) {
    BottomAppBar(modifier = Modifier.height(68.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onToggleRelated) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Toggle Related",
                    tint = if (showRelated) MaterialTheme.colorScheme.primary else Color.Gray
                )
            }
            IconButton(onClick = onTogglePin) {
                Icon(
                    Pin,
                    contentDescription = "Pin note",
                    tint = if (showRelated) MaterialTheme.colorScheme.primary else Color.Gray

                )
            }
            IconButton(onClick = onToggleHighlight) {
                Icon(
                    imageVector = Stars,
                    contentDescription = "Toggle Highlights",
                    tint = if (showHighlights) MaterialTheme.colorScheme.primary else Color.Gray
                )
            }
        }
    }
}






