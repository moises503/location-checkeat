package com.checkeat.sample

import com.checkeat.location.lib.model.LocationState

interface SessionPreferences {
    fun saveCurrentLocationState(locationState: LocationState)
    fun userHasAcceptedLocationPermissions()
    fun getCurrentLocationState(): LocationState
}