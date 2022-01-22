package com.example.volumechanger

import android.app.Application
import android.content.Context
import android.location.Address
import android.location.Geocoder
import com.naver.maps.geometry.LatLng
import java.io.IOException
import java.util.*

class App: Application() {
    companion object{
        lateinit var prefs: SharedPreferences
    }

    override fun onCreate() {
        prefs = SharedPreferences(applicationContext)
        super.onCreate()
    }
}