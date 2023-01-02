package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.displayLocationPermissionError
import com.udacity.project4.utils.isAndroidQ
import com.udacity.project4.utils.isPermissionGranted
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.util.concurrent.TimeUnit

class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by sharedViewModel()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var geofencingClient: GeofencingClient

    private var reminderDataItem: ReminderDataItem? = null

    // A PendingIntent for the Broadcast Receiver that handles geofence transitions.
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(requireContext(), 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel
        geofencingClient = LocationServices.getGeofencingClient(requireContext())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            // Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            reminderDataItem = _viewModel.toReminderDataItem()
            if (reminderDataItem!= null && _viewModel.validateEnteredData(reminderDataItem!!)) {
                checkPermissionsAndSaveData()
            }
        }
    }

    private fun checkPermissionsAndSaveData() {
        if (isPermissionGranted(requireContext())) {
            addGeofenceAndSaveReminder()
        } else {
            requestPermissions()
        }
    }

    private fun addGeofenceAndSaveReminder() {
        checkDeviceLocationServices()
        reminderDataItem?.let { _viewModel.validateAndSaveReminder(it) }
    }

    private fun checkDeviceLocationServices(showLocationSettingsDialog: Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val locationSettingsBuilder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val locationSettingsClient = LocationServices.getSettingsClient(requireContext())
        val locationTask = locationSettingsClient.checkLocationSettings(locationSettingsBuilder.build())

        locationTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && showLocationSettingsDialog) {
                exception.startResolutionForResult(
                    requireActivity(), REQUEST_DEVICE_LOCATION
                )
            } else {
                Snackbar.make(this.view!!, R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(R.string.retry) {
                    checkDeviceLocationServices()
                }.show()
            }
        }
        locationTask.addOnCompleteListener {
            if (it.isSuccessful) {
                addGeofenceRequest()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_DEVICE_LOCATION) {
            checkDeviceLocationServices(false)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                addGeofenceAndSaveReminder()
            } else {
                displayLocationPermissionError(this.view!!, requireContext())
            }
        }
    }

    private fun requestPermissions() {
        if (isAndroidQ()) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ),
                REQUEST_LOCATION_PERMISSION
            )
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun addGeofenceRequest() {
        reminderDataItem?.let { reminder ->
            val geofence = Geofence.Builder()
                .setRequestId(reminder.id)
                .setCircularRegion(reminder.latitude!!,
                    reminder.longitude!!,
                    GEOFENCE_RADIUS_IN_METERS
                )
                .setExpirationDuration(GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build()

            val geofencingRequest = GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build()

            // Need to remove the other intents before adding a new one, otherwise the notification
            // is never displayed.
            geofencingClient.removeGeofences(geofencePendingIntent)?.run {
                addOnCompleteListener {
                    geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)
                        .addOnSuccessListener { Log.d(TAG, "Geofence added with success!" ) }
                        .addOnFailureListener { Log.e(TAG, "Error adding geofence: ${it.stackTrace}")}
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    companion object {
        private const val TAG = "SaveReminderFragment"
        private const val GEOFENCE_RADIUS_IN_METERS = 500f
        internal const val ACTION_GEOFENCE_EVENT =
            "SaveReminderFragment.action.ACTION_GEOFENCE_EVENT"
        val GEOFENCE_EXPIRATION_IN_MILLISECONDS: Long = TimeUnit.HOURS.toMillis(1)
        private const val REQUEST_LOCATION_PERMISSION = 1
        private const val REQUEST_DEVICE_LOCATION = 2
    }
}
