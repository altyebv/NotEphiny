package com.zeros.notephiny.presentation.components.menus


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.zeros.notephiny.presentation.components.menus.MenuAction

@Composable
fun GenericDropdownMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    actions: List<MenuAction>
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        actions.forEach { action ->
            DropdownMenuItem(
                onClick = action.onClick,
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = action.label,
                            color = if (action.isSelected) Color(0xFFFFD700) else Color.Unspecified, // Yellow if selected
                            modifier = Modifier.weight(1f)
                        )
                        if (action.isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = Color(0xFFFFD700)
                            )
                        }
                    }
                }
            )
        }
    }
}


