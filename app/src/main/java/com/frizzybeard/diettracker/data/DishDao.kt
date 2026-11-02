package com.frizzybeard.diettracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface DishDao {

    @Query("SELECT * FROM dishes ORDER BY dishName")
    suspend fun getAllDishes(): List<DishEntity>

    @Query("SELECT COUNT(*) FROM dishes")
    suspend fun getDishCount(): Int

    @Insert
    suspend fun insertDishes(dishes: List<DishEntity>)

    @Insert
    suspend fun insertDish(dish: DishEntity): Long
}