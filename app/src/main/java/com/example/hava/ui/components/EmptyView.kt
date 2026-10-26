package com.example.hava.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.hava.ui.theme.*

@Composable
fun EmptyView(message: String) {
    Text(message, color = WeatherMuted)
}
