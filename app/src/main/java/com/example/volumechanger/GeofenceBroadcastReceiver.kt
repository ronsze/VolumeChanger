package com.example.volumechanger

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.media.AudioManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver: BroadcastReceiver(){

    override fun onReceive(context: Context?, intent: Intent?) {
        if(context == null){
            Log.e("GeofenceErr", "Context is unvalid")
            return
        }

        val geofencingEvent = GeofencingEvent.fromIntent(intent!!)

        if (geofencingEvent.hasError()) {
            Log.e("GeofenceErr", GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode))
            return
        }

        val geofenceTransaction = geofencingEvent.geofenceTransition

        if (geofenceTransaction == Geofence.GEOFENCE_TRANSITION_ENTER ||
            geofenceTransaction == Geofence.GEOFENCE_TRANSITION_EXIT
        ) {
            val triggeringGeofences = geofencingEvent.triggeringGeofences
            val transitionMsg = when (geofenceTransaction) {
                Geofence.GEOFENCE_TRANSITION_ENTER -> "Enter"
                Geofence.GEOFENCE_TRANSITION_EXIT -> "Exit"
                else -> "-"
            }
            triggeringGeofences.forEach {
                if(transitionMsg == "Enter"){
                    Log.e("Geofence", "Enter")
                    volumeChange(it.requestId.toInt(), context)
                }else if(transitionMsg == "Exit"){
                    Log.e("Geofence", "Exit")
                }
            }
        } else {
            Log.e("GeofenceReceiver", "트랜잭션 불일치")
        }
    }

    fun volumeChange(id: Int, context: Context) {
        val audioManager: AudioManager
        audioManager = context.applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val query = "SELECT volume FROM lists WHERE id = ${id};"
        val cursor = App.database.rawQuery(query, null)
        cursor.moveToNext()

        if(cursor.count > 0){
            val vol = cursor.getString(0).toInt()
            when (vol) {
                0 -> audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
                -1 -> audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                else -> {
                    audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
                    audioManager.setStreamVolume(
                        AudioManager.STREAM_RING,
                        (audioManager.getStreamMaxVolume(AudioManager.STREAM_RING) * vol/100.0).toInt(),
                        AudioManager.FLAG_PLAY_SOUND
                    )
                }
            }
        }
    }
}