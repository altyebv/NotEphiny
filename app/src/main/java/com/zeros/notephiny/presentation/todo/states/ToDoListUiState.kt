package com.zeros.notephiny.presentation.todo.states

import com.zeros.notephiny.data.model.Todo

data class TodoListUiState(
    val todos: List<TodoItemUiState> = emptyList(),
    val isCompleted: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAddingTodo: Boolean = false,
    val isInBulkEditMode: Boolean = false,
    val selectedTodoIds: Set<Int> = emptySet(),
    val hideCompleted: Boolean = false,
    val selectedCategory: TodoCategory = TodoCategory.All,
    val expandedSections: Set<TodoSectionType> = setOf(
        TodoSectionType.ACTIVE,
        TodoSectionType.DERIVED,
        TodoSectionType.COMPLETED
    )

)
