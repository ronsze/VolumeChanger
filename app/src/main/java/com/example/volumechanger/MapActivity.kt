package com.example.volumechanger

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.example.volumechanger.databinding.ActivityMapBinding
import com.google.android.gms.location.*
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.CircleOverlay
import com.naver.maps.map.overlay.Marker
import java.io.IOException
import java.util.*

@SuppressLint("Range")
class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    companion object{
        lateinit var naverMap: NaverMap
    }
    private val colorArr = arrayOf(Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW,
                                Color.CYAN, Color.GRAY, Color.MAGENTA, Color.BLACK)

    private val defaultLoc = LatLng(37.58667, 126.97482)
    private val defaultZoomLv = 12.0

    private val context = this
    private lateinit var mapView: MapView
    private lateinit var binding: ActivityMapBinding

    private lateinit var geofencingClient: GeofencingClient

    private val geofenceList: MutableList<Geofence> by lazy{ mutableListOf() }
    private val circleArray: MutableList<CircleOverlay> by lazy{ mutableListOf() }

    private lateinit var inputManager: InputMethodManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_map)
        binding.lifecycleOwner = this

        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        geofencingClient = LocationServices.getGeofencingClient(this)
        inputManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.showSoftInput(binding.searchText, 0)

        showHowTo()

        binding.searchBtn.setOnClickListener{
            searchAddress(binding.searchText.text.toString())
        }

        binding.searchText.setOnEditorActionListener { textView, i, keyEvent ->
            var handled = false
            if(i == EditorInfo.IME_ACTION_DONE){
                searchAddress(binding.searchText.text.toString())
                handled = true
            }
            handled
        }
    }
//ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡMapㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
    override fun onMapReady(map: NaverMap) {
        naverMap = map

        setInitLoc()
        initMarkers()

        naverMap.setOnMapLongClickListener { pointF, latLng ->
            val dialog = MarkerDialog(this)
            dialog.showDia()
            dialog.setOnClickListener(object : MarkerDialog.ButtonOnClickLister{

                override fun onClicked(name: String, range: Int, volume: Int) {
                    if(checkOverlap(latLng, range)){
                        Toast.makeText(context, "다른 장소와 겹칩니다.", Toast.LENGTH_SHORT).show()
                    }
                    else{
                        val location = "${latLng.latitude},${latLng.longitude}"

                        val id = insertLocToDB(name, range, volume, location)

                        val geofence = getGeofence(id, LatLng(latLng.latitude, latLng.longitude), range.toFloat())
                        geofenceList.add(geofence)
                        createMarker(id.toInt(), name, latLng, range)
                        addGeofences()
                    }
                }
            })
        }
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

    private fun setInitLoc(){
        val select = intent.getStringExtra("select")
        if(select == "item"){
            val location = intent.getStringExtra("location")!!.split(",")
            val lat = location[0].toDouble() ; val lng = location[1].toDouble()
            changeCamPos(LatLng(lat, lng),14.0)
        }else{
            changeCamPos(defaultLoc, defaultZoomLv)
        }
    }

    private fun changeCamPos(latLng: LatLng, zoom: Double){
        val camPos = CameraPosition(
            latLng,
            zoom
        )
        naverMap.cameraPosition = camPos
    }

    private fun searchAddress(address: String){
        val addr = address.replace(" ", "")
        if(addr != ""){
            lateinit var list: MutableList<Address>
            try{
                list = Geocoder(context).getFromLocationName(addr, 5)
            }catch(e: IOException){
                e.printStackTrace()
                Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
            }

            if(list.size == 0){
                Toast.makeText(context, "일치하는 주소가 없습니다.", Toast.LENGTH_SHORT).show()
            }else{
                changeCamPos(LatLng(list.get(0).latitude, list.get(0).longitude), 16.0)
            }
            inputManager.hideSoftInputFromWindow(binding.searchText.windowToken, 0)
        }
    }
//ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡMarkers and overlayㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
    private fun initMarkers(){
        val query = "SELECT id, name, location, range FROM lists;"
        val cursor = App.database.rawQuery(query, null)
        with(cursor){
            while(moveToNext()){
                val id = getString(getColumnIndex("id")).toInt()
                val name = getString(getColumnIndex("name"))
                val location = getString(getColumnIndex("location")).toString().split(",")
                val lat = location[0].toDouble() ; val lng = location[1].toDouble()
                val range = getShort(getColumnIndex("range")).toInt()
                createMarker(id, name, LatLng(lat, lng), range)
            }
        }
    }

    private fun createMarker(id: Int, name: String, latLng: LatLng, range: Int){
        val marker = getMarker(id, latLng)
        val circle = getCircle(id, latLng, range)

        marker.setOnClickListener {
            val builder = AlertDialog.Builder(this)
                .setTitle("삭제하시겠습니까? [${name}]")
                .setPositiveButton("예",
                    DialogInterface.OnClickListener { dialog, which ->
                        delMarker(marker)
                    })
                .setNegativeButton("아니오",
                    DialogInterface.OnClickListener { dialog, which ->
                    })
                .show()
            false
        }

        circle.map = naverMap
        circleArray.add(circle)

        marker.map = naverMap
    }

    private fun getMarker(id: Int, latLng: LatLng): Marker{
        val marker = Marker()
        with(marker){
            position = latLng
            iconTintColor = getMyColor(2)
            tag = id
        }
        return marker
    }

    private fun getCircle(id: Int, latLng: LatLng, range: Int): CircleOverlay{
        val circle = CircleOverlay()
        with(circle){
            center = latLng
            radius = range.toDouble()
            tag = id
            color = Color.TRANSPARENT
            outlineWidth = 5
            outlineColor = getMyColor(1)
        }
        return circle
    }

    private fun delMarker(marker: Marker){
        val id = marker.tag
        marker.map = null
        for (i in circleArray){
            if(i.tag == id){
                i.map = null
                break
            }
        }
        App.database.execSQL("DELETE FROM lists WHERE id = ${marker.tag}")
        removeGeofences()
    }

    private fun getMyColor(mode: Int): Int{
        val random = Random()
        var num = random.nextInt(4)
        if(mode == 1) num += 4
        return colorArr[num]
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
//ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡGeofenceㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
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

    private val geoPending: PendingIntent by lazy{
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun addGeofences(){
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED){
            geofencingClient.addGeofences(getGeofencingRequest(geofenceList), geoPending).run{
                addOnSuccessListener {
                    Log.e("addGeofence", "Success")
                }
                addOnFailureListener {
                    Log.e("addGeofence", "${it}")
                }
            }
        }
    }

    private fun removeGeofences(){
        geofencingClient.removeGeofences(geoPending).run{
            addOnSuccessListener {
                Log.e("removeGeofence", "Success")
                geofenceList.clear()
                updateGeofences()
            }
            addOnFailureListener{
                Log.e("removeGeofence", "Fail")
            }
        }
    }

    private fun updateGeofences(){
        val query = "SELECT id, range, location FROM lists;"
        val cursor = App.database.rawQuery(query, null)
        with(cursor){
            if(count > 0){
                while(moveToNext()){
                    val id = getString(getColumnIndex("id"))
                    val location = getString(getColumnIndex("location")).split(",")
                    val lat = location[0].toDouble() ; val lng = location[1].toDouble()
                    val range = getString(getColumnIndex("range"))
                    geofenceList.add(getGeofence(id, LatLng(lat, lng), range.toFloat()))
                }
                addGeofences()
            }
        }
    }

    private fun getGeofencingRequest(list: List<Geofence>): GeofencingRequest {
        return GeofencingRequest.Builder().apply{
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofences(list)
        }.build()
    }

    private fun showHowTo(){
        val query = "SELECT * FROM lists"
        val cursor = App.database.rawQuery(query, null)
        if(cursor.count == 0){
            val dialog = AlertDialog.Builder(context)
                .setTitle("사용법")
                .setMessage("화면을 꾹 누르면 원하는\n장소를 추가할 수 있습니다.\n지우고 싶을 땐 마커를 터치해주세요.")
                .show()
        }
    }
}