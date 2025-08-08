package com.zeros.notephiny.presentation.add_edit_note

import androidx.compose.ui.graphics.toArgb
import com.zeros.notephiny.data.model.Note
import com.zeros.notephiny.domain.repository.DefaultNoteCategory

data class AddEditNoteUiState(
    val id: Int? = null,
    val title: String = "",
    val content: String = "",
    val color: Int = Note.noteColors.random().toArgb(),
    val category: String = DefaultNoteCategory,
    val createdAt: Long? = null,
    val updatedAt: Long? = null,
    val showMoveNotebookSheet: Boolean = false,
    val availableCategories: List<String> = emptyList(),
    val errorMessage: String? = null,
    val showDeleteDialog: Boolean = false,
    val isEdited: Boolean = false,
    val isPinned: Boolean = false,
    val isMoreMenuVisible: Boolean = false,
    val isShareMenuVisible: Boolean = false,
    val isFindMode: Boolean = false,
    val findQuery: String = "",
    val showRelated: Boolean = false,
    val highlightActions: Boolean = false,
    val extractedActions: List<String> = emptyList(),
    val relatedNotes: List<Note> = emptyList()

)
fun AddEditNoteUiState.toNote(): Note {
    return Note(
        id = this.id,
        title = this.title,
        content = this.content,
        color = this.color,
        category = this.category,
        createdAt = this.createdAt ?: System.currentTimeMillis(),
        updatedAt = this.updatedAt ?: System.currentTimeMillis(),
        isPinned = this.isPinned
        // add other Note fields here if needed
    )
}
fun Note.toUiState(
    currentUiState: AddEditNoteUiState? = null
): AddEditNoteUiState = AddEditNoteUiState(
    id = this.id,
    title = this.title,
    content = this.content,
    color = this.color,
    category = this.category,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt,
    isPinned = this.isPinned,
)





