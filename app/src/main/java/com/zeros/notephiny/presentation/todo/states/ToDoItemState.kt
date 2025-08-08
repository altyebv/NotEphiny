package com.zeros.notephiny.presentation.todo.states

import com.zeros.notephiny.data.model.Todo

data class TodoItemUiState(
    val id: Int? = null,
    val title: String,
    val isDone: Boolean,
    val createdAt: Long,
    val dueDate: Long? = null,
    val noteId: Int? = null,
    val isEditing: Boolean = false,
    val isSelected: Boolean = false,
    val sourceNoteTitle: String? = null,
    val embeddingConfidence: Float? = null,
    val groupTag: String? = null,
    val isArchived: Boolean = false,
    val isDerived: Boolean = false
)

fun TodoItemUiState.toTodo(): Todo = Todo(
    id = id,
    title = title,
    isDone = isDone,
    createdAt = createdAt,
    dueDate = dueDate,
    noteId = noteId,
    isArchived = isArchived,
    isDerived =isDerived,
    embedding = emptyList()
)

fun Todo.toUiState(): TodoItemUiState = TodoItemUiState(
    id = id,
    title = title,
    isDone = isDone,
    createdAt = createdAt,
    dueDate = dueDate,
    noteId = noteId,
    isArchived = isArchived,
    isDerived = isDerived,
)