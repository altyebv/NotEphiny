package com.zeros.notephiny.presentation.todo.states

import com.zeros.notephiny.data.model.Todo

data class TodoListUiState(
    val todos: List<Todo> = emptyList(),
    val isCompleted: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAddingTodo: Boolean = false,
    val isInBulkEditMode: Boolean = false,
    val selectedTodoIds: Set<Int> = emptySet(),
    val hideCompleted: Boolean = false

)
