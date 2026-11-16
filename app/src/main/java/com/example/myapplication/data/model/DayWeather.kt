package com.example.myapplication.data.model

/**
 * Météo résumée pour un moment donné : soit "maintenant", soit une journée de prévision.
 * C'est l'unique entrée du moteur de recommandation de tenues.
 */
data class DayWeather(
    val label: String,
    /** Jour local (jours depuis l'epoch) ; null pour la météo actuelle. */
    val epochDay: Long?,
    val temp: Double,
    val tempMin: Double,
    val tempMax: Double,
    val feelsLike: Double,
    val humidity: Int,
    val windKmh: Double,
    /** Condition principale OpenWeatherMap : Clear, Clouds, Rain, Snow... */
    val condition: String,
    val description: String,
    /** Probabilité de pluie entre 0 et 1, null si inconnue. */
    val precipitationChance: Double?,
    val isNight: Boolean,
    val isCurrent: Boolean
)
