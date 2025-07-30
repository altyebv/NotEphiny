package com.zeros.notephiny.presentation.components


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

@Composable
fun NotesTop(
    noteCount: Int,
    searchQuery: String,
    isSearching: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onCancelSearch: () -> Unit,
    onStartSearch: () -> Unit,
    onCategorySelected: (String) -> Unit,
    onOverflowClick: () -> Unit,
    selectedCategory: String,
    categories: List<String>,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        AnimatedVisibility(
            visible = !isSearching,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
        ) {
            TopIconsRow(
                onSearchClick = onStartSearch,
                onMenuClick = { menuExpanded = true },
                menuExpanded = menuExpanded,
                onMenuDismissRequest = { menuExpanded = false },
                onMenuItemClick = { action ->
                    println("Menu action selected: $action")
                    menuExpanded = false
                    onOverflowClick()
                }
            )
        }

        AnimatedVisibility(visible = isSearching) {
            SearchBarRow(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                onCancelClick = onCancelSearch
            )
        }


        Spacer(modifier = Modifier.height(12.dp))
        TitleRow(noteCount = noteCount)
        Spacer(modifier = Modifier.height(16.dp))

        CategoryRow(
            categories = categories,
            selectedCategory = selectedCategory,
            onCategorySelected = onCategorySelected
        )
    }
}

@Composable
private fun CategoryRow(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Home,
            contentDescription = "Categories",
            modifier = Modifier.padding(end = 8.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories, key = { it }) { category ->
                val isSelected = category == selectedCategory
                AssistChip(
                    onClick = { onCategorySelected(category) },
                    label = {
                        Text(
                            text = category,
                            color = if (isSelected)
                                MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (isSelected)
                            MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surface
                    )
                )
            }
        }
    }
}

enum class MenuAction {
    Edit, Settings, SortByCreated, SortByEdited
}

@Composable
private fun TopIconsRow(
    onSearchClick: () -> Unit,
    onMenuClick: () -> Unit,
    menuExpanded: Boolean,
    onMenuDismissRequest: () -> Unit,
    onMenuItemClick: (MenuAction) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 8.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onSearchClick) {
            Icon(Icons.Default.Search, contentDescription = "Search")
        }

        // 🟡 Wrap the IconButton and DropdownMenu in a Box
        Box {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.MoreVert, contentDescription = "More Options")
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = onMenuDismissRequest,
                offset = DpOffset(x = 0.dp, y = 8.dp)
            ) {
                DropdownMenuItem(
                    text = { Text("Edit") },
                    onClick = { onMenuItemClick(MenuAction.Edit) }
                )
                DropdownMenuItem(
                    text = { Text("Settings") },
                    onClick = { onMenuItemClick(MenuAction.Settings) }
                )
                DropdownMenuItem(
                    text = { Text("Sort by Time Created") },
                    onClick = { onMenuItemClick(MenuAction.SortByCreated) }
                )
                DropdownMenuItem(
                    text = { Text("Sort by Time Edited") },
                    onClick = { onMenuItemClick(MenuAction.SortByEdited) }
                )
            }
        }
    }
}



@Composable
private fun TitleRow(noteCount: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Notes",
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "($noteCount)",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}



