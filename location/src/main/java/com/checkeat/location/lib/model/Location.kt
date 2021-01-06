package com.checkeat.location.lib.model

import android.os.Parcel
import android.os.Parcelable
import com.checkeat.location.util.readStringSafe


data class Location(
    val address: String,
    val city: String,
    val latitude: Double,
    val longitude: Double) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readStringSafe(),
        parcel.readStringSafe(),
        parcel.readDouble(),
        parcel.readDouble()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(address)
        parcel.writeString(city)
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Location> {
        override fun createFromParcel(parcel: Parcel): Location {
            return Location(parcel)
        }

        override fun newArray(size: Int): Array<Location?> {
            return arrayOfNulls(size)
        }
    }

}