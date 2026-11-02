
package com.frizzybeard.diettracker.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "meal_dishes",
    primaryKeys = ["mealId", "dishId"],
    foreignKeys = [
        ForeignKey(
            entity = MealEntity::class,
            parentColumns = ["id"],
            childColumns = ["mealId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DishEntity::class,
            parentColumns = ["id"],
            childColumns = ["dishId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("mealId"),
        Index("dishId")
    ]
)
data class MealDishEntity(
    val mealId: Int,
    val dishId: Int,
    val quantity: Int
)
