package com.checkeat.location.db

import androidx.room.*

@Dao
interface LocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLocation(location : LocationEntity)

    @Query("SELECT * FROM latest_locations ORDER BY id DESC")
    suspend fun retrieveStoredLocations() : List<LocationEntity>

    @Query("SELECT * FROM latest_locations ORDER BY favorite DESC LIMIT 1")
    suspend fun retrieveLastLocation() : LocationEntity?

    @Query("SELECT * FROM latest_locations ORDER BY id DESC LIMIT 1")
    suspend fun retrieveLastKnownLocation(): LocationEntity?

    @Query("DELETE FROM latest_locations")
    suspend fun removeStoredLocations()

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateAllStoreLocations(list: List<LocationEntity>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateLocation(location: LocationEntity)
}