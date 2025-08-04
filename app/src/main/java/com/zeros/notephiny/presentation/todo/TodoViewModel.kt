package com.zeros.notephiny.presentation.todo

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeros.notephiny.data.model.Todo
import com.zeros.notephiny.domain.repository.TodoRepository
import com.zeros.notephiny.presentation.components.menus.TodoMenuItem
import com.zeros.notephiny.presentation.notes.NoteListViewModel.NavigationEvent
import com.zeros.notephiny.presentation.todo.states.TodoItemUiState
import com.zeros.notephiny.presentation.todo.states.TodoListUiState
import com.zeros.notephiny.presentation.todo.states.toTodo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TodoViewModel @Inject constructor(
    private val repository: TodoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodoListUiState())
    val uiState: StateFlow<TodoListUiState> = _uiState
    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()


    init {
        viewModelScope.launch {
            repository.getAllTodos()
                .collectLatest { todos ->
                    _uiState.update { it.copy(todos = todos) }
                }
        }
    }

    fun toggleDone(todo: Todo) {
        viewModelScope.launch {
            repository.updateTodo(todo.copy(isDone = !todo.isDone))
        }
    }

    fun addTodo(title: String) {
        viewModelScope.launch {
            repository.saveTodoWithEmbedding(title = title.trim())
            onDismissAddTodo()
            Log.d("TodoVM", "Request to save todo with title: $title")

        }
    }

    fun searchTodos(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val results = repository.searchTodosBySemantic(query)
            _uiState.update { it.copy(todos = results, isLoading = false) }
        }
    }


    fun deleteTodo(todo: Todo) {
        viewModelScope.launch {
            repository.deleteTodo(todo)
        }
    }

    fun showAddTodoSheet() {
        _uiState.update { it.copy(isAddingTodo = true) }
    }

    fun onDismissAddTodo() {
        _uiState.update { it.copy(isAddingTodo = false) }
    }

    fun restoreTodo(todo: Todo) {
        viewModelScope.launch {
            repository.addTodo(todo)
        }
    }
    fun toggleTodoSelection(id: Int) {
        _uiState.update { state ->
            val current = state.selectedTodoIds
            val newSet = if (id in current) current - id else current + id
            state.copy(selectedTodoIds = newSet)
        }
    }

    fun clearTodoSelection() {
        _uiState.update { it.copy(selectedTodoIds = emptySet()) }
    }

    fun deleteSelectedTodos() {
        val idsToDelete = _uiState.value.selectedTodoIds
        viewModelScope.launch {
            idsToDelete.forEach { id ->
                repository.deleteTodoById(id) // or filter + delete one-by-one
            }
            exitBulkEditMode()
        }
    }


    fun exitBulkEditMode() {
        _uiState.update { it.copy(isInBulkEditMode = false, selectedTodoIds = emptySet()) }
    }


    fun onMenuAction(action: TodoMenuItem) {
        when (action) {
            TodoMenuItem.Edit -> {
                _uiState.update { it.copy(isInBulkEditMode = !it.isInBulkEditMode) }
            }
            TodoMenuItem.HideCompleted -> {
                _uiState.update { it.copy(hideCompleted = !it.hideCompleted) }
            }
            TodoMenuItem.Settings -> {
                viewModelScope.launch {
                    _navigationEvent.emit(NavigationEvent.GoToSettings)
                }
            }
        }
    }

    fun selectAll() {
        val allTodosIds = _uiState.value.todos.mapNotNull { it.id }.toSet()
        _uiState.update { it.copy(selectedTodoIds = allTodosIds) }

    }

}
