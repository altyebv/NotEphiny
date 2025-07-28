package com.zeros.notephiny.presentation.navigation


import androidx.compose.runtime.Composable
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.zeros.notephiny.data.model.Note
import com.zeros.notephiny.presentation.add_edit_note.AddEditScreen
import com.zeros.notephiny.presentation.notes.NoteListScreen
import com.zeros.notephiny.presentation.notes.NoteListViewModel
import com.zeros.notephiny.core.util.Screen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.NoteList.route
    ) {
        composable(route = Screen.NoteList.route) {
            NoteListScreen(navController = navController)
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
        ) {
            AddEditScreen(
                onNoteSaved = {
                    navController.popBackStack()
                }
            )
        }
    }
}
