package com.checkeat.location.framework.view

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.Context.LOCATION_SERVICE
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.checkeat.location.databinding.FragmentLocationServicesBinding
import com.checkeat.location.framework.di.LocationKoinComponent
import com.checkeat.location.framework.location.GPSLocation
import com.checkeat.location.framework.viewmodel.LocationViewModel
import com.checkeat.location.framework.viewmodel.LocationViewState
import com.checkeat.location.lib.model.Location
import com.checkeat.location.util.BaseFragment
import com.checkeat.location.util.PermissionRequester
import com.checkeat.location.util.ScreenState
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.checkeat.location.R
import com.checkeat.location.framework.view.adapter.StoredLocationsAdapter
import com.checkeat.location.util.longToast

class LocationServicesFragment : BaseFragment<ScreenState<LocationViewState>>(),
    LocationKoinComponent {

    private var onLocationRetrieved: (Location) -> Unit = {}
    private lateinit var locationServicesBinding: FragmentLocationServicesBinding
    private lateinit var requestAccessFineLocation: PermissionRequester
    private val locationViewModel: LocationViewModel by viewModel()
    private lateinit var gpsLocation: GPSLocation
    private lateinit var locationManager: LocationManager
    private lateinit var storedLocationAdapter: StoredLocationsAdapter

    override fun bindFragmentView(inflater: LayoutInflater, container: ViewGroup?): View {
        locationServicesBinding =
            FragmentLocationServicesBinding.inflate(inflater, container, false)
        return locationServicesBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        attachObservers()
        bindViews()
    }

    @SuppressLint("MissingPermission")
    override fun bindViews() = with(locationServicesBinding) {
        locationManager = requireActivity().getSystemService(LOCATION_SERVICE) as LocationManager
        gpsLocation = GPSLocation(
            locationRetrieved = {
                locationViewModel.storeLocation(it)
            },
            context = requireContext(),
            locale = Locale.getDefault()
        )
        storedLocationAdapter = StoredLocationsAdapter(locationSelected = {
            onLocationRetrieved(it)
        })
        lstStoredLocations.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = storedLocationAdapter
        }
        locationViewModel.retrieveLocationList()
        if (Build.VERSION.SDK_INT >= 23) {
            requestAccessFineLocation = PermissionRequester(requireActivity(),
                ACCESS_FINE_LOCATION, onDenied = {}, onShowRationale = {})
            btnLocationBasedOnGps.setOnClickListener {
                requestAccessFineLocation.runWithPermission {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        MIN_TIME_IN_MILLIS,
                        MIN_DISTANCE,
                        gpsLocation
                    )
                }
            }
        } else {
            requireContext().longToast(getString(R.string.location_notice))
        }
    }

    override fun attachObservers() {
        locationViewModel.locationViewState.observe(viewLifecycleOwner, Observer {
            renderScreenState(it)
        })
    }

    override fun renderScreenState(screenState: ScreenState<LocationViewState>) {
        when (screenState) {
            is ScreenState.Render -> renderLocations(screenState.data)
            else -> showError(getString(R.string.location_general_error))
        }
    }

    override fun showError(message: String) {
        requireContext().longToast(message)
    }

    @SuppressLint("MissingPermission")
    override fun onDestroy() {
        super.onDestroy()
        locationManager.removeUpdates(gpsLocation)
    }

    private fun renderLocations(locationViewState: LocationViewState) {
        when (locationViewState) {
            is LocationViewState.LocationStored -> {
                onLocationRetrieved(locationViewState.location)
                locationViewModel.retrieveLocationList()
            }
            is LocationViewState.Locations -> {
                storedLocationAdapter.updateDataSet(locationViewState.list.toMutableList())
            }
            is LocationViewState.Error -> {
                showError(getString(R.string.location_general_error))
            }
        }
    }

    companion object {
        fun newInstance(onLocationRetrieved: (Location) -> Unit) =
            LocationServicesFragment().apply {
                this.onLocationRetrieved = onLocationRetrieved
            }

        const val MIN_TIME_IN_MILLIS = 5000L
        const val MIN_DISTANCE = 5f
    }
}