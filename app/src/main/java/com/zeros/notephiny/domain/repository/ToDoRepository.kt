package com.zeros.notephiny.domain.repository

import android.content.Context
import android.util.Log
import com.zeros.notephiny.ai.embedder.OnnxEmbedder
import com.zeros.notephiny.data.local.TodoDao
import com.zeros.notephiny.data.model.Todo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TodoRepository @Inject constructor(
    private val dao: TodoDao,
    @ApplicationContext private val context: Context

) {

    fun getAllTodos(): Flow<List<Todo>> = dao.getAllTodos()

    suspend fun deleteTodoById(id: Int) = dao.deleteTodoById(id)

    suspend fun deleteTodo(todo: Todo) = dao.deleteTodo(todo)

    suspend fun updateTodo(todo: Todo) = dao.updateTodo(todo)

    suspend fun addTodo(todo: Todo) = dao.insertTodo(todo)

    suspend fun searchTodosBySemantic(query: String): List<Todo> {
        val queryEmbedding = OnnxEmbedder.embed(query,context)
        val allTodos = dao.getAllTodosOnce()

        return allTodos
            .filter { it.embedding?.isNotEmpty() == true }
            .sortedByDescending { todo ->
                OnnxEmbedder.cosineSimilarity(todo.embedding!!, queryEmbedding.toList())
            }
    }


    suspend fun saveTodoWithEmbedding(
        id: Int? = null,
        title: String,
        isDone: Boolean = false,
        createdAt: Long? = null,
        dueDate: Long? = null,
        noteId: Int? = null
    ) {
        val embedding = OnnxEmbedder.embed(title, context).toList()
        val todo = Todo(
            id = id ?: 0,
            title = title,
            isDone = isDone,
            createdAt = createdAt ?: System.currentTimeMillis(),
            dueDate = dueDate,
            noteId = noteId,
            embedding = embedding
        )
        dao.insertTodo(todo)
        Log.d("TodoRepo", "Saved todo '$title' with embedding (len=${embedding.size})")
    }
}

