package com.zeros.notephiny.presentation.add_edit_note

import androidx.compose.ui.graphics.toArgb
import com.zeros.notephiny.data.model.Note

data class AddEditNoteUiState(
    val title: String = "",
    val content: String = "",
    val color: Int = Note.noteColors.random().toArgb(),
    val category: String = "Journal",
    val availableCategories: List<String> = listOf("Journal", "Ideas", "Quick Notes", "Tasks"),
    val errorMessage: String? = null
)


