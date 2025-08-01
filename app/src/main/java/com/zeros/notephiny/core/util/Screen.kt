package com.zeros.notephiny.core.util

sealed class Screen(val route: String) {
    object NoteList : Screen("note_list")
    object AddEditNote : Screen("note_screen")
    object Settings : Screen("settings")
}
