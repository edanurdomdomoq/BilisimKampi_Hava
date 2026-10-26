package com.example.hava.ui.components

import android.content.Context
import coil3.ImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.example.hava.data.ApiClient

/** One image cache, with the same Wikimedia identification headers as metadata requests. */
object CityPhotoImageLoader {
    private var loader: ImageLoader? = null

    fun get(context: Context): ImageLoader =
        synchronized(this) {
            loader
                ?: ImageLoader.Builder(context.applicationContext)
                    .components {
                        add(
                            OkHttpNetworkFetcherFactory(
                                callFactory = { ApiClient.wikimediaHttpClient }
                            )
                        )
                    }
                    .build()
                    .also { loader = it }
        }
}
