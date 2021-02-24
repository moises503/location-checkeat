package com.checkeat.location.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "latest_locations",
    indices = [Index(value = ["address"], unique = true)])
data class LocationEntity(
    @PrimaryKey(autoGenerate = true)
    val id : Int = 0,
    @ColumnInfo(name = "address")
    val address: String,
    @ColumnInfo(name = "latitude")
    val latitude: Double,
    @ColumnInfo(name = "longitude")
    val longitude: Double,
    @ColumnInfo(name = "city")
    val city: String,
    @ColumnInfo(name = "favorite")
    var favorite: Boolean
)