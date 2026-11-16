package com.example.myapplication.domain

import com.example.myapplication.data.model.ClothingCategory
import com.example.myapplication.data.model.ClothingItem
import com.example.myapplication.data.model.DayWeather
import kotlin.math.abs
import kotlin.math.roundToInt

data class WardrobePick(val item: ClothingItem, val reason: String)

/** Une pièce dont on aurait besoin mais qu'aucun vêtement de la garde-robe ne couvre. */
data class MissingSlot(val category: ClothingCategory, val targetWarmth: Int, val needsWaterproof: Boolean)

data class WardrobeOutfit(
    val picks: List<WardrobePick>,
    val missing: List<MissingSlot>
)

/**
 * Point d'extension : l'implémentation actuelle est à base de règles ([RuleBasedOutfitPlanner]).
 * Un planner LLM pourra implémenter la même interface (en recevant la météo + la garde-robe
 * sérialisée) et retomber sur les règles en cas d'erreur réseau.
 */
interface OutfitPlanner {
    fun plan(day: DayWeather, wardrobe: List<ClothingItem>, referenceDay: Long): WardrobeOutfit
}

object RuleBasedOutfitPlanner : OutfitPlanner {

    private const val MAX_WARMTH_GAP = 2
    private const val MAX_ACCESSORIES = 2

    /** Niveau de chaleur (1..5) souhaité pour une température ressentie. */
    fun targetWarmth(feelsLike: Double): Int = when {
        feelsLike < 0 -> 5
        feelsLike < 6 -> 4
        feelsLike < 13 -> 3
        feelsLike < 21 -> 2
        else -> 1
    }

    override fun plan(day: DayWeather, wardrobe: List<ClothingItem>, referenceDay: Long): WardrobeOutfit {
        val target = targetWarmth(day.feelsLike)
        val rainy = OutfitRecommender.isRainy(day)
        val windy = day.windKmh > 25
        val needsOuterwear = day.feelsLike < 18 || rainy || windy
        val ctx = Context(day, rainy, windy, referenceDay)

        val picks = mutableListOf<WardrobePick>()
        val missing = mutableListOf<MissingSlot>()

        fun fill(category: ClothingCategory, slotTarget: Int, waterproofMatters: Boolean) {
            val best = wardrobe
                .filter { it.category == category && abs(it.warmth - slotTarget) <= MAX_WARMTH_GAP }
                .maxWithOrNull(compareBy<ClothingItem> { score(it, slotTarget, waterproofMatters, ctx) }
                    .thenBy { -it.wearCount })
            if (best != null) picks += WardrobePick(best, reason(best, slotTarget, waterproofMatters, ctx))
            else missing += MissingSlot(category, slotTarget, waterproofMatters && rainy)
        }

        if (needsOuterwear) fill(ClothingCategory.OUTERWEAR, target, waterproofMatters = true)
        // Sous un manteau, le haut peut être un cran plus léger.
        fill(ClothingCategory.TOP, if (needsOuterwear) (target - 1).coerceAtLeast(1) else target, false)
        fill(ClothingCategory.BOTTOM, target.coerceAtMost(4), false)
        fill(ClothingCategory.SHOES, target.coerceAtMost(4), waterproofMatters = true)

        picks += pickAccessories(wardrobe, target, ctx)

        return WardrobeOutfit(picks, missing)
    }

    private data class Context(val day: DayWeather, val rainy: Boolean, val windy: Boolean, val referenceDay: Long)

    private fun score(item: ClothingItem, target: Int, waterproofMatters: Boolean, ctx: Context): Double {
        var score = -abs(item.warmth - target) * 2.0
        if (waterproofMatters && ctx.rainy) score += if (item.waterproof) 3.0 else -2.0
        if (ctx.windy && item.windproof) score += 2.0
        if (item.favorite) score += 0.5
        score += recencyScore(item, ctx.referenceDay)
        return score
    }

    /**
     * Ce qui est déjà porté aujourd'hui reste en tête (la tenue ne change pas après "I'm wearing this"),
     * ce qui a été porté hier ou avant-hier est pénalisé pour varier.
     */
    private fun recencyScore(item: ClothingItem, referenceDay: Long): Double {
        val last = item.lastWornEpochDay ?: return 0.0
        return when (referenceDay - last) {
            0L -> 5.0
            1L -> -3.0
            2L -> -1.5
            else -> 0.0
        }
    }

    private fun pickAccessories(wardrobe: List<ClothingItem>, target: Int, ctx: Context): List<WardrobePick> {
        val accessories = wardrobe.filter { it.category == ClothingCategory.ACCESSORY }
        val chosen = linkedMapOf<String, WardrobePick>()

        if (ctx.rainy) {
            accessories.filter { it.waterproof }
                .maxByOrNull { recencyScore(it, ctx.referenceDay) + if (it.favorite) 0.5 else 0.0 }
                ?.let { chosen[it.id] = WardrobePick(it, rainReason(ctx.day)) }
        }
        if (target >= 4) {
            accessories.filter { it.warmth >= 3 && it.id !in chosen }
                .sortedByDescending { it.warmth + recencyScore(it, ctx.referenceDay) }
                .take(MAX_ACCESSORIES - chosen.size)
                .forEach { chosen[it.id] = WardrobePick(it, "Extra warmth for ${ctx.day.feelsLike.roundToInt()}°") }
        }
        return chosen.values.toList()
    }

    private fun reason(item: ClothingItem, target: Int, waterproofMatters: Boolean, ctx: Context): String {
        val parts = mutableListOf<String>()
        parts += if (item.warmth == target) "${ClothingItem.warmthLabel(item.warmth)} — right for ${ctx.day.feelsLike.roundToInt()}°"
        else if (item.warmth > target) "A bit warm for ${ctx.day.feelsLike.roundToInt()}°"
        else "A bit light for ${ctx.day.feelsLike.roundToInt()}°"
        if (waterproofMatters && ctx.rainy && item.waterproof) parts += "waterproof"
        if (ctx.windy && item.windproof) parts += "windproof"
        if (item.lastWornEpochDay == ctx.referenceDay) parts += "worn today"
        return parts.joinToString(" · ")
    }

    private fun rainReason(day: DayWeather): String =
        day.precipitationChance?.let { "${(it * 100).roundToInt()}% chance of rain" } ?: "Rain expected"
}
