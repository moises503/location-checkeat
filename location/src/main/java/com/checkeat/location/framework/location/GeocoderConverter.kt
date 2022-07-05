package com.checkeat.location.framework.location

import android.content.Context
import android.location.Geocoder
import android.location.Location
import java.util.*

object GeocoderConverter {
    fun toCheckEatLocation(
        context: Context,
        locale: Locale,
        location: Location
    ): CheckEatLocation? {
        val geocoder = Geocoder(context, locale)
        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        return if (addresses.isNotEmpty()) {
            CheckEatLocation(
                id = 0,
                address = addresses[0].getAddressLine(0),
                latitude = location.latitude,
                longitude = location.longitude,
                city = addresses[0].locality,
                province = addresses[0].adminArea
            )
        } else {
            null
        }
    }
}