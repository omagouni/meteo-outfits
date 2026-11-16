package com.example.myapplication.domain

import com.example.myapplication.R
import com.example.myapplication.data.model.ClothingCategory
import com.example.myapplication.data.model.ClothingItem
import com.example.myapplication.data.model.DayWeather
import com.example.myapplication.ui.UiText

/** Résultat d'une coordination : ma tenue, et ce qui la relie à celle de l'ami. */
data class MatchyOutfit(
    val outfit: WardrobeOutfit,
    /** Score de coordination avec la tenue de l'ami (voir [OutfitHarmony.pairScore]). */
    val pairScore: Double,
    /** La couleur qui fait le lien, s'il y en a une. */
    val linkColor: ColorFamily?
) {
    val matchLevelRes: Int
        get() = when {
            pairScore >= 3.5 -> R.string.meetup_match_great
            pairScore >= 2.0 -> R.string.meetup_match_good
            else -> R.string.meetup_match_ok
        }
}

/**
 * Compose ma tenue pour un rendez-vous : adaptée à la météo du lieu ET coordonnée
 * avec les couleurs de la tenue de l'ami. Tout est calculé sur le téléphone.
 */
object MatchyPlanner {

    /** Nombre de candidats gardés par emplacement avant d'énumérer les combinaisons. */
    private const val CANDIDATES_PER_SLOT = 4

    fun plan(
        day: DayWeather,
        wardrobe: List<ClothingItem>,
        partnerColors: List<ColorFamily>,
        partnerName: String,
        referenceDay: Long
    ): MatchyOutfit {
        val ctx = RuleBasedOutfitPlanner.Context(day, referenceDay)
        val slots = RuleBasedOutfitPlanner.slots(ctx)
        val perSlot = slots.map { slot ->
            RuleBasedOutfitPlanner.candidates(slot, wardrobe, ctx).take(CANDIDATES_PER_SLOT)
        }

        // Énumère toutes les combinaisons (au plus 4^4 = 256) et garde la meilleure :
        // score météo (règles) + harmonie interne + coordination avec l'ami.
        var best: List<RuleBasedOutfitPlanner.Candidate?>? = null
        var bestScore = Double.NEGATIVE_INFINITY
        fun explore(index: Int, current: List<RuleBasedOutfitPlanner.Candidate?>) {
            if (index == slots.size) {
                val chosen = current.filterNotNull()
                val colors = chosen.map { ColorFamily.fromText(it.item.color) }
                val score = chosen.sumOf { it.score } +
                    OutfitHarmony.internalScore(colors) +
                    OutfitHarmony.pairScore(colors, partnerColors) * 1.5
                if (score > bestScore) { bestScore = score; best = current }
                return
            }
            val options = perSlot[index].ifEmpty { listOf(null) }
            for (candidate in options) explore(index + 1, current + candidate)
        }
        explore(0, emptyList())

        val chosen = best.orEmpty()
        val colors = chosen.filterNotNull().map { ColorFamily.fromText(it.item.color) }
        val link = OutfitHarmony.linkColor(colors, partnerColors)
        val pairScore = OutfitHarmony.pairScore(colors, partnerColors)

        val picks = chosen.filterNotNull().map { candidate ->
            WardrobePick(candidate.item, matchyReason(candidate, partnerColors, partnerName, link))
        }
        val missing = slots.filterIndexed { i, _ -> chosen.getOrNull(i) == null }
            .map { MissingSlot(it.category, it.targetWarmth, it.waterproofMatters && ctx.rainy) }
        val accessories = RuleBasedOutfitPlanner.pickAccessories(wardrobe, ctx)

        return MatchyOutfit(WardrobeOutfit(picks + accessories, missing), pairScore, link)
    }

    /** Ajoute à la raison météo ce que la pièce apporte à la coordination. */
    private fun matchyReason(
        candidate: RuleBasedOutfitPlanner.Candidate,
        partnerColors: List<ColorFamily>,
        partnerName: String,
        link: ColorFamily?
    ): UiText {
        val color = ColorFamily.fromText(candidate.item.color)
        val extra: UiText? = when {
            color == ColorFamily.UNKNOWN -> null
            color in partnerColors && !color.neutral ->
                UiText.Res(R.string.reason_matchy_same, listOf(UiText.res(color.labelRes), partnerName))
            !color.neutral && partnerColors.any { it in color.analogous } ->
                UiText.Res(R.string.reason_matchy_close, listOf(partnerName, UiText.res(partnerColors.first { it in color.analogous }.labelRes)))
            color.neutral && link != null && link != color && !link.neutral ->
                UiText.Res(R.string.reason_matchy_neutral, listOf(UiText.res(link.labelRes)))
            else -> null
        }
        return if (extra == null) candidate.reason else UiText.Joined(listOf(candidate.reason, extra))
    }

    /** Garde-robe "virtuelle" quand l'utilisateur n'a rien saisi : une pièce par catégorie, dans la couleur choisie. */
    fun virtualWardrobe(colorsByCategory: Map<ClothingCategory, ColorFamily>, feelsLike: Double): List<ClothingItem> {
        val target = RuleBasedOutfitPlanner.targetWarmth(feelsLike)
        return colorsByCategory.map { (category, color) ->
            ClothingItem(
                id = "virtual-${category.name}",
                name = "",
                category = category,
                warmth = when (category) {
                    ClothingCategory.OUTERWEAR -> target
                    ClothingCategory.TOP -> (target - 1).coerceAtLeast(1)
                    else -> target.coerceAtMost(4)
                },
                color = color.name
            )
        }
    }
}
