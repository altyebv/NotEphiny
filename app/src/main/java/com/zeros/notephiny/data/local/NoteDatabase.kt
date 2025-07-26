package com.zeros.notephiny.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.zeros.notephiny.data.model.Note

@Database(
    entities = [Note::class],
    version = 2,
    exportSchema = false)

abstract class NoteDatabase : RoomDatabase() {
    abstract val noteDao: NoteDao
}