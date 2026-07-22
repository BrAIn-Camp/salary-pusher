package com.braincamp.salarypusher.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * The single Room database for Salary Pusher.
 *
 * Version history:
 *   1 — initial schema: earning_events table
 *
 * When adding a migration, increment [version] and add a Migration object
 * to the [Room.databaseBuilder] call in [getInstance].
 */
@Database(
    entities = [EarningEventEntity::class],
    version = 1,
    exportSchema = true
)
abstract class SalaryPusherDatabase : RoomDatabase() {

    abstract fun earningEventDao(): EarningEventDao

    companion object {
        private const val DATABASE_NAME = "salary_pusher.db"

        @Volatile
        private var instance: SalaryPusherDatabase? = null

        fun getInstance(context: Context): SalaryPusherDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    SalaryPusherDatabase::class.java,
                    DATABASE_NAME
                ).build().also { instance = it }
            }
        }
    }
}
