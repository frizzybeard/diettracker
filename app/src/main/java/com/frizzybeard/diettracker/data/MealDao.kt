
package com.frizzybeard.diettracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MealDao {

    @Insert
    suspend fun insertMeal(meal: MealEntity): Long

    @Query("SELECT * FROM meals ORDER BY timestamp DESC")
    suspend fun getAllMeals(): List<MealEntity>

    @Query("SELECT * FROM meals WHERE id = :mealId")
    suspend fun getMealById(mealId: Int): MealEntity?
}
