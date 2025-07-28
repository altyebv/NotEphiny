package com.zeros.notephiny.presentation.components


import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesTopBar(
    onNavigationClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = "Notephiny",
                style = MaterialTheme.typography.titleLarge,)
                },
        navigationIcon = {
            IconButton(onClick = onNavigationClick) {
                Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu")
            }
        }
    )
}
@Preview
@Composable
fun TopAppBarWithDrawerPreview() {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    NotesTopBar(onNavigationClick = {})
}



