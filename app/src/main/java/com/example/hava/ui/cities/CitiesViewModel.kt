package com.example.hava.ui.cities

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hava.AppContainer
import com.example.hava.data.CityPhotoRepository
import com.example.hava.data.WeatherRepository
import com.example.hava.model.City
import com.example.hava.model.CurrentWeather
import com.example.hava.ui.LoadState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

/** Weather requests belong to the screen lifetime, not recycled grid cards. */
class CitiesViewModel(private val repository: WeatherRepository = AppContainer.weatherRepository) :
    ViewModel() {
    var weather: Map<String, LoadState<CurrentWeather>> by mutableStateOf(emptyMap())
        private set

    val photos = CityPhotoRepository.photos
    private var photoJob: Job? = null
    private val requests = Semaphore(3)

    fun loadPhotos(context: Context, country: String) {
        photoJob?.cancel()
        photoJob =
            viewModelScope.launch { CityPhotoRepository.load(context.applicationContext, country) }
    }

    fun loadWeather(cities: List<City>) {
        cities.forEach { city -> if (city.plateCode !in weather) loadCity(city) }
    }

    fun retry(city: City) {
        if (weather[city.plateCode] == LoadState.Error) loadCity(city)
    }

    private fun loadCity(city: City) {
        setState(city, LoadState.Loading)
        viewModelScope.launch {
            try {
                val current =
                    requests.withPermit {
                        repository.getForecast(city.latitude, city.longitude).current
                    }
                setState(city, LoadState.Success(current))
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                setState(city, LoadState.Error)
            }
        }
    }

    private fun setState(city: City, state: LoadState<CurrentWeather>) {
        weather = weather + (city.plateCode to state)
    }
}
