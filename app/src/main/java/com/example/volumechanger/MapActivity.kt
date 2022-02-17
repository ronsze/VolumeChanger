package com.example.volumechanger

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import com.example.volumechanger.databinding.ActivityMapBinding
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.CircleOverlay
import com.naver.maps.map.overlay.Marker
import java.io.IOException
import java.util.*
import androidx.lifecycle.Observer
import com.google.android.gms.location.LocationServices

@SuppressLint("Range")
class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    data class MarkerItem(val marker: Marker, val id: Int, val name: String, val lat: Double, val lng: Double, val range: Int)

    companion object{
        lateinit var naverMap: NaverMap
    }

    private val colorArr = arrayOf(Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW,
                                Color.CYAN, Color.GRAY, Color.MAGENTA, Color.BLACK)

    private val context = this
    private lateinit var mapView: MapView
    private lateinit var binding: ActivityMapBinding

    private val circleArray: MutableList<CircleOverlay> by lazy{ mutableListOf() }

    private lateinit var inputManager: InputMethodManager
    private val mapViewModel: MapViewModel by viewModels()
    private val markers: MutableList<MarkerItem> by lazy{
        mutableListOf()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_map)
        binding.lifecycleOwner = this

        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        inputManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.showSoftInput(binding.searchText, 0)

        mapViewModel.setPendingIntent(geoPending)

        if(!App.firstCheck()) showHowTo()
//ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡObserversㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
        mapViewModel.isLocAddSuccess.observe(this, Observer {
            if(it){
                val locItem = mapViewModel.getLocItem()
                with(locItem){
                    addMarker(id, name, LatLng(lat, lng), range)
                }
                Log.d("AddLocation", "Success")
            }else{
                val failReason = mapViewModel.getFailReason()
                Log.e("AddLocation", "Fail, ${failReason}")
            }
        })

        mapViewModel.isLocRemoveSuccess.observe(this, Observer {
            if(it){
                val id = mapViewModel.getMarkerID()
                removeMarker(id)
            }else{
                val failReason = mapViewModel.getFailReason()
                Log.e("removeLocation", "Fail, ${failReason}")
            }
        })

        mapViewModel.camPos.observe(this, Observer {
            with(it){
                changeCamPos(LatLng(lat, lng), zoom)
            }
        })
//ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡClickEventsㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
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
//ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡMapㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
    override fun onMapReady(map: NaverMap) {
        naverMap = map
        val geoClienet = LocationServices.getGeofencingClient(this)

        setInitLoc()
        initMarkers()
        mapViewModel.setGeoClient(geoClienet)

        naverMap.setOnMapLongClickListener { pointF, latLng ->
            val dialog = MarkerDialog(this)
            dialog.showDia()
            dialog.setOnClickListener(object : MarkerDialog.ButtonOnClickLister{

                override fun onClicked(name: String, range: Int, volume: Int) {
                    mapViewModel.addLocation(name, range, volume, latLng)
                }
            })
        }
    }

    private fun setInitLoc(){
        val select = intent.getStringExtra("select")
        if(select == "item"){
            val location = intent.getStringExtra("location")!!.split(",")
            val lat = location[0].toDouble()
            val lng = location[1].toDouble()
            mapViewModel.setCamPos(LatLng(lat, lng), 14.0)
        }else{
            mapViewModel.setCamPos(App.model.defaultLoc, App.model.defaultZoomLv)
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
                mapViewModel.setCamPos(LatLng(list.get(0).latitude, list.get(0).longitude), 16.0)
            }
            inputManager.hideSoftInputFromWindow(binding.searchText.windowToken, 0)
        }
    }
//ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡMarkers and overlayㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
    private fun initMarkers() {
        val query = "SELECT * FROM lists;"
        val cursor = App.database.rawQuery(query, null)
        while (cursor.moveToNext()) {
            val id = cursor.getString(cursor.getColumnIndex("id")).toInt()
            val name = cursor.getString(cursor.getColumnIndex("name"))
            val location = cursor.getString(cursor.getColumnIndex("location")).toString().split(",")
            val lat = location[0].toDouble();
            val lng = location[1].toDouble()
            val range = cursor.getShort(cursor.getColumnIndex("range")).toInt()
            addMarker(id, name, LatLng(lat, lng), range)
        }
    }

    private fun addMarker(id: Int, name: String, latLng: LatLng, range: Int){
        val marker = getMarker(id, latLng)
        val circle = getCircle(id, latLng, range)

        marker.setOnClickListener {
            val builder = AlertDialog.Builder(this)
                .setTitle("삭제하시겠습니까? [${name}]")
                .setPositiveButton("예",
                    DialogInterface.OnClickListener { dialog, which ->
                        mapViewModel.removeLocation(id)
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

        markers.add(MarkerItem(marker, id, name, latLng.latitude, latLng.longitude, range))
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

    private fun removeMarker(id: Int){
        var marker: Marker? = null
        markers.forEach{
            if(it.id == id) marker = it.marker
        }
        if(marker == null){
            Log.e("removeMarker", "Fail, can't find markerID")
        }else{
            marker!!.map = null
            for (i in circleArray){
                if(i.tag == id){
                    i.map = null
                    break
                }
            }
        }
    }

    private fun getMyColor(mode: Int): Int{
        val random = Random()
        var num = random.nextInt(4)
        if(mode == 1) num += 4
        return colorArr[num]
    }

//ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡGeofenceㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
    private val geoPending: PendingIntent by lazy{
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun showHowTo(){
        val dialog = AlertDialog.Builder(context)
            .setTitle("사용법")
            .setMessage("화면을 꾹 누르면 원하는\n장소를 추가할 수 있습니다.\n지우고 싶을 땐 마커를 터치해주세요.")
            .show()
        App.endHowTo()
    }
}