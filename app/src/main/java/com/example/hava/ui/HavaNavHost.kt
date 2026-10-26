package com.example.hava.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.example.hava.data.AppPreferences
import com.example.hava.ui.cities.CitiesScreen
import com.example.hava.ui.components.*
import com.example.hava.ui.forecast.ForecastScreen
import com.example.hava.ui.theme.HAVATheme

/** Owns navigation and saveable UI state; Activity only hosts the composition. */
@Composable
fun HavaNavHost() {
    val context = LocalContext.current
    val preferences = remember(context) { AppPreferences(context) }
    val saved = remember { preferences.load() }
    var language by rememberSaveable { mutableStateOf(saved.language) }
    var destination by rememberSaveable { mutableStateOf(HavaDestination.Cities) }
    var cityName by rememberSaveable { mutableStateOf(saved.city.name) }
    var englishCityName by rememberSaveable { mutableStateOf(saved.city.englishName) }
    var latitude by rememberSaveable { mutableDoubleStateOf(saved.city.latitude) }
    var longitude by rememberSaveable { mutableDoubleStateOf(saved.city.longitude) }
    var country by rememberSaveable { mutableStateOf(saved.countryCode) }
    val stateHolder = rememberSaveableStateHolder()
    val landscape = isLandscapeLayout()
    val keyboardVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val focus = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val selectDestination: (HavaDestination) -> Unit = {
        focus.clearFocus()
        keyboard?.hide()
        destination = it
    }
    BackHandler(enabled = destination != HavaDestination.Cities) {
        destination = HavaDestination.Cities
    }

    CompositionLocalProvider(LocalAppLanguage provides language) {
        HAVATheme(darkTheme = true) {
            SkyBackground {
                Scaffold(
                    containerColor = Color.Transparent,
                    bottomBar = {
                        if (!landscape && !keyboardVisible)
                            HavaNavigation(destination, onSelect = selectDestination)
                    },
                ) { padding ->
                    Row(
                        Modifier.fillMaxSize()
                            .padding(padding)
                            .consumeWindowInsets(padding)
                            .imePadding()
                    ) {
                        if (landscape)
                            HavaNavigation(
                                destination,
                                landscape = true,
                                onSelect = selectDestination,
                            )
                        Box(Modifier.weight(1f).fillMaxHeight()) {
                            when (destination) {
                                HavaDestination.Cities ->
                                    stateHolder.SaveableStateProvider("cities") {
                                        CitiesScreen(
                                            countryCode = country,
                                            onCountryChange = {
                                                country = it
                                                preferences.saveCountry(it)
                                            },
                                            onLanguageChange = {
                                                language = it
                                                preferences.saveLanguage(it)
                                            },
                                            onCityClick = { city ->
                                                preferences.saveCity(city)
                                                cityName = city.name
                                                englishCityName = city.englishName
                                                latitude = city.latitude
                                                longitude = city.longitude
                                                selectDestination(HavaDestination.Today)
                                            },
                                        )
                                    }
                                HavaDestination.Today,
                                HavaDestination.Forecast ->
                                    ForecastScreen(
                                        cityName =
                                            if (language == "en") englishCityName else cityName,
                                        latitude = latitude,
                                        longitude = longitude,
                                        onBack = { selectDestination(HavaDestination.Cities) },
                                        showDaily = destination == HavaDestination.Forecast,
                                        onToday = { selectDestination(HavaDestination.Today) },
                                        onForecast = { selectDestination(HavaDestination.Forecast) },
                                    )
                            }
                        }
                    }
                }
            }
        }
    }
}
