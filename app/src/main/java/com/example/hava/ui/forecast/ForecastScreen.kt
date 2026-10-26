package com.example.hava.ui.forecast

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hava.model.*
import com.example.hava.ui.LoadState
import com.example.hava.ui.components.*
import com.example.hava.ui.theme.*

@Composable
fun ForecastScreen(
    cityName: String,
    latitude: Double,
    longitude: Double,
    onBack: () -> Unit,
    showDaily: Boolean = false,
    onToday: () -> Unit = {},
    onForecast: () -> Unit = {},
    forecastViewModel: ForecastViewModel = viewModel(),
) {
    LaunchedEffect(latitude, longitude) { forecastViewModel.loadForecast(latitude, longitude) }
    val landscape = isLandscapeLayout()
    val scroll = rememberSaveable(showDaily, saver = ScrollState.Saver) { ScrollState(0) }
    Column(
        Modifier.fillMaxSize().verticalScroll(scroll).padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Spacer(Modifier.height(4.dp))
        if (showDaily) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onToday) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        localized("Bugüne dön", "Back to today"),
                        tint = WeatherInk,
                    )
                }
                Text(
                    localized("7 günlük tahmin", "7-day forecast"),
                    fontFamily = FontFamily.Serif,
                    fontSize = if (landscape) 24.sp else 27.sp,
                    fontWeight = FontWeight.Bold,
                    color = WeatherInk,
                    modifier = Modifier.weight(1f),
                )
                Text(cityName, fontSize = 16.sp, color = WeatherMuted)
            }
        } else {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Surface(
                    onClick = onBack,
                    shape = RoundedCornerShape(28.dp),
                    color = Color(0xFF1B2941),
                ) {
                    Row(
                        Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(Icons.Filled.Place, null, Modifier.size(20.dp), tint = WeatherInk)
                        Text(
                            cityName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = WeatherInk,
                        )
                        Icon(
                            Icons.Filled.KeyboardArrowDown,
                            null,
                            Modifier.size(18.dp),
                            tint = WeatherInk,
                        )
                    }
                }
                FilledIconButton(
                    onClick = onBack,
                    colors =
                        IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color(0xFF1B2941),
                            contentColor = WeatherInk,
                        ),
                ) {
                    Icon(Icons.Filled.Search, localized("Şehir ara", "Search cities"))
                }
            }
        }
        when (val state = forecastViewModel.uiState) {
            LoadState.Loading ->
                WeatherPanel {
                    LoadingView(localized("Hava durumu yükleniyor…", "Loading weather…"))
                }
            LoadState.Error ->
                WeatherPanel {
                    ErrorView(
                        localized("Hava durumu alınamadı", "Weather unavailable"),
                        onRetry = { forecastViewModel.loadForecast(latitude, longitude) },
                    )
                }
            LoadState.Empty ->
                WeatherPanel {
                    EmptyView(localized("Hava durumu bulunamadı", "Weather unavailable"))
                }
            is LoadState.Success -> {
                val response = state.data
                if (showDaily) {
                    if (landscape)
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column(Modifier.weight(1.15f)) { DailyPanel(response.daily) }
                            Column(
                                Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                RainPanel(response.daily)
                                RainNote()
                            }
                        }
                    else {
                        DailyPanel(response.daily)
                        RainPanel(response.daily)
                        RainNote()
                    }
                } else {
                    if (landscape)
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column(Modifier.weight(1f)) { TodayHero(response) }
                            Column(
                                Modifier.weight(1.15f),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                HourlyPanel(response)
                                ForecastMetrics(response)
                            }
                        }
                    else {
                        TodayHero(response)
                        HourlyPanel(response)
                        ForecastMetrics(response)
                    }
                    TextButton(
                        onClick = onForecast,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    ) {
                        Text(
                            localized("7 günlük tahmini gör", "View 7-day forecast"),
                            color = WeatherInk,
                        )
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = WeatherInk)
                    }
                }
                Text(
                    localized("Hava verileri: Open-Meteo", "Weather data: Open-Meteo"),
                    fontSize = 10.sp,
                    color = WeatherMuted,
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 8.dp),
                )
            }
        }
    }
}
