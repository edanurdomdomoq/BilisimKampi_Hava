package com.example.hava.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.hava.model.City
import com.example.hava.model.WeatherCondition
import java.util.Locale

val LocalAppLanguage = staticCompositionLocalOf { "tr" }

@Composable
fun localized(turkish: String, english: String): String =
    if (LocalAppLanguage.current == "en") english else turkish

@Composable
fun City.displayName(): String = if (LocalAppLanguage.current == "en") englishName else name

fun weatherDescription(code: Int, language: String): String =
    if (language != "en") WeatherCondition.getDescription(code)
    else
        when (code) {
            0 -> "Clear"
            1 -> "Mostly clear"
            2 -> "Partly cloudy"
            3 -> "Cloudy"
            45,
            48 -> "Foggy"
            51,
            53,
            55 -> "Drizzle"
            56,
            57 -> "Freezing drizzle"
            61,
            63,
            65,
            80,
            81,
            82 -> "Rainy"
            66,
            67 -> "Freezing rain"
            71,
            73,
            75,
            77,
            85,
            86 -> "Snowy"
            95,
            96,
            99 -> "Thunderstorms"
            else -> "Unknown conditions"
        }

fun normalizedSearch(value: String): String =
    java.text.Normalizer.normalize(
            value.lowercase(Locale.ROOT).replace('ı', 'i'),
            java.text.Normalizer.Form.NFD,
        )
        .replace("\\p{M}+".toRegex(), "")
        .trim()
