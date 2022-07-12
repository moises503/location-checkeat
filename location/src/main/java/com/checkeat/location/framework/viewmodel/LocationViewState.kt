package com.checkeat.location.framework.viewmodel

import com.checkeat.location.lib.model.Location
import com.checkeat.location.lib.model.LocationState

sealed class LocationViewState {
    class Error(val message : String) : LocationViewState()
    class Locations(val list : List<Location>) : LocationViewState()
    class LocationStored(val location : Location, val state: LocationState) : LocationViewState()
    object LocationUpdated : LocationViewState()
}