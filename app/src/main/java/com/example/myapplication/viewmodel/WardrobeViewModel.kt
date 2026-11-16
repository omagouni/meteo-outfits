package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.WardrobeRepository
import com.example.myapplication.data.model.ClothingCategory
import com.example.myapplication.data.model.ClothingItem
import com.example.myapplication.data.remote.LinkPreview
import com.example.myapplication.data.remote.LinkPreviewFetcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class WardrobeSort(val label: String) {
    RECENT("Recently added"),
    NAME("Name"),
    CATEGORY("Category"),
    WARMEST("Warmest first"),
    LIGHTEST("Lightest first"),
    LEAST_WORN("Least worn"),
    LAST_WORN("Recently worn")
}

class WardrobeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WardrobeRepository.get(application)

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _category = MutableStateFlow<ClothingCategory?>(null)
    val category: StateFlow<ClothingCategory?> = _category.asStateFlow()

    private val _sort = MutableStateFlow(WardrobeSort.RECENT)
    val sort: StateFlow<WardrobeSort> = _sort.asStateFlow()

    val totalCount: StateFlow<Int> = repository.items
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val visibleItems: StateFlow<List<ClothingItem>> =
        combine(repository.items, _query, _category, _sort) { items, query, category, sort ->
            filterAndSort(items, query, category, sort)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setQuery(value: String) { _query.value = value }
    fun setCategory(value: ClothingCategory?) { _category.value = value }
    fun setSort(value: WardrobeSort) { _sort.value = value }

    fun save(item: ClothingItem) {
        viewModelScope.launch { repository.upsert(item.copy(name = item.name.trim())) }
    }

    fun delete(id: String) {
        viewModelScope.launch { repository.delete(id) }
    }

    fun toggleFavorite(item: ClothingItem) = save(item.copy(favorite = !item.favorite))

    suspend fun fetchPreview(url: String): Result<LinkPreview> = LinkPreviewFetcher.fetch(url)

    companion object {
        fun filterAndSort(
            items: List<ClothingItem>,
            query: String,
            category: ClothingCategory?,
            sort: WardrobeSort
        ): List<ClothingItem> {
            val q = query.trim().lowercase()
            val filtered = items.filter { item ->
                (category == null || item.category == category) &&
                    (q.isEmpty() || listOf(item.name, item.brand, item.reference, item.color, item.notes)
                        .any { it.lowercase().contains(q) })
            }
            return when (sort) {
                WardrobeSort.RECENT -> filtered.sortedByDescending { it.createdAt }
                WardrobeSort.NAME -> filtered.sortedBy { it.name.lowercase() }
                WardrobeSort.CATEGORY -> filtered.sortedWith(compareBy({ it.category.ordinal }, { it.name.lowercase() }))
                WardrobeSort.WARMEST -> filtered.sortedByDescending { it.warmth }
                WardrobeSort.LIGHTEST -> filtered.sortedBy { it.warmth }
                WardrobeSort.LEAST_WORN -> filtered.sortedBy { it.wearCount }
                WardrobeSort.LAST_WORN -> filtered.sortedByDescending { it.lastWornEpochDay ?: Long.MIN_VALUE }
            }
        }
    }
}
