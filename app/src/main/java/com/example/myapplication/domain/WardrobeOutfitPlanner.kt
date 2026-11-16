package com.example.myapplication.domain

import com.example.myapplication.R
import com.example.myapplication.data.model.ClothingCategory
import com.example.myapplication.data.model.ClothingItem
import com.example.myapplication.data.model.DayWeather
import com.example.myapplication.ui.UiText
import kotlin.math.abs
import kotlin.math.roundToInt

data class WardrobePick(val item: ClothingItem, val reason: UiText)

/** Une pièce dont on aurait besoin mais qu'aucun vêtement de la garde-robe ne couvre. */
data class MissingSlot(val category: ClothingCategory, val targetWarmth: Int, val needsWaterproof: Boolean)

enum class PlanSource { RULES, AI }

data class WardrobeOutfit(
    val picks: List<WardrobePick>,
    val missing: List<MissingSlot>,
    /** Conseil de style (fourni par l'IA). */
    val tip: String? = null,
    val source: PlanSource = PlanSource.RULES
) {
    val colors: List<ColorFamily> get() = picks.map { ColorFamily.fromText(it.item.color) }
}

/**
 * Choisit une tenue dans la garde-robe pour une journée.
 * Implémentations : [RuleBasedOutfitPlanner] (instantané, hors ligne) et
 * [ClaudeOutfitPlanner] (styliste IA, réseau).
 */
interface OutfitPlanner {
    suspend fun plan(day: DayWeather, wardrobe: List<ClothingItem>, referenceDay: Long): WardrobeOutfit
}

object RuleBasedOutfitPlanner : OutfitPlanner {

    override suspend fun plan(day: DayWeather, wardrobe: List<ClothingItem>, referenceDay: Long) =
        planNow(day, wardrobe, referenceDay)

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

    /** Un emplacement à remplir pour la journée (catégorie + chaleur visée). */
    data class Slot(val category: ClothingCategory, val targetWarmth: Int, val waterproofMatters: Boolean)

    /** Un vêtement candidat pour un emplacement, avec son score météo et sa raison. */
    data class Candidate(val item: ClothingItem, val score: Double, val reason: UiText)

    /** Contexte météo de la journée, partagé par tous les calculs. */
    class Context(val day: DayWeather, val referenceDay: Long) {
        val rainy = OutfitRecommender.isRainy(day)
        val windy = day.windKmh > 25
        val target = targetWarmth(day.feelsLike)
        val needsOuterwear = day.feelsLike < 18 || rainy || windy
        val feelsLike = day.feelsLike.roundToInt()
    }

    fun slots(ctx: Context): List<Slot> = buildList {
        val target = ctx.target
        if (ctx.needsOuterwear) add(Slot(ClothingCategory.OUTERWEAR, target, waterproofMatters = true))
        // Sous un manteau, le haut peut être un cran plus léger.
        add(Slot(ClothingCategory.TOP, if (ctx.needsOuterwear) (target - 1).coerceAtLeast(1) else target, false))
        add(Slot(ClothingCategory.BOTTOM, target.coerceAtMost(4), false))
        add(Slot(ClothingCategory.SHOES, target.coerceAtMost(4), waterproofMatters = true))
    }

    /** Candidats d'un emplacement, du meilleur au moins bon. Vide si rien ne convient. */
    fun candidates(slot: Slot, wardrobe: List<ClothingItem>, ctx: Context): List<Candidate> =
        wardrobe
            .filter { it.category == slot.category && abs(it.warmth - slot.targetWarmth) <= MAX_WARMTH_GAP }
            .map { Candidate(it, score(it, slot, ctx), reason(it, slot, ctx)) }
            .sortedWith(compareByDescending<Candidate> { it.score }.thenBy { it.item.wearCount })

    /** Version synchrone, utilisée pour l'affichage immédiat. */
    fun planNow(day: DayWeather, wardrobe: List<ClothingItem>, referenceDay: Long): WardrobeOutfit {
        val ctx = Context(day, referenceDay)
        val picks = mutableListOf<WardrobePick>()
        val missing = mutableListOf<MissingSlot>()

        for (slot in slots(ctx)) {
            val best = candidates(slot, wardrobe, ctx).firstOrNull()
            if (best != null) picks += WardrobePick(best.item, best.reason)
            else missing += MissingSlot(slot.category, slot.targetWarmth, slot.waterproofMatters && ctx.rainy)
        }
        picks += pickAccessories(wardrobe, ctx)
        return WardrobeOutfit(picks, missing)
    }

    private fun score(item: ClothingItem, slot: Slot, ctx: Context): Double {
        // Écart de chaleur au carré : une pièce deux crans à côté ne doit pas être rattrapée par la couleur.
        val gap = abs(item.warmth - slot.targetWarmth)
        var score = -gap * gap * 2.0
        if (slot.waterproofMatters && ctx.rainy) score += if (item.waterproof) 3.0 else -2.0
        if (ctx.windy && item.windproof) score += 2.0
        if (item.favorite) score += 0.5
        score += recencyScore(item, ctx.referenceDay)
        return score
    }

    /**
     * Ce qui est déjà porté aujourd'hui reste en tête (la tenue ne change pas après "Je porte ça"),
     * ce qui a été porté hier ou avant-hier est pénalisé pour varier.
     */
    fun recencyScore(item: ClothingItem, referenceDay: Long): Double {
        val last = item.lastWornEpochDay ?: return 0.0
        return when (referenceDay - last) {
            0L -> 5.0
            1L -> -3.0
            2L -> -1.5
            else -> 0.0
        }
    }

    fun pickAccessories(wardrobe: List<ClothingItem>, ctx: Context): List<WardrobePick> {
        val accessories = wardrobe.filter { it.category == ClothingCategory.ACCESSORY }
        val chosen = linkedMapOf<String, WardrobePick>()

        if (ctx.rainy) {
            accessories.filter { it.waterproof }
                .maxByOrNull { recencyScore(it, ctx.referenceDay) + if (it.favorite) 0.5 else 0.0 }
                ?.let { chosen[it.id] = WardrobePick(it, OutfitRecommender.rainDescription(ctx.day)) }
        }
        if (ctx.target >= 4) {
            accessories.filter { it.warmth >= 3 && it.id !in chosen }
                .sortedByDescending { it.warmth + recencyScore(it, ctx.referenceDay) }
                .take(MAX_ACCESSORIES - chosen.size)
                .forEach { chosen[it.id] = WardrobePick(it, UiText.res(R.string.reason_extra_warmth, ctx.feelsLike)) }
        }
        return chosen.values.toList()
    }

    private fun reason(item: ClothingItem, slot: Slot, ctx: Context): UiText {
        val parts = mutableListOf<UiText>()
        parts += when {
            item.warmth == slot.targetWarmth ->
                UiText.Res(R.string.reason_right_for, listOf(UiText.res(ClothingItem.warmthRes(item.warmth)), ctx.feelsLike))
            item.warmth > slot.targetWarmth -> UiText.res(R.string.reason_bit_warm, ctx.feelsLike)
            else -> UiText.res(R.string.reason_bit_light, ctx.feelsLike)
        }
        if (slot.waterproofMatters && ctx.rainy && item.waterproof) parts += UiText.res(R.string.reason_waterproof)
        if (ctx.windy && item.windproof) parts += UiText.res(R.string.reason_windproof)
        if (item.lastWornEpochDay == ctx.referenceDay) parts += UiText.res(R.string.reason_worn_today)
        return UiText.Joined(parts)
    }
}
