package com.example.hava.ui.cities

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hava.data.WorldCities
import com.example.hava.model.City
import com.example.hava.ui.LoadState
import com.example.hava.ui.components.*
import com.example.hava.ui.theme.*
import kotlinx.coroutines.flow.distinctUntilChanged

private val Lavender = Color(0xFFC6B5FF)
private val Hairline = Color(0xFF3B4963)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitiesScreen(
    countryCode: String = "TR",
    onCountryChange: (String) -> Unit = {},
    onLanguageChange: (String) -> Unit = {},
    citiesViewModel: CitiesViewModel = viewModel(),
    onCityClick: (City) -> Unit,
) {
    val photoContext = LocalContext.current
    LaunchedEffect(countryCode) {
        citiesViewModel.loadPhotos(photoContext.applicationContext, countryCode)
    }
    val photos by citiesViewModel.photos.collectAsState()
    val language = LocalAppLanguage.current
    val landscape = isLandscapeLayout()
    val country =
        WorldCities.countries.firstOrNull { it.code == countryCode }
            ?: WorldCities.countries.first()
    val cities = remember(countryCode) { WorldCities.cities(countryCode) }
    var query by rememberSaveable(countryCode) { mutableStateOf("") }
    var visibleCount by rememberSaveable(countryCode) { mutableIntStateOf(7) }
    var showCountries by rememberSaveable { mutableStateOf(false) }
    var countryQuery by rememberSaveable { mutableStateOf("") }
    var showCredits by remember { mutableStateOf(false) }
    val listState = rememberLazyGridState()
    var previousFilter by rememberSaveable { mutableStateOf("$countryCode|$query") }
    val filtered =
        remember(query, cities) {
            val search = normalizedSearch(query)
            cities.filter {
                normalizedSearch(it.name).contains(search) ||
                    normalizedSearch(it.englishName).contains(search)
            }
        }
    val visible = if (query.isNotBlank()) filtered else cities.take(visibleCount)
    LaunchedEffect(listState, visible) {
        snapshotFlow {
                val items = listState.layoutInfo.visibleItemsInfo
                val first = (items.firstOrNull()?.index ?: 0).coerceAtLeast(0)
                val last = items.lastOrNull()?.index ?: -1
                // Cover two grid rows ahead, including Compose's prefetched cards.
                if (last < 0) emptyList()
                else
                    visible.subList(
                        (first - 4).coerceAtLeast(0).coerceAtMost(visible.size),
                        (last + 5).coerceAtMost(visible.size),
                    )
            }
            .distinctUntilChanged()
            .collect { citiesViewModel.loadWeather(it) }
    }
    LaunchedEffect(query, countryCode) {
        val filter = "$countryCode|$query"
        if (previousFilter != filter) {
            visibleCount = 7
            listState.scrollToItem(0)
            previousFilter = filter
        }
    }
    LaunchedEffect(listState, query, countryCode) {
        if (query.isBlank())
            snapshotFlow {
                    val info = listState.layoutInfo
                    (info.visibleItemsInfo.lastOrNull()?.index ?: -1) to info.totalItemsCount
                }
                .distinctUntilChanged()
                .collect { (last, total) ->
                    if (total > 0 && last >= total - 2 && visibleCount < cities.size)
                        visibleCount = (visibleCount + 7).coerceAtMost(cities.size)
                }
    }
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(if (landscape) 4.dp else 16.dp))
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                localized("Şehirler", "Cities"),
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = if (landscape) 28.sp else 38.sp,
                color = Color(0xFFF6F3FB),
            )
            Row(
                Modifier.border(1.dp, Hairline, RoundedCornerShape(24.dp)).padding(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                listOf("tr", "en").forEach { code ->
                    val selected = language == code
                    Box(
                        Modifier.clip(RoundedCornerShape(20.dp))
                            .background(
                                if (selected)
                                    Brush.linearGradient(
                                        listOf(Color(0xFFBEA6FF), Color(0xFFDCD3FF))
                                    )
                                else
                                    Brush.linearGradient(
                                        listOf(Color.Transparent, Color.Transparent)
                                    )
                            )
                            .clickable { onLanguageChange(code) }
                            .padding(horizontal = 14.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            code.uppercase(),
                            fontSize = 14.sp,
                            color = if (selected) Color(0xFF17142C) else WeatherMuted,
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(if (landscape) 6.dp else 14.dp))
        if (landscape) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.weight(1f)) {
                    CitySearchField(query, { query = it }, localized("Şehir ara", "Search cities"))
                }
                CountryChip(localized("Ülke: ${country.turkish}", "Country: ${country.english}")) {
                    countryQuery = ""
                    showCountries = true
                }
            }
        } else {
            CitySearchField(query, { query = it }, localized("Şehir ara", "Search cities"))
            Spacer(Modifier.height(14.dp))
            CountryChip(localized("Ülke: ${country.turkish}", "Country: ${country.english}")) {
                countryQuery = ""
                showCountries = true
            }
        }
        Spacer(Modifier.height(if (landscape) 6.dp else 15.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                if (query.isNotBlank()) localized("Arama sonuçları", "Search results")
                else localized("${country.turkish} şehirleri", "Cities in ${country.english}"),
                fontSize = 14.sp,
                color = WeatherMuted,
            )
            Text(
                if (query.isBlank()) "${visible.size} / ${cities.size}" else "${visible.size}",
                fontSize = 11.sp,
                color = WeatherMuted.copy(alpha = .7f),
            )
        }
        Spacer(Modifier.height(if (landscape) 6.dp else 12.dp))
        if (visible.isEmpty()) {
            Column(
                Modifier.fillMaxWidth().padding(top = 50.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(Icons.Filled.Search, null, Modifier.size(34.dp), tint = WeatherMuted)
                Text(
                    localized("Şehir bulunamadı", "No cities found"),
                    color = WeatherMuted,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(if (landscape) 2 else 1),
                modifier = Modifier.weight(1f),
                state = listState,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
            ) {
                gridItems(visible, key = { it.plateCode }) { city ->
                    CityWeatherCard(
                        city,
                        remotePhoto = photos[city.plateCode],
                        weatherState = citiesViewModel.weather[city.plateCode] ?: LoadState.Loading,
                        onRetry = { citiesViewModel.retry(city) },
                    ) {
                        onCityClick(city)
                    }
                }
                item(key = "photo-credits", span = { GridItemSpan(maxLineSpan) }) {
                    TextButton(
                        onClick = { showCredits = true },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            localized("Fotoğraf kaynakları", "Photo credits"),
                            fontSize = 11.sp,
                            color = WeatherMuted,
                        )
                    }
                }
            }
        }
    }
    if (showCountries)
        CountryPicker(
            countryCode,
            countryQuery,
            onQueryChange = { countryQuery = it },
            onSelect = {
                onCountryChange(it)
                showCountries = false
            },
            onDismiss = { showCountries = false },
        )
    if (showCredits) PhotoCreditsDialog(photos = photos.values) { showCredits = false }
}
