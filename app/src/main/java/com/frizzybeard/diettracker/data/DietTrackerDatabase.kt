
package com.frizzybeard.diettracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        DishEntity::class,
        MealEntity::class,
        MealDishEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class DietTrackerDatabase : RoomDatabase() {

    abstract fun dishDao(): DishDao
    abstract fun mealDao(): MealDao
    abstract fun mealDishDao(): MealDishDao

    companion object {

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS meals (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        mealType TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        preMealGlucose REAL,
                        twoHourGlucose REAL,
                        lyumjevUnits REAL,
                        basalogUnits REAL
                    )
                """)

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS meal_dishes (
                        mealId INTEGER NOT NULL,
                        dishId INTEGER NOT NULL,
                        quantity INTEGER NOT NULL,
                        PRIMARY KEY(mealId, dishId),
                        FOREIGN KEY(mealId) REFERENCES meals(id)
                            ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(dishId) REFERENCES dishes(id)
                            ON UPDATE NO ACTION ON DELETE RESTRICT
                    )
                """)

                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_meal_dishes_mealId ON meal_dishes(mealId)"
                )

                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_meal_dishes_dishId ON meal_dishes(dishId)"
                )
            }
        }

        fun create(context: Context): DietTrackerDatabase {
            return Room.databaseBuilder(
                context,
                DietTrackerDatabase::class.java,
                "diet_tracker.db"
            )
                .addMigrations(MIGRATION_1_2)
                .build()
        }
    }
}
