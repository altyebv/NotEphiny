package com.zeros.notephiny.presentation.todo

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.zeros.notephiny.data.model.Todo
import com.zeros.notephiny.presentation.components.DeleteNoteDialog
import com.zeros.notephiny.presentation.components.menus.GenericDropdownMenu
import com.zeros.notephiny.presentation.components.menus.MenuAction
import com.zeros.notephiny.presentation.components.menus.TodoMenuItem
import com.zeros.notephiny.presentation.components.menus.label
import com.zeros.notephiny.presentation.notes.NoteListViewModel.NavigationEvent
import com.zeros.notephiny.presentation.todo.states.TodoCategory
import com.zeros.notephiny.presentation.todo.states.TodoItemUiState
import com.zeros.notephiny.presentation.todo.states.TodoSectionType
import com.zeros.notephiny.presentation.todo.states.toTodo
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoAiScreen(
    viewModel: TodoViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
    navController: NavController,
    setFabClick: ((() -> Unit)?) -> Unit,
    selectedTodoIds: Set<Int> = emptySet(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var todoToDelete by remember { mutableStateOf<Todo?>(null) }
    val todos = uiState.todos
    val derivedTodos = todos.filter { !it.isDone && it.isDerived }
    val activeTodos = todos.filter { !it.isDone && !it.isDerived }
    val completedTodos = todos.filter { it.isDone }
    var recentlyDeletedTodo by remember { mutableStateOf<Todo?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var menuExpanded by remember { mutableStateOf(false) }

    val navigationEvent = viewModel.navigationEvent

    LaunchedEffect(Unit) {
        navigationEvent.collect { event ->
            when (event) {
                is NavigationEvent.GoToSettings -> {
                    navController.navigate("settings") // or your actual route
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        setFabClick {
            viewModel.showAddTodoSheet()
        }
    }

    if (uiState.isAddingTodo) {
        AddTodo(
            isVisible = uiState.isAddingTodo,
            onDismiss = { viewModel.onDismissAddTodo() },
            onSave = { title -> viewModel.addTodo(title) }
        )
    }
    todoToDelete?.let { todo ->
        DeleteNoteDialog(
            noteTitle = todo.title,
            onDelete = {
                viewModel.deleteTodo(todo)
                recentlyDeletedTodo = todo
                todoToDelete = null

                scope.launch {
                    val result = snackbarHostState.showSnackbar(
                        message = "Todo deleted",
                        actionLabel = "Undo"
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        recentlyDeletedTodo?.let { viewModel.restoreTodo(it) }
                        recentlyDeletedTodo = null
                    }
                }
            },
            onDismiss = { todoToDelete = null }
        )
    }

    Scaffold(
        topBar = {
            if (uiState.isInBulkEditMode) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    MultiSelectIconsRow(
                        onCancel = { viewModel.exitBulkEditMode() },
                        onSelectAll = { viewModel.selectAll() },
                        selectedCount = uiState.selectedTodoIds.size
                    )

                    TitleWithCountRow(
                        title = if (selectedTodoIds.isEmpty()) "Select items" else "${selectedTodoIds.size} selected",
                        selectedTodoIds = uiState.selectedTodoIds
                    )
                }
            } else {
                TodoTopSection(
                    count = todos.size,
                    hideCompleted = uiState.hideCompleted,
                    menuExpanded = menuExpanded,
                    onMenuClick = { menuExpanded = true },
                    onMenuDismissRequest = { menuExpanded = false },
                    onMenuItemClick = { item ->
                        viewModel.onMenuAction(item)
                        menuExpanded = false
                    },
                    selectedCategory = uiState.selectedCategory,
                    onCategorySelected = {viewModel.selectCategory(it)  },
                )
            }
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.navigationBarsPadding()
            )
        },
        modifier = modifier
    ) { innerPadding ->


        TodoContentSection(
            activeTodos = activeTodos,
            derivedTodos = derivedTodos,
            completedTodos = completedTodos,
            expandedSections = uiState.expandedSections,
            onToggleSection = viewModel::toggleSection,
            isInBulkEditMode = uiState.isInBulkEditMode,
            selectedTodoIds = uiState.selectedTodoIds,
            onToggleDone = { viewModel.toggleDone(it.toTodo()) },
            onToggleSelection = { viewModel.toggleTodoSelection(it.id!!) },
            onLongPress = { todoToDelete = it.toTodo() },
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoTopSection(
    count: Int,
    hideCompleted: Boolean,
    menuExpanded: Boolean,
    onMenuClick: () -> Unit,
    onMenuDismissRequest: () -> Unit,
    onMenuItemClick: (TodoMenuItem) -> Unit,
    selectedCategory: TodoCategory,
    onCategorySelected: (TodoCategory) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Box {
                IconButton(onClick = onMenuClick) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options"
                    )
                }

                GenericDropdownMenu(
                    expanded = menuExpanded,
                    onDismiss = onMenuDismissRequest,
                    actions = TodoMenuItem.entries.map { item ->
                        MenuAction(
                            label = item.label(isHideCompleted = hideCompleted),
                            onClick = { onMenuItemClick(item) },
                            isSelected = false
                        )
                    }
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text(
                text = "ToDos",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "$count items",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

        }
    }
}


@Composable
fun TodoContentSection(
    activeTodos: List<TodoItemUiState>,
    derivedTodos: List<TodoItemUiState>,
    completedTodos: List<TodoItemUiState>,
    expandedSections: Set<TodoSectionType>,
    onToggleSection: (TodoSectionType) -> Unit,
    isInBulkEditMode: Boolean,
    selectedTodoIds: Set<Int>,
    onToggleDone: (TodoItemUiState) -> Unit,
    onToggleSelection: (TodoItemUiState) -> Unit,
    onLongPress: (TodoItemUiState) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 120.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {

        // === Active Section ===
        if (activeTodos.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "🟢 Active",
                    isExpanded = TodoSectionType.ACTIVE in expandedSections,
                    onToggle = { onToggleSection(TodoSectionType.ACTIVE) }
                )
            }

            if (TodoSectionType.ACTIVE in expandedSections) {
                items(activeTodos, key = { it.id ?: -1 }) { todo ->
                    TodoCard(
                        todo = todo,
                        isInBulkEditMode = isInBulkEditMode,
                        isSelected = selectedTodoIds.contains(todo.id),
                        onToggleDone = onToggleDone,
                        onToggleSelection = onToggleSelection,
                        onLongPress = onLongPress
                    )
                }
            }
        }

        // === Derived Section ===
        if (derivedTodos.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(
                    title = "🧠 From Notes",
                    isExpanded = TodoSectionType.DERIVED in expandedSections,
                    onToggle = { onToggleSection(TodoSectionType.DERIVED) }
                )
            }

            if (TodoSectionType.DERIVED in expandedSections) {
                items(derivedTodos, key = { it.id ?: -1 }) { todo ->
                    TodoCard(
                        todo = todo,
                        isInBulkEditMode = isInBulkEditMode,
                        isSelected = selectedTodoIds.contains(todo.id),
                        onToggleDone = onToggleDone,
                        onToggleSelection = onToggleSelection,
                        onLongPress = onLongPress
                    )
                }
            }
        }

        // === Completed Section ===
        if (completedTodos.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(
                    title = "✅ Completed",
                    isExpanded = TodoSectionType.COMPLETED in expandedSections,
                    onToggle = { onToggleSection(TodoSectionType.COMPLETED) }
                )
            }

            if (TodoSectionType.COMPLETED in expandedSections) {
                items(completedTodos, key = { it.id ?: -1 }) { todo ->
                    TodoCard(
                        todo = todo,
                        isInBulkEditMode = isInBulkEditMode,
                        isSelected = selectedTodoIds.contains(todo.id),
                        onToggleDone = onToggleDone,
                        onToggleSelection = onToggleSelection,
                        onLongPress = onLongPress
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TodoCard(
    todo: TodoItemUiState,
    isInBulkEditMode: Boolean,
    isSelected: Boolean,
    onToggleDone: (TodoItemUiState) -> Unit,
    onToggleSelection: (TodoItemUiState) -> Unit,
    onLongPress: ((TodoItemUiState) -> Unit)? = null,
) {
    val haptic = LocalHapticFeedback.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .combinedClickable(
                onClick = { onToggleDone(todo) },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongPress?.invoke(todo)
                }
            ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!isInBulkEditMode) {
                    Checkbox(
                        checked = todo.isDone,
                        onCheckedChange = { onToggleDone(todo) },
                        interactionSource = remember { MutableInteractionSource() },
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Text(
                    text = todo.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        textDecoration = if (todo.isDone && !isInBulkEditMode) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    modifier = Modifier.weight(1f)
                )

                if (isInBulkEditMode) {
                    IconToggleButton(
                        checked = isSelected,
                        onCheckedChange = { onToggleSelection(todo) }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = if (isSelected) "Selected" else "Not selected"
                        )
                    }
                }
            }

            if (todo.isDerived && todo.sourceNoteTitle != null) {
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info, // or Notes icon
                        contentDescription = "Source Note",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "Derived from: ${todo.sourceNoteTitle}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.secondary
                        )
                    )
                }
            }

        }
    }
}

@Composable
fun MultiSelectIconsRow(
    onCancel: () -> Unit,
    onSelectAll: () -> Unit,
    selectedCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onCancel) {
            Text("Cancel", style = MaterialTheme.typography.labelLarge)
        }

        TextButton(onClick = onSelectAll) {
            Text("Select All", style = MaterialTheme.typography.labelLarge)
        }
    }
}
@Composable
fun TitleWithCountRow(
    title: String,
    count: Int? = null,
    selectedTodoIds: Set<Int>? = null,
    showCount: Boolean = true,
) {
    val resolvedCount = selectedTodoIds?.size ?: count

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge
        )
        if (showCount && (resolvedCount ?: 0) > 0) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "(${resolvedCount})",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
@Composable
fun CategoryChips(
    modifier: Modifier = Modifier,
    selectedCategory: TodoCategory,
    onCategorySelected: (TodoCategory) -> Unit
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState()), // Now it's safe
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TodoCategory.values().forEach { category ->
            AssistChip(
                onClick = { onCategorySelected(category) },
                label = { Text(category.label) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (category == selectedCategory)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }
}



