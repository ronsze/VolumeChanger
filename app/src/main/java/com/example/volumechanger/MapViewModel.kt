package com.example.volumechanger

import android.annotation.SuppressLint
import android.app.Application
import android.app.PendingIntent
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.naver.maps.geometry.LatLng

@SuppressLint("Range")
class MapViewModel(application: Application): AndroidViewModel(application) {
    data class locItems(val id: Int, val name: String, val lat: Double, val lng: Double, val range: Int)
    data class camPosItems(val lat: Double, val lng: Double, val zoom: Double)

    private val geofencingClient by lazy{
        App.model.getGeoClient()
    }
    private val geofenceList by lazy{
        App.model.getGeoList()
    }

    private lateinit var geoPending: PendingIntent
    private lateinit var locItem: locItems

    private var failReason = ""
    private var markerID = -1

    val isLocAddSuccess = MutableLiveData<Boolean>()
    val isLocRemoveSuccess = MutableLiveData<Boolean>()
    val camPos = MutableLiveData<camPosItems>()

    fun setPendingIntent(pendingIntent: PendingIntent){
        this.geoPending = pendingIntent
    }

    fun addLocation(name: String, range: Int, volume: Int, latLng: LatLng){
        if(checkOverlap(latLng, range)){
            failReason = "isOverlab"
            isLocAddSuccess.postValue(false)
        }else{
            val locStr = "${latLng.latitude},${latLng.longitude}"
            val id = insertLocToDB(name, range, volume, locStr)
            val geofence = getGeofence(id, LatLng(latLng.latitude, latLng.longitude), range.toFloat())
            val tmpGeoList = mutableListOf<Geofence>()
            tmpGeoList.addAll(geofenceList)
            App.model.addNewGeoToList(geofence)
            locItem = locItems(id.toInt(), name, latLng.latitude, latLng.longitude, range)

            addGeofences(tmpGeoList)
        }
    }

    fun removeLocation(id: Int){
        removeGeofences(id)
    }

    private fun checkOverlap(latLng: LatLng, range: Int): Boolean{
        val query = "SELECT range, location FROM lists;"
        val cursor = App.database.rawQuery(query, null)

        with(cursor){
            while(moveToNext()){
                val dbRange = getString(getColumnIndex("range")).toInt()
                val location = getString(getColumnIndex("location")).split(",")
                val lat = location[0].toDouble() ; val lng = location[1].toDouble()
                val distance = floatArrayOf(1.0f)
                Location.distanceBetween(latLng.latitude, latLng.longitude, lat, lng, distance)
                if(distance[0] < range+dbRange) return true
            }
        }
        return false
    }

    private fun insertLocToDB(name: String, range: Int, volume: Int, location: String): String{
        var query = "INSERT INTO lists('name', 'range', 'volume', 'location') values('${name}', '${range}', '${volume}', '${location}');"
        App.database.execSQL(query)

        query = "SELECT id FROM lists WHERE location = '${location}';"
        val cursor = App.database.rawQuery(query, null)
        cursor.moveToNext()

        val id = cursor.getString(cursor.getColumnIndex("id"))

        return id
    }

    fun getLocItem(): locItems{
        return locItem
    }

    fun getFailReason(): String{
        return failReason
    }

    fun getMarkerID(): Int{
        return markerID
    }

    fun setCamPos(latLng: LatLng, zoom: Double){
        camPos.postValue(camPosItems(latLng.latitude, latLng.longitude, zoom))
    }

//ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡGeofenceㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
    private fun getGeofence(reqId: String, geo: LatLng, radius: Float): Geofence {
        return Geofence.Builder()
            .setRequestId(reqId)
            .setCircularRegion(geo.latitude, geo.longitude, radius)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setLoiteringDelay(10000)
            .setTransitionTypes(
                Geofence.GEOFENCE_TRANSITION_ENTER
                        or Geofence.GEOFENCE_TRANSITION_EXIT
            )
            .build()
    }

    private fun addGeofences(tmpGeoList: MutableList<Geofence>){
        val context = getApplication<Application>().applicationContext
        if(ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED){
            geofencingClient.addGeofences(getGeofencingRequest(geofenceList), geoPending).run{
                addOnSuccessListener {
                    isLocAddSuccess.postValue(true)
                    Log.d("addGeofence", "Success")
                }
                addOnFailureListener {
                    failReason = "add/geofenceAddFail"
                    App.model.revertGeoList(tmpGeoList)
                    isLocAddSuccess.postValue(false)
                    Log.e("addGeofence", "${it}")
                }
            }
        }
    }

    private fun addGeofences(id: Int){
        val context = getApplication<Application>().applicationContext
        if(ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED){
            geofencingClient.addGeofences(getGeofencingRequest(geofenceList), geoPending).run{
                addOnSuccessListener {
                    markerID = id
                    App.database.execSQL("DELETE FROM lists WHERE id = ${id}")
                    isLocRemoveSuccess.postValue(true)
                }
                addOnFailureListener {
                    failReason = "remove/geofenceAddFail"
                    isLocRemoveSuccess.postValue(false)
                    Log.e("addGeofence", "${it}")
                }
            }
        }
    }

    private fun removeGeofences(id: Int){
        geofencingClient.removeGeofences(geoPending).run{
            addOnSuccessListener {
                App.model.removeGeoFromList(id)
                addGeofences(id)
                Log.e("removeGeofence", "Success")
            }
            addOnFailureListener{
                failReason = "geofenceRemoveFail"
                isLocRemoveSuccess.postValue(false)
                Log.e("removeGeofence", "Fail, ${it}")
            }
        }
    }

    private fun getGeofencingRequest(list: List<Geofence>): GeofencingRequest {
        return GeofencingRequest.Builder().apply{
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofences(list)
        }.build()
    }

    fun setGeoClient(geoClient: GeofencingClient){
        App.model.geofencingClient = geoClient
    }
}