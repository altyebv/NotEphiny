package com.zeros.notephiny.data.model

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    val title: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val embedding: List<Float>? = null,
    val category: String = "Journal",
    val color: Int = 0xFFFFF9C4.toInt(),
    val isPinned: Boolean = false,
    val isLocked: Boolean = false,
    val similarity: Float? = null
) {
    companion object {
        val noteColors = listOf(
            Color(0xFFFFF59D),
            Color(0xFF80DEEA),
            Color(0xFFA5D6A7),
            Color(0xFFFFAB91)
        )
    }
}

