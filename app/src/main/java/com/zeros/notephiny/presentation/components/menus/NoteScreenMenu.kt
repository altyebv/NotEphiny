package com.zeros.notephiny.presentation.components.menus

enum class NoteScreenMenu {
    Find, Pin, Move, Delete
}

fun NoteScreenMenu.label(): String = when (this) {
    NoteScreenMenu.Find -> "Find"
    NoteScreenMenu.Pin -> "Pin"
    NoteScreenMenu.Move -> "Move"
    NoteScreenMenu.Delete -> "Delete"
}