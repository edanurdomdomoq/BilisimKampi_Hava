package com.example.hava.model

data class City(
    val name: String,
    val plateCode: String,
    val latitude: Double,
    val longitude: Double,
    val englishName: String = name,
    val countryCode: String = "TR",
    val photoKey: String = "",
)

data class CitySearchResponse(val results: List<CityResult>? = null)

data class CityResult(
    val id: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String? = null,
    val admin1: String? = null,
)
