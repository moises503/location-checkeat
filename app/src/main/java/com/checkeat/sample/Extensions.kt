package com.checkeat.sample

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.provider.Settings

fun Context.openSettings() {
    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        addCategory(Intent.CATEGORY_DEFAULT)
        data = Uri.parse("package:$packageName")
    }.let(::startActivity)
}

fun Context.getSharedPreferencesEditor(applicationId: String): SharedPreferences.Editor =
    this.appSharedPreferences(applicationId).edit()

fun Context.appSharedPreferences(applicationId: String): SharedPreferences =
    this.getSharedPreferences(applicationId, Context.MODE_PRIVATE)
