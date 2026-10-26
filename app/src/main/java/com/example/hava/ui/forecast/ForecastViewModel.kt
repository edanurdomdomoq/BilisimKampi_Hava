package com.example.hava.ui.forecast

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hava.AppContainer
import com.example.hava.data.WeatherRepository
import com.example.hava.model.Forecast
import com.example.hava.ui.LoadState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ForecastViewModel(
    private val repository: WeatherRepository = AppContainer.weatherRepository
) : ViewModel() {
    var uiState: LoadState<Forecast> by mutableStateOf(LoadState.Empty)
        private set

    private var requestJob: Job? = null
    private var coordinates: Pair<Double, Double>? = null
    private var generation = 0

    fun loadForecast(latitude: Double, longitude: Double) {
        val requestedCoordinates = latitude to longitude
        if (
            coordinates == requestedCoordinates &&
                (uiState is LoadState.Success || uiState == LoadState.Loading)
        )
            return
        coordinates = requestedCoordinates
        val requestGeneration = ++generation
        requestJob?.cancel()
        uiState = LoadState.Loading
        requestJob =
            viewModelScope.launch {
                try {
                    val forecast = repository.getForecast(latitude, longitude)
                    // Even a source that ignores cancellation cannot overwrite a newer city.
                    if (requestGeneration == generation) uiState = LoadState.Success(forecast)
                } catch (exception: CancellationException) {
                    throw exception
                } catch (exception: Exception) {
                    Log.w(
                        "ForecastViewModel",
                        "Forecast request failed: ${exception.javaClass.simpleName}: ${exception.message}",
                        exception,
                    )
                    if (requestGeneration == generation) uiState = LoadState.Error
                }
            }
    }
}
