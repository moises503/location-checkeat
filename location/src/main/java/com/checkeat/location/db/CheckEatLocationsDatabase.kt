package com.checkeat.location.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.checkeat.location.db.CheckEatLocationsDatabase.Companion.DB_VERSION

@Database(entities = [LocationEntity::class],
version = DB_VERSION)
abstract class CheckEatLocationsDatabase : RoomDatabase() {
    companion object {
        const val DB_VERSION = 1
        private const val DB_NAME = "checkeatmx-locations"
        fun create(context: Context) : CheckEatLocationsDatabase {
            return Room.databaseBuilder(
                context,
                CheckEatLocationsDatabase::class.java,
                DB_NAME
            ).build()
        }
    }
    abstract fun locationDao(): LocationDao
}