package com.example.myapplication.domain

import com.example.myapplication.data.model.DayWeather
import com.example.myapplication.data.model.OutfitItem
import com.example.myapplication.data.model.OutfitRecommendation
import kotlin.math.roundToInt

object OutfitRecommender {

    /** Au-delà de cet écart min/max dans la journée, on conseille une couche amovible. */
    private const val LAYERING_SPREAD = 8.0

    /** Probabilité de pluie à partir de laquelle on sort le parapluie. */
    private const val RAIN_PROBABILITY_THRESHOLD = 0.5

    fun getOutfit(day: DayWeather): OutfitRecommendation {
        val mainOutfit = mutableListOf<OutfitItem>()
        val accessories = mutableListOf<OutfitItem>()
        val condition = day.condition.lowercase()
        // On s'habille pour la température ressentie, pas la température brute.
        val temp = day.feelsLike
        val windSpeed = day.windKmh
        val rainy = isRainy(day)

        when {
            temp < 0 -> {
                mainOutfit.add(OutfitItem("Heavy Winter Coat", "🧥", "Insulated & waterproof"))
                mainOutfit.add(OutfitItem("Thermal Layers", "🧣", "Base + mid layers"))
                mainOutfit.add(OutfitItem("Warm Boots", "🥾", "Insulated & waterproof"))
                mainOutfit.add(OutfitItem("Thick Wool Pants", "👖", "Wind-resistant"))
                accessories.add(OutfitItem("Warm Gloves", "🧤", "Thermal insulated"))
                accessories.add(OutfitItem("Beanie Hat", "🧢", "Keeps your head warm"))
                accessories.add(OutfitItem("Thick Scarf", "🧣", "Wrap up tight"))
            }
            temp < 5 -> {
                mainOutfit.add(OutfitItem("Heavy Coat", "🧥", "Warm & cozy"))
                mainOutfit.add(OutfitItem("Warm Sweater", "🧶", "Knit or fleece"))
                mainOutfit.add(OutfitItem("Warm Pants", "👖", "Thick fabric"))
                mainOutfit.add(OutfitItem("Winter Boots", "🥾", "Keep feet warm"))
                accessories.add(OutfitItem("Scarf", "🧣", "Essential warmth"))
                accessories.add(OutfitItem("Gloves", "🧤", "Protect your hands"))
            }
            temp < 12 -> {
                mainOutfit.add(OutfitItem("Jacket", "🧥", "Medium weight"))
                mainOutfit.add(OutfitItem("Hoodie", "👕", "Comfortable layer"))
                mainOutfit.add(OutfitItem("Jeans", "👖", "Classic fit"))
                mainOutfit.add(OutfitItem("Sneakers", "👟", "Comfortable shoes"))
                accessories.add(OutfitItem("Light Scarf", "🧣", "Optional warmth"))
            }
            temp < 20 -> {
                mainOutfit.add(OutfitItem("Light Jacket", "🧥", "For cool breezes"))
                mainOutfit.add(OutfitItem("Long Sleeve Shirt", "👔", "Breathable fabric"))
                mainOutfit.add(OutfitItem("Chinos", "👖", "Smart casual"))
                mainOutfit.add(OutfitItem("Casual Shoes", "👞", "Versatile footwear"))
            }
            temp < 28 -> {
                mainOutfit.add(OutfitItem("T-Shirt", "👕", "Light & breathable"))
                mainOutfit.add(OutfitItem("Shorts or Light Pants", "🩳", "Stay cool"))
                mainOutfit.add(OutfitItem("Sneakers", "👟", "Comfortable & airy"))
            }
            else -> {
                mainOutfit.add(OutfitItem("Tank Top", "🎽", "Maximum airflow"))
                mainOutfit.add(OutfitItem("Light Shorts", "🩳", "Stay breezy"))
                mainOutfit.add(OutfitItem("Sandals", "🩴", "Let your feet breathe"))
                accessories.add(OutfitItem("Sun Hat", "👒", "Shade from the sun"))
            }
        }

        if (needsLayering(day)) {
            accessories.add(
                OutfitItem(
                    "Removable Layer", "🧥",
                    "${day.tempMin.roundToInt()}° → ${day.tempMax.roundToInt()}° today"
                )
            )
        }

        if (rainy) {
            accessories.add(OutfitItem("Umbrella", "☂️", rainDescription(day)))
            accessories.add(OutfitItem("Rain Jacket", "🌧️", "Waterproof layer"))
            if (temp < 15) {
                accessories.add(OutfitItem("Waterproof Boots", "🥾", "Keep feet dry"))
            }
        }

        if (condition.contains("snow")) {
            accessories.add(OutfitItem("Snow Boots", "🥾", "Traction & warmth"))
            accessories.add(OutfitItem("Warm Hat", "🧢", "Cover your ears"))
        }

        if (windSpeed > 25) {
            accessories.add(OutfitItem("Windbreaker", "🌬️", "Block the strong winds"))
        } else if (windSpeed > 15) {
            accessories.add(OutfitItem("Light Windbreaker", "💨", "Some wind protection"))
        }

        if (condition.contains("clear") && !day.isNight) {
            accessories.add(OutfitItem("Sunglasses", "🕶️", "Protect your eyes"))
            if (temp > 20) {
                accessories.add(OutfitItem("Sunscreen", "🧴", "SPF 30+ recommended"))
            }
        }

        if (condition.contains("thunderstorm")) {
            accessories.add(OutfitItem("Sturdy Umbrella", "⛈️", "Heavy-duty protection"))
        }

        if (condition.contains("fog") || condition.contains("mist")) {
            accessories.add(OutfitItem("Reflective Gear", "🦺", "Be visible in fog"))
        }

        return OutfitRecommendation(mainOutfit, accessories, generateTip(day, temp, condition, rainy))
    }

