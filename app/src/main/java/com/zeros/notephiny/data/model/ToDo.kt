package com.zeros.notephiny.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.zeros.notephiny.presentation.todo.states.TodoCategory


@Entity(tableName = "todos")
data class Todo(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val title: String,
    val isDone: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val dueDate: Long? = null,
    val noteId: Int? = null,
    val embedding: List<Float> = emptyList(),
    val isArchived: Boolean = false,
    val isDerived : Boolean = false,
)
fun Todo.matchesCategory(category: TodoCategory): Boolean {
    return when (category) {
        TodoCategory.All -> true
        TodoCategory.Active -> !isDone && !isArchived
        TodoCategory.Completed -> isDone
        TodoCategory.Linked -> noteId != null
        TodoCategory.Archived -> isArchived
    }
}

