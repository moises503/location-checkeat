package com.checkeat.location.framework.location

import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.util.Log
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

        if (addresses.size != 0) {
            return CheckEatLocation(
                id = 0,
                address = addresses[0].getAddressLine(0),
                latitude = location.latitude,
                longitude = location.longitude,
                city = addresses[0].locality,
                province = addresses[0].adminArea
            )
        } else {
            return CheckEatLocation(
                id = 0,
                address = "null",
                latitude = 0.000000000000000,
                longitude = 0.000000000000000,
                city = "null",
                province = "null"
            )
        }
    }
}