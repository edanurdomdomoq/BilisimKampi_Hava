package com.example.hava.data

import com.example.hava.model.*
import com.google.gson.annotations.SerializedName

// API'den gelen tüm hava durumu verileri
data class ForecastResponse(
    @SerializedName("current") val current: CurrentWeatherResponse,
    @SerializedName("hourly") val hourly: HourlyWeatherResponse? = null,
    @SerializedName("daily") val daily: DailyWeatherResponse? = null,
)

// Anlık hava durumu
data class CurrentWeatherResponse(
    @SerializedName("time") val time: String,
    @SerializedName("temperature_2m") val temperature: Double,
    @SerializedName("relative_humidity_2m") val humidity: Int,
    @SerializedName("apparent_temperature") val feelsLike: Double,
    @SerializedName("wind_speed_10m") val windSpeed: Double,
    @SerializedName("weather_code") val weatherCode: Int,
    @SerializedName("is_day") val isDay: Int? = null,
)

// Saatlik hava tahminleri
data class HourlyWeatherResponse(
    @SerializedName("time") val time: List<String>,
    @SerializedName("temperature_2m") val temperatures: List<Double>,
    @SerializedName("weather_code") val weatherCodes: List<Int>,
    @SerializedName("precipitation_probability") val precipitationProbabilities: List<Int?>? = null,
)

// 7 günlük hava tahminleri
data class DailyWeatherResponse(
    @SerializedName("time") val time: List<String>,
    @SerializedName("temperature_2m_max") val maxTemperatures: List<Double>,
    @SerializedName("temperature_2m_min") val minTemperatures: List<Double>,
    @SerializedName("weather_code") val weatherCodes: List<Int>,
    @SerializedName("precipitation_probability_max")
    val precipitationProbabilities: List<Int?>? = null,
    @SerializedName("uv_index_max") val uvIndices: List<Double?>? = null,
)

/** API columns may be incomplete. Only complete required rows become domain records. */
fun ForecastResponse.toForecast(): Forecast {
    require(
        current.temperature.isFinite() &&
            current.feelsLike.isFinite() &&
            current.windSpeed.isFinite()
    ) {
        "Invalid current weather values"
    }
    val hours =
        hourly
            ?.let { columns ->
                columns.time.mapIndexedNotNull { index, time ->
                    val temperature =
                        columns.temperatures.getOrNull(index)?.takeIf { it.isFinite() }
                            ?: return@mapIndexedNotNull null
                    val code =
                        columns.weatherCodes.getOrNull(index) ?: return@mapIndexedNotNull null
                    HourlyForecast(
                        time,
                        temperature,
                        code,
                        columns.precipitationProbabilities?.getOrNull(index)?.coerceIn(0, 100),
                    )
                }
            }
            .orEmpty()
    val days =
        daily
            ?.let { columns ->
                columns.time.mapIndexedNotNull { index, date ->
                    val minimum =
                        columns.minTemperatures.getOrNull(index)?.takeIf { it.isFinite() }
                            ?: return@mapIndexedNotNull null
                    val maximum =
                        columns.maxTemperatures.getOrNull(index)?.takeIf { it.isFinite() }
                            ?: return@mapIndexedNotNull null
                    val code =
                        columns.weatherCodes.getOrNull(index) ?: return@mapIndexedNotNull null
                    DailyForecast(
                        date,
                        minimum,
                        maximum,
                        code,
                        columns.precipitationProbabilities?.getOrNull(index)?.coerceIn(0, 100),
                        columns.uvIndices?.getOrNull(index)?.takeIf { it.isFinite() && it >= 0 },
                    )
                }
            }
            .orEmpty()
    return Forecast(
        current =
            CurrentWeather(
                current.time,
                current.temperature,
                current.humidity,
                current.feelsLike,
                current.windSpeed,
                current.weatherCode,
                current.isDay,
            ),
        hourly = hours,
        daily = days,
    )
}
