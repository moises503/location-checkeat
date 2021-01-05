package com.checkeat.location

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.checkeat.location.view.LocationServicesFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bindViews()
    }

    private fun bindViews() {
        val locationServices = LocationServicesFragment.newInstance { locationRetrieved ->

        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.lyt_container, locationServices)
            .commitAllowingStateLoss()
    }
}