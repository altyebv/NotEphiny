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

@HiltViewModel
class AddEditNoteViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditNoteUiState())
    val uiState: StateFlow<AddEditNoteUiState> = _uiState

    private var currentNoteId: Int? = null

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
                        category = note.category ?: "Journal"
                    )
                }
            }
        }
    }

    fun onTitleChange(newTitle: String) {
        _uiState.value = _uiState.value.copy(title = newTitle)
    }

    fun onContentChange(newContent: String) {
        _uiState.value = _uiState.value.copy(content = newContent)
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

    fun saveNote(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val state = _uiState.value

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
                    category = state.category
                )

                noteRepository.saveNoteWithEmbedding(
                    note.title,
                    note.content,
                    note.category ?: ""
                )

                onSuccess()
            } catch (e: Exception) {
                val message = "Failed to save note: ${e.message}"
                _uiState.value = state.copy(errorMessage = message)
                onError(message)
            }
        }
    }
}


