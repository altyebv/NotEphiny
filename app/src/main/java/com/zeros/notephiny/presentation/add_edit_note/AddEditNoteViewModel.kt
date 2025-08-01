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
import com.zeros.notephiny.presentation.components.menus.NoteScreenMenu
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
        val initialCategory = savedStateHandle.get<String>("category") ?: "Journal"
        setInitialCategory(initialCategory)

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
                        updatedAt = note.updatedAt,
                        isPinned = note.isPinned
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
                val createdAt = if (isNewNote) now else state.createdAt ?: now
                val updatedAt = now

                noteRepository.saveNoteWithEmbedding(
                    id = currentNoteId,
                    title = state.title,
                    content = state.content,
                    category = state.category,
                    color = state.color,
                    isPinned = state.isPinned,
                    createdAt = createdAt,
                    updatedAt = updatedAt
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
    fun togglePin() {
        val current = _uiState.value
        val newPinState = !current.isPinned
        _uiState.value = current.copy(isPinned = newPinState)

        currentNoteId?.let { id ->
            viewModelScope.launch {
                noteRepository.togglePinById(id, newPinState)
            }
        }
    }

    private fun setInitialCategory(category: String) {
        _uiState.value = _uiState.value.copy(category = category)
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


    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun onMenuAction(action: NoteScreenMenu) {
        when (action) {
            NoteScreenMenu.Find -> {
                enterFindMode()
            }

            NoteScreenMenu.Pin -> {
                togglePin()
            }

            NoteScreenMenu.Move -> {
                // TODO: open a bottom sheet or dialog for categories
            }

            NoteScreenMenu.Delete -> {
                _uiState.update { it.copy(showDeleteDialog = true) }
            }
        }
    }
    fun enterFindMode() {
        _uiState.update { it.copy(isFindMode = true) }
    }

    fun exitFindMode() {
        _uiState.update { it.copy(isFindMode = false, findQuery = "") }
    }

    fun onFindQueryChanged(query: String) {
        _uiState.update { it.copy(findQuery = query) }
    }



}


