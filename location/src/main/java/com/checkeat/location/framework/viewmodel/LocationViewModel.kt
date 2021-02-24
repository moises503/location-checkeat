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
    val locationViewState: LiveData<ScreenState<LocationViewState>>
        get() = _locationViewState

    fun storeLocation(location: Location) {
        viewModelScope.launch {
            val storeLocationResult = runCatching { locationRepository.storeLocation(location) }
            storeLocationResult.onSuccess {
                _locationViewState.postValue(ScreenState.Render(LocationViewState.LocationStored(location)))
            }.onFailure {
                _locationViewState.postValue(ScreenState.Render(LocationViewState.Error(it.localizedMessage.orEmpty())))
            }
        }
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
}