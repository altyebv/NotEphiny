package com.zeros.notephiny.presentation.components.menus

enum class TodoMenuItem {
    Edit,
    HideCompleted,
    Settings
}

fun TodoMenuItem.label( isHideCompleted: Boolean): String = when (this) {
    TodoMenuItem.Edit ->  "Edit"
    TodoMenuItem.HideCompleted -> if (isHideCompleted) "Show Completed" else "Hide Completed"
    TodoMenuItem.Settings -> "Settings"
}
