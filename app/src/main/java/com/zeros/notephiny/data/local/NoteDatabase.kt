package com.zeros.notephiny.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.zeros.notephiny.data.model.Note

@Database(
    entities = [Note::class],
    version = 3,
    exportSchema = false)

@TypeConverters(Converters::class)
abstract class NoteDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

}