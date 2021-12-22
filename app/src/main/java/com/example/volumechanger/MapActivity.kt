package com.example.volumechanger

import android.content.DialogInterface
import android.graphics.PointF
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

        val task = intent.getStringExtra("Task")
        Log.e("task", task.toString())

        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
    }

    override fun onMapReady(map: NaverMap) {
        naverMap = map

        var camPos = CameraPosition(
            LatLng(33.38, 126.55),
            9.0
        )
        naverMap.cameraPosition = camPos

        naverMap.setOnMapLongClickListener { pointF, latLng ->
            val builder = AlertDialog.Builder(this)
                    .setTitle("해당 위치에 설정하시겠습니까?")
                    .setPositiveButton("확인",
                            DialogInterface.OnClickListener { dialog, which ->
                                Toast.makeText(this, "마커생성", Toast.LENGTH_SHORT).show()
                                createMarker(latLng)
                            })
                    .setNegativeButton("취소",
                            DialogInterface.OnClickListener { dialog, which ->
                            })
                    .show()
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
}