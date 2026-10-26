package com.example.hava.ui.cities

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hava.ui.components.*
import com.example.hava.ui.theme.*

private val Lavender = Color(0xFFC6B5FF)
private val Hairline = Color(0xFF3B4963)

@Composable
internal fun CitySearchField(value: String, onValueChange: (String) -> Unit, placeholder: String) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = TextStyle(color = Color(0xFFE8E4F2), fontSize = 15.sp),
        cursorBrush = SolidColor(Lavender),
        modifier =
            Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF121D30))
                .border(1.dp, Hairline, RoundedCornerShape(24.dp)),
        decorationBox = { inner ->
            Row(
                Modifier.padding(horizontal = 16.dp).heightIn(min = 46.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(13.dp),
            ) {
                Icon(Icons.Filled.Search, null, Modifier.size(22.dp), tint = Color(0xFF9498B0))
                Box(Modifier.weight(1f)) {
                    if (value.isEmpty())
                        Text(placeholder, fontSize = 15.sp, color = Color(0xFF9498B0))
                    inner()
                }
                if (value.isNotEmpty())
                    IconButton(onClick = { onValueChange("") }, modifier = Modifier.size(30.dp)) {
                        Icon(
                            Icons.Filled.Close,
                            localized("Aramayı temizle", "Clear search"),
                            Modifier.size(17.dp),
                            tint = WeatherMuted,
                        )
                    }
            }
        },
    )
}

@Composable
internal fun CountryChip(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(28.dp),
        color = Color(0xFF26243F),
        border = BorderStroke(1.dp, Lavender),
        contentColor = Lavender,
    ) {
        Row(
            Modifier.padding(horizontal = 15.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(Icons.Filled.Language, null, Modifier.size(23.dp))
            Text(label, fontSize = 15.sp)
            Icon(Icons.Filled.KeyboardArrowDown, null, Modifier.size(20.dp))
        }
    }
}
