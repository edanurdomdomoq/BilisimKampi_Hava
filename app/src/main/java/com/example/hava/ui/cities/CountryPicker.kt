package com.example.hava.ui.cities

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hava.data.WorldCities
import com.example.hava.ui.components.*
import com.example.hava.ui.theme.*

private val Lavender = Color(0xFFC6B5FF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CountryPicker(
    selectedCountry: String,
    query: String,
    onQueryChange: (String) -> Unit,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val language = LocalAppLanguage.current
    val landscape = isLandscapeLayout()
    ModalBottomSheet(
        onDismissRequest = { onDismiss() },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFF111C30),
        contentColor = Color.White,
        scrimColor = Color.Black.copy(alpha = .55f),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                Modifier.padding(top = 12.dp, bottom = 5.dp)
                    .width(42.dp)
                    .height(5.dp)
                    .background(Color(0xFF505575), RoundedCornerShape(10.dp))
            )
        },
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp).padding(bottom = 24.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    localized("Ülke seç", "Choose country"),
                    fontFamily = FontFamily.Serif,
                    fontSize = 27.sp,
                    color = Color.White,
                )
                IconButton(onClick = { onDismiss() }) {
                    Icon(Icons.Filled.Close, localized("Kapat", "Close"))
                }
            }
            Spacer(Modifier.height(8.dp))
            CitySearchField(query, onQueryChange, localized("Ülke ara", "Search countries"))
            Text(
                localized("Tüm ülkeler", "All countries"),
                fontSize = 14.sp,
                color = WeatherMuted,
                modifier = Modifier.padding(start = 6.dp, top = 16.dp, bottom = 8.dp),
            )
            val countries =
                WorldCities.countries.filter {
                    normalizedSearch(it.turkish).contains(normalizedSearch(query)) ||
                        normalizedSearch(it.english).contains(normalizedSearch(query))
                }
            if (countries.isEmpty())
                Text(
                    localized("Ülke bulunamadı", "No countries found"),
                    color = WeatherMuted,
                    modifier = Modifier.padding(16.dp),
                )
            LazyColumn(
                Modifier.weight(1f, fill = false).heightIn(max = if (landscape) 180.dp else 300.dp)
            ) {
                items(countries, key = { it.code }) { option ->
                    val selected = option.code == selectedCountry
                    Row(
                        Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (selected) Color(0xFF2C3354) else Color.Transparent)
                            .clickable { onSelect(option.code) }
                            .padding(horizontal = 12.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text(option.flag, fontSize = 27.sp)
                        Text(
                            if (language == "en") option.english else option.turkish,
                            Modifier.weight(1f),
                            fontSize = 16.sp,
                            color = Color.White,
                        )
                        if (selected)
                            Box(
                                Modifier.size(26.dp).background(Lavender, CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    Icons.Filled.Check,
                                    null,
                                    Modifier.size(18.dp),
                                    tint = Color(0xFF241940),
                                )
                            }
                    }
                    if (!selected)
                        HorizontalDivider(
                            Modifier.padding(horizontal = 12.dp),
                            color = Color(0xFF263248),
                        )
                }
            }
        }
    }
}
