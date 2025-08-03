package com.zeros.notephiny.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "todos")
data class Todo(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val isDone: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val dueDate: Long? = null, // optional
    val noteId: Int? = null // for later semantic linking
)
