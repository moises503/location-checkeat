package com.checkeat.location.framework.viewmodel

import com.checkeat.location.lib.model.Location

sealed class LocationViewState {
    class Error(val message : String) : LocationViewState()
    class Locations(val list : List<Location>) : LocationViewState()
    class LocationStored(val location : Location) : LocationViewState()
}