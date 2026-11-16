package com.example.myapplication.data.model

import java.util.UUID

enum class ClothingCategory(val label: String, val emoji: String) {
    OUTERWEAR("Outerwear", "🧥"),
    TOP("Top", "👕"),
    BOTTOM("Bottom", "👖"),
    SHOES("Shoes", "👟"),
    ACCESSORY("Accessory", "🧣")
}

/**
 * Un vêtement de la garde-robe de l'utilisateur.
 *
 * Tous les paramètres ont une valeur par défaut : Kotlin génère alors un constructeur
 * sans argument, ce qui permet à Gson d'appliquer ces défauts si un champ manque
 * dans le JSON (ex. après l'ajout d'un nouveau champ).
 */
data class ClothingItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val category: ClothingCategory = ClothingCategory.TOP,
    /** Chaleur de 1 (très léger) à 5 (très chaud). */
    val warmth: Int = 3,
    val waterproof: Boolean = false,
    val windproof: Boolean = false,
    val brand: String = "",
    /** Référence produit (SKU, modèle...) pour le retrouver en ligne. */
    val reference: String = "",
    val productUrl: String = "",
    val imageUrl: String = "",
    val color: String = "",
    val notes: String = "",
    val favorite: Boolean = false,
    val wearCount: Int = 0,
    /** Dernier jour porté (jours depuis l'epoch, heure locale), null si jamais porté. */
    val lastWornEpochDay: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val MIN_WARMTH = 1
        const val MAX_WARMTH = 5

        fun warmthLabel(warmth: Int): String = when (warmth) {
            1 -> "Very light"
            2 -> "Light"
            3 -> "Medium"
            4 -> "Warm"
            else -> "Very warm"
        }
    }
}
