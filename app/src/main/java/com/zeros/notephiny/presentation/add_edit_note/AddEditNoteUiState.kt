package com.zeros.notephiny.presentation.add_edit_note

import androidx.compose.ui.graphics.toArgb
import com.zeros.notephiny.data.model.Note
import com.zeros.notephiny.domain.repository.DefaultCategories
import com.zeros.notephiny.domain.repository.DefaultNoteCategory

data class AddEditNoteUiState(

    val title: String = "",
    val content: String = "",
    val color: Int = Note.noteColors.random().toArgb(),
    val category: String = DefaultNoteCategory,
    val createdAt: Long? = null,
    val updatedAt: Long? = null,
    val availableCategories: List<String> = emptyList(),
    val errorMessage: String? = null,
    val showDeleteDialog: Boolean = false,
    val isEdited: Boolean = false,
    val isMoreMenuVisible: Boolean = false,
    val isShareMenuVisible: Boolean = false



)


