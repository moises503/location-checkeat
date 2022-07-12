package com.checkeat.location.lib

import android.content.Context
import com.checkeat.location.framework.di.coreModule
import com.checkeat.location.framework.di.locationModule
import com.checkeat.location.framework.view.LocationDisclaimerCallbackContract
import com.checkeat.location.lib.model.Location
import com.checkeat.location.lib.provider.LocationProvider
import com.checkeat.location.framework.view.LocationServicesFragment
import com.checkeat.location.lib.model.LocationState
import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinApplication

object LocationLibrary {
    internal val koinApplication = KoinApplication.create()

    fun init(context: Context) {
        koinApplication.apply {
            androidContext(context)
            modules(listOf(coreModule, locationModule))
        }
    }

    fun locationServices(
        onLocationRetrieved: (Location, LocationState) -> Unit,
        onProvidePermission: () -> Unit,
        onAgreementCalled: (LocationDisclaimerCallbackContract) -> Unit = {},
        locationState: LocationState = LocationState.DISABLED,
        googleKey: String
    ): LocationServicesFragment {
        return LocationServicesFragment.newInstance(onLocationRetrieved, onProvidePermission, onAgreementCalled, locationState, googleKey)
    }

    fun locationProvider(): LocationProvider {
        return LocationProvider()
    }
}