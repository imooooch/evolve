package com.example.evolve.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import androidx.room.Entity
import androidx.room.PrimaryKey

@Serializable
@Entity(tableName = "cards")
data class CardModel(
    @SerialName("card")
    @PrimaryKey val card: String,
    @SerialName("expansion") val expansion: String,
    @SerialName("cardclass") val cardclass: String,
    @SerialName("name") val name: String,
    @SerialName("rare") val rare: String? = null,
    @SerialName("kind") val kind: String,
    @SerialName("type") val type: String,
    @SerialName("cost") val cost: Int? = null,
    @SerialName("power") val power: Int? = null,
    @SerialName("hp") val hp: Int? = null,
    @SerialName("ability") val ability: String? = null,
    @SerialName("evolve") val evolve: String? = null,
    @SerialName("advance") val advance: String? = null,
    @SerialName("image") val image: String
)