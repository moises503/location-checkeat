package com.checkeat.location

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.checkeat.location.lib.LocationLibrary

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bindViews()
    }

    private fun bindViews() {
        LocationLibrary.init(applicationContext)

        val locationServices = LocationLibrary.locationServices(onLocationRetrieved = {
            Toast.makeText(this, it.address, Toast.LENGTH_LONG).show()
        }, onProvidePermission = {
            openSettings()
        }, googleKey = "AIzaSyCpEJu45h811XXT13HqBoePWAmrJnhB64U")

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
}