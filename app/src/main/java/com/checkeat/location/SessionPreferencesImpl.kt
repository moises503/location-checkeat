package com.checkeat.location

import android.content.Context
import com.checkeat.location.lib.model.LocationState

class SessionPreferencesImpl(context: Context): SessionPreferences {

    private val editor = context.getSharedPreferencesEditor(PACKAGE_ID)
    private val preferences = context.appSharedPreferences(PACKAGE_ID)

    override fun saveCurrentLocationState(locationState: LocationState) {
        editor.putString(LOCATION_STATE, locationState.state)
        editor.apply()
    }

    override fun userHasAcceptedLocationPermissions() {
        editor.putBoolean(LOCATION_PERMISSION_PROVIDED, true)
        editor.apply()
    }

    override fun getCurrentLocationState(): LocationState {
        val currentState = preferences.getString(LOCATION_STATE, "").orEmpty()
        val userHasAcceptedPermissions = preferences.getBoolean(LOCATION_PERMISSION_PROVIDED, false)
        return when {
            currentState.isEmpty() && !userHasAcceptedPermissions -> LocationState.DISABLED
            currentState.isNotEmpty() && !userHasAcceptedPermissions -> LocationState.SEARCH_LOCATION
            currentState.isNotEmpty() && userHasAcceptedPermissions -> LocationState.GET_LOCATION
            else -> LocationState.DISABLED
        }
    }

    companion object {
        private const val PACKAGE_ID = "com.checkeat.location"
        private const val LOCATION_STATE = "LOCATION_STATE"
        private const val LOCATION_PERMISSION_PROVIDED = "LOCATION_PERMISSION_PROVIDED"
    }
}