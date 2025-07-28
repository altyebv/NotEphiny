package com.zeros.notephiny.presentation.notes

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.zeros.notephiny.core.util.Screen.NoteList
import com.zeros.notephiny.data.model.Note

@Composable
fun NoteListContent(
    notes: List<Note>,
    onNoteClick: (Note) -> Unit,
    onDeleteRequest: (Note) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(notes) { note ->
            NoteItem(
                note = note,
                onClick = { onNoteClick(note) },
                onLongPress = { onDeleteRequest(note) }
            )
        }
    }
}


@Preview
@Composable
fun NoteListContentPreview(){

}