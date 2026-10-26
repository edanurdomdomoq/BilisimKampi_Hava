package com.example.hava.data

import com.example.hava.model.City
import com.example.hava.model.CityPhoto
import com.google.gson.JsonObject
import java.io.IOException
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface WikimediaApi {
    @GET("w/api.php") suspend fun query(@QueryMap parameters: Map<String, String>): JsonObject
}

/** HTTP and provider-specific rules are kept outside the cache and UI. */
class WikimediaPhotoSource(
    private val wikipedia: WikimediaApi = ApiClient.wikipediaApi,
    private val commons: WikimediaApi = ApiClient.commonsApi,
) {
    suspend fun fetchBatch(cities: List<City>): Map<String, CityPhoto> {
        if (cities.isEmpty()) return emptyMap()
        val articles =
            wikipedia
                .query(
                    parameters(
                        "prop" to "pageimages|coordinates",
                        // The API otherwise returns coordinates for only 10 pages.
                        "colimit" to "max",
                        "piprop" to "name|thumbnail",
                        "pilicense" to "free",
                        "pithumbsize" to "960",
                        "redirects" to "1",
                        "titles" to cities.joinToString("|") { CityPhotoParser.articleTitle(it) },
                    )
                )
                .checked()
        val pages =
            cities.mapNotNull { city ->
                CityPhotoParser.parsePage(articles, city)?.let { city.plateCode to it }
            }
        val result = mutableMapOf<String, CityPhoto>()
        if (pages.isNotEmpty()) {
            val credits =
                commons
                    .query(
                        parameters(
                            "prop" to "imageinfo",
                            "iiprop" to "extmetadata|url",
                            "titles" to
                                pages.map { "File:${it.second.file}" }.distinct().joinToString("|"),
                        )
                    )
                    .checked()
            pages.forEach { (key, page) ->
                CityPhotoParser.parseCredit(page, credits)?.let { result[key] = it }
            }
        }
        if (cities.any { it.countryCode == "TR" && it.plateCode == "74" } && "74" !in result) {
            fetchBartinFallback()?.let { result["74"] = it }
        }
        return result
    }

    private suspend fun fetchBartinFallback(): CityPhoto? {
        // This Commons panorama is the lead image of the Turkish Bartın article.
        val file = "Bartin_SehirMerkezi.jpg"
        val response =
            commons
                .query(
                    parameters(
                        "prop" to "imageinfo",
                        "iiprop" to "extmetadata|url",
                        "iiurlwidth" to "960",
                        "titles" to "File:$file",
                    )
                )
                .checked()
        val info =
            response
                .getAsJsonObject("query")
                ?.getAsJsonArray("pages")
                ?.firstOrNull()
                ?.asJsonObject
                ?.getAsJsonArray("imageinfo")
                ?.firstOrNull()
                ?.asJsonObject
        val url = info?.get("thumburl")?.asString ?: return null
        return CityPhotoParser.parseCredit(PhotoPage("Bartın", file, url), response)
    }

    private fun parameters(vararg values: Pair<String, String>): Map<String, String> =
        mapOf("action" to "query", "format" to "json", "formatversion" to "2", "maxlag" to "5") +
            values

    private fun JsonObject.checked(): JsonObject {
        if (has("error")) throw IOException("Wikimedia API returned an error")
        return this
    }
}
