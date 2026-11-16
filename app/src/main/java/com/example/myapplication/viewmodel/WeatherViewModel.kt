package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.WeatherUiState
import com.example.myapplication.data.remote.RetrofitClient
import com.example.myapplication.data.remote.WeatherApiService
import com.example.myapplication.domain.OutfitRecommender
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WeatherViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Idle)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private val _city = MutableStateFlow("Paris")
    val city: StateFlow<String> = _city.asStateFlow()

    init {
        fetchWeather()
    }

    fun updateCity(newCity: String) {
        _city.value = newCity
    }

    fun fetchWeather() {
        val cityName = _city.value.trim()
        if (cityName.isBlank()) {
            _uiState.value = WeatherUiState.Error("Please enter a city name")
            return
        }

        _uiState.value = WeatherUiState.Loading

        viewModelScope.launch {
            try {
                val response = RetrofitClient.weatherApi.getWeather(
                    city = cityName,
                    apiKey = WeatherApiService.API_KEY
                )

                val currentTime = System.currentTimeMillis() / 1000
                val isNight = currentTime < response.sys.sunrise + response.timezone ||
                        currentTime > response.sys.sunset + response.timezone

                val mainCondition = response.weather.firstOrNull()?.main ?: "Clear"
                val windSpeedKmh = response.wind.speed * 3.6 // Convert m/s to km/h

                val outfit = OutfitRecommender.getOutfit(
                    temp = response.main.temp,
                    condition = mainCondition,
                    windSpeed = windSpeedKmh,
                    isNight = isNight
                )

                _uiState.value = WeatherUiState.Success(
                    weather = response,
                    outfit = outfit,
                    isNight = isNight
                )
            } catch (e: Exception) {
                val errorMsg = when {
                    e.message?.contains("Unable to resolve host") == true ->
                        "No internet connection. Please check your network."
                    e.message?.contains("404") == true ->
                        "City not found. Please check the spelling."
                    e.message?.contains("401") == true ->
                        "Invalid API key. Please update your API key."
                    else -> "Something went wrong: ${e.localizedMessage ?: "Unknown error"}"
                }
                _uiState.value = WeatherUiState.Error(errorMsg)
            }
        }
    }
}
