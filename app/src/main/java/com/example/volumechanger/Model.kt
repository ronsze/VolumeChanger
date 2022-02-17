package com.example.volumechanger

import android.app.Application
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.naver.maps.geometry.LatLng

class Model: Application(){
    val defaultLoc = LatLng(37.58667, 126.97482)
    val defaultZoomLv = 12.0

    lateinit var geofencingClient: GeofencingClient
    val geofenceList: MutableList<Geofence> by lazy{ mutableListOf() }

    override fun onCreate() {
        super.onCreate()
    }

    fun addNewGeoToList(geofence: Geofence){
        geofenceList.add(geofence)
    }

    fun removeGeoFromList(id: Int){
        for(i in geofenceList){
            if(i.requestId == id.toString()){
                geofenceList.remove(i)
                break
            }
        }
    }

    fun revertGeoList(list: MutableList<Geofence>){
        geofenceList.clear()
        geofenceList.addAll(list)
    }

    fun getGeoClient(): GeofencingClient{
        return geofencingClient
    }

    fun getGeoList(): MutableList<Geofence>{
        return geofenceList
    }
}