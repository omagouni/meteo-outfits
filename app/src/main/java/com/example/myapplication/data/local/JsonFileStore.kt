package com.example.myapplication.data.local

import android.util.AtomicFile
import android.util.Log
import com.google.gson.Gson
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
import java.lang.reflect.Type

/**
 * Liste d'objets persistée en JSON dans le stockage interne, avec écriture atomique.
 * Suffisant pour quelques centaines d'éléments ; pourra migrer vers Room si besoin.
 */
class JsonFileStore<T>(file: File, private val listType: Type, private val tag: String) {

    private val atomicFile = AtomicFile(file)
    private val gson = Gson()
    private val mutex = Mutex()
    private var loaded = false

    private val _items = MutableStateFlow<List<T>>(emptyList())
    val items: StateFlow<List<T>> = _items.asStateFlow()

    init {
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            mutex.withLock { ensureLoaded() }
        }
    }

    suspend fun mutate(transform: (List<T>) -> List<T>) {
        mutex.withLock {
            ensureLoaded()
            val updated = transform(_items.value)
            _items.value = updated
            withContext(Dispatchers.IO) { write(updated) }
        }
    }

    /** Lecture synchrone (pour le widget et le job du matin, hors UI). */
    fun readNow(): List<T> = read()

    /** Doit être appelé sous [mutex]. */
    private suspend fun ensureLoaded() {
        if (loaded) return
        _items.value = withContext(Dispatchers.IO) { read() }
        loaded = true
    }

    private fun read(): List<T> = try {
        if (!atomicFile.baseFile.exists()) emptyList()
        else gson.fromJson<List<T>>(atomicFile.readFully().decodeToString(), listType).orEmpty()
    } catch (e: Exception) {
        Log.e(tag, "Could not read $tag", e)
        emptyList()
    }

    private fun write(items: List<T>) {
        val stream = atomicFile.startWrite()
        try {
            stream.write(gson.toJson(items, listType).encodeToByteArray())
            atomicFile.finishWrite(stream)
        } catch (e: Exception) {
            atomicFile.failWrite(stream)
            Log.e(tag, "Could not save $tag", e)
        }
    }
}
