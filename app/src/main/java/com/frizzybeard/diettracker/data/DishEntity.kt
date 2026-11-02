package com.frizzybeard.diettracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dishes")
data class DishEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val dishName: String,

    val mainIngredients: String
)