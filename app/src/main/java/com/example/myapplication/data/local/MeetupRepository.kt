package com.example.myapplication.data.local

import android.content.Context
import com.example.myapplication.data.model.Meetup
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.StateFlow
import java.io.File

/** Rendez-vous enregistrés sur le téléphone (envoyés et reçus). */
class MeetupRepository private constructor(file: File) {

    private val store = JsonFileStore<Meetup>(
        file, object : TypeToken<List<Meetup>>() {}.type, "MeetupRepository"
    )

    val items: StateFlow<List<Meetup>> = store.items

    suspend fun upsert(meetup: Meetup) = store.mutate { items ->
        if (items.any { it.id == meetup.id }) items.map { if (it.id == meetup.id) meetup else it }
        else items + meetup
    }

    suspend fun delete(id: String) = store.mutate { items -> items.filterNot { it.id == id } }

    companion object {
        @Volatile
        private var instance: MeetupRepository? = null

        fun get(context: Context): MeetupRepository =
            instance ?: synchronized(this) {
                instance ?: MeetupRepository(
                    File(context.applicationContext.filesDir, "meetups.json")
                ).also { instance = it }
            }
    }
}
