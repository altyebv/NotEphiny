package com.zeros.notephiny.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.zeros.notephiny.data.model.Todo
import dagger.Provides
import kotlinx.coroutines.flow.Flow
import javax.inject.Singleton

@Dao
interface TodoDao {

    @Query("SELECT * FROM todos ORDER BY isDone ASC, createdAt DESC")
    fun getAllTodos(): Flow<List<Todo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodo(todo: Todo)

    @Update
    suspend fun updateTodo(todo: Todo)

    @Delete
    suspend fun deleteTodo(todo: Todo)


    @Query("DELETE FROM todos WHERE id = :id")
    suspend fun deleteTodoById(id: Int)

}
