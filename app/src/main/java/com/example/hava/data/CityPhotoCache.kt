package com.example.hava.data

import android.content.Context
import com.example.hava.model.CityPhoto
import com.google.gson.Gson

internal data class CachedCityPhotos(val photos: Map<String, CityPhoto>, val updatedAt: Long)

/** Small metadata only. Image bytes are managed separately by Coil. */
internal class CityPhotoCache(context: Context) {
    private val preferences =
        context.applicationContext.getSharedPreferences("city_photos_v2", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun read(country: String): CachedCityPhotos {
        val entries =
            runCatching {
                    gson
                        .fromJson(preferences.getString(country, null), Array<Entry>::class.java)
                        ?.toList()
                }
                .getOrNull()
                .orEmpty()
        return CachedCityPhotos(
            entries.associate { it.key to it.photo },
            preferences.getLong("${country}_time", 0),
        )
    }

    fun write(country: String, photos: Map<String, CityPhoto>, updatedAt: Long) {
        val entries = photos.map { Entry(it.key, it.value) }
        preferences
            .edit()
            .putString(country, gson.toJson(entries))
            .putLong("${country}_time", updatedAt)
            .apply()
    }

    private data class Entry(val key: String, val photo: CityPhoto)
}
