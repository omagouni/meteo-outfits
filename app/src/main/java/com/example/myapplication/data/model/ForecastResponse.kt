package com.example.myapplication.data.model

import com.google.gson.annotations.SerializedName

/** Réponse de l'endpoint OpenWeatherMap `data/2.5/forecast` (pas de 3 h sur 5 jours). */
data class ForecastResponse(
    @SerializedName("list") val list: List<ForecastEntry>,
    @SerializedName("city") val city: ForecastCity
)

data class ForecastEntry(
    @SerializedName("dt") val dt: Long,
    @SerializedName("main") val main: Main,
    @SerializedName("weather") val weather: List<Weather>,
    @SerializedName("wind") val wind: Wind,
    /** Probabilité de précipitations, entre 0 et 1. */
    @SerializedName("pop") val pop: Double = 0.0
)

data class ForecastCity(
    @SerializedName("name") val name: String,
    @SerializedName("country") val country: String,
    @SerializedName("timezone") val timezone: Int,
    @SerializedName("sunrise") val sunrise: Long,
    @SerializedName("sunset") val sunset: Long
)
