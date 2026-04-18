package com.example.evolve.model

import com.squareup.moshi.Json

// デッキデータモデル
data class DeckModel(
    @Json(name = "deck_id") val deckId: String,
    @Json(name = "name") val name: String,
    @Json(name = "cards") val cards: List<String>, // カードIDのリスト
    @Json(name = "created_at") val createdAt: String
)
