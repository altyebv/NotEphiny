package com.zeros.notephiny.presentation.add_edit_note

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeros.notephiny.data.model.Note
import com.zeros.notephiny.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditNoteViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private var currentNoteId: Int? = null

    private val _title = mutableStateOf("")
    val title: State<String> = _title

    private val _content = mutableStateOf("")
    val content: State<String> = _content

    private val _noteColor = mutableStateOf(Note.noteColors.random().toArgb()) // fallback
    val noteColor: State<Int> = _noteColor

    init {
        val noteId = savedStateHandle.get<Int>("noteId") ?: -1
        val color = savedStateHandle.get<Int>("noteColor") ?: -1
        if (color != -1) {
            _noteColor.value = color
        }

        if (noteId != -1) {
            viewModelScope.launch {
                noteRepository.getNoteById(noteId)?.also { note ->
                    currentNoteId = note.id
                    _title.value = note.title
                    _content.value = note.content
                    _noteColor.value = note.color
                }
            }
        }
    }


    fun onTitleChange(newTitle: String) {
        _title.value = newTitle
    }

    fun onContentChange(newContent: String) {
        _content.value = newContent
    }

    fun saveNote(onSaved: () -> Unit) {
        viewModelScope.launch {
            val trimmedTitle = _title.value.trim()
            val trimmedContent = _content.value.trim()

            if (trimmedTitle.isBlank()) {
                _errorMessage.value = "Title cannot be empty!"
                return@launch
            }

            val note = Note(
                title = trimmedTitle,
                content = trimmedContent,
                color = noteColor.value,
                id = currentNoteId
            )
            noteRepository.insertNote(note)
            onSaved()
        }
    }
    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    fun clearError() {
        _errorMessage.value = null
    }
}
