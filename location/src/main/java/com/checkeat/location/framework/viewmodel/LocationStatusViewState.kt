package com.checkeat.location.framework.viewmodel

sealed class LocationStatusViewState {
    object SearchLocation: LocationStatusViewState()
    object GetLocation: LocationStatusViewState()
    object Disabled: LocationStatusViewState()
}