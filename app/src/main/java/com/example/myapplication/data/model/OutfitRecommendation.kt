package com.example.myapplication.data.model

data class OutfitRecommendation(
    val mainOutfit: List<OutfitItem>,
    val accessories: List<OutfitItem>,
    val tip: String
)

data class OutfitItem(
    val name: String,
    val emoji: String,
    val description: String
)
