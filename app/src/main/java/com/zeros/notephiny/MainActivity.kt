package com.zeros.notephiny

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.zeros.notephiny.presentation.note_list.NoteListViewModel
import com.zeros.notephiny.ui.theme.NotephinyTheme
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
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
    val notes = viewModel.notes.collectAsState().value

    NavGraph(
        navController = navController,
        notes = notes,
        viewModel = viewModel
    )
}



