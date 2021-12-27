package com.example.volumechanger

import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.media.AudioManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.example.volumechanger.databinding.ActivityMapBinding
import com.google.android.gms.location.*
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.LocationOverlay
import com.naver.maps.map.overlay.Marker

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    companion object{
        lateinit var naverMap: NaverMap
    }
    private lateinit var mapView: MapView
    private lateinit var binding: ActivityMapBinding
    lateinit var dbHelper: DBHelper
    lateinit var database: SQLiteDatabase
    lateinit var geofencingClient: GeofencingClient
    val geofenceList: MutableList<Geofence> by lazy{
        mutableListOf()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        geofencingClient = LocationServices.getGeofencingClient(this)

        dbHelper = DBHelper(this, "newdb.db", null, 1)
        database = dbHelper.writableDatabase

        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
    }

    override fun onMapReady(map: NaverMap) {
        naverMap = map

        var pointF = getInitPoint()
        changeCamPos(pointF, 9.0)
        initMarkers()

        naverMap.setOnMapLongClickListener { pointF, latLng ->
            val dialog = MarkerDialog(this)
            dialog.showDia()
            dialog.setOnClickListener(object : MarkerDialog.ButtonOnClickLister{
                override fun onClicked(name: String, range: Int, volume: Int) {
                    var point = "${latLng.latitude},${latLng.longitude}"
                    var query = "INSERT INTO lists('name', 'range', 'volume', 'point') values('${name}', '${range}', '${volume}', '${point}');"
                    database.execSQL(query)

                    query = "SELECT id FROM lists WHERE point = '${point}';"
                    var cursor = database.rawQuery(query, null)
                    cursor.moveToNext()

                    val id = cursor.getString(0)
                    val geofence = getGeofence(id, LatLng(latLng.latitude, latLng.longitude), range.toFloat())
                    geofenceList.add(geofence)
                    createMarker(id.toInt(), latLng)
                    addGeofences(volume)
                }
            })
        }
    }
    private fun getInitPoint(): LatLng{
        lateinit var pointF: LatLng
        val select = intent.getStringExtra("select")
        if(select == "item"){
            var point = intent.getStringExtra("point")!!.split(",")
            var lat = point[0].toDouble()
            var lng = point[1].toDouble()
            pointF = LatLng(lat, lng)
        }else{
            pointF = LatLng(33.38, 126.55)
        }
        return pointF
    }

    private fun initMarkers(){
        var query: String
        query = "SELECT * FROM lists;"
        var c = database.rawQuery(query, null)
        while(c.moveToNext()){
            var latLng = c.getString(c.getColumnIndex("point")).toString().split(",")
            var lat = latLng[0].toDouble()
            var lng = latLng[1].toDouble()
            var id = c.getString(0).toInt()
            createMarker(id, LatLng(lat, lng))
        }
    }

    private fun createMarker(id: Int, latLng: LatLng){
        val marker = Marker()
        marker.position = latLng
        marker.setOnClickListener {
            val items = arrayOf("수정", "삭제")
            val builder = AlertDialog.Builder(this)
                    .setTitle("위치 이름")
                    .setItems(items){ dialog, which ->
                        if(items[which] == "수정"){
                            reviseMarker()
                        }else if(items[which] == "삭제"){
                            delMarker(marker)
                        }
                    }
                    .show()
            false
        }
        marker.map = naverMap
        marker.tag = id
    }

    private fun reviseMarker(){
        Log.e("마커", "수정")
    }

    private fun delMarker(marker: Marker){
        marker.map = null
        database.execSQL("DELETE FROM lists WHERE id = ${marker.tag}")
    }

    private fun changeCamPos(latLng: LatLng, zoom: Double){
        val camPos = CameraPosition(
                latLng,
                zoom
        )
        naverMap.cameraPosition = camPos
    }

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

    private fun addGeofences(volume: Int){
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED){
                Log.e("asdasd", "asdasd")
                val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
                intent.putExtra("volume", volume)
                val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            geofencingClient.addGeofences(getGeofencingRequest(geofenceList), pendingIntent).run{
                addOnSuccessListener {
                    Log.e("addGeo", "add Success")
                }
                addOnFailureListener {
                    Log.e("addGeo", "add Fail")

                }
            }
        }
    }

    private fun getGeofencingRequest(list: List<Geofence>): GeofencingRequest {
        return GeofencingRequest.Builder().apply{
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofences(list)
        }.build()
    }


}