package com.example.myapplication.data.model

import com.example.myapplication.domain.WardrobeOutfit

sealed class WeatherUiState {
    data object Idle : WeatherUiState()
    data object Loading : WeatherUiState()
    data class Success(
        val cityLabel: String,
        /** Index 0 = maintenant, puis un élément par jour de prévision. */
        val days: List<DayWeather>,
        val selectedIndex: Int,
        val outfit: OutfitRecommendation,
        /** Tenue tirée de la garde-robe ; null si la garde-robe est vide. */
        val wardrobeOutfit: WardrobeOutfit? = null,
        /** Vrai si le jour sélectionné est aujourd'hui (on peut "porter" la tenue). */
        val canWearToday: Boolean = false
    ) : WeatherUiState() {
        val selectedDay: DayWeather get() = days[selectedIndex]
    }
    data class Error(val message: String) : WeatherUiState()
}
