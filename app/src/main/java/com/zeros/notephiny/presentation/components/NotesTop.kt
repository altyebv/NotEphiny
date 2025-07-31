package com.zeros.notephiny.presentation.components


import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import com.zeros.notephiny.presentation.components.menus.GenericDropdownMenu
import com.zeros.notephiny.presentation.components.menus.MainScreenMenu
import com.zeros.notephiny.presentation.components.menus.MenuAction
import com.zeros.notephiny.presentation.components.menus.label
import com.zeros.notephiny.presentation.notes.NoteListViewModel.NoteListMode
import com.zeros.notephiny.presentation.notes.NoteListViewModel.SortOrder
@Composable
fun NotesTop(
    noteCount: Int,
    mode: NoteListMode,
    searchQuery: String,
    isSearching: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onCancelSearch: () -> Unit,
    onStartSearch: () -> Unit,
    onCategorySelected: (String) -> Unit,
    onOverflowClick: (MainScreenMenu) -> Unit,
    selectedCategory: String,
    categories: List<String>,
    sortOrder: SortOrder,
    selectedNoteIds: Set<Int> = emptySet(),
    onCancelMultiSelect: () -> Unit = {},
    onSelectAll: () -> Unit = {},
    modifier: Modifier = Modifier,

) {
    var menuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {

        AnimatedContent(
            targetState = mode,
            transitionSpec = {
                fadeIn() + slideInVertically { -it } togetherWith
                        fadeOut() + slideOutVertically { -it }
            },
            label = "TopContentSwitcher"
        ) { currentMode ->
            when (currentMode) {
                NoteListMode.NORMAL -> {
                    Column {
                        if (isSearching) {
                            SearchBarRow(
                                query = searchQuery,
                                onQueryChange = onSearchQueryChange,
                                onCancelClick = onCancelSearch
                            )
                        } else {
                            TopIconsRow(
                                onSearchClick = onStartSearch,
                                onMenuClick = { menuExpanded = true },
                                menuExpanded = menuExpanded,
                                onMenuDismissRequest = { menuExpanded = false },
                                onMenuItemClick = { action ->
                                    menuExpanded = false
                                    onOverflowClick(action)
                                },
                                sortOrder = sortOrder
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        TitleWithCountRow(title = "Notes", count = noteCount)

                    }
                }

                NoteListMode.MULTI_SELECT -> {
                    Column {
                        MultiSelectIconsRow(
                            selectedCount = selectedNoteIds.size,
                            onCancel = onCancelMultiSelect,
                            onSelectAll = onSelectAll
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        TitleWithCountRow(
                            title = if (selectedNoteIds.isEmpty()) "Select items" else "${selectedNoteIds.size} selected",
                            selectedNoteIds = selectedNoteIds,
                            showCount = false
                        )
                    }
                }
            }
        }

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
    Row(verticalAlignment = Alignment.CenterVertically) {
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

@Composable
private fun TopIconsRow(
    onSearchClick: () -> Unit,
    onMenuClick: () -> Unit,
    menuExpanded: Boolean,
    onMenuDismissRequest: () -> Unit,
    onMenuItemClick: (MainScreenMenu) -> Unit,
    sortOrder: SortOrder
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

        Box {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.MoreVert, contentDescription = "More Options")
            }

            GenericDropdownMenu(
                expanded = menuExpanded,
                onDismiss = onMenuDismissRequest,
                actions = MainScreenMenu.entries.map { item ->
                    MenuAction(
                        label = item.label(),
                        onClick = { onMenuItemClick(item) },
                        isSelected = when (item) {
                            MainScreenMenu.SortByCreated -> sortOrder == SortOrder.CREATED
                            MainScreenMenu.SortByEdited -> sortOrder == SortOrder.EDITED
                            else -> false
                        }
                    )
                }
            )
        }
    }
}

@Composable
private fun MultiSelectIconsRow(
    onCancel: () -> Unit,
    onSelectAll: () -> Unit,
    selectedCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onCancel) {
            Text("Cancel", style = MaterialTheme.typography.labelLarge)
        }

        TextButton(onClick = onSelectAll) {
            Text("Select All", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun TitleWithCountRow(
    title: String,
    count: Int? = null,
    selectedNoteIds: Set<Int>? = null,
    showCount: Boolean = true,
) {
    val resolvedCount = selectedNoteIds?.size ?: count

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge
        )
        if (showCount && (resolvedCount ?: 0) > 0) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "(${resolvedCount})",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}




//@Composable
//private fun MultiSelectTitleRow(selectedCount: Int) {
//    Row(
//        verticalAlignment = Alignment.CenterVertically,
//        modifier = Modifier.padding(top = 4.dp)
//    ) {
//        Text(
//            text = if (selectedCount == 0) "Select items" else "$selectedCount selected",
//            style = MaterialTheme.typography.headlineLarge
//        )
//    }
//}

//@Composable
//private fun TitleRow(noteCount: Int) {
//    Row(
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Text(
//            text = "Notes",
//            style = MaterialTheme.typography.headlineLarge
//        )
//        Spacer(modifier = Modifier.width(8.dp))
//        Text(
//            text = "($noteCount)",
//            style = MaterialTheme.typography.titleMedium,
//            color = MaterialTheme.colorScheme.onSurfaceVariant
//        )
//    }
//}




