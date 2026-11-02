
package com.frizzybeard.diettracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meals")
data class MealEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val mealType: String,
    val timestamp: Long,

    val preMealGlucose: Double?,
    val twoHourGlucose: Double?,

    val lyumjevUnits: Double?,
    val basalogUnits: Double?
)
