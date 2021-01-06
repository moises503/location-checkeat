package com.checkeat.location.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLocation(location : LocationEntity)

    @Query("SELECT * FROM latest_locations ORDER BY id DESC")
    suspend fun retrieveStoredLocations() : List<LocationEntity>

    @Query("SELECT * FROM latest_locations ORDER BY id DESC LIMIT 1")
    suspend fun retrieveLastLocation() : LocationEntity?

    @Query("DELETE FROM latest_locations")
    suspend fun removeStoredLocations()
}