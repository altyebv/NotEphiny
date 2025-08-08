package com.zeros.notephiny.core.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    object NoteList : Screen("note_list")
    object AddEditNote : Screen("note_screen")
    object TodoList : Screen("todo_list")
    object Settings : Screen("settings")
    object NoteAI : Screen("note_ai")
    object TodoAI : Screen("todo_ai")
}
sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    object Notes : BottomNavItem("notes", Icons.Default.Home, "Notes")
    object Todos : BottomNavItem("todos", Icons.Default.Check, "ToDos")

    companion object {
        val items = listOf(Notes, Todos)
    }
}


