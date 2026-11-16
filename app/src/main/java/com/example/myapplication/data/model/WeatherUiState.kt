package com.example.myapplication.data.model

import com.example.myapplication.domain.WardrobeOutfit
import com.example.myapplication.ui.UiText

/** État du styliste IA pour la tenue affichée. */
sealed class AiStatus {
    /** IA désactivée ou non configurée : tenue calculée par les règles. */
    data object Off : AiStatus()
    /** Tenue des règles affichée en attendant la réponse de l'IA. */
    data object Loading : AiStatus()
    data object Ready : AiStatus()
    data object Failed : AiStatus()
}

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
        val canWearToday: Boolean = false,
        val aiStatus: AiStatus = AiStatus.Off
    ) : WeatherUiState() {
        val selectedDay: DayWeather get() = days[selectedIndex]
    }
    data class Error(val message: UiText) : WeatherUiState()
}
