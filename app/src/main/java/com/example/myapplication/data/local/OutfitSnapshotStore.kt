package com.example.myapplication.data.local

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson

/**
 * Dernière tenue calculée, pour le widget et la notification du matin.
 * Textes déjà résolus (traduits) : le widget n'a pas besoin de recalculer.
 */
data class OutfitSnapshot(
    val city: String = "",
    val temp: Int = 0,
    val feelsLike: Int = 0,
    val condition: String = "",
    val emoji: String = "",
    /** Noms des pièces à porter, dans l'ordre. */
    val items: List<String> = emptyList(),
    val tip: String = "",
    val updatedAt: Long = 0
)

object OutfitSnapshotStore {
    private const val PREFS = "weather_prefs"
    private const val KEY = "outfit_snapshot"
    private val gson = Gson()

    fun read(context: Context): OutfitSnapshot? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, null)
            ?.let { runCatching { gson.fromJson(it, OutfitSnapshot::class.java) }.getOrNull() }

    fun write(context: Context, snapshot: OutfitSnapshot) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit { putString(KEY, gson.toJson(snapshot)) }
    }
}
