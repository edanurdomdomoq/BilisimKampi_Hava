package com.example.hava

import com.example.hava.data.*
import com.google.gson.JsonParser
import org.junit.Assert.*
import org.junit.Test

class CityPhotoTest {
    private val berlin = WorldCities.cities("DE").first()

    private fun json(value: String) = JsonParser.parseString(value).asJsonObject

    private fun page(lat: Double = 52.52, file: String = "Berlin.jpg") =
        json(
            """{"query":{"pages":[{"title":"Berlin","coordinates":[{"lat":$lat,"lon":13.405}],"pageimage":"$file","thumbnail":{"source":"https://upload.wikimedia.org/photo.jpg"}}]}}"""
        )

    private fun credit(license: String = "CC BY-SA 4.0") =
        json(
            """{"query":{"pages":[{"title":"File:Berlin.jpg","imageinfo":[{"descriptionurl":"https://commons.wikimedia.org/wiki/File:Berlin.jpg","extmetadata":{"Artist":{"value":"Photographer"},"LicenseShortName":{"value":"$license"},"LicenseUrl":{"value":"https://creativecommons.org/licenses/by-sa/4.0/"}}}]}]}}"""
        )

    @Test
    fun acceptsNearbyCityPhoto() {
        assertEquals("Berlin.jpg", CityPhotoParser.parsePage(page(), berlin)?.file)
    }

    @Test
    fun rejectsWrongCityCoordinates() {
        assertNull(CityPhotoParser.parsePage(page(lat = 10.0), berlin))
    }

    @Test
    fun rejectsSvgAndMissingPages() {
        assertNull(CityPhotoParser.parsePage(page(file = "Map.svg"), berlin))
        assertNull(CityPhotoParser.parsePage(json("{}"), berlin))
    }

    @Test
    fun followsArticleRedirect() {
        val root = page()
        root
            .getAsJsonObject("query")
            .add("redirects", JsonParser.parseString("""[{"from":"Berlin alias","to":"Berlin"}]"""))
        assertNotNull(CityPhotoParser.parsePage(root, berlin.copy(englishName = "Berlin alias")))
    }

    @Test
    fun keepsAuthorSourceAndLicenseTogether() {
        val result =
            CityPhotoParser.parseCredit(CityPhotoParser.parsePage(page(), berlin)!!, credit())!!
        assertEquals("Photographer", result.author)
        assertEquals("CC BY-SA 4.0", result.license)
        assertTrue(result.source.startsWith("https://commons.wikimedia.org/"))
    }

    @Test
    fun rejectsUnlicensedPhotos() {
        assertNull(
            CityPhotoParser.parseCredit(
                CityPhotoParser.parsePage(page(), berlin)!!,
                credit(license = ""),
            )
        )
    }
}
