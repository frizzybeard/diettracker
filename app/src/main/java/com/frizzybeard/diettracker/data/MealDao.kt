
package com.frizzybeard.diettracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface MealDao {

    @Insert
    suspend fun insertMeal(meal: MealEntity): Long

    @Insert
    suspend fun insertMealDishes(dishes: List<MealDishEntity>)

    @Transaction
    suspend fun insertMealWithDishes(
        meal: MealEntity,
        dishes: List<MealDishEntity>
    ): Long {

        val mealId = insertMeal(meal)

        val mealDishes = dishes.map {
            it.copy(mealId = mealId.toInt())
        }

        insertMealDishes(mealDishes)

        return mealId
    }

    @Query("SELECT * FROM meals ORDER BY timestamp DESC")
    suspend fun getAllMeals(): List<MealEntity>

    @Query("SELECT * FROM meals WHERE id = :mealId")
    suspend fun getMealById(mealId: Int): MealEntity?

    @Query("""
        SELECT dishes.dishName AS dishName,
               meal_dishes.quantity AS quantity
        FROM meal_dishes
        INNER JOIN dishes
        ON meal_dishes.dishId = dishes.id
        WHERE meal_dishes.mealId = :mealId
        ORDER BY dishes.dishName
    """)
    suspend fun getMealDishDetails(
        mealId: Int
    ): List<MealDishDetail>
}
