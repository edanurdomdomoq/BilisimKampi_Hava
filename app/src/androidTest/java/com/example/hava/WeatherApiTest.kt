package com.example.hava

import com.example.hava.data.WeatherRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

/** Exercises the real Retrofit DTO mapping independently of deterministic UI tests. */
class WeatherApiTest {
    @Test
    fun liveForecastMapsCurrentHourlyAndDailyRecords() = runBlocking {
        val forecast = WeatherRepository().getForecast(39.93, 32.86)
        assertTrue(forecast.current.temperature.isFinite())
        assertTrue(forecast.hourly.isNotEmpty())
        assertEquals(7, forecast.daily.size)
        assertTrue(forecast.upcomingHours().isNotEmpty())
        assertTrue(forecast.daily.all { it.minimumTemperature <= it.maximumTemperature })
    }
}