    fun isRainy(day: DayWeather): Boolean {
        val condition = day.condition.lowercase()
        return condition.contains("rain") || condition.contains("drizzle") ||
            condition.contains("thunderstorm") ||
            (day.precipitationChance ?: 0.0) >= RAIN_PROBABILITY_THRESHOLD
    }

    fun needsLayering(day: DayWeather): Boolean =
        !day.isCurrent && day.tempMax - day.tempMin >= LAYERING_SPREAD

    private fun rainDescription(day: DayWeather): String =
        day.precipitationChance?.let { "${(it * 100).roundToInt()}% chance of rain" } ?: "Stay dry"

    private fun generateTip(day: DayWeather, temp: Double, condition: String, rainy: Boolean): String {
        val windSpeed = day.windKmh
        return when {
            temp < 0 -> "❄️ Extreme cold! Layer up heavily and limit time outdoors."
            temp < 5 && windSpeed > 20 -> "🥶 Wind chill makes it feel much colder. Bundle up tight!"
            temp < 5 -> "🧊 Freezing temperatures — dress in warm layers."
            condition.contains("thunderstorm") -> "⛈️ Thunderstorms expected. Stay indoors if possible."
            rainy && temp < 10 -> "🌧️ Cold rain — waterproof layers are a must."
            rainy -> "🌦️ Don't forget your umbrella! Rainy weather ahead."
            condition.contains("snow") -> "🌨️ Snowy conditions — wear insulated, waterproof gear."
            temp > 35 -> "🔥 Extreme heat! Stay hydrated and seek shade."
            temp > 28 -> "☀️ Hot weather — wear light fabrics and drink plenty of water."
            needsLayering(day) -> "🌡️ Big temperature swing today — dress in layers you can remove."
            condition.contains("clear") && day.isNight -> "🌙 Clear night — might get chilly, bring a light layer."
            condition.contains("clear") && temp > 20 -> "☀️ Beautiful day! Light clothing and sun protection."
            windSpeed > 25 -> "💨 Very windy today — secure loose items and wear a windbreaker."
            condition.contains("fog") -> "🌫️ Low visibility — wear bright or reflective clothing."
            else -> "👍 Enjoy your day! You're dressed for the weather."
        }
    }
}
