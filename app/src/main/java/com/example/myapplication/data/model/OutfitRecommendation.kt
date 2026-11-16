package com.example.myapplication.data.model

import com.example.myapplication.ui.UiText

data class OutfitRecommendation(
    val mainOutfit: List<OutfitItem>,
    val accessories: List<OutfitItem>,
    val tip: UiText
)

data class OutfitItem(
    val name: UiText,
    val emoji: String,
    val description: UiText
)
