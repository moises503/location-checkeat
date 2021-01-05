package com.checkeat.location.view

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.checkeat.location.databinding.FragmentLocationServicesBinding
import com.checkeat.location.model.Location
import com.checkeat.location.util.PermissionRequester

open class LocationServicesFragment : Fragment() {

    private var onLocationRetrieved : (Location) -> Unit = {}
    private lateinit var locationServicesBinding: FragmentLocationServicesBinding
    private lateinit var requestAccessFineLocation : PermissionRequester

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        locationServicesBinding = FragmentLocationServicesBinding.inflate(inflater, container, false)
        return locationServicesBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews()
    }

    private fun bindViews() = with(locationServicesBinding) {
        if (Build.VERSION.SDK_INT >= 23) {
            requestAccessFineLocation = PermissionRequester(requireActivity(),
                ACCESS_FINE_LOCATION, onDenied = {}, onShowRationale = {})
            btnLocationBasedOnGps.setOnClickListener {
                requestAccessFineLocation.runWithPermission {

                }
            }
            btnSearch.setOnClickListener {
                requestAccessFineLocation.runWithPermission {

                }
            }
        } else {

        }
    }

    companion object {
        fun newInstance(onLocationRetrieved : (Location) -> Unit) =
            LocationServicesFragment().apply {
                this.onLocationRetrieved = onLocationRetrieved
            }
    }
}