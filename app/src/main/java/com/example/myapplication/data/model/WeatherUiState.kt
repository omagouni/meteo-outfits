package com.example.myapplication.data.model

sealed class WeatherUiState {
    data object Idle : WeatherUiState()
    data object Loading : WeatherUiState()
    data class Success(
        val weather: WeatherResponse,
        val outfit: OutfitRecommendation,
        val isNight: Boolean
    ) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
}
