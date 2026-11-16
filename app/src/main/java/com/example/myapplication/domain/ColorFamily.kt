package com.example.myapplication.domain

import androidx.annotation.StringRes
import com.example.myapplication.R

/**
 * Familles de couleurs utilisées pour coordonner des tenues.
 * Les noms libres saisis par l'utilisateur ("bleu marine", "navy", "beige clair") y sont rattachés.
 */
enum class ColorFamily(@StringRes val labelRes: Int, val hex: Long, val neutral: Boolean) {
    BLACK(R.string.color_black, 0xFF1C1C1E, true),
    WHITE(R.string.color_white, 0xFFF5F5F0, true),
    GREY(R.string.color_grey, 0xFF8E8E93, true),
    BEIGE(R.string.color_beige, 0xFFD9C3A3, true),
    BROWN(R.string.color_brown, 0xFF6B4F3A, true),
    NAVY(R.string.color_navy, 0xFF1F2A5C, true),
    DENIM(R.string.color_denim, 0xFF5B7DB1, true),
    BLUE(R.string.color_blue, 0xFF2F6FDB, false),
    LIGHT_BLUE(R.string.color_light_blue, 0xFF9CC9F2, false),
    GREEN(R.string.color_green, 0xFF2E8B57, false),
    OLIVE(R.string.color_olive, 0xFF6B7A3A, true),
    RED(R.string.color_red, 0xFFC62828, false),
    BURGUNDY(R.string.color_burgundy, 0xFF7B1E3A, false),
    PINK(R.string.color_pink, 0xFFE8A0BF, false),
    ORANGE(R.string.color_orange, 0xFFE8743B, false),
    YELLOW(R.string.color_yellow, 0xFFE9C94A, false),
    PURPLE(R.string.color_purple, 0xFF7B4FB5, false),
    UNKNOWN(R.string.color_unknown, 0xFFB0B0B0, true);

    /** Familles proches sur le cercle chromatique : vont bien ensemble sans être identiques. */
    val analogous: Set<ColorFamily>
        get() = when (this) {
            BLUE -> setOf(NAVY, DENIM, LIGHT_BLUE, PURPLE)
            LIGHT_BLUE -> setOf(BLUE, DENIM, WHITE)
            NAVY -> setOf(BLUE, DENIM)
            DENIM -> setOf(BLUE, NAVY, LIGHT_BLUE)
            GREEN -> setOf(OLIVE, YELLOW)
            OLIVE -> setOf(GREEN, BEIGE, BROWN)
            RED -> setOf(BURGUNDY, ORANGE, PINK)
            BURGUNDY -> setOf(RED, PINK, BROWN)
            PINK -> setOf(RED, BURGUNDY, PURPLE)
            ORANGE -> setOf(RED, YELLOW, BROWN)
            YELLOW -> setOf(ORANGE, GREEN)
            PURPLE -> setOf(PINK, BLUE)
            BEIGE -> setOf(BROWN, WHITE, OLIVE)
            BROWN -> setOf(BEIGE, OLIVE, BURGUNDY)
            else -> emptySet()
        }

    /** Couples qui jurent (sauf si on cherche volontairement le contraste). */
    val clashes: Set<ColorFamily>
        get() = when (this) {
            RED -> setOf(GREEN, ORANGE, PURPLE)
            GREEN -> setOf(RED, PINK)
            ORANGE -> setOf(RED, PINK, PURPLE)
            PINK -> setOf(ORANGE, GREEN, YELLOW)
            PURPLE -> setOf(RED, ORANGE, YELLOW)
            YELLOW -> setOf(PINK, PURPLE)
            else -> emptySet()
        }

    companion object {
        private val keywords: List<Pair<ColorFamily, List<String>>> = listOf(
            NAVY to listOf("navy", "marine", "bleu nuit", "midnight", "indigo"),
            LIGHT_BLUE to listOf("bleu clair", "light blue", "sky", "ciel", "baby blue", "pastel blue", "bleu pastel", "powder blue"),
            DENIM to listOf("denim", "jean"),
            BLUE to listOf("bleu", "blue", "cobalt", "azur", "royal", "turquoise", "teal", "pétrole", "petrol"),
            OLIVE to listOf("olive", "kaki", "khaki", "army", "militaire", "sauge", "sage", "forêt", "forest"),
            GREEN to listOf("vert", "green", "émeraude", "emerald", "menthe", "mint", "lime"),
            BURGUNDY to listOf("bordeaux", "burgundy", "wine", "maroon", "cerise", "prune", "plum"),
            PINK to listOf("rose", "pink", "fuchsia", "magenta", "blush", "corail", "coral", "saumon", "salmon"),
            RED to listOf("rouge", "red", "crimson", "scarlet", "cherry", "brique", "brick", "rust", "rouille"),
            ORANGE to listOf("orange", "abricot", "apricot", "pêche", "peach", "terracotta", "tangerine"),
            YELLOW to listOf("jaune", "yellow", "moutarde", "mustard", "ocre", "ochre", "gold", "doré", "citron", "lemon"),
            PURPLE to listOf("violet", "purple", "lavande", "lavender", "lilas", "lilac", "mauve", "aubergine"),
            BEIGE to listOf("beige", "camel", "sable", "sand", "crème", "cream", "écru", "ecru", "taupe", "ivoire", "ivory", "nude", "tan", "stone", "oatmeal", "greige"),
            BROWN to listOf("marron", "brown", "chocolat", "chocolate", "café", "coffee", "cognac", "noisette", "hazelnut", "caramel", "moka", "mocha"),
            GREY to listOf("gris", "grey", "gray", "anthracite", "charcoal", "argent", "silver", "chiné", "heather"),
            WHITE to listOf("blanc", "white", "off-white", "cassé"),
            BLACK to listOf("noir", "black", "ébène", "jet")
        )

        /** Rattache un nom de couleur libre (FR/EN) à une famille. [UNKNOWN] si rien ne correspond. */
        fun fromText(text: String): ColorFamily {
            val t = text.trim().lowercase()
            if (t.isEmpty()) return UNKNOWN
            // L'ordre de la liste compte : "bleu marine" doit donner NAVY avant BLUE, "rose" avant RED ("rouge").
            keywords.forEach { (family, words) ->
                if (words.any { w -> t == w || t.contains(w) }) return family
            }
            return UNKNOWN
        }

        /** Familles proposées quand l'ami n'a pas de garde-robe et choisit juste ses couleurs. */
        val pickable: List<ColorFamily> = entries.filter { it != UNKNOWN }
    }
}
