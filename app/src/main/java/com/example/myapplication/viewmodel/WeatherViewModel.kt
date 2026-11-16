package com.example.myapplication.viewmodel

import android.app.Application
import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.WardrobeRepository
import com.example.myapplication.data.model.DayWeather
import com.example.myapplication.data.model.WeatherUiState
import com.example.myapplication.data.remote.RetrofitClient
import com.example.myapplication.domain.ForecastAggregator
import com.example.myapplication.domain.LocalDays
import com.example.myapplication.domain.OutfitPlanner
import com.example.myapplication.domain.OutfitRecommender
import com.example.myapplication.domain.RuleBasedOutfitPlanner
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.net.UnknownHostException

class WeatherViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("weather_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Idle)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private val _city = MutableStateFlow(prefs.getString(KEY_CITY, DEFAULT_CITY) ?: DEFAULT_CITY)
    val city: StateFlow<String> = _city.asStateFlow()

    private val wardrobe = WardrobeRepository.get(application)
    private val planner: OutfitPlanner = RuleBasedOutfitPlanner

    private var fetchJob: Job? = null

    init {
        fetchWeather()
        // Recalcule la tenue dès que la garde-robe change.
        viewModelScope.launch {
            wardrobe.items.collect {
                _uiState.update { state -> if (state is WeatherUiState.Success) withOutfits(state) else state }
            }
        }
    }

    /** Recalcule les deux tenues (générique + garde-robe) pour le jour sélectionné. */
    private fun withOutfits(state: WeatherUiState.Success): WeatherUiState.Success {
        val day = state.selectedDay
        val today = LocalDays.today()
        val items = wardrobe.items.value
        return state.copy(
            outfit = OutfitRecommender.getOutfit(day),
            wardrobeOutfit = if (items.isEmpty()) null
            else planner.plan(day, items, referenceDay = day.epochDay ?: today),
            canWearToday = day.isCurrent || day.epochDay == today
        )
    }

    /** Enregistre que l'utilisateur porte aujourd'hui la tenue proposée. */
    fun wearSuggestedOutfit() {
        val state = _uiState.value as? WeatherUiState.Success ?: return
        val ids = state.wardrobeOutfit?.picks?.map { it.item.id }.orEmpty()
        if (!state.canWearToday || ids.isEmpty()) return
        viewModelScope.launch { wardrobe.markWorn(ids, LocalDays.today()) }
    }

    fun updateCity(newCity: String) {
        _city.value = newCity
    }

    fun selectDay(index: Int) {
        _uiState.update { state ->
            if (state is WeatherUiState.Success && index in state.days.indices) {
                withOutfits(state.copy(selectedIndex = index))
            } else state
        }
    }

    fun fetchWeather() {
        val cityName = _city.value.trim()
        if (cityName.isBlank()) {
            _uiState.value = WeatherUiState.Error("Please enter a city name")
            return
        }
        if (RetrofitClient.apiKey.isBlank()) {
            _uiState.value = WeatherUiState.Error(
                "Missing API key. Add WEATHER_API_KEY to local.properties and rebuild."
            )
            return
        }

        // Garde l'onglet sélectionné si on rafraîchit la même ville.
        val previousIndex = (_uiState.value as? WeatherUiState.Success)?.selectedIndex ?: 0
        _uiState.value = WeatherUiState.Loading

        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            try {
                val (label, days) = loadDays(cityName)
                val index = previousIndex.coerceIn(0, days.lastIndex)
                prefs.edit { putString(KEY_CITY, cityName) }
                _uiState.value = withOutfits(
                    WeatherUiState.Success(
                        cityLabel = label,
                        days = days,
                        selectedIndex = index,
                        outfit = OutfitRecommender.getOutfit(days[index])
                    )
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = WeatherUiState.Error(errorMessage(e))
            }
        }
    }

    /** Charge météo actuelle + prévisions en parallèle. Les prévisions sont optionnelles. */
    private suspend fun loadDays(cityName: String): Pair<String, List<DayWeather>> = coroutineScope {
        val api = RetrofitClient.weatherApi
        val forecastDeferred = async { runCatching { api.getForecast(cityName) } }
        val current = api.getWeather(cityName)
        val forecastDays = forecastDeferred.await()
            .getOrNull()
            ?.let { ForecastAggregator.dailyFrom(it) }
            .orEmpty()

        val label = "${current.cityName}, ${current.sys.country}"
        label to (listOf(ForecastAggregator.fromCurrent(current)) + forecastDays)
    }

    private fun errorMessage(e: Exception): String = when (e) {
        is HttpException -> when (e.code()) {
            404 -> "City not found. Please check the spelling."
            401 -> "Invalid API key. Please update WEATHER_API_KEY in local.properties."
            429 -> "Too many requests. Please try again in a minute."
            else -> "Weather service error (${e.code()})."
        }
        is UnknownHostException -> "No internet connection. Please check your network."
        is IOException -> "Network problem. Please try again."
        else -> "Something went wrong: ${e.localizedMessage ?: "Unknown error"}"
    }

    private companion object {
        const val KEY_CITY = "last_city"
        const val DEFAULT_CITY = "Paris"
    }
}
