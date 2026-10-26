package com.example.hava.model

object WeatherCondition {

    fun getDescription(code: Int): String {
        return when (code) {
            0 -> "Açık ve güneşli"
            1 -> "Az bulutlu"
            2 -> "Parçalı bulutlu"
            3 -> "Kapalı"
            45,
            48 -> "Sisli"
            51,
            53,
            55 -> "Çisenti"
            56,
            57 -> "Dondurucu çisenti"
            61,
            63,
            65 -> "Yağmurlu"
            66,
            67 -> "Dondurucu yağmur"
            71,
            73,
            75 -> "Karlı"
            77 -> "Kar taneleri"
            80,
            81,
            82 -> "Sağanak yağışlı"
            85,
            86 -> "Kar sağanakları"
            95 -> "Gök gürültülü fırtına"
            96,
            99 -> "Dolu ve gök gürültülü fırtına"
            else -> "Bilinmeyen hava durumu"
        }
    }
}
