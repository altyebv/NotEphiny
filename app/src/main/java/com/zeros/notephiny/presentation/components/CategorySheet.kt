package com.zeros.notephiny.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zeros.notephiny.data.model.CategoryGroup
import com.zeros.notephiny.data.model.CategoryItem
import com.zeros.notephiny.data.model.CategoryType

data class NotebookItem(
    val name: String,
    val count: Int? = null,
    val icon: ImageVector = Icons.Default.Home,
    val type: CategoryType,
)


@Composable
fun CategorySelectorUI(
    userNotebooks: List<NotebookItem>,
    specialNotebooks: List<NotebookItem>,
    onNotebookClick: (NotebookItem) -> Unit,
    onCreateNew: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        NotebookRow(
            item = NotebookItem(name = "All Notes", icon = Icons.Default.Home, type = CategoryType.APP),
            onClick = { onNotebookClick(NotebookItem("All Notes", type = CategoryType.APP)) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        NotebooksHeader(onCreateNew)

        NotebookGroupCard(
            notebooks = userNotebooks,
            onClick = onNotebookClick
        )

        NotebookGroupCard(
            notebooks = specialNotebooks,
            onClick = onNotebookClick
        )
    }
}

@Composable
fun NotebooksHeader(onCreateNew: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "My Notebooks",
            style = MaterialTheme.typography.labelLarge
        )
        Text(
            text = "New",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.clickable { onCreateNew() }
        )
    }
}

@Composable
fun NotebookRow(
    item: NotebookItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            modifier = Modifier.padding(end = 16.dp)
        )

        Text(
            text = item.name,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge
        )

        item.count?.let {
            Text(
                text = it.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 8.dp)
            )
        }

        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null
        )
    }
}

@Composable
fun NotebookGroupCard(
    notebooks: List<NotebookItem>,
    onClick: (NotebookItem) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            notebooks.forEachIndexed { index, item ->
                NotebookRow(item = item, onClick = { onClick(item) })

                if (index < notebooks.lastIndex) {
                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}







