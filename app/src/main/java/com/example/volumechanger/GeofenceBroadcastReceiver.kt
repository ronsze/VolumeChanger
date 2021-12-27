package com.example.volumechanger

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver: BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            Log.e("GeofenceErr", GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode))
            return
        }else{
            Log.e("GeofenceErr", "NoErr")
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
                Log.e("geofence", "${it.requestId} - $transitionMsg")
            }
        } else {
            Log.e("geofence", "Unknown")
        }
    }

    /*private fun volumeChange(vol: Int, context: Context){
        var audioManager: AudioManager
        audioManager = applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        when(vol){
            0 -> audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
            -1 -> audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
            else -> {
                audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
                audioManager.adjustStreamVolume(
                    AudioManager.STREAM_RING,
                    (audioManager.getStreamMaxVolume(AudioManager.STREAM_RING) * (vol/100.0)).toInt(),
                    AudioManager.FLAG_PLAY_SOUND)
            }
        }
    }*/
}