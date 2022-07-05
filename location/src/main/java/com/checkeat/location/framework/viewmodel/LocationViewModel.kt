package com.checkeat.location.framework.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.checkeat.location.contract.LocationContract
import com.checkeat.location.lib.model.Location
import com.checkeat.location.util.ScreenState
import kotlinx.coroutines.launch

internal class LocationViewModel(private val locationRepository: LocationContract.Repository) :
    ViewModel() {
    private val _locationViewState = MutableLiveData<ScreenState<LocationViewState>>()
    private val _locationStatusViewState = MutableLiveData<LocationStatusViewState>()
    val locationViewState: LiveData<ScreenState<LocationViewState>>
        get() = _locationViewState
    val locationStatusViewState: LiveData<LocationStatusViewState>
        get() = _locationStatusViewState

    fun storeLocation(location: Location?) {
        location?.let { safeLocation ->
            viewModelScope.launch {
                val storeLocationResult = runCatching { locationRepository.storeLocation(location) }
                storeLocationResult.onSuccess {
                    _locationViewState.postValue(
                        ScreenState.Render(
                            LocationViewState.LocationStored(
                                safeLocation
                            )
                        )
                    )
                }.onFailure {
                    _locationViewState.postValue(ScreenState.Render(LocationViewState.Error(it.localizedMessage.orEmpty())))
                }
            }
        } ?: _locationViewState.postValue(ScreenState.Render(LocationViewState.Error("")))
    }

    fun updateLocation(location: Location) {
        viewModelScope.launch {
            val storeLocationResult = runCatching { locationRepository.updateLocation(location) }
            storeLocationResult.onSuccess {
                _locationViewState.postValue(ScreenState.Render(LocationViewState.LocationUpdated))
            }.onFailure {
                _locationViewState.postValue(ScreenState.Render(LocationViewState.Error(it.localizedMessage.orEmpty())))
            }
        }
    }

    fun retrieveLocationList() {
        viewModelScope.launch {
            val allLocationsResult = runCatching { locationRepository.retrieveAllLocations() }
            allLocationsResult.onSuccess {
                _locationViewState.postValue(ScreenState.Render(LocationViewState.Locations(it)))
            }.onFailure {
                _locationViewState.postValue(ScreenState.Render(LocationViewState.Error(it.localizedMessage.orEmpty())))
            }
        }
    }

    fun isLocationEnabled(isEnabled: Boolean) {
        if (isEnabled) {
            _locationStatusViewState.postValue(LocationStatusViewState.Enabled)
        } else {
            _locationStatusViewState.postValue(LocationStatusViewState.Disabled)
        }
    }
}