package com.zeros.notephiny.presentation.todo


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTodo(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    if (!isVisible) return

    var newTodoText by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    // Automatically focus the input field on open
    LaunchedEffect(Unit) {
        delay(100)
        focusRequester.requestFocus()
    }

    ModalBottomSheet(
        onDismissRequest = {
            onDismiss()
            newTodoText = ""
        },
        modifier = Modifier
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = {
                    onDismiss()
                    newTodoText = ""
                }) {
                    Text("Cancel")
                }

                Text("New To-Do", style = MaterialTheme.typography.titleMedium)

                TextButton(
                    onClick = {
                        onSave(newTodoText.trim())
                        onDismiss()
                        newTodoText = ""
                        focusManager.clearFocus()
                    },
                    enabled = newTodoText.isNotBlank()
                ) {
                    Text("Save")
                }
            }

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = newTodoText,
                onValueChange = { newTodoText = it },
                placeholder = { Text("Enter task...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (newTodoText.isNotBlank()) {
                            onSave(newTodoText.trim())
                            onDismiss()
                            newTodoText = ""
                            focusManager.clearFocus()
                        }
                    }
                ),
                singleLine = true
            )
        }
    }
}
