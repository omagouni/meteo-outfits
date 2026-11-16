package com.example.myapplication.domain

import com.example.myapplication.data.model.OutfitItem
import com.example.myapplication.data.model.OutfitRecommendation

object OutfitRecommender {

    fun getOutfit(temp: Double, condition: String, windSpeed: Double, isNight: Boolean): OutfitRecommendation {
        val mainOutfit = mutableListOf<OutfitItem>()
        val accessories = mutableListOf<OutfitItem>()
        val conditionLower = condition.lowercase()

        // Main outfit based on temperature
        when {
            temp < 0 -> {
                mainOutfit.add(OutfitItem("Heavy Winter Coat", "🧥", "Insulated & waterproof"))
                mainOutfit.add(OutfitItem("Thermal Layers", "🧣", "Base + mid layers"))
                mainOutfit.add(OutfitItem("Warm Boots", "🥾", "Insulated & waterproof"))
                mainOutfit.add(OutfitItem("Thick Wool Pants", "👖", "Wind-resistant"))
                accessories.add(OutfitItem("Warm Gloves", "🧤", "Thermal insulated"))
                accessories.add(OutfitItem("Beanie Hat", "🎩", "Keeps your head warm"))
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

        // Weather condition extras
        if (conditionLower.contains("rain") || conditionLower.contains("drizzle")) {
            accessories.add(OutfitItem("Umbrella", "☂️", "Stay dry"))
            accessories.add(OutfitItem("Rain Jacket", "🌧️", "Waterproof layer"))
            if (temp < 15) {
                accessories.add(OutfitItem("Waterproof Boots", "🥾", "Keep feet dry"))
            }
        }

        if (conditionLower.contains("snow")) {
            accessories.add(OutfitItem("Snow Boots", "🥾", "Traction & warmth"))
            accessories.add(OutfitItem("Warm Hat", "🎩", "Cover your ears"))
        }

        if (windSpeed > 25) {
            accessories.add(OutfitItem("Windbreaker", "🌬️", "Block the strong winds"))
        } else if (windSpeed > 15) {
            accessories.add(OutfitItem("Light Windbreaker", "💨", "Some wind protection"))
        }

        if (conditionLower.contains("clear") && !isNight) {
            accessories.add(OutfitItem("Sunglasses", "🕶️", "Protect your eyes"))
            if (temp > 20) {
                accessories.add(OutfitItem("Sunscreen", "🧴", "SPF 30+ recommended"))
            }
        }

        if (conditionLower.contains("thunderstorm")) {
            accessories.add(OutfitItem("Sturdy Umbrella", "⛈️", "Heavy-duty protection"))
        }

        if (conditionLower.contains("fog") || conditionLower.contains("mist")) {
            accessories.add(OutfitItem("Reflective Gear", "🦺", "Be visible in fog"))
        }

        // Tip
        val tip = generateTip(temp, conditionLower, windSpeed, isNight)

        return OutfitRecommendation(mainOutfit, accessories, tip)
    }

    private fun generateTip(temp: Double, condition: String, windSpeed: Double, isNight: Boolean): String {
        return when {
            temp < 0 -> "❄️ Extreme cold! Layer up heavily and limit time outdoors."
            temp < 5 && windSpeed > 20 -> "🥶 Wind chill makes it feel much colder. Bundle up tight!"
            temp < 5 -> "🧊 Freezing temperatures — dress in warm layers."
            condition.contains("thunderstorm") -> "⛈️ Thunderstorms expected. Stay indoors if possible."
            condition.contains("rain") && temp < 10 -> "🌧️ Cold rain — waterproof layers are a must."
            condition.contains("rain") -> "🌦️ Don't forget your umbrella! Rainy weather ahead."
            condition.contains("snow") -> "🌨️ Snowy conditions — wear insulated, waterproof gear."
            temp > 35 -> "🔥 Extreme heat! Stay hydrated and seek shade."
            temp > 28 -> "☀️ Hot weather — wear light fabrics and drink plenty of water."
            condition.contains("clear") && isNight -> "🌙 Clear night — might get chilly, bring a light layer."
            condition.contains("clear") && temp > 20 -> "☀️ Beautiful day! Light clothing and sun protection."
            windSpeed > 25 -> "💨 Very windy today — secure loose items and wear a windbreaker."
            condition.contains("fog") -> "🌫️ Low visibility — wear bright or reflective clothing."
            else -> "👍 Enjoy your day! You're dressed for the weather."
        }
    }
}
