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
import kr.sdbk.volumechanger.data.mapper.LocationMapper.toEntity
import kr.sdbk.volumechanger.data.model.Location
import kr.sdbk.volumechanger.util.Constants
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class GeofenceModule(
    private val context: Context
) {
    private val client = LocationServices.getGeofencingClient(context)

    suspend fun addGeofencing(
        locationList: List<Location>
    ) = suspendCoroutine { outer ->
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            CoroutineScope(Dispatchers.IO).launch {
                val taskList = locationList.map { location ->
                    val pendingIntent = getGeofencePendingIntent(location)
                    val geofence = getGeofence(location)
                    val request = getGeofencingRequest(geofence)

                    async {
                        suspendCoroutine { inner ->
                            client.addGeofences(request, pendingIntent).run {
                                addOnSuccessListener {
                                    inner.resume(true)
                                }
                                addOnFailureListener {
                                    outer.resumeWithException(Exception("Add geofence failed"))
                                }
                            }
                        }
                    }
                }
                taskList.awaitAll()
                outer.resume(true)
            }
        }
    }

    suspend fun removeGeofencing(
        locationList: List<Location>
    ) = suspendCoroutine { continuation ->
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            client.removeGeofences(locationList.map { it.created.toString() }).run {
                addOnSuccessListener { continuation.resume(true) }
                addOnFailureListener { continuation.resumeWithException(Exception("Remove geofence failed")) }
            }
        }
    }

    private fun getGeofence(location: Location): Geofence {
        return Geofence.Builder()
            .setRequestId(location.created.toString())
            .setCircularRegion(
                location.location.latitude,
                location.location.longitude,
                location.range.toFloat()
            )
            .setNotificationResponsiveness(30000)
            .setLoiteringDelay(30000)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()
    }

    private fun getGeofencePendingIntent(data: Location): PendingIntent {
        val intent = Intent(context, GeoFencingBroadcastReceiver::class.java).apply {
            putExtra(Constants.LOCATION_ENTITY, data.toEntity())
        }
        return PendingIntent.getBroadcast(context, data.created.toInt(), intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun getGeofencingRequest(geofence: Geofence): GeofencingRequest = GeofencingRequest.Builder().apply {
        setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_EXIT)
        addGeofence(geofence)
    }.build()
}