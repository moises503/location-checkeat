package com.checkeat.location.framework.di

import com.checkeat.location.lib.LocationLibrary
import org.koin.core.Koin
import org.koin.core.KoinComponent

internal interface LocationKoinComponent: KoinComponent {
    override fun getKoin(): Koin {
        return LocationLibrary.koinApplication.koin
    }
}