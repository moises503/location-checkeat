package com.checkeat.location.util

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.M)
class PermissionRequester(activity: ComponentActivity,
                          private val permission : String,
                          onDenied: () -> Unit = {},
                          onShowRationale: () -> Unit = {}) {

    private var onGranted: () -> Unit = {}

    private val launcher =
        activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            when {
                isGranted -> onGranted()
                activity.shouldShowRequestPermissionRationale(permission) -> onShowRationale()
                else -> onDenied()
            }
        }

    fun runWithPermission(onGranted: () -> Unit) {
        this.onGranted = onGranted
        launcher.launch(permission)
    }
}