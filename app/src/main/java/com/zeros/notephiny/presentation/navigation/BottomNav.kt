package com.zeros.notephiny.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.zeros.notephiny.core.util.Screen

@Composable
fun BottomNavBar(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == Screen.NoteList.route,
            onClick = { navController.navigate(Screen.NoteList.route) },
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Notes") }
        )
        NavigationBarItem(
            selected = currentRoute == Screen.TodoList.route,
            onClick = { navController.navigate(Screen.TodoList.route) },
            icon = { Icon(Icons.Default.Check, contentDescription = null) },
            label = { Text("To-Dos") }
        )
    }
}

@Preview
@Composable
fun BottomNavBarPreview() {
    BottomNavBar(navController = NavController(LocalContext.current))
}

