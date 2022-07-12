package com.checkeat.location.lib.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class LocationState(val state: String): Parcelable {
    SEARCH_LOCATION("SEARCH_LOCATION"),
    GET_LOCATION("GET_LOCATION"),
    DISABLED("DISABLED"),
    CLICKED("CLICKED")
}