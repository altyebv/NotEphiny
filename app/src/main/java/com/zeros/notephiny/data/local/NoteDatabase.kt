package com.zeros.notephiny.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.zeros.notephiny.data.model.Note
import com.zeros.notephiny.data.model.Todo

@Database(
    entities = [Note::class, Todo::class],
    version = 6,
    exportSchema = false)

@TypeConverters(Converters::class)
abstract class NoteDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun todoDao(): TodoDao

}