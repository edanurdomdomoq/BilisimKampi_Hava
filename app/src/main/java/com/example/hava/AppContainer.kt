package com.example.hava

import com.example.hava.data.WeatherRepository

/** App-scoped dependencies. Constructors can still receive a repository directly. */
object AppContainer {
    var weatherRepository: WeatherRepository = WeatherRepository()
        internal set
}
