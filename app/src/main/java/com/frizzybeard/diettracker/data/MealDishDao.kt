
package com.frizzybeard.diettracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MealDishDao {

    @Insert
    suspend fun insertMealDishes(dishes: List<MealDishEntity>)

    @Query("SELECT * FROM meal_dishes WHERE mealId = :mealId")
    suspend fun getDishesForMeal(mealId: Int): List<MealDishEntity>
}
