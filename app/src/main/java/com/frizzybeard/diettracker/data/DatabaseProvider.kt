package com.frizzybeard.diettracker.data

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
                .addCallback(object : RoomDatabase.Callback() {

                    override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                        super.onCreate(db)

                        CoroutineScope(Dispatchers.IO).launch {
                            INSTANCE?.dishDao()?.insertDishes(
                                DishSeeder.getInitialDishes()
                            )
                        }
                    }

                })
                .build()
                .also {
                    INSTANCE = it
                }
        }
    }
}