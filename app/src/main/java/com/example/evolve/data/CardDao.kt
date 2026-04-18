package com.example.evolve.data

import androidx.room.Dao
import androidx.room.Query
import com.example.evolve.model.CardModel

@Dao
interface CardDao {
    @Query("SELECT * FROM cards")
    suspend fun getAllCards(): List<CardModel>
}
