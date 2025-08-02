package com.zeros.notephiny.presentation.add_edit_note

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material3.OutlinedTextField

import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditScreen(
    viewModel: AddEditNoteViewModel = hiltViewModel(),
    onNoteSaved: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val categories = viewModel.availableCategories



    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // This will expand and push others downward
        AddEditNoteFields(
            title = uiState.title,
            content = uiState.content,
            onTitleChange = viewModel::onTitleChange,
            onContentChange = viewModel::onContentChange,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        AddEditNoteCategorySelector(
            selectedCategory = uiState.category,
            availableCategories = uiState.availableCategories,
            onCategorySelected = viewModel::moveNoteToCategory
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.weight(1f))
            AddEditSaveButton {
                viewModel.saveNote(
                    onSuccess = onNoteSaved,
                    onError = { /* already handled */ }
                )
            }
        }
    }
}
@Composable
fun AddEditNoteFields(
    title: String,
    content: String,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text("Title") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = content,
            onValueChange = onContentChange,
            label = { Text("Content") },
            modifier = Modifier
                .fillMaxSize(), // Expand to fill remaining space inside this Column
            maxLines = Int.MAX_VALUE
        )
    }
}


@Composable
fun AddEditSaveButton(
    onSaveClicked: () -> Unit
) {
    Button(onClick = onSaveClicked) {
        Text("Save")
    }
}


@Composable
fun AddEditNoteCategorySelector(
    selectedCategory: String,
    availableCategories: List<String>,
    onCategorySelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedTextField(
            value = selectedCategory,
            onValueChange = {},
            label = { Text("Category") },
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            availableCategories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category) },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    }
                )
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun AddEditScreenPreview() {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        AddEditNoteFields(
            title = "Preview Title",
            content = "This is a preview note content.\nSupports multiple lines.",
            onTitleChange = {},
            onContentChange = {}
        )

        Spacer(modifier = Modifier.height(8.dp))

        AddEditNoteCategorySelector(
            selectedCategory = "Work",
            availableCategories = listOf("Work", "Personal", "Ideas"),
            onCategorySelected = {}
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.weight(1f))
            AddEditSaveButton(onSaveClicked = {})
        }
    }
}




