package com.zeros.notephiny.presentation.add_edit_note

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeros.notephiny.data.model.Note
import com.zeros.notephiny.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.zeros.notephiny.core.util.formatDateTime
import kotlinx.coroutines.flow.update


@HiltViewModel
class AddEditNoteViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditNoteUiState())
    val uiState: StateFlow<AddEditNoteUiState> = _uiState
    val availableCategories = noteRepository.getDefaultCategories()

    private var currentNoteId: Int? = null
    val now = System.currentTimeMillis()


    init {
        val noteId = savedStateHandle.get<Int>("noteId") ?: -1
        val color = savedStateHandle.get<Int>("noteColor") ?: -1

        if (color != -1) {
            _uiState.value = _uiState.value.copy(color = color)
        }

        if (noteId != -1) {
            viewModelScope.launch {
                noteRepository.getNoteById(noteId)?.let { note ->
                    currentNoteId = note.id
                    _uiState.value = _uiState.value.copy(
                        title = note.title,
                        content = note.content,
                        color = note.color,
                        category = note.category ?: "General",
                        createdAt = note.createdAt,
                        updatedAt = note.updatedAt
                    )
                }
            }
        }
    }

    fun saveNote(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val state = _uiState.value
        val isNewNote = currentNoteId == null

        if (state.title.isBlank() && state.content.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Note is empty")
            onError("Note is empty")
            return
        }

        viewModelScope.launch {
            try {
                val note = Note(
                    id = currentNoteId ?: 0,
                    title = state.title,
                    content = state.content,
                    color = state.color,
                    category = state.category,
                    createdAt = if (isNewNote) now else state.createdAt ?: now,
                    updatedAt = now
                )

                noteRepository.saveNoteWithEmbedding(
                    id = currentNoteId,
                    title = note.title,
                    content = note.content,
                    category = note.category ?: "",
                    color = note.color,
                    createdAt = note.createdAt,
                    updatedAt = now
                )

                onSuccess()
            } catch (e: Exception) {
                val message = "Failed to save note: ${e.message}"
                _uiState.value = state.copy(errorMessage = message)
                onError(message)
            }
        }
    }
    fun onTitleChange(newTitle: String) {
        _uiState.update { current ->
            current.copy(
                title = newTitle,
                isEdited = newTitle.isNotBlank() || current.content.isNotBlank()
            )
        }
    }

    fun onContentChange(newContent: String) {
        _uiState.update { current ->
            current.copy(
                content = newContent,
                isEdited = current.title.isNotBlank() || newContent.isNotBlank()
            )
        }
    }


    fun onCategoryChange(newCategory: String) {
        _uiState.value = _uiState.value.copy(category = newCategory)
    }

    fun onColorChange(newColor: Int) {
        _uiState.value = _uiState.value.copy(color = newColor)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    fun toggleMoreMenu() {
        _uiState.value = _uiState.value.copy(isMoreMenuVisible = !_uiState.value.isMoreMenuVisible)
    }

    fun toggleShareMenu() {
        _uiState.value = _uiState.value.copy(isShareMenuVisible = !_uiState.value.isShareMenuVisible)
    }

    fun dismissMenus() {
        _uiState.value = _uiState.value.copy(
            isMoreMenuVisible = false,
            isShareMenuVisible = false
        )
    }
}


