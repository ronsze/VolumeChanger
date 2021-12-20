package com.example.volumechanger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.volumechanger.databinding.ActivityMapBinding
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback

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

    override fun onMapReady(naverMap: NaverMap) {
        MapActivity.naverMap = naverMap

        var camPos = CameraPosition(
            LatLng(33.38, 126.55),
            9.0
        )
        naverMap.cameraPosition = camPos
    }
}