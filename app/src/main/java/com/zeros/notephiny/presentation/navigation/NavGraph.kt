package com.zeros.notephiny.presentation.navigation


import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.zeros.notephiny.data.model.Note
import com.zeros.notephiny.presentation.add_edit_note.AddEditScreen
import com.zeros.notephiny.presentation.notes.NoteListScreen
import com.zeros.notephiny.presentation.notes.NoteListViewModel
import com.zeros.notephiny.core.util.Screen
import com.zeros.notephiny.presentation.add_edit_note.NoteScreen
import com.zeros.notephiny.presentation.components.SettingsScreen

//@Composable
//fun NavGraph(navController: NavHostController) {
//    NavHost(
//        navController = navController,
//        startDestination = Screen.NoteList.route
//    ) {
//        composable(route = Screen.NoteList.route) {
//            NoteListScreen(navController = navController)
//        }
//        composable(route = Screen.Settings.route) {
//            SettingsScreen(
//                onBack = { navController.popBackStack() },
//                navController = navController
//            )
//        }
//
//        composable(
//            route = Screen.AddEditNote.route + "?noteId={noteId}&noteColor={noteColor}",
//            arguments = listOf(
//                navArgument("noteId") {
//                    type = NavType.IntType
//                    defaultValue = -1
//                },
//                navArgument("noteColor") {
//                    type = NavType.IntType
//                    defaultValue = -1
//                }
//            )
//        ) { backStackEntry ->
//
//            NoteScreen(
//                onNoteSaved = {
//                    navController.popBackStack()
//                }
//            )
//        }
//    }
//}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.NoteList.route
    ) {
        composable(
            route = Screen.NoteList.route,
            enterTransition = { slideInHorizontally(initialOffsetX = { -300 }) + fadeIn() },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -300 }) + fadeOut() },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { 300 }) + fadeIn() },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { 300 }) + fadeOut() },
        ) {
            NoteListScreen(navController = navController)
        }

        composable(
            route = Screen.Settings.route,
            enterTransition = { slideInHorizontally(initialOffsetX = { 300 }) + fadeIn() },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -300 }) + fadeOut() },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -300 }) + fadeIn() },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { 300 }) + fadeOut() },
        ) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                navController = navController
            )
        }

        composable(
            route = Screen.AddEditNote.route + "?noteId={noteId}&noteColor={noteColor}&category={category}",
            arguments = listOf(
                navArgument("noteId") { type = NavType.IntType; defaultValue = -1 },
                navArgument("noteColor") { type = NavType.IntType; defaultValue = -1 },
                navArgument("category") {
                    type = NavType.StringType
                    defaultValue = "Journal"
                }

            ),
            enterTransition = { slideInHorizontally(initialOffsetX = { 300 }) + fadeIn() },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -300 }) + fadeOut() },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -300 }) + fadeIn() },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { 300 }) + fadeOut() },
        ) {
            NoteScreen(
                onNoteSaved = { navController.popBackStack() },
                category = it.arguments?.getString("category") ?: "Journal"
            )

        }
    }
}

