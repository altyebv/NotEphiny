package com.zeros.notephiny

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.zeros.notephiny.core.util.Screen
import com.zeros.notephiny.data.model.Note
import com.zeros.notephiny.presentation.navigation.BottomNavBar
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
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        Screen.NoteList.route,
        Screen.TodoList.route
    )
    var fabClick by remember { mutableStateOf<(() -> Unit)?>(null) }


    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    modifier = Modifier.navigationBarsPadding(),
                    navController = navController
                )
            }
        },
        floatingActionButton = {
            fabClick?.let { onClick ->
                FloatingActionButton(
                    onClick = onClick,
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }
        }
    ) { innerPadding ->
        NavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            setFabClick = { fabClick = it }
        )
    }
}




