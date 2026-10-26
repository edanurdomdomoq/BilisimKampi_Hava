package com.example.hava

import com.example.hava.data.WorldCities
import com.example.hava.ui.components.normalizedSearch
import org.junit.Assert.*
import org.junit.Test

class CityDataTest {
    @Test
    fun turkeyContainsEveryPlateExactlyOnce() {
        val cities = WorldCities.cities("TR")
        assertEquals(81, cities.size)
        assertEquals(
            (1..81).map { it.toString().padStart(2, '0') }.toSet(),
            cities.map { it.plateCode }.toSet(),
        )
    }

    @Test
    fun supportedCountriesHaveUniqueCitiesAndValidCoordinates() {
        val countries = WorldCities.countries
        assertEquals(countries.size, countries.map { it.code }.distinct().size)
        val cities = countries.flatMap { WorldCities.cities(it.code) }
        assertEquals(cities.size, cities.map { it.plateCode }.distinct().size)
        countries.forEach { country ->
            val records = WorldCities.cities(country.code)
            assertTrue(records.isNotEmpty())
            records.forEach { city ->
                assertEquals(country.code, city.countryCode)
                assertTrue(city.latitude in -90.0..90.0)
                assertTrue(city.longitude in -180.0..180.0)
                assertTrue(city.name.isNotBlank() && city.englishName.isNotBlank())
            }
        }
    }

    @Test
    fun turkishSearchHandlesDottedAndDotlessLetters() {
        assertEquals("istanbul", normalizedSearch("İSTANBUL"))
        assertEquals("isparta", normalizedSearch("ISPARTA"))
        assertEquals("igdir", normalizedSearch("IĞDIR"))
    }

    @Test
    fun searchIgnoresWhitespaceAndDiacritics() {
        assertEquals("munih", normalizedSearch("  MÜNİH  "))
        assertEquals("dusseldorf", normalizedSearch("Düsseldorf"))
        assertEquals("sanliurfa", normalizedSearch("Şanlıurfa"))
    }

    @Test
    fun germanyHasBothTurkishAndEnglishPlaceNames() {
        val records = WorldCities.cities("DE")
        assertEquals("Munich", records.single { it.name == "Münih" }.englishName)
        assertEquals("Cologne", records.single { it.name == "Köln" }.englishName)
    }
}
