package com.checkeat.location.lib.service

import android.location.Location

sealed class ServiceLocationState {
    data class Success(val location: Location): ServiceLocationState()
    object Error: ServiceLocationState()
}
