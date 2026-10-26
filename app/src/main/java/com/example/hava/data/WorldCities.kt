package com.example.hava.data

import com.example.hava.model.City

data class WeatherCountry(
    val code: String,
    val turkish: String,
    val english: String,
    val flag: String,
)

object WorldCities {
    val countries =
        listOf(
            WeatherCountry("TR", "Türkiye", "Türkiye", "🇹🇷"),
            WeatherCountry("DE", "Almanya", "Germany", "🇩🇪"),
            WeatherCountry("GB", "Birleşik Krallık", "United Kingdom", "🇬🇧"),
            WeatherCountry("FR", "Fransa", "France", "🇫🇷"),
            WeatherCountry("IT", "İtalya", "Italy", "🇮🇹"),
        )
    private val featured = listOf("06", "34", "35", "07", "16", "01", "27")
    private val turkeyPhotos =
        mapOf(
            "06" to "ankara",
            "34" to "istanbul",
            "35" to "izmir",
            "07" to "antalya",
            "16" to "bursa",
            "01" to "adana",
            "27" to "gaziantep",
        )
    private val turkey =
        (featured.mapNotNull { plate ->
                TurkiyeSehirleri.tumSehirler.find { it.plateCode == plate }
            } + TurkiyeSehirleri.tumSehirler.filter { it.plateCode !in featured })
            .map {
                it.copy(
                    photoKey = turkeyPhotos[it.plateCode] ?: "",
                    englishName =
                        when (it.name) {
                            "İstanbul" -> "Istanbul"
                            "İzmir" -> "Izmir"
                            else -> it.name
                        },
                )
            }

    private fun city(tr: String, en: String, code: String, key: String, lat: Double, lon: Double) =
        City(tr, "$code-$key", lat, lon, en, code, key)

    fun cities(country: String): List<City> =
        when (country) {
            "DE" ->
                listOf(
                    city("Berlin", "Berlin", "DE", "berlin", 52.52, 13.405),
                    city("Hamburg", "Hamburg", "DE", "hamburg", 53.5511, 9.9937),
                    city("Münih", "Munich", "DE", "munich", 48.1351, 11.582),
                    city("Köln", "Cologne", "DE", "cologne", 50.9375, 6.9603),
                    city("Frankfurt", "Frankfurt", "DE", "frankfurt", 50.1109, 8.6821),
                    city("Stuttgart", "Stuttgart", "DE", "stuttgart", 48.7758, 9.1829),
                    city("Düsseldorf", "Düsseldorf", "DE", "dusseldorf", 51.2277, 6.7735),
                )
            "GB" ->
                listOf(
                    city("Londra", "London", "GB", "london", 51.5074, -0.1278),
                    city("Edinburgh", "Edinburgh", "GB", "edinburgh", 55.9533, -3.1883),
                    city("Manchester", "Manchester", "GB", "manchester", 53.4808, -2.2426),
                    city("Birmingham", "Birmingham", "GB", "birmingham", 52.4862, -1.8904),
                    city("Liverpool", "Liverpool", "GB", "liverpool", 53.4084, -2.9916),
                    city("Glasgow", "Glasgow", "GB", "glasgow", 55.8642, -4.2518),
                    city("Bristol", "Bristol", "GB", "bristol", 51.4545, -2.5879),
                )
            "FR" ->
                listOf(
                    city("Paris", "Paris", "FR", "paris", 48.8566, 2.3522),
                    city("Lyon", "Lyon", "FR", "lyon", 45.764, 4.8357),
                    city("Marsilya", "Marseille", "FR", "marseille", 43.2965, 5.3698),
                    city("Nice", "Nice", "FR", "nice", 43.7102, 7.262),
                    city("Bordeaux", "Bordeaux", "FR", "bordeaux", 44.8378, -0.5792),
                    city("Toulouse", "Toulouse", "FR", "toulouse", 43.6047, 1.4442),
                    city("Strazburg", "Strasbourg", "FR", "strasbourg", 48.5734, 7.7521),
                )
            "IT" ->
                listOf(
                    city("Roma", "Rome", "IT", "rome", 41.9028, 12.4964),
                    city("Milano", "Milan", "IT", "milan", 45.4642, 9.19),
                    city("Venedik", "Venice", "IT", "venice", 45.4408, 12.3155),
                    city("Floransa", "Florence", "IT", "florence", 43.7696, 11.2558),
                    city("Napoli", "Naples", "IT", "naples", 40.8518, 14.2681),
                    city("Torino", "Turin", "IT", "turin", 45.0703, 7.6869),
                    city("Bolonya", "Bologna", "IT", "bologna", 44.4949, 11.3426),
                )
            else -> turkey
        }
}
