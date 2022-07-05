package com.checkeat.location.framework.viewmodel

sealed class LocationStatusViewState {
    object Enabled: LocationStatusViewState()
    object Disabled: LocationStatusViewState()
}