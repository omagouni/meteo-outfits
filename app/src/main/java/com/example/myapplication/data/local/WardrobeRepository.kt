package com.example.myapplication.data.local

import android.content.Context
import com.example.myapplication.data.model.ClothingItem
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.StateFlow
import java.io.File

/** Garde-robe stockée en JSON dans le stockage interne de l'app. */
class WardrobeRepository private constructor(file: File) {

    private val store = JsonFileStore<ClothingItem>(
        file, object : TypeToken<List<ClothingItem>>() {}.type, "WardrobeRepository"
    )

    val items: StateFlow<List<ClothingItem>> = store.items

    /** Lecture synchrone, pour le widget et le job du matin. */
    fun readNow(): List<ClothingItem> = store.readNow()

    suspend fun upsert(item: ClothingItem) = store.mutate { items ->
        if (items.any { it.id == item.id }) items.map { if (it.id == item.id) item else it }
        else items + item
    }

    suspend fun delete(id: String) = store.mutate { items -> items.filterNot { it.id == id } }

    /** Marque des vêtements comme portés ce jour-là (une seule fois par jour). */
    suspend fun markWorn(ids: Collection<String>, epochDay: Long) = store.mutate { items ->
        items.map { item ->
            if (item.id in ids && item.lastWornEpochDay != epochDay) {
                item.copy(wearCount = item.wearCount + 1, lastWornEpochDay = epochDay)
            } else item
        }
    }

    companion object {
        @Volatile
        private var instance: WardrobeRepository? = null

        fun get(context: Context): WardrobeRepository =
            instance ?: synchronized(this) {
                instance ?: WardrobeRepository(
                    File(context.applicationContext.filesDir, "wardrobe.json")
                ).also { instance = it }
            }
    }
}
