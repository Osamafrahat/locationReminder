package com.udacity.project4.locationreminders.geofence

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.sendNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext

class GeofenceTransitionsJobIntentService : JobIntentService(), CoroutineScope {

    private var coroutineJob: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + coroutineJob

    companion object {
        private const val JOB_ID = 573
        private const val TAG = "GeofenceTransitions"

        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(
                context,
                GeofenceTransitionsJobIntentService::class.java, JOB_ID,
                intent
            )
        }
    }

    override fun onHandleWork(intent: Intent) {
        val event = GeofencingEvent.fromIntent(intent)

        if (event.hasError()) {
            Log.e(TAG, "Error in Geofencing event: ${event.errorCode}")
            return
        }

        if (event.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            sendNotification(event.triggeringGeofences)
        }
    }

    private fun sendNotification(triggeringGeofences: List<Geofence>) {
        //Get the local repository instance
        val remindersLocalRepository: ReminderDataSource by inject()
        // Interaction to the repository has to be through a coroutine scope
        CoroutineScope(coroutineContext).launch(SupervisorJob()) {
            for (geofence in triggeringGeofences) {
                //get the reminder with the request id
                val result = remindersLocalRepository.getReminder(geofence.requestId)
                if (result is Result.Success<ReminderDTO>) {
                    sendNotification(result)
                }
            }
        }
    }

    private fun sendNotification(result: Result.Success<ReminderDTO>) {
        val reminderDTO = result.data
        //send a notification to the user with the reminder details
        sendNotification(
            this@GeofenceTransitionsJobIntentService, ReminderDataItem(
                reminderDTO.title,
                reminderDTO.description,
                reminderDTO.location,
                reminderDTO.latitude,
                reminderDTO.longitude,
                reminderDTO.id
            )
        )
    }
}