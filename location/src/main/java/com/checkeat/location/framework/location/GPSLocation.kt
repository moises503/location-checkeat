package com.checkeat.location.framework.location

import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import java.util.*

typealias CheckEatLocation = com.checkeat.location.lib.model.Location

class GPSLocation(
        private val locationRetrieved: (CheckEatLocation) -> Unit,
        private val context: Context,
        private val locale: Locale
) : LocationListener {

    override fun onLocationChanged(location: Location) {
        locationRetrieved(retrieveLocation(location))
    }

    private fun retrieveLocation(location: Location): CheckEatLocation {
        val geocoder = Geocoder(context, locale)
        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        return CheckEatLocation(
                id = 0,
                address = addresses[0].getAddressLine(0),
                latitude = location.latitude,
                longitude = location.longitude,
                city = addresses[0].locality,
                province = addresses[0].adminArea
        )
    }
}