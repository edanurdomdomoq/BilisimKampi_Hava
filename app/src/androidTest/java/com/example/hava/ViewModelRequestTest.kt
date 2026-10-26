package com.example.hava

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.ViewModelStore
import com.example.hava.data.*
import com.example.hava.ui.LoadState
import com.example.hava.ui.cities.CitiesViewModel
import com.example.hava.ui.forecast.ForecastViewModel
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class ViewModelRequestTest {
    @get:Rule val compose = createComposeRule()
    private val store = ViewModelStore()

    @After
    fun clearModels() {
        compose.runOnIdle { store.clear() }
    }

    private class FakeApi(val result: suspend (Double, Double) -> ForecastResponse) : ForecastApi {
        override suspend fun getForecast(
            latitude: Double,
            longitude: Double,
            current: String,
            hourly: String,
            daily: String,
            forecastDays: Int,
            timezone: String,
        ) = result(latitude, longitude)
    }

    private fun response(temperature: Double = 18.0) =
        ForecastResponse(
            CurrentWeatherResponse("2026-10-26T15:00", temperature, 60, 17.0, 12.0, 2, 1)
        )

    @Test
    fun oldCityCannotOverwriteANewerCityEvenIfSourceIgnoresCancellation() {
        val oldStarted = CompletableDeferred<Unit>()
        val oldResult = CompletableDeferred<ForecastResponse>()
        val oldReturned = CompletableDeferred<Unit>()
        val api = FakeApi { latitude, _ ->
            if (latitude == 39.93)
                withContext(NonCancellable) {
                    oldStarted.complete(Unit)
                    val result = oldResult.await()
                    oldReturned.complete(Unit)
                    result
                }
            else response(22.0)
        }
        val model = ForecastViewModel(WeatherRepository(api))
        store.put("forecast", model)
        compose.setContent {}
        compose.runOnIdle { model.loadForecast(39.93, 32.86) }
        compose.waitUntil(5000) { oldStarted.isCompleted }
        compose.runOnIdle { model.loadForecast(52.52, 13.405) }
        compose.waitUntil(5000) {
            (model.uiState as? LoadState.Success)?.data?.current?.temperature == 22.0
        }
        oldResult.complete(response(10.0))
        compose.waitUntil(5000) { oldReturned.isCompleted }
        compose.runOnIdle {
            assertEquals(22.0, (model.uiState as LoadState.Success).data.current.temperature, 0.0)
        }
    }

    @Test
    fun citiesCoalesceDuplicateRequestsAndLimitParallelCalls() {
        val calls = AtomicInteger(0)
        val inFlight = AtomicInteger(0)
        val peak = AtomicInteger(0)
        val api = FakeApi { _, _ ->
            calls.incrementAndGet()
            val active = inFlight.incrementAndGet()
            peak.updateAndGet { maxOf(it, active) }
            try {
                delay(60)
                response()
            } finally {
                inFlight.decrementAndGet()
            }
        }
        val model = CitiesViewModel(WeatherRepository(api))
        store.put("cities", model)
        val cities = WorldCities.cities("DE")
        compose.setContent {}
        compose.runOnIdle {
            model.loadWeather(cities)
            model.loadWeather(cities)
        }
        compose.waitUntil(5000) {
            model.weather.size == cities.size &&
                model.weather.values.all { it is LoadState.Success }
        }
        assertEquals(cities.size, calls.get())
        assertTrue(peak.get() <= 3)
        compose.runOnIdle { model.loadWeather(cities) }
        assertEquals(cities.size, calls.get())
    }

    @Test
    fun cityFailureCanBeRetriedAndSuccessIsNotRequestedAgain() {
        val calls = AtomicInteger(0)
        val api = FakeApi { _, _ ->
            if (calls.incrementAndGet() == 1) throw IOException("offline") else response()
        }
        val model = CitiesViewModel(WeatherRepository(api))
        store.put("cities", model)
        val city = WorldCities.cities("TR").first()
        compose.setContent {}
        compose.runOnIdle { model.loadWeather(listOf(city)) }
        compose.waitUntil(5000) { model.weather[city.plateCode] == LoadState.Error }
        compose.runOnIdle { model.retry(city) }
        compose.waitUntil(5000) { model.weather[city.plateCode] is LoadState.Success }
        compose.runOnIdle { model.retry(city) }
        assertEquals(2, calls.get())
    }
}
