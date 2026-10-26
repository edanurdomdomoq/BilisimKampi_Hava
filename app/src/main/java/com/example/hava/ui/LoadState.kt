package com.example.hava.ui

/** A single state prevents contradictory loading / error / data flags. */
sealed interface LoadState<out T> {
    data object Loading : LoadState<Nothing>

    data class Success<T>(val data: T) : LoadState<T>

    data object Empty : LoadState<Nothing>

    data object Error : LoadState<Nothing>
}
