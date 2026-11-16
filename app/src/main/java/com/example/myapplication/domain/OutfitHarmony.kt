package com.example.myapplication.domain

/** Règles de coordination des couleurs, utilisées pour une tenue seule et pour deux tenues ensemble. */
object OutfitHarmony {

    /** Score d'harmonie interne d'une tenue (0 = neutre, positif = cohérent, négatif = ça jure). */
    fun internalScore(colors: List<ColorFamily>): Double {
        val known = colors.filter { it != ColorFamily.UNKNOWN }
        if (known.size < 2) return 0.0
        var score = 0.0
        val accents = known.filter { !it.neutral }.distinct()
        // Plus de deux couleurs vives dans une même tenue : trop chargé.
        if (accents.size > 2) score -= 1.5 * (accents.size - 2)
        for (i in known.indices) for (j in i + 1 until known.size) {
            val a = known[i]; val b = known[j]
            when {
                b in a.clashes || a in b.clashes -> score -= 2.0
                a == b && !a.neutral -> score -= 0.5 // total look d'une même couleur vive : un peu lourd
                b in a.analogous || a in b.analogous -> score += 0.5
            }
        }
        return score
    }

    /**
     * Score de coordination entre deux tenues (celle de l'utilisateur et celle de son ami).
     * Récompense un accent partagé ou des couleurs voisines, pénalise les couleurs qui jurent.
     */
    fun pairScore(mine: List<ColorFamily>, partner: List<ColorFamily>): Double {
        val a = mine.filter { it != ColorFamily.UNKNOWN }.distinct()
        val b = partner.filter { it != ColorFamily.UNKNOWN }.distinct()
        if (a.isEmpty() || b.isEmpty()) return 0.0
        var score = 0.0
        val sharedAccent = a.any { !it.neutral && it in b }
        val sharedNeutral = a.any { it.neutral && it in b }
        if (sharedAccent) score += 3.0
        else if (a.any { x -> !x.neutral && b.any { y -> y in x.analogous } }) score += 2.0
        if (sharedNeutral) score += 1.0
        for (x in a) for (y in b) if (y in x.clashes || x in y.clashes) score -= 2.0
        // Deux tenues 100 % neutres sont assorties par défaut mais sans "effet matchy".
        if (a.all { it.neutral } && b.all { it.neutral }) score += 0.5
        return score
    }

    /** La couleur qui fait le lien entre les deux tenues, pour l'expliquer à l'utilisateur. */
    fun linkColor(mine: List<ColorFamily>, partner: List<ColorFamily>): ColorFamily? {
        val a = mine.filter { it != ColorFamily.UNKNOWN }
        val b = partner.filter { it != ColorFamily.UNKNOWN }.toSet()
        return a.firstOrNull { !it.neutral && it in b }
            ?: a.firstOrNull { !it.neutral && it.analogous.any { n -> n in b } }
            ?: a.firstOrNull { it in b }
    }
}
