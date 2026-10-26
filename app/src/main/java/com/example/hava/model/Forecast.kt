package com.example.hava.model

/** UI-independent weather values; HTTP field names stay in data/ForecastResponse.kt. */
data class Forecast(
    val current: CurrentWeather,
    val hourly: List<HourlyForecast> = emptyList(),
    val daily: List<DailyForecast> = emptyList(),
) {
    val currentHour: HourlyForecast?
        get() = hourly.firstOrNull { it.time.take(13) == current.time.take(13) }

    fun upcomingHours(limit: Int = 24): List<HourlyForecast> =
        hourly.dropWhile { it.time.take(13) < current.time.take(13) }.take(limit.coerceAtLeast(0))
}

data class CurrentWeather(
    val time: String,
    val temperature: Double,
    val humidity: Int,
    val feelsLike: Double,
    val windSpeed: Double,
    val weatherCode: Int,
    val isDay: Int? = null,
)

data class HourlyForecast(
    val time: String,
    val temperature: Double,
    val weatherCode: Int,
    val precipitationProbability: Int? = null,
)

data class DailyForecast(
    val date: String,
    val minimumTemperature: Double,
    val maximumTemperature: Double,
    val weatherCode: Int,
    val precipitationProbability: Int? = null,
    val uvIndex: Double? = null,
)
