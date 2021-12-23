package com.example.volumechanger

import android.content.Context
import android.content.DialogInterface
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
    }

    override fun onMapReady(map: NaverMap) {
        naverMap = map

        lateinit var pointF: LatLng
        val select = intent.getStringExtra("select")
        if(select == "item"){
            pointF = LatLng(33.38, 126.55)
        }else{
            pointF = LatLng(33.38, 126.55)
        }

        var camPos = CameraPosition(
                pointF,
                9.0
        )
        naverMap.cameraPosition = camPos

        naverMap.setOnMapLongClickListener { pointF, latLng ->
            val dialog = MarkerDialog(this)
            dialog.showDia()
            dialog.setOnClickListener(object : MarkerDialog.ButtonOnClickLister{
                override fun onClicked(name: String, range: Int, volume: Int) {
                    Log.e("받아온 값", "${name}, ${range}, ${volume}")
                    createMarker(latLng)
                }
            })
        }
    }

    private fun createMarker(latLng: LatLng){
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
        changeCamPos(latLng, 12.0)
    }

    private fun reviseMarker(){
        Log.e("마커", "수정")
    }

    private fun delMarker(marker: Marker){
        Log.e("마커", "삭제")
        marker.map = null
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