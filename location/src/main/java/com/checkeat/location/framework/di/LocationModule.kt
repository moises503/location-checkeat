package com.checkeat.location.framework.di

import android.content.Context
import com.checkeat.location.contract.LocationContract
import com.checkeat.location.datasource.LocationDataSource
import com.checkeat.location.db.CheckEatLocationsDatabase
import com.checkeat.location.db.LocationDao
import com.checkeat.location.repository.LocationRepository
import com.checkeat.location.framework.viewmodel.LocationViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


internal object LocationModule {
    fun providesLocationsDatabase(context: Context): CheckEatLocationsDatabase =
        CheckEatLocationsDatabase.create(context)

    fun providesLocationDao(checkEatLocationsDatabase: CheckEatLocationsDatabase): LocationDao =
        checkEatLocationsDatabase.locationDao()

    fun providesLocationDataSource(locationDao: LocationDao): LocationContract.DataSource =
        LocationDataSource(locationDao)

    fun providesLocationRepository(locationDataSource: LocationContract.DataSource): LocationContract.Repository =
        LocationRepository(locationDataSource)
}

val coreModule = module {
    single { LocationModule.providesLocationsDatabase(get()) }
    single { LocationModule.providesLocationDao(get()) }
    single { LocationModule.providesLocationDataSource(get()) }
    single { LocationModule.providesLocationRepository(get()) }
}

val locationModule = module {
    viewModel { LocationViewModel(get()) }
}