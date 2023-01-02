package com.udacity.project4.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.view.View
import androidx.core.app.ActivityCompat
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R

fun isAndroidQ() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

fun isPermissionGranted(context: Context) : Boolean {
    val accessFineLocation = isAccessFineLocationGranted(context)

    val accessBackgroundLocation = if (isAndroidQ()) {
        ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) === PackageManager.PERMISSION_GRANTED
    } else true

    return accessFineLocation && accessBackgroundLocation
}

fun isAccessFineLocationGranted(context: Context): Boolean {
    return ActivityCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) === PackageManager.PERMISSION_GRANTED
}

fun displayLocationPermissionError(view: View, context: Context) {
    Snackbar.make(
        view,
        context.getString(R.string.permission_denied_explanation),
        Snackbar.LENGTH_LONG
    ).show()
}