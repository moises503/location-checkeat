package com.checkeat.sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.checkeat.location.framework.view.LocationDisclaimerCallbackContract
import com.checkeat.location.lib.LocationLibrary

class MainActivity : AppCompatActivity() {

    private lateinit var sessionPreferences: SessionPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bindViews()
    }

    private fun bindViews() {
        LocationLibrary.init(applicationContext)
        sessionPreferences = SessionPreferencesImpl(applicationContext)
        val locationServices =
            LocationLibrary.locationServices(onLocationRetrieved = { location, state ->
                Toast.makeText(this, location.address, Toast.LENGTH_LONG).show()
                sessionPreferences.saveCurrentLocationState(state)
            }, onAgreementCalled = { disclaimerCallback ->
                showPopupMessage(disclaimerCallback)
            }, locationState = sessionPreferences.getCurrentLocationState(),
                onProvidePermission = {
                    openSettings()
                }, googleKey = "AIzaSyCpEJu45h811XXT13HqBoePWAmrJnhB64U"
            )

        val locationProvider = LocationLibrary.locationProvider()

        locationProvider.providesLastLocation(lastLocation = { location ->
            location?.let {
                Toast.makeText(this, it.address, Toast.LENGTH_LONG).show()
            } ?: let {
                Toast.makeText(this, "Location not found", Toast.LENGTH_LONG).show()
            }
        }, error = {
            Toast.makeText(this, "An error occurred $it", Toast.LENGTH_LONG).show()
        })

        supportFragmentManager.beginTransaction()
            .replace(R.id.lyt_container, locationServices)
            .commitAllowingStateLoss()
    }

    private fun showPopupMessage(callback: LocationDisclaimerCallbackContract) {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle(getString(R.string.disclaimer_title))
        dialog.setMessage(getString(R.string.disclaimer_description))
        dialog.setCancelable(false)
        dialog.setPositiveButton(getString(R.string.continue_text)) { dialogView, _ ->
            sessionPreferences.userHasAcceptedLocationPermissions()
            callback.onAgreementAccepted()
            dialogView.dismiss()
        }
        dialog.setNegativeButton(getString(R.string.cancel)) { dialogView, _ ->
            dialogView.dismiss()
        }
        dialog.show()
    }
}