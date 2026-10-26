package com.example.hava.data

import com.example.hava.model.Forecast

class WeatherRepository(private val forecastApi: ForecastApi = ApiClient.forecastApi) {
    suspend fun getForecast(latitude: Double, longitude: Double): Forecast =
        forecastApi.getForecast(latitude = latitude, longitude = longitude).toForecast()
}
