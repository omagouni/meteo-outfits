package com.example.myapplication.domain

import com.example.myapplication.data.model.ClothingCategory
import com.example.myapplication.data.model.ClothingItem
import com.example.myapplication.data.model.DayWeather
import com.example.myapplication.ui.UiText
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlin.math.roundToInt

/**
 * Construction du prompt et lecture de la réponse du styliste IA.
 * Volontairement sans dépendance au SDK pour pouvoir être testé en JVM pur.
 */
object ClaudeOutfitPrompt {

    private const val MAX_ACCESSORIES = 2
    private const val MAX_REASON_LENGTH = 90
    private val gson = Gson()

    val SYSTEM_PROMPT = """
        You are the stylist inside a weather-based outfit app. The user gives you the weather for one day
        and their wardrobe as JSON. Choose what they should wear that day from their own clothes only.

        How to choose:
        - Dress for the "feels like" temperature, rain chance and wind. A coat or jacket is usually needed
          below about 18°C, when rain is likely, or when it is windy; prefer waterproof pieces when rain is likely.
        - At most one item per category (OUTERWEAR, TOP, BOTTOM, SHOES), plus up to $MAX_ACCESSORIES ACCESSORY items,
          and only when they help (umbrella for rain, scarf or gloves in the cold).
        - Make the colors and styles go together.
        - Items with "wornToday": true are what the user is already wearing: keep them in the outfit.
          Otherwise avoid items worn 1 or 2 days ago when another suitable item exists.
        - Prefer favorites when it is a close call.
        - Use only "id" values from the wardrobe, copied exactly.

        For each pick, give a reason of at most 10 words that refers to the weather or to the rest of the outfit.
        In "missing", list each category the day needs but that no wardrobe item covers well, with the warmth
        (1 = very light ... 5 = very warm) and whether it should be waterproof. Use an empty list if nothing is missing.
        "tip" is one short, friendly styling sentence for the day. Write in English.
    """.trimIndent()

    /** Schéma JSON imposé à la réponse (sortie structurée). */
    val RESPONSE_SCHEMA: Map<String, Any> = mapOf(
        "type" to "object",
        "additionalProperties" to false,
        "required" to listOf("picks", "missing", "tip"),
        "properties" to mapOf(
            "picks" to mapOf(
                "type" to "array",
                "items" to mapOf(
                    "type" to "object",
                    "additionalProperties" to false,
                    "required" to listOf("id", "reason"),
                    "properties" to mapOf(
                        "id" to mapOf("type" to "string"),
                        "reason" to mapOf("type" to "string")
                    )
                )
            ),
            "missing" to mapOf(
                "type" to "array",
                "items" to mapOf(
                    "type" to "object",
                    "additionalProperties" to false,
                    "required" to listOf("category", "warmth", "waterproof"),
                    "properties" to mapOf(
                        "category" to mapOf("type" to "string", "enum" to ClothingCategory.entries.map { it.name }),
                        "warmth" to mapOf("type" to "integer"),
                        "waterproof" to mapOf("type" to "boolean")
                    )
                )
            ),
            "tip" to mapOf("type" to "string")
        )
    )

    fun userMessage(day: DayWeather, wardrobe: List<ClothingItem>, referenceDay: Long): String {
        val weather = linkedMapOf<String, Any>(
            "day" to day.label,
            "feelsLikeC" to day.feelsLike.roundToInt(),
            "minC" to day.tempMin.roundToInt(),
            "maxC" to day.tempMax.roundToInt(),
            "condition" to day.description.ifBlank { day.condition },
            "windKmh" to day.windKmh.roundToInt(),
            "isNight" to day.isNight
        )
        day.precipitationChance?.let { weather["rainChancePercent"] = (it * 100).roundToInt() }

        val items = wardrobe.map { item ->
            linkedMapOf<String, Any>(
                "id" to item.id,
                "name" to item.name,
                "category" to item.category.name,
                "warmth" to item.warmth,
                "waterproof" to item.waterproof,
                "windproof" to item.windproof
            ).apply {
                if (item.color.isNotBlank()) put("color", item.color)
                if (item.brand.isNotBlank()) put("brand", item.brand)
                if (item.favorite) put("favorite", true)
                item.lastWornEpochDay?.let { last ->
                    val daysAgo = referenceDay - last
                    if (daysAgo == 0L) put("wornToday", true)
                    else if (daysAgo > 0) put("lastWornDaysAgo", daysAgo)
                }
            }
        }
        return "Weather:\n${gson.toJson(weather)}\n\nWardrobe:\n${gson.toJson(items)}"
    }

    private class Response(val picks: List<Pick>?, val missing: List<Missing>?, val tip: String?)
    private class Pick(val id: String?, val reason: String?)
    private class Missing(val category: String?, val warmth: Int?, val waterproof: Boolean?)

    /**
     * Transforme le JSON renvoyé en [WardrobeOutfit] en gardant uniquement ce qui est valide :
     * ids connus, une pièce par catégorie (hors accessoires), catégories manquantes réellement vides.
     */
    fun parse(json: String, wardrobe: List<ClothingItem>): WardrobeOutfit {
        val response = try {
            gson.fromJson(json, Response::class.java)
        } catch (e: JsonSyntaxException) {
            throw IllegalStateException("Invalid AI response", e)
        } ?: throw IllegalStateException("Empty AI response")

        val byId = wardrobe.associateBy { it.id }
        val usedCategories = mutableSetOf<ClothingCategory>()
        var accessories = 0
        val picks = response.picks.orEmpty().mapNotNull { pick ->
            val item = pick.id?.let(byId::get) ?: return@mapNotNull null
            val allowed = if (item.category == ClothingCategory.ACCESSORY) accessories++ < MAX_ACCESSORIES
            else usedCategories.add(item.category)
            if (!allowed) return@mapNotNull null
            WardrobePick(item, UiText.Raw(pick.reason.orEmpty().trim().take(MAX_REASON_LENGTH)))
        }.distinctBy { it.item.id }

        val pickedCategories = picks.map { it.item.category }.toSet()
        val missing = response.missing.orEmpty().mapNotNull { m ->
            val category = ClothingCategory.entries.firstOrNull { it.name == m.category } ?: return@mapNotNull null
            if (category in pickedCategories) return@mapNotNull null
            MissingSlot(
                category,
                (m.warmth ?: 3).coerceIn(ClothingItem.MIN_WARMTH, ClothingItem.MAX_WARMTH),
                m.waterproof ?: false
            )
        }.distinctBy { it.category }

        if (picks.isEmpty() && wardrobe.isNotEmpty() && missing.isEmpty()) {
            throw IllegalStateException("AI returned no usable outfit")
        }
        return WardrobeOutfit(picks, missing, response.tip?.trim()?.takeIf { it.isNotEmpty() }, PlanSource.AI)
    }
}
