package com.zeros.notephiny.presentation.components.menus

enum class MainScreenMenu {
    Edit, Settings, SortByCreated, SortByEdited
}

fun MainScreenMenu.label(): String = when (this) {
    MainScreenMenu.Edit -> "Edit"
    MainScreenMenu.Settings -> "Settings"
    MainScreenMenu.SortByCreated -> "Sort by Time Created"
    MainScreenMenu.SortByEdited -> "Sort by Time Edited"
}