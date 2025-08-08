package com.zeros.notephiny.presentation.add_edit_note

import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeros.notephiny.ai.embedder.OnnxEmbedder
import com.zeros.notephiny.core.util.CategoryProvider
import com.zeros.notephiny.data.model.Note
import com.zeros.notephiny.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.zeros.notephiny.data.model.CategoryType
import com.zeros.notephiny.domain.repository.AiActionRepository
import com.zeros.notephiny.domain.repository.TodoRepository
import com.zeros.notephiny.presentation.components.NotebookItem
import com.zeros.notephiny.presentation.components.menus.NoteScreenMenu
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update


@HiltViewModel
class AddEditNoteViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    savedStateHandle: SavedStateHandle,
    private val aiActionRepo: AiActionRepository,
    private val todoRepository: TodoRepository,


) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditNoteUiState())
    val uiState: StateFlow<AddEditNoteUiState> = _uiState
    val availableCategories = noteRepository.getDefaultCategories()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var currentNoteId: Int? = null
    val now = System.currentTimeMillis()
    private var originalNote: Note? = null

    private val categoryProvider = CategoryProvider(noteRepository)

    val userNotebooks = mutableStateOf<List<NotebookItem>>(emptyList())
    val appNotebooks = mutableStateOf<List<NotebookItem>>(emptyList())
    private val _extractedActions = mutableStateOf<List<String>>(emptyList())
    val extractedActions: State<List<String>> = _extractedActions
    private val _similarNotes = mutableStateOf<List<Note>>(emptyList())
    val similarNotes: State<List<Note>> = _similarNotes
    val noteId: Int?
        get() = uiState.value.id




    sealed class UiEvent {
        data class ShowMessage(val message: String) : UiEvent()
        object NavigateBack : UiEvent()
        // Add more as needed
    }


    fun fetchNotebookGroups() {
        viewModelScope.launch {
            val structured = categoryProvider.getStructuredCategories()
            val user = structured.filter { it.type == CategoryType.USER }
            val app = structured.filter { it.type == CategoryType.APP }

            userNotebooks.value = user.map {
                NotebookItem(name = it.name, count = it.count, type = it.type)
            }

            appNotebooks.value = app.map {
                NotebookItem(name = it.name, count = it.count, type = it.type)
            }
        }
    }


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
                    _uiState.value = _uiState.value.copy(
                        id = note.id,
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



    fun getOriginalNote(): Note? = originalNote

    fun saveNote(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val state = _uiState.value
        val isNewNote = noteId == null

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
                    id = noteId,
                    title = state.title,
                    content = state.content,
                    category = state.category,
                    color = state.color,
                    isPinned = state.isPinned,
                    createdAt = createdAt,
                    updatedAt = updatedAt
                )

                onSuccess()
                Log.d("AddEditViewModel", "Saving note with title: ${state.title}")

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
    fun togglePin(note: Note) {
        viewModelScope.launch {
            noteRepository.togglePin(note)
            val updatedNote = noteRepository.getNoteById(note.id!!)
            updatedNote?.let {
                _uiState.update { currentState -> it.toUiState(currentState) }
            }
        }
    }


    private fun setInitialCategory(category: String) {
        _uiState.value = _uiState.value.copy(category = category)
    }

    fun deriveTodosFromCurrentNote() {
        viewModelScope.launch {
            val noteToUse = originalNote ?: buildNoteFromUiState() ?: return@launch

            val actions = aiActionRepo.extractActionsFromText(noteToUse.content)

            actions.forEach { action ->
                todoRepository.saveTodoWithEmbedding(
                    title = action,
                    noteId = noteToUse.id,
                    isDerived = true
                )
            }

            _extractedActions.value = actions
            // Optional: add a success event
            // _eventFlow.emit(UiEvent.ShowMessage("Derived ${actions.size} To-Dos from note"))

        }
    }

    private fun buildNoteFromUiState(): Note? {
        val state = uiState.value
        if (state.title.isBlank() && state.content.isBlank()) return null

        return Note(
            id = state.id,
            title = state.title,
            content = state.content,
            category = state.category,
            color = state.color,
            createdAt = state.createdAt ?: System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }





    fun onContentChange(newContent: String) {
        _uiState.update { current ->
            current.copy(
                content = newContent,
                isEdited = current.title.isNotBlank() || newContent.isNotBlank(),
            )
        }
    }


    fun moveNoteToCategory(
        newCategory: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        _uiState.update {
            it.copy(
                category = newCategory,
                showMoveNotebookSheet = false
            )
        }

        saveNote(
            onSuccess = onSuccess,
            onError = onError
        )
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
                togglePin(_uiState.value.toNote())
            }

            NoteScreenMenu.Move -> {
                _uiState.update { it.copy(showMoveNotebookSheet = true) }
            }

            NoteScreenMenu.Delete -> {
                _uiState.update { it.copy(showDeleteDialog = true) }
            }
        }
    }
    fun onDeleteDialogDismissed() {
        _uiState.update { it.copy(showDeleteDialog = false) }
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

    fun detectActionsFromText() {
        viewModelScope.launch {
            val text = uiState.value.content
            val actions = aiActionRepo.extractActionsFromText(text)
            _uiState.update { it.copy(extractedActions = actions) }
            Log.d("AI_ACTIONS", "Detected: ${actions.size} actions")
        }
    }

    private var detectionJob: Job? = null

    fun toggleHighlightMode() {
        val newValue = !uiState.value.highlightActions
        _uiState.update { it.copy(highlightActions = newValue) }

        if (newValue) {
            // Cancel any ongoing job if you have one
            detectionJob?.cancel()
            detectionJob = viewModelScope.launch {
                detectActionsFromText() // detect + update state
            }
        } else {
            // Clear detected actions so fake highlighter starts fresh next time
            _uiState.update { it.copy(extractedActions = emptyList()) }
        }
    }


    fun toggleShowRelated() {
        Log.d("ToggleShowRelated", "Function called")

        val shouldShow = !_uiState.value.showRelated
        _uiState.update { it.copy(showRelated = shouldShow) }

        if (shouldShow) {
            viewModelScope.launch {
                val noteId = _uiState.value.id
                Log.d("ToggleShowRelated", "Current note ID: $noteId")

                val relatedNotes = if (noteId != null) {
                    noteRepository.getSimilarNotesForNoteId(noteId, topK = 5)
                } else {
                    // fallback: treat current UI state as a temp Note for similarity check
                    Log.w("ToggleShowRelated", "No note ID found — using fallback content matching.")
                    noteRepository.getSimilarNotes(
                        currentNote = Note(
                            id = -1,
                            title = _uiState.value.title ?: "",
                            content = _uiState.value.content ?: "",
                            category = _uiState.value.category ?: "",
                            embedding = OnnxEmbedder.embed(
                                "${_uiState.value.title} ${_uiState.value.content} ${_uiState.value.category}",
                                noteRepository.context
                            ).toList(),
                            color = _uiState.value.color ?: 0
                        ),
                        topK = 5
                    )
                }

                Log.d("ToggleShowRelated", "Fetched ${relatedNotes.size} related notes")
                _uiState.update { it.copy(relatedNotes = relatedNotes) }
            }
        }
    }


    fun toggleMoveSheet(show: Boolean) {
        _uiState.update { it.copy(showMoveNotebookSheet = show) }
    }



}



