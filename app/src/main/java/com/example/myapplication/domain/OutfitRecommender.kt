package com.example.myapplication.domain

import com.example.myapplication.R
import com.example.myapplication.data.model.DayWeather
import com.example.myapplication.data.model.OutfitItem
import com.example.myapplication.data.model.OutfitRecommendation
import com.example.myapplication.ui.UiText
import kotlin.math.roundToInt

/** Suggestion générique (sans garde-robe) : quoi porter pour une journée donnée. */
object OutfitRecommender {

    /** Au-delà de cet écart min/max dans la journée, on conseille une couche amovible. */
    private const val LAYERING_SPREAD = 8.0

    /** Probabilité de pluie à partir de laquelle on sort le parapluie. */
    private const val RAIN_PROBABILITY_THRESHOLD = 0.5

    private fun item(nameRes: Int, emoji: String, descRes: Int, vararg descArgs: Any) =
        OutfitItem(UiText.res(nameRes), emoji, UiText.res(descRes, *descArgs))

    fun getOutfit(day: DayWeather): OutfitRecommendation {
        val main = mutableListOf<OutfitItem>()
        val extras = mutableListOf<OutfitItem>()
        val condition = day.condition.lowercase()
        // On s'habille pour la température ressentie, pas la température brute.
        val temp = day.feelsLike
        val windSpeed = day.windKmh
        val rainy = isRainy(day)
        val t = temp.roundToInt()

        when {
            temp < 0 -> {
                main += item(R.string.oi_heavy_winter_coat, "🧥", R.string.oi_heavy_winter_coat_d)
                main += item(R.string.oi_thermal_layers, "🧣", R.string.oi_thermal_layers_d)
                main += item(R.string.oi_warm_boots, "🥾", R.string.oi_warm_boots_d)
                main += item(R.string.oi_thick_wool_pants, "👖", R.string.oi_thick_wool_pants_d)
                extras += item(R.string.oi_warm_gloves, "🧤", R.string.oi_warm_gloves_d)
                extras += item(R.string.oi_beanie, "🧢", R.string.oi_beanie_d)
                extras += item(R.string.oi_thick_scarf, "🧣", R.string.oi_thick_scarf_d)
            }
            temp < 5 -> {
                main += item(R.string.oi_heavy_coat, "🧥", R.string.oi_heavy_coat_d)
                main += item(R.string.oi_warm_sweater, "🧶", R.string.oi_warm_sweater_d)
                main += item(R.string.oi_warm_pants, "👖", R.string.oi_warm_pants_d)
                main += item(R.string.oi_winter_boots, "🥾", R.string.oi_winter_boots_d)
                extras += item(R.string.oi_scarf, "🧣", R.string.oi_scarf_d)
                extras += item(R.string.oi_gloves, "🧤", R.string.oi_gloves_d)
            }
            temp < 12 -> {
                main += item(R.string.oi_jacket, "🧥", R.string.oi_jacket_d)
                main += item(R.string.oi_hoodie, "👕", R.string.oi_hoodie_d)
                main += item(R.string.oi_jeans, "👖", R.string.oi_jeans_d)
                main += item(R.string.oi_sneakers, "👟", R.string.oi_sneakers_d)
                extras += item(R.string.oi_light_scarf, "🧣", R.string.oi_light_scarf_d)
            }
            temp < 20 -> {
                main += item(R.string.oi_light_jacket, "🧥", R.string.oi_light_jacket_d)
                main += item(R.string.oi_long_sleeve, "👔", R.string.oi_long_sleeve_d)
                main += item(R.string.oi_chinos, "👖", R.string.oi_chinos_d)
                main += item(R.string.oi_casual_shoes, "👞", R.string.oi_casual_shoes_d)
            }
            temp < 28 -> {
                main += item(R.string.oi_tshirt, "👕", R.string.oi_tshirt_d)
                main += item(R.string.oi_shorts_or_light, "🩳", R.string.oi_shorts_or_light_d)
                main += item(R.string.oi_sneakers, "👟", R.string.oi_sneakers_airy_d)
            }
            else -> {
                main += item(R.string.oi_tank_top, "🎽", R.string.oi_tank_top_d)
                main += item(R.string.oi_light_shorts, "🩳", R.string.oi_light_shorts_d)
                main += item(R.string.oi_sandals, "🩴", R.string.oi_sandals_d)
                extras += item(R.string.oi_sun_hat, "👒", R.string.oi_sun_hat_d)
            }
        }

        if (needsLayering(day)) {
            extras += item(
                R.string.oi_removable_layer, "🧥", R.string.oi_removable_layer_d,
                day.tempMin.roundToInt(), day.tempMax.roundToInt()
            )
        }

        if (rainy) {
            extras += OutfitItem(UiText.res(R.string.oi_umbrella), "☂️", rainDescription(day))
            extras += item(R.string.oi_rain_jacket, "🌧️", R.string.oi_rain_jacket_d)
            if (temp < 15) extras += item(R.string.oi_waterproof_boots, "🥾", R.string.oi_waterproof_boots_d)
        }

        if (condition.contains("snow")) {
            extras += item(R.string.oi_snow_boots, "🥾", R.string.oi_snow_boots_d)
            extras += item(R.string.oi_warm_hat, "🧢", R.string.oi_warm_hat_d)
        }

        if (windSpeed > 25) extras += item(R.string.oi_windbreaker, "🌬️", R.string.oi_windbreaker_d)
        else if (windSpeed > 15) extras += item(R.string.oi_light_windbreaker, "💨", R.string.oi_light_windbreaker_d)

        if (condition.contains("clear") && !day.isNight) {
            extras += item(R.string.oi_sunglasses, "🕶️", R.string.oi_sunglasses_d)
            if (temp > 20) extras += item(R.string.oi_sunscreen, "🧴", R.string.oi_sunscreen_d)
        }

        if (condition.contains("thunderstorm")) extras += item(R.string.oi_sturdy_umbrella, "⛈️", R.string.oi_sturdy_umbrella_d)
        if (condition.contains("fog") || condition.contains("mist")) extras += item(R.string.oi_reflective, "🦺", R.string.oi_reflective_d)

        return OutfitRecommendation(main, extras, UiText.res(tipRes(day, temp, condition, rainy)))
    }

