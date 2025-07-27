package com.zeros.notephiny.data.local

import androidx.room.TypeConverter

class Converters {

    @TypeConverter
    fun fromFloatList(list: List<Float>?): String {
        return list?.joinToString(",") ?: ""
    }

    @TypeConverter
    fun toFloatList(data: String): List<Float> {
        return if (data.isEmpty()) emptyList()
        else data.split(",").map { it.toFloat() }
    }
}