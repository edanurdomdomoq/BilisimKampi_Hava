package com.example.hava.data

import android.content.Context
import com.example.hava.model.City

/** Retains the existing keys so installed users keep their selected city and language. */
class AppPreferences(context: Context) {
    private val preferences =
        context.applicationContext.getSharedPreferences("weather_city", Context.MODE_PRIVATE)

    fun load(): AppSettings {
        val name = preferences.getString("name", "Ankara") ?: "Ankara"
        return AppSettings(
            language = preferences.getString("language", "tr") ?: "tr",
            countryCode = preferences.getString("country", "TR") ?: "TR",
            city =
                City(
                    name,
                    "",
                    Double.fromBits(preferences.getLong("latitude", 39.93.toBits())),
                    Double.fromBits(preferences.getLong("longitude", 32.86.toBits())),
                    preferences.getString("englishName", name) ?: name,
                ),
        )
    }

    fun saveLanguage(language: String) {
        preferences.edit().putString("language", language).apply()
    }

    fun saveCountry(country: String) {
        preferences.edit().putString("country", country).apply()
    }

    fun saveCity(city: City) {
        preferences
            .edit()
            .putString("name", city.name)
            .putString("englishName", city.englishName)
            .putLong("latitude", city.latitude.toBits())
            .putLong("longitude", city.longitude.toBits())
            .apply()
    }
}

data class AppSettings(val language: String, val countryCode: String, val city: City)
