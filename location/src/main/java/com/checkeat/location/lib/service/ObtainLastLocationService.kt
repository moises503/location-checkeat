package com.checkeat.location.lib.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.checkeat.location.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

@SuppressLint("MissingPermission")
class ObtainLastLocationService : Service() {

    private val binder: IBinder = ObtainLastLocationBinder()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val _serviceLocationState = MutableLiveData<ServiceLocationState>()
    val serviceLocationState: LiveData<ServiceLocationState> get() = _serviceLocationState

    inner class ObtainLastLocationBinder : Binder() {
        val service: ObtainLastLocationService get() = this@ObtainLastLocationService
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        startNotification()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext)
        val priority = Priority.PRIORITY_BALANCED_POWER_ACCURACY
        val cancellationTokenSource = CancellationTokenSource()
        fusedLocationClient.getCurrentLocation(priority, cancellationTokenSource.token)
            .addOnSuccessListener { location ->
                _serviceLocationState.postValue(ServiceLocationState.Success(location))
            }
            .addOnFailureListener {
                _serviceLocationState.postValue(ServiceLocationState.Error)
            }
    }

    /**
     * Used for creating and starting notification
     * whenever we start our Bound service
     */
    private fun startNotification() {
        val notificationStrategy = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                LOCATION_CHANNEL_ID,
                "Location channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
                channel
            )
            NotificationCompat.Builder(this, LOCATION_CHANNEL_ID)
                .setContentTitle(applicationContext.getString(R.string.obtaining_location_title))
                .setContentText(applicationContext.getString(R.string.obtaining_location_message)).build()
        } else {
            NotificationCompat.Builder(this)
                .setContentTitle(applicationContext.getString(R.string.obtaining_location_title))
                .setContentText(applicationContext.getString(R.string.obtaining_location_message)).build()
        }
        startForeground(1, notificationStrategy)
    }

    companion object {
        private const val LOCATION_CHANNEL_ID = "LOCATION_CHANNEL_ID"
    }
}
