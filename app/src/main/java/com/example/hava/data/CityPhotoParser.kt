package com.example.hava.data

import com.example.hava.model.City
import com.example.hava.model.CityPhoto
import com.google.gson.JsonObject
import kotlin.math.*

/** Temporary API record, before Commons author and license have been verified. */
data class PhotoPage(val title: String, val file: String, val url: String)

/** Rejects a same-name article outside the requested city. */
object CityPhotoParser {
    private const val MAX_CITY_DISTANCE_KM = 50.0
    private const val EARTH_RADIUS_KM = 6371.0

    fun articleTitle(city: City): String =
        when (city.name) {
            "Kocaeli" -> "İzmit"
            "Sakarya" -> "Adapazarı"
            "Van" -> "Van, Turkey"
            "Batman" -> "Batman, Turkey"
            "Sinop" -> "Sinop, Turkey"
            "Hakkari",
            "Hakkâri" -> "Hakkâri (city)"
            else -> city.englishName
        }

    fun parsePage(root: JsonObject, city: City): PhotoPage? {
        var title = articleTitle(city)
        val query = root.getAsJsonObject("query") ?: return null
        listOf("normalized", "redirects").forEach { key ->
            query.getAsJsonArray(key)?.forEach { entry ->
                val value = entry.asJsonObject
                if (value.get("from")?.asString == title) title = value.get("to").asString
            }
        }
        val page =
            query
                .getAsJsonArray("pages")
                ?.firstOrNull { it.asJsonObject.get("title")?.asString == title }
                ?.asJsonObject ?: return null
        val coord = page.getAsJsonArray("coordinates")?.firstOrNull()?.asJsonObject ?: return null
        val lat = coord.get("lat").asDouble
        val lon = coord.get("lon").asDouble
        val dy = Math.toRadians(lat - city.latitude)
        val dx = Math.toRadians(lon - city.longitude)
        val a =
            sin(dy / 2).pow(2) +
                cos(Math.toRadians(lat)) * cos(Math.toRadians(city.latitude)) * sin(dx / 2).pow(2)
        if (EARTH_RADIUS_KM * 2 * asin(sqrt(a.coerceIn(0.0, 1.0))) > MAX_CITY_DISTANCE_KM)
            return null
        val file = page.get("pageimage")?.asString ?: return null
        val url = page.getAsJsonObject("thumbnail")?.get("source")?.asString ?: return null
        if (!url.startsWith("https://") || file.endsWith(".svg", true)) return null
        return PhotoPage(title, file, url)
    }

    fun parseCredit(page: PhotoPage, root: JsonObject): CityPhoto? {
        val entry =
            root
                .getAsJsonObject("query")
                ?.getAsJsonArray("pages")
                ?.firstOrNull {
                    it.asJsonObject.get("title")?.asString?.replace('_', ' ') ==
                        "File:${page.file.replace('_',' ')}"
                }
                ?.asJsonObject ?: return null
        val info = entry.getAsJsonArray("imageinfo")?.firstOrNull()?.asJsonObject ?: return null
        val meta = info.getAsJsonObject("extmetadata") ?: return null
        fun field(key: String) = meta.getAsJsonObject(key)?.get("value")?.asString.orEmpty()
        val author = field("Artist")
        val license = field("LicenseShortName")
        val source = info.get("descriptionurl")?.asString.orEmpty()
        if (author.isBlank() || license.isBlank() || !source.startsWith("https://")) return null
        val licenseUrl = field("LicenseUrl").let { if (it.startsWith("//")) "https:$it" else it }
        return CityPhoto(page.title, page.url, author, license, source, licenseUrl)
    }
}
