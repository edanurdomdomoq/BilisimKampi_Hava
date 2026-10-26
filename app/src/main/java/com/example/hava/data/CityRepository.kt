package com.example.hava.data

import com.example.hava.model.CityResult

class CityRepository(private val cityApi: CityApi = ApiClient.cityApi) {

    suspend fun searchCities(cityName: String): List<CityResult> {

        val response = cityApi.searchCities(name = cityName)

        return response.results ?: emptyList()
    }
}
