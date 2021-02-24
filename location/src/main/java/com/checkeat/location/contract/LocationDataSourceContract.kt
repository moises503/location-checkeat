package com.checkeat.location.contract

import com.checkeat.location.lib.model.Location

internal interface LocationContract {

    interface DataSource {
        suspend fun storeLocation(location : Location)
        suspend fun retrieveAllLocations() : List<Location>
        suspend fun retrieveLastLocation() : Location?
        suspend fun removeStoredLocations()
        suspend fun updateLocation(location: Location)
    }

    interface Repository {
        suspend fun storeLocation(location : Location)
        suspend fun retrieveAllLocations() : List<Location>
        suspend fun retrieveLastLocation() : Location?
        suspend fun removeStoredLocations()
        suspend fun updateLocation(location: Location)
    }

    interface Provider {
        fun providesLastLocation(lastLocation: (Location?) -> Unit, error: (String) -> Unit)
        fun providesSuspendLastLocation(lastLocation: suspend (Location?) -> Unit)
        fun removeStoredLocations(success: () -> Unit, error: (String) -> Unit)
        fun destroyProvider()
    }
}