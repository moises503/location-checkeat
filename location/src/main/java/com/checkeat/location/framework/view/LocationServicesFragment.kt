package com.checkeat.location.framework.view

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.checkeat.location.R
import com.checkeat.location.databinding.FragmentLocationServicesBinding
import com.checkeat.location.framework.di.LocationKoinComponent
import com.checkeat.location.framework.location.GeocoderConverter
import com.checkeat.location.framework.view.adapter.PlacesFoundAdapter
import com.checkeat.location.framework.view.adapter.SearchPlaceTextWatcher
import com.checkeat.location.framework.view.adapter.StoredLocationsAdapter
import com.checkeat.location.framework.viewmodel.LocationStatusViewState
import com.checkeat.location.framework.viewmodel.LocationViewModel
import com.checkeat.location.framework.viewmodel.LocationViewState
import com.checkeat.location.lib.model.Location
import com.checkeat.location.lib.model.LocationState
import com.checkeat.location.lib.service.ObtainLastLocationService
import com.checkeat.location.lib.service.ServiceLocationState
import com.checkeat.location.util.*
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

@RequiresApi(Build.VERSION_CODES.M)
class LocationServicesFragment : BaseFragment<ScreenState<LocationViewState>>(),
    LocationKoinComponent, LocationDisclaimerCallbackContract {

    private var onLocationRetrieved: (Location, LocationState) -> Unit = { _, _ -> }
    private var onProvidePermission: () -> Unit = {}
    private var onAgreementCalled: (LocationDisclaimerCallbackContract) -> Unit = {}
    private var googleKey: String = ""
    private lateinit var locationServicesBinding: FragmentLocationServicesBinding
    private val locationViewModel: LocationViewModel by viewModel()
    private lateinit var storedLocationAdapter: StoredLocationsAdapter
    private lateinit var placesFoundAdapter: PlacesFoundAdapter
    private lateinit var requestAccessFineLocation: PermissionRequester
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var obtainLastLocationService: ObtainLastLocationService? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val obtainLastLocationBinder = service as? ObtainLastLocationService.ObtainLastLocationBinder
            obtainLastLocationService = obtainLastLocationBinder?.service
            handleLastLocationResult()
        }

        override fun onServiceDisconnected(name: ComponentName?) = Unit
    }

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

    override fun bindViews() = with(locationServicesBinding) {
        locationViewModel.checkLocationStatus(
            arguments?.getParcelable(LOCATION_STATE) ?: LocationState.DISABLED
        )
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        storedLocationAdapter = StoredLocationsAdapter(locationSelected = {
            locationViewModel.updateLocation(it)
            onLocationRetrieved(it, LocationState.CLICKED)
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
        } else {
            requireContext().longToast(getString(R.string.location_notice))
        }
    }

    override fun attachObservers() {
        locationViewModel.locationViewState.observe(viewLifecycleOwner, Observer {
            renderScreenState(it)
        })
        locationViewModel.locationStatusViewState.observe(viewLifecycleOwner, Observer {
            renderLocationStatus(it)
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

    private fun renderLocationStatus(locationStatusViewState: LocationStatusViewState) {
        with(locationServicesBinding) {
            when (locationStatusViewState) {
                is LocationStatusViewState.SearchLocation -> {
                    btnLocationBasedOnGps.text = getString(R.string.enable_my_location)
                    btnLocationBasedOnGps.setOnClickListener {
                        onAgreementCalled(this@LocationServicesFragment)
                    }
                }
                is LocationStatusViewState.GetLocation -> {
                    btnLocationBasedOnGps.text = getString(R.string.location_based_on_gps)
                    btnLocationBasedOnGps.setOnClickListener {
                        onAgreementAccepted()
                    }
                }
                is LocationStatusViewState.Disabled -> {
                    edtSearchPlace.requestFocus()
                    btnLocationBasedOnGps.text = getString(R.string.enable_my_location)
                    btnLocationBasedOnGps.setOnClickListener {
                        onAgreementCalled(this@LocationServicesFragment)
                    }
                }
            }
        }
    }

    private fun renderLocations(locationViewState: LocationViewState) {
        when (locationViewState) {
            is LocationViewState.LocationStored -> {
                onLocationRetrieved(locationViewState.location, locationViewState.state)
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
            locationViewModel.storeLocation(it, LocationState.SEARCH_LOCATION)
        }
    }

    companion object {
        fun newInstance(
            onLocationRetrieved: (Location, LocationState) -> Unit,
            onProvidePermission: () -> Unit,
            onAgreementCalled: (LocationDisclaimerCallbackContract) -> Unit = {},
            locationState: LocationState = LocationState.DISABLED,
            googleKey: String
        ) =
            LocationServicesFragment().apply {
                this.onProvidePermission = onProvidePermission
                this.onLocationRetrieved = onLocationRetrieved
                this.onAgreementCalled = onAgreementCalled
                this.googleKey = googleKey
                arguments = bundleOf(LOCATION_STATE to locationState)
            }

        const val TAG = "LocationServices"
        private const val LOCATION_STATE = "LOCATION_STATE"
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

    @SuppressLint("MissingPermission")
    override fun onAgreementAccepted() {
        requestAccessFineLocation.runWithPermission {
            bindService()
        }
    }

    private fun handleLastLocationResult() {
        obtainLastLocationService?.serviceLocationState?.observe(viewLifecycleOwner) { state ->
            when(state) {
                is ServiceLocationState.Success -> {
                    locationViewModel.storeLocation(
                        GeocoderConverter.toCheckEatLocation(
                            requireContext(),
                            Locale.getDefault(),
                            state.location
                        ),
                        LocationState.GET_LOCATION
                    )
                    unbindService()
                }
                is ServiceLocationState.Error -> {
                    locationViewModel.storeLocation(null, LocationState.DISABLED)
                    unbindService()
                }
            }
        }
    }

    private fun bindService() {
        Intent(requireContext(), ObtainLastLocationService::class.java).also { intent ->
            requireActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    private fun unbindService() {
        Intent(requireContext(), ObtainLastLocationService::class.java).also {
            requireActivity().unbindService(serviceConnection)
        }
    }
}