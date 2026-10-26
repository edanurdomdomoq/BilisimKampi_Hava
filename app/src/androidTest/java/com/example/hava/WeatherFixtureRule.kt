package com.example.hava

import com.example.hava.data.*
import org.junit.rules.ExternalResource

/** UI assertions are repeatable; provider availability belongs to separate API tests. */
class WeatherFixtureRule : ExternalResource() {
    private lateinit var previous: WeatherRepository

    override fun before() {
        previous = AppContainer.weatherRepository
        AppContainer.weatherRepository = WeatherRepository(FixtureApi())
    }

    override fun after() {
        AppContainer.weatherRepository = previous
    }

    private class FixtureApi : ForecastApi {
        override suspend fun getForecast(
            latitude: Double,
            longitude: Double,
            current: String,
            hourly: String,
            daily: String,
            forecastDays: Int,
            timezone: String,
        ): ForecastResponse {
            val dates =
                listOf(
                    "2026-10-26",
                    "2026-10-27",
                    "2026-10-28",
                    "2026-10-29",
                    "2026-10-30",
                    "2026-10-31",
                    "2026-11-01",
                )
            return ForecastResponse(
                current = CurrentWeatherResponse("2026-10-26T15:15", 18.0, 60, 17.0, 12.0, 2, 1),
                hourly =
                    HourlyWeatherResponse(
                        (0..23).map { "2026-10-26T${it.toString().padStart(2,'0')}:00" },
                        List(24) { 18.0 },
                        List(24) { 2 },
                        List(24) { 20 },
                    ),
                daily =
                    DailyWeatherResponse(
                        dates,
                        List(7) { 20.0 },
                        List(7) { 10.0 },
                        List(7) { 2 },
                        List(7) { 20 },
                        List(7) { 3.0 },
                    ),
            )
        }
    }
}
