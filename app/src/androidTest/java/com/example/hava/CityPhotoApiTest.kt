package com.example.hava

import androidx.test.platform.app.InstrumentationRegistry
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import com.example.hava.data.CityPhotoRepository
import com.example.hava.data.WorldCities
import com.example.hava.ui.components.CityPhotoImageLoader
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class CityPhotoApiTest {
    @Test
    fun liveWikimediaPhotoHasCreditsAndDecodes() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        // Exercise the new HTTP path, rather than relying on metadata from an earlier run.
        context
            .getSharedPreferences("city_photos_v2", android.content.Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
        CityPhotoRepository.load(context, "DE")
        for (country in WorldCities.countries) CityPhotoRepository.load(context, country.code)
        val missing =
            WorldCities.countries
                .flatMap { WorldCities.cities(it.code) }
                .filter { it.plateCode !in CityPhotoRepository.photos.value }
        java.io
            .File(context.getExternalFilesDir(null), "photo-api-report.txt")
            .writeText(
                "Photos: ${CityPhotoRepository.photos.value.size}/109; missing: ${missing.map {it.name}}"
            )
        assertTrue("Missing city photos: ${missing.map {it.name}}", missing.isEmpty())
        val photo = CityPhotoRepository.photos.value["DE-berlin"]
        assertNotNull("Live Wikimedia Berlin photo", photo)
        assertTrue(photo!!.author.isNotBlank() && photo.license.isNotBlank())
        val loader = CityPhotoImageLoader.get(context)
        run {
            val result =
                loader.execute(ImageRequest.Builder(context).data(photo.url).size(960, 400).build())
            assertTrue("Remote photo should decode: $result", result is SuccessResult)
            CityPhotoRepository.load(context, "DE")
            assertEquals(photo, CityPhotoRepository.photos.value["DE-berlin"])
        }
    }
}
