package com.example.hava.data

import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/** Shared connection pools and timeouts; Wikimedia also requires an identifying User-Agent. */
object ApiClient {
    private val httpClient =
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .build()

    val wikimediaHttpClient: OkHttpClient by lazy {
        httpClient
            .newBuilder()
            .addInterceptor { chain ->
                val request =
                    chain
                        .request()
                        .newBuilder()
                        .header(
                            "User-Agent",
                            "HAVA-Android/1.0 (city weather photo viewer; Wikimedia API)",
                        )
                        .build()
                chain.proceed(request)
            }
            .build()
    }
    val cityApi: CityApi by lazy {
        service("https://geocoding-api.open-meteo.com/", CityApi::class.java)
    }
    val forecastApi: ForecastApi by lazy {
        service("https://api.open-meteo.com/", ForecastApi::class.java)
    }
    val wikipediaApi: WikimediaApi by lazy {
        service("https://en.wikipedia.org/", WikimediaApi::class.java, wikimediaHttpClient)
    }
    val commonsApi: WikimediaApi by lazy {
        service("https://commons.wikimedia.org/", WikimediaApi::class.java, wikimediaHttpClient)
    }

    private fun <T> service(baseUrl: String, type: Class<T>, client: OkHttpClient = httpClient): T =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(type)
}
