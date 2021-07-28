package com.checkeat.location.framework.view

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context.LOCATION_SERVICE
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.checkeat.location.R
import com.checkeat.location.databinding.FragmentLocationServicesBinding
import com.checkeat.location.framework.di.LocationKoinComponent
import com.checkeat.location.framework.location.GPSLocation
import com.checkeat.location.framework.view.adapter.PlacesFoundAdapter
import com.checkeat.location.framework.view.adapter.SearchPlaceTextWatcher
import com.checkeat.location.framework.view.adapter.StoredLocationsAdapter
import com.checkeat.location.framework.viewmodel.LocationViewModel
import com.checkeat.location.framework.viewmodel.LocationViewState
import com.checkeat.location.lib.model.Location
import com.checkeat.location.util.*
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

@RequiresApi(Build.VERSION_CODES.M)
class LocationServicesFragment : BaseFragment<ScreenState<LocationViewState>>(),
    LocationKoinComponent {

    private var onLocationRetrieved: (Location) -> Unit = {}
    private var onProvidePermission: () -> Unit = {}
    private var googleKey: String = ""
    private lateinit var locationServicesBinding: FragmentLocationServicesBinding
    private val locationViewModel: LocationViewModel by viewModel()
    private lateinit var gpsLocation: GPSLocation
    private lateinit var locationManager: LocationManager
    private lateinit var storedLocationAdapter: StoredLocationsAdapter
    private lateinit var placesFoundAdapter: PlacesFoundAdapter
    private lateinit var requestAccessFineLocation: PermissionRequester

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
            locationViewModel.updateLocation(it)
            onLocationRetrieved(it)
        })
        lstStoredLocations.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = storedLocationAdapter
        }
        locationViewModel.retrieveLocationList()
        initPlacesClient()
        if (Build.VERSION.SDK_INT >= 23) {
            requestAccessFineLocation = PermissionRequester(requireActivity(),
                ACCESS_FINE_LOCATION, onDenied = {
                    showPermissionRequestDialog()
                }, onShowRationale = {
                    showPermissionRequestDialog()
                })
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
            is LocationViewState.LocationUpdated -> {
                locationViewModel.retrieveLocationList()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun initPlacesClient() = with(locationServicesBinding) {
        if (googleKey.isEmpty()) {
            throw IllegalArgumentException("Google key is missing")
        }
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), googleKey)
        }
        val placesClient = Places.createClient(requireContext())
        val token = AutocompleteSessionToken.newInstance()
        val placeFields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.LAT_LNG,
            Place.Field.ADDRESS,
            Place.Field.ADDRESS_COMPONENTS
        )
        placesFoundAdapter = PlacesFoundAdapter(onPredictionClicked = {
            edtSearchPlace.text.clear()
            retrieveLocationFromPrediction(it, placesClient, placeFields)
        })
        lstPlacesFound.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = placesFoundAdapter
        }
        edtSearchPlace.addTextChangedListener(object : SearchPlaceTextWatcher {
            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                if (s.isNullOrEmpty()) {
                    lstPlacesFound.gone()
                    txtOccurrences.gone()
                } else {
                    lstPlacesFound.visible()
                    txtOccurrences.visible()
                    val request =
                        FindAutocompletePredictionsRequest.builder()
                            .setCountry("MX")
                            .setSessionToken(token)
                            .setQuery(s.toString())
                            .build()
                    placesClient.findAutocompletePredictions(request)
                        .addOnSuccessListener { response: FindAutocompletePredictionsResponse ->
                            placesFoundAdapter.updateDataSet(response.autocompletePredictions)
                        }.addOnFailureListener { exception: Exception? ->
                            if (exception is ApiException) {
                                Log.e(
                                    TAG,
                                    "Place not found: " + exception.statusCode
                                )
                            }
                        }
                }
            }
        })
    }

    private fun retrieveLocationFromPrediction(
        prediction: AutocompletePrediction,
        placesClient: PlacesClient,
        placeFields: List<Place.Field>
    ) {
        val request =
            FetchPlaceRequest.builder(prediction.placeId, placeFields)
                .build()
        placesClient.fetchPlace(request)
            .addOnSuccessListener { response: FetchPlaceResponse ->
                saveAddress(response.place)
            }.addOnFailureListener { exception: java.lang.Exception ->
                if (exception is ApiException) {
                    Log.e(
                        TAG,
                        "Place not found: " + exception.message
                    )
                }
            }
    }

    private fun saveAddress(place: Place) {
        place.toLocation()?.let {
            locationViewModel.storeLocation(it)
        }
    }

    companion object {
        fun newInstance(
            onLocationRetrieved: (Location) -> Unit,
            onProvidePermission: () -> Unit,
            googleKey: String
        ) =
            LocationServicesFragment().apply {
                this.onProvidePermission = onProvidePermission
                this.onLocationRetrieved = onLocationRetrieved
                this.googleKey = googleKey
            }

        const val MIN_TIME_IN_MILLIS = 5000L
        const val MIN_DISTANCE = 5f
        const val TAG = "LocationServices"
    }

    private fun showPermissionRequestDialog() {
        AlertDialog.Builder(requireContext()).apply {
            setTitle(getString(R.string.location_permission_required))
            setMessage(getString(R.string.location_permission_message))
            setPositiveButton(getString(R.string.location_permission_ok)) { dialog, _ ->
                onProvidePermission()
                dialog.dismiss()
            }
            setNegativeButton(getString(R.string.location_permission_cancel)) { dialog, _ ->
                dialog.dismiss()
            }
        }.show()
    }
}