package com.checkeat.location.lib.provider

import com.checkeat.location.contract.LocationContract
import com.checkeat.location.framework.di.LocationKoinComponent
import com.checkeat.location.lib.model.Location
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import org.koin.core.inject
import kotlin.coroutines.CoroutineContext

class LocationProvider : LocationContract.Provider, CoroutineScope, LocationKoinComponent {

    private val locationRepository : LocationContract.Repository by inject()
    private val job = SupervisorJob()
    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace()
    }

    override fun providesLastLocation(lastLocation: (Location?) -> Unit, error: (String) -> Unit) {
        launch {
            val lastLocationResult = runCatching {
                locationRepository.retrieveLastLocation()
            }
            lastLocationResult.onSuccess {
                lastLocation(it)
            }.onFailure {
                error(it.localizedMessage.orEmpty())
            }
        }
    }

    override fun removeStoredLocations(success: () -> Unit, error: (String) -> Unit) {
        launch {
            val removeStoredLocationsResult = runCatching {
                locationRepository.removeStoredLocations()
            }
            removeStoredLocationsResult.onSuccess {
                success()
            }.onFailure {
                error(it.localizedMessage.orEmpty())
            }
        }
    }

    override suspend fun providesSuspendLastLocation(): Location? {
        return locationRepository.retrieveLastLocation()
    }

    override fun destroyProvider() = job.cancelChildren()

    override val coroutineContext: CoroutineContext
        get() = Main + job + coroutineExceptionHandler
}