package com.zeros.notephiny.presentation.components.menus

data class MenuAction(
    val label: String,
    val onClick: () -> Unit,
    val isSelected: Boolean = false
)
