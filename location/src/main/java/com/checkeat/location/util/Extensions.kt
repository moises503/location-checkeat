package com.checkeat.location.util

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.checkeat.location.db.LocationEntity
import com.checkeat.location.lib.model.Location

fun Location.toLocationEntity(): LocationEntity {
    return LocationEntity(
        address = this.address,
        latitude = this.latitude,
        longitude = this.longitude,
        city = this.city
    )
}

fun LocationEntity.toLocation(): Location {
    return Location(
        address = this.address,
        latitude = this.latitude,
        longitude = this.longitude,
        city = this.city
    )
}

fun List<LocationEntity>.toLocationList(): List<Location> {
    return this.map {
        Location(
            address = it.address,
            latitude = it.latitude,
            longitude = it.longitude,
            city = it.city
        )
    }
}

fun Context.longToast(message : String) =
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()

fun ViewGroup.inflate(layoutId : Int) : View = LayoutInflater.from(context)
    .inflate(layoutId, this, false)