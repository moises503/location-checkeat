package com.checkeat.location.datasource

import com.checkeat.location.contract.LocationContract
import com.checkeat.location.db.LocationDao
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
}