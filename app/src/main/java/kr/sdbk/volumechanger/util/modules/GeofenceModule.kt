package kr.sdbk.volumechanger.util.modules

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kr.sdbk.volumechanger.broadcast.GeoFencingBroadcastReceiver
import kr.sdbk.volumechanger.data.room.entity.LocationEntity
import kr.sdbk.volumechanger.util.Constants
import kr.sdbk.volumechanger.util.toLatLng
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class GeofenceModule(
    private val context: Context
) {
    private val client = LocationServices.getGeofencingClient(context)

    fun addGeofencing(
        locationEntityList: List<LocationEntity>,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            CoroutineScope(Dispatchers.IO).launch {
                val taskList = locationEntityList.map { locationEntity ->
                    val pendingIntent = getGeofencePendingIntent(locationEntity)
                    val geofence = getGeofence(locationEntity)
                    val request = getGeofencingRequest(geofence)

                    async {
                        suspendCoroutine { continuation ->
                            client.addGeofences(request, pendingIntent).run {
                                addOnSuccessListener {
                                    continuation.resume(true)
                                }
                                addOnFailureListener {
                                    continuation.resume(false)
                                }
                            }
                        }
                    }
                }
                val res = taskList.awaitAll()
                if (res.all { it }) onSuccess()
                else onFailure()
            }
        }
    }

    fun removeGeofencing(
        locationEntityList: List<LocationEntity>,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            client.removeGeofences(locationEntityList.map { it.created.toString() }).run {
                addOnSuccessListener { onSuccess() }
                addOnFailureListener { onFailure() }
            }
        }
    }

    private fun getGeofence(locationEntity: LocationEntity): Geofence {
        val location = locationEntity.location.toLatLng()
        return Geofence.Builder()
            .setRequestId(locationEntity.created.toString())
            .setCircularRegion(
                location.latitude,
                location.longitude,
                locationEntity.range.toFloat()
            )
            .setNotificationResponsiveness(30000)
            .setLoiteringDelay(30000)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()
    }

    private fun getGeofencePendingIntent(data: LocationEntity): PendingIntent {
        val intent = Intent(context, GeoFencingBroadcastReceiver::class.java).apply {
            putExtra(Constants.LOCATION_ENTITY, data)
        }
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun getGeofencingRequest(geofence: Geofence): GeofencingRequest = GeofencingRequest.Builder().apply {
        setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_EXIT)
        addGeofence(geofence)
    }.build()
}