package com.zeros.notephiny.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun DashboardScreen() {
    Text(
        text = "Welcome to Notephiny!",
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier
            .background(Color.White)
            .fillMaxSize()
            .padding(16.dp)
    )
}

@Preview
@Composable
fun DashboardScreenPreview() {
    DashboardScreen()
}