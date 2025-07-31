package com.zeros.notephiny.core.util


import java.text.SimpleDateFormat
import java.util.*

fun formatDateTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("d/M/yyyy, h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
