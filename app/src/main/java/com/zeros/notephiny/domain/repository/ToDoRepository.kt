package com.zeros.notephiny.domain.repository

import com.zeros.notephiny.data.local.TodoDao
import com.zeros.notephiny.data.model.Todo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TodoRepository @Inject constructor(
    private val dao: TodoDao
) {

    fun getAllTodos(): Flow<List<Todo>> = dao.getAllTodos()

    suspend fun deleteTodoById(id: Int) = dao.deleteTodoById(id)

    suspend fun addTodo(todo: Todo) = dao.insertTodo(todo)

    suspend fun updateTodo(todo: Todo) = dao.updateTodo(todo)

    suspend fun deleteTodo(todo: Todo) = dao.deleteTodo(todo)
}
