package com.example.evolve.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.evolve.model.CardModel

class CardRepository(private val cardDao: CardDao) {
    suspend fun getAllCards(): List<CardModel> {
        return withContext(Dispatchers.IO) {
            cardDao.getAllCards()
        }
    }
}
