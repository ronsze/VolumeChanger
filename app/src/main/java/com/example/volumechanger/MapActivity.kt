package com.example.volumechanger

import android.content.Context
import android.content.DialogInterface
import android.database.sqlite.SQLiteDatabase
import android.media.AudioManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.volumechanger.databinding.ActivityMapBinding
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

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
                    Log.e("받아온 값", "${name}, ${range}, ${volume}")
                    var point = "${latLng.latitude},${latLng.longitude}"
                    var query = "INSERT INTO lists('name', 'range', 'volume', 'point') " +
                            "values('${name}', '${range}', '${volume}', '${point}');"
                    database.execSQL(query)

                    query = "SELECT id FROM lists " +
                            "WHERE point = '${point}';"
                    var cursor = database.rawQuery(query, null)
                    cursor.moveToNext()

                    query = "SELECT * FROM lists;"
                    var c = database.rawQuery(query, null)
                    while(c.moveToNext()){
                        Log.e("select", "${c.getString(c.getColumnIndex("id"))} " +
                                "${c.getString(c.getColumnIndex("name"))} " +
                                "${c.getString(c.getColumnIndex("range"))} " +
                                "${c.getString(c.getColumnIndex("volume"))} " +
                                "${c.getString(c.getColumnIndex("point"))}")
                    }
                    createMarker(cursor.getString(0).toInt(), latLng)
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

    private fun volumeChange(vol: Int){
        var audioManager: AudioManager
        audioManager = applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        when(vol){
            0 -> audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
            -1 -> audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
            else -> {
                audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
                audioManager.adjustStreamVolume(AudioManager.STREAM_RING,
                        (audioManager.getStreamMaxVolume(AudioManager.STREAM_RING) * (vol/100.0)).toInt(),
                AudioManager.FLAG_PLAY_SOUND)
            }
        }
    }
}