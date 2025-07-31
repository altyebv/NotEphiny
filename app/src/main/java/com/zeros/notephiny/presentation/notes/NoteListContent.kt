package com.zeros.notephiny.presentation.notes

import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.zeros.notephiny.core.util.Screen.NoteList
import com.zeros.notephiny.data.model.Note
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteListContent(
    notes: List<Note>,
    selectedNoteIds: Set<Int>,
    onNoteClick: (Note) -> Unit,
    onDeleteRequest: (Note) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(notes) { note ->
            NoteItem(
                note = note,
                onClick = { onNoteClick(note) },
                onLongPress = { onDeleteRequest(note) },
                isSelected = note.id in selectedNoteIds,

            )
        }
    }
}



@Preview(showBackground = true)
@Composable
fun NoteListContentPreview() {
    val sampleNotes = listOf(
        Note(id = 1, title = "Note One", content = "This is a short note.", color = 0),
        Note(id = 2, title = "Note Two", content = "This one is a bit longer and has more to say about things and ideas.", color = 1),
        Note(id = 3, title = "Note Three", content = "A really long note to simulate what happens when there's a lot of content. This should span multiple lines and help test dynamic height.", color = 2),
        Note(id = 4, title = "Note Four", content = "Another sample note.", color = 3)
    )

    NoteListContent(
        notes = sampleNotes,
        onNoteClick = {},
        onDeleteRequest = {},
        selectedNoteIds = setOf(1, 3),

    )
}