    fun isRainy(day: DayWeather): Boolean {
        val condition = day.condition.lowercase()
        return condition.contains("rain") || condition.contains("drizzle") ||
            condition.contains("thunderstorm") ||
            (day.precipitationChance ?: 0.0) >= RAIN_PROBABILITY_THRESHOLD
    }

    fun needsLayering(day: DayWeather): Boolean =
        !day.isCurrent && day.tempMax - day.tempMin >= LAYERING_SPREAD

    fun rainDescription(day: DayWeather): UiText =
        day.precipitationChance?.let { UiText.res(R.string.reason_rain_chance, (it * 100).roundToInt()) }
            ?: UiText.res(R.string.oi_umbrella_d)

    fun tipRes(day: DayWeather, temp: Double, condition: String, rainy: Boolean): Int {
        val windSpeed = day.windKmh
        return when {
            temp < 0 -> R.string.tip_extreme_cold
            temp < 5 && windSpeed > 20 -> R.string.tip_wind_chill
            temp < 5 -> R.string.tip_freezing
            condition.contains("thunderstorm") -> R.string.tip_thunderstorm
            rainy && temp < 10 -> R.string.tip_cold_rain
            rainy -> R.string.tip_rain
            condition.contains("snow") -> R.string.tip_snow
            temp > 35 -> R.string.tip_extreme_heat
            temp > 28 -> R.string.tip_hot
            needsLayering(day) -> R.string.tip_layering
            condition.contains("clear") && day.isNight -> R.string.tip_clear_night
            condition.contains("clear") && temp > 20 -> R.string.tip_beautiful_day
            windSpeed > 25 -> R.string.tip_windy
            condition.contains("fog") -> R.string.tip_fog
            else -> R.string.tip_default
        }
    }
}
