package com.checkeat.location.contract

import com.checkeat.location.model.Location

internal interface LocationContract {

    interface DataSource {
        suspend fun storeLocation(location : Location)
        suspend fun retrieveAllLocations() : List<Location>
        suspend fun retrieveLastLocation() : Location
    }

    interface Repository {
        suspend fun storeLocation(location : Location)
        suspend fun retrieveAllLocations() : List<Location>
        suspend fun retrieveLastLocation() : Location
    }

    interface Provider {
        fun providesLastLocation(lastLocation: (Location) -> Unit, error: (String) -> Unit)
        fun removeStoredLocations(success: () -> Unit, error: (String) -> Unit)
        fun destroyProvider()
    }
}