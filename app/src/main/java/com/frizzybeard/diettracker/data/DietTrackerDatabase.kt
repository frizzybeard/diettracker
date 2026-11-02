package com.frizzybeard.diettracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [DishEntity::class],
    version = 1,
    exportSchema = false
)
abstract class DietTrackerDatabase : RoomDatabase() {

    abstract fun dishDao(): DishDao

    companion object {
        fun create(context: Context): DietTrackerDatabase {
            return Room.databaseBuilder(
                context,
                DietTrackerDatabase::class.java,
                "diet_tracker.db"
            )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                        super.onCreate(db)

                        // Initial dishes will be inserted here
                    }
                })
                .build()
        }
    }
}