package com.example.hava

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.hava.data.CurrentWeatherResponse
import com.example.hava.data.ForecastApi
import com.example.hava.data.ForecastResponse
import com.example.hava.data.WeatherRepository
import com.example.hava.ui.forecast.ForecastScreen
import com.example.hava.ui.forecast.ForecastViewModel
import com.example.hava.ui.theme.HAVATheme
import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ForecastRecoveryTest {
    @get:Rule val compose = createComposeRule()

    private class FakeForecastApi(private val failFirst: Boolean = false) : ForecastApi {
        var calls = 0

        override suspend fun getForecast(
            latitude: Double,
            longitude: Double,
            current: String,
            hourly: String,
            daily: String,
            forecastDays: Int,
            timezone: String,
        ): ForecastResponse {
            calls++
            if (failFirst && calls == 1) throw IOException("Simulated network outage")
            return ForecastResponse(
                CurrentWeatherResponse("2026-10-25T15:00", 18.0, 60, 17.0, 12.0, 2, 1)
            )
        }
    }

    private fun waitFor(text: String) {
        compose.waitUntil(10000) {
            compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun failedRequestCanBeRetriedWithoutRestartingApp() {
        val api = FakeForecastApi(failFirst = true)
        val model = ForecastViewModel(WeatherRepository(api))
        compose.setContent {
            HAVATheme(darkTheme = true) {
                ForecastScreen("Ankara", 39.93, 32.86, onBack = {}, forecastViewModel = model)
            }
        }
        waitFor("Hava durumu alınamadı")
        compose.onNodeWithText("Tekrar dene").performClick()
        waitFor("Hissedilen 17°")
        compose.onNodeWithText("Hava durumu alınamadı").assertDoesNotExist()
        assertEquals(2, api.calls)
    }

    @Test
    fun sameCoordinatesReuseLoadedForecastButNewCityFetches() {
        val api = FakeForecastApi()
        val model = ForecastViewModel(WeatherRepository(api))
        compose.setContent {
            HAVATheme(darkTheme = true) {
                ForecastScreen("Ankara", 39.93, 32.86, onBack = {}, forecastViewModel = model)
            }
        }
        waitFor("Hissedilen 17°")
        compose.runOnIdle { model.loadForecast(39.93, 32.86) }
        assertEquals(1, api.calls)
        compose.runOnIdle { model.loadForecast(52.52, 13.405) }
        compose.waitForIdle()
        assertEquals(2, api.calls)
    }
}
