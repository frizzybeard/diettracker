
package com.frizzybeard.diettracker.data

import android.content.Context
import androidx.room.Room

object DatabaseProvider {

    @Volatile
    private var INSTANCE: DietTrackerDatabase? = null

    fun getDatabase(context: Context): DietTrackerDatabase {
        return INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                DietTrackerDatabase::class.java,
                "diet_tracker.db"
            )
                .addMigrations(DietTrackerDatabase.MIGRATION_1_2)
                .build()
                .also {
                    INSTANCE = it
                }
        }
    }

    suspend fun ensureDishesSeeded(context: Context) {
        val dao = getDatabase(context).dishDao()

        if (dao.getDishCount() == 0) {
            dao.insertDishes(DishSeeder.getInitialDishes())
        }
    }
}
