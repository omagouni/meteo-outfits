package com.example.myapplication.data.model

sealed class WeatherUiState {
    data object Idle : WeatherUiState()
    data object Loading : WeatherUiState()
    data class Success(
        val cityLabel: String,
        /** Index 0 = maintenant, puis un élément par jour de prévision. */
        val days: List<DayWeather>,
        val selectedIndex: Int,
        val outfit: OutfitRecommendation
    ) : WeatherUiState() {
        val selectedDay: DayWeather get() = days[selectedIndex]
    }
    data class Error(val message: String) : WeatherUiState()
}
