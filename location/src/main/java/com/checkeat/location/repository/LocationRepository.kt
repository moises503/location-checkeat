package com.checkeat.location.repository

import com.checkeat.location.contract.LocationContract
import com.checkeat.location.lib.model.Location

internal class LocationRepository(private val dataSource: LocationContract.DataSource) : LocationContract.Repository {

    override suspend fun storeLocation(location: Location) =
        dataSource.storeLocation(location)

    override suspend fun retrieveAllLocations(): List<Location> =
        dataSource.retrieveAllLocations()

    override suspend fun retrieveLastLocation(): Location? =
        dataSource.retrieveLastLocation()

    override suspend fun removeStoredLocations() =
        dataSource.removeStoredLocations()
}