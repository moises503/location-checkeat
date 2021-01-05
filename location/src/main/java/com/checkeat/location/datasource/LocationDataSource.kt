package com.checkeat.location.datasource

import com.checkeat.location.contract.LocationContract
import com.checkeat.location.model.Location

internal class LocationDataSource : LocationContract.DataSource {
    override suspend fun storeLocation(location: Location) {
        TODO("Not yet implemented")
    }

    override suspend fun retrieveAllLocations(): List<Location> {
        TODO("Not yet implemented")
    }

    override suspend fun retrieveLastLocation(): Location {
        TODO("Not yet implemented")
    }
}