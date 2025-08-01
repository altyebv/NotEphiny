package com.zeros.notephiny

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.zeros.notephiny.presentation.notes.NoteListViewModel
import com.zeros.notephiny.ui.theme.NotephinyTheme
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.zeros.notephiny.data.model.Note
import com.zeros.notephiny.presentation.navigation.NavGraph
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NotephinyTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    NotephinyApp(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
@Composable
fun NotephinyApp(
    modifier: Modifier = Modifier,
    viewModel: NoteListViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val notes by viewModel.filteredNotes.collectAsState()
    val selectedCategory = viewModel.selectedCategory.collectAsState().value
    var noteToDelete by remember { mutableStateOf<Note?>(null) }


    NavGraph(navController = navController)

}



