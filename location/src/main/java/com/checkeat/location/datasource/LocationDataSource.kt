package com.checkeat.location.datasource

import com.checkeat.location.contract.LocationContract
import com.checkeat.location.db.LocationDao
import com.checkeat.location.db.LocationEntity
import com.checkeat.location.lib.model.Location
import com.checkeat.location.util.toLocation
import com.checkeat.location.util.toLocationEntity
import com.checkeat.location.util.toLocationList
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

internal class LocationDataSource(private val locationDao: LocationDao) :
    LocationContract.DataSource {

    override suspend fun storeLocation(location: Location) = withContext(IO) {
        locationDao.saveLocation(location.toLocationEntity())
        updateAllLocationsAsNonFavorite()
    }

    override suspend fun retrieveAllLocations(): List<Location> = withContext(IO) {
        locationDao.retrieveStoredLocations().toLocationList()
    }

    override suspend fun retrieveLastLocation(): Location? = withContext(IO) {
        locationDao.retrieveLastLocation()?.toLocation()
    }

    override suspend fun removeStoredLocations() = withContext(IO) {
        locationDao.removeStoredLocations()
    }

    override suspend fun updateLocation(location: Location) {
        locationDao.updateLocation(location.toLocationEntity())
        updateAllLocationsToFalse(location.toLocationEntity())
    }

    private suspend fun updateAllLocationsAsNonFavorite() {
        locationDao.retrieveLastKnownLocation()?.let { locationFind ->
            updateAllLocationsToFalse(locationFind)
        }
    }

    private suspend fun updateAllLocationsToFalse(location: LocationEntity) {
        locationDao.retrieveStoredLocations().run {
            if (this.size > 1) {
                val mutableLocations = this.toMutableList()
                mutableLocations.remove(location)
                mutableLocations.forEach {
                    it.favorite = false
                }
                locationDao.updateAllStoreLocations(this)
            }
        }
    }
}