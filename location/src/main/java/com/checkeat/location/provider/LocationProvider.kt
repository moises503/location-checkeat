package com.checkeat.location.provider

import com.checkeat.location.contract.LocationContract
import com.checkeat.location.datasource.LocationDataSource
import com.checkeat.location.model.Location
import com.checkeat.location.repository.LocationRepository

class LocationProvider : LocationContract.Provider {

    private val locationRepository : LocationContract.Repository

    init {
        locationRepository = LocationRepository(LocationDataSource())
    }

    override fun providesLastLocation(lastLocation: (Location) -> Unit, error: (String) -> Unit) =
        Unit

    override fun removeStoredLocations(success: () -> Unit, error: (String) -> Unit) = Unit

    override fun destroyProvider() = Unit
}