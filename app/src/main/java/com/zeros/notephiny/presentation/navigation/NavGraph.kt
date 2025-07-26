package com.zeros.notephiny.presentation.navigation


import androidx.compose.runtime.Composable
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.zeros.notephiny.data.model.Note
import com.zeros.notephiny.presentation.add_edit_note.AddEditNoteScreen
import com.zeros.notephiny.presentation.note_list.NoteListScreen
import com.zeros.notephiny.presentation.note_list.NoteListViewModel
import com.zeros.notephiny.util.Screen

@Composable
fun NavGraph(
    navController: NavHostController,
    notes: List<Note>,
    viewModel: NoteListViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.NoteList.route
    ) {
        composable(route = Screen.NoteList.route) {
            NoteListScreen(
                navController = navController
            )
        }

        composable(
            route = Screen.AddEditNote.route + "?noteId={noteId}&noteColor={noteColor}",
            arguments = listOf(
                navArgument("noteId") {
                    type = NavType.IntType
                    defaultValue = -1
                },
                navArgument("noteColor") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            val color = backStackEntry.arguments?.getInt("noteColor") ?: -1
            AddEditNoteScreen(
                navController = navController,
                noteColor = color
            )
        }
    }
}
