package com.example.myapplication.data.local

import android.content.Context
import android.util.AtomicFile
import android.util.Log
import com.example.myapplication.data.model.ClothingItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Garde-robe stockée en JSON dans le stockage interne de l'app.
 * Suffisant pour quelques centaines de vêtements ; pourra migrer vers Room si besoin.
 */
class WardrobeRepository private constructor(file: File) {

    private val atomicFile = AtomicFile(file)
    private val gson = Gson()
    private val listType = object : TypeToken<List<ClothingItem>>() {}.type
    private val mutex = Mutex()
    private var loaded = false

    private val _items = MutableStateFlow<List<ClothingItem>>(emptyList())
    val items: StateFlow<List<ClothingItem>> = _items.asStateFlow()

    init {
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            mutex.withLock { ensureLoaded() }
        }
    }

    suspend fun upsert(item: ClothingItem) = mutate { items ->
        if (items.any { it.id == item.id }) items.map { if (it.id == item.id) item else it }
        else items + item
    }

    suspend fun delete(id: String) = mutate { items -> items.filterNot { it.id == id } }

    /** Marque des vêtements comme portés ce jour-là (une seule fois par jour). */
    suspend fun markWorn(ids: Collection<String>, epochDay: Long) = mutate { items ->
        items.map { item ->
            if (item.id in ids && item.lastWornEpochDay != epochDay) {
                item.copy(wearCount = item.wearCount + 1, lastWornEpochDay = epochDay)
            } else item
        }
    }

    private suspend fun mutate(transform: (List<ClothingItem>) -> List<ClothingItem>) {
        mutex.withLock {
            ensureLoaded()
            val updated = transform(_items.value)
            _items.value = updated
            withContext(Dispatchers.IO) { write(updated) }
        }
    }

    /** Doit être appelé sous [mutex]. */
    private suspend fun ensureLoaded() {
        if (loaded) return
        _items.value = withContext(Dispatchers.IO) { read() }
        loaded = true
    }

    private fun read(): List<ClothingItem> = try {
        if (!atomicFile.baseFile.exists()) emptyList()
        else gson.fromJson<List<ClothingItem>>(atomicFile.readFully().decodeToString(), listType)
            .orEmpty()
    } catch (e: Exception) {
        Log.e(TAG, "Could not read wardrobe", e)
        emptyList()
    }

    private fun write(items: List<ClothingItem>) {
        val stream = atomicFile.startWrite()
        try {
            stream.write(gson.toJson(items, listType).encodeToByteArray())
            atomicFile.finishWrite(stream)
        } catch (e: Exception) {
            atomicFile.failWrite(stream)
            Log.e(TAG, "Could not save wardrobe", e)
        }
    }

    companion object {
        private const val TAG = "WardrobeRepository"

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
