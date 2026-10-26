package com.example.hava.data

import android.content.Context
import android.util.Log
import com.example.hava.model.CityPhoto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** A process-wide metadata cache shared by the city screen and photo credits. */
object CityPhotoRepository {
    private const val BATCH_SIZE = 40
    private const val CACHE_LIFETIME_MS = 7 * 24 * 60 * 60 * 1000L
    private const val RETRY_DELAY_MS = 60_000L

    private val mutablePhotos = MutableStateFlow<Map<String, CityPhoto>>(emptyMap())
    val photos = mutablePhotos.asStateFlow()
    private val mutex = Mutex()
    private val lastAttempt = mutableMapOf<String, Long>()
    private val source = WikimediaPhotoSource()

    suspend fun load(context: Context, country: String) =
        withContext(Dispatchers.IO) {
            mutex.withLock {
                val cache = CityPhotoCache(context)
                val cached = cache.read(country)
                val cities = WorldCities.cities(country)
                mutablePhotos.value += cached.photos
                val now = System.currentTimeMillis()
                val completeCache = cached.photos.size == cities.size
                if (completeCache && now - cached.updatedAt < CACHE_LIFETIME_MS) return@withLock
                if (now - (lastAttempt[country] ?: 0) < RETRY_DELAY_MS) return@withLock
                lastAttempt[country] = now
                val result = cached.photos.toMutableMap()
                try {
                    for (batch in cities.chunked(BATCH_SIZE)) {
                        result += source.fetchBatch(batch)
                        mutablePhotos.value += result
                        // Persist progress, without marking a partly refreshed cache as fresh.
                        cache.write(country, result, updatedAt = 0)
                    }
                    if (result.isNotEmpty()) cache.write(country, result, updatedAt = now)
                } catch (exception: CancellationException) {
                    throw exception
                } catch (exception: Exception) {
                    Log.w(
                        "CityPhotoRepository",
                        "Photo refresh failed for $country; keeping cached photos",
                        exception,
                    )
                }
            }
        }
}
