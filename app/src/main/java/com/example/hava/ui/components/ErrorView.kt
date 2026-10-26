package com.example.hava.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.example.hava.ui.theme.*

@Composable
fun ErrorView(title: String, onRetry: () -> Unit) {
    Column {
        Text(title, fontWeight = FontWeight.SemiBold, color = WeatherInk)
        Text(
            localized(
                "Bağlantını kontrol edip tekrar deneyebilirsin.",
                "Check your connection and try again.",
            ),
            color = WeatherMuted,
        )
        TextButton(onClick = onRetry) { Text(localized("Tekrar dene", "Retry")) }
    }
}
