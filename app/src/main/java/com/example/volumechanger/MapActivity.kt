package com.example.volumechanger

import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.example.volumechanger.databinding.ActivityMapBinding
import com.google.android.gms.location.*
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
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

        val initLoc = getInitPoint()
        changeCamPos(initLoc)
        initMarkers()

        naverMap.setOnMapLongClickListener { pointF, latLng ->
            val dialog = MarkerDialog(this)
            dialog.showDia()
            dialog.setOnClickListener(object : MarkerDialog.ButtonOnClickLister{
                override fun onClicked(name: String, range: Int, volume: Int) {
                    val point = "${latLng.latitude},${latLng.longitude}"
                    var query = "INSERT INTO lists('name', 'range', 'volume', 'point') values('${name}', '${range}', '${volume}', '${point}');"
                    database.execSQL(query)

                    query = "SELECT id FROM lists WHERE point = '${point}';"
                    val cursor = database.rawQuery(query, null)
                    cursor.moveToNext()

                    val id = cursor.getString(0)
                    val geofence = getGeofence(id, LatLng(latLng.latitude, latLng.longitude), range.toFloat())
                    geofenceList.add(geofence)
                    createMarker(id.toInt(), latLng)
                    addGeofences()
                }
            })
        }
    }
    private fun getInitPoint(): LatLng{
        lateinit var pointF: LatLng
        val select = intent.getStringExtra("select")
        if(select == "item"){
            val point = intent.getStringExtra("point")!!.split(",")
            val lat = point[0].toDouble()
            val lng = point[1].toDouble()
            pointF = LatLng(lat, lng)
        }else{
            pointF = LatLng(33.38, 126.55)
        }
        return pointF
    }

    private fun initMarkers(){
        val query: String
        query = "SELECT * FROM lists;"
        val cursor = database.rawQuery(query, null)
        while(cursor.moveToNext()){
            val latLng = cursor.getString(cursor.getColumnIndex("point")).toString().split(",")
            val lat = latLng[0].toDouble()
            val lng = latLng[1].toDouble()
            val id = cursor.getString(0).toInt()
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
        removeGeofences()

    }

    private fun changeCamPos(latLng: LatLng){
        val camPos = CameraPosition(
                latLng,
                9.0
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

    val geoPending: PendingIntent by lazy{
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun addGeofences(){
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED){
            geofencingClient.addGeofences(getGeofencingRequest(geofenceList), geoPending).run{
                addOnSuccessListener {
                    Log.e("addGeo", "add Success")
                }
                addOnFailureListener {
                    Log.e("addGeo", "add Fail")
                }
            }
        }
    }

    private fun removeGeofences(){
        geofencingClient.removeGeofences(geoPending).run{
            addOnSuccessListener {
                Log.e("removeGeo", "remove Success")
                geofenceList.clear()
                updateGeofences()
            }
            addOnFailureListener{
                Log.e("removeGeo", "remove Fail")
            }
        }
    }

    private fun updateGeofences(){
        val query = "SELECT id, range, point FROM lists;"
        val cursor = database.rawQuery(query, null)
        if(cursor != null){
            while(cursor.moveToNext()){
                val id = cursor.getString(cursor.getColumnIndex("id"))
                val point = cursor.getString(cursor.getColumnIndex("point")).split(",")
                val lat = point[0].toDouble()
                val lng = point[1].toDouble()
                val range = cursor.getString(cursor.getColumnIndex("range"))
                geofenceList.add(getGeofence(id, LatLng(lat, lng), range.toFloat()))
            }
            addGeofences()
        }
    }

    private fun getGeofencingRequest(list: List<Geofence>): GeofencingRequest {
        return GeofencingRequest.Builder().apply{
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofences(list)
        }.build()
    }
}
