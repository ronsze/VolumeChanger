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
    lateinit var dbHelper: DBHelper
    lateinit var database: SQLiteDatabase

    override fun onReceive(context: Context?, intent: Intent?) {
        if(context == null){
            Log.e("GeofenceErr", "Context is unvalid")
            return
        }

        dbHelper = DBHelper(context, "newdb.db", null, 1)
        database = dbHelper.writableDatabase

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
                if(transitionMsg == "Enter"){
                    volumeChange(it.requestId.toInt(), context)
                }else if(transitionMsg == "Exit"){

                }
                Log.e("geofence", "${it.requestId} - $transitionMsg")
            }
        } else {
            Log.e("geofence", "Unknown")
        }
    }

    fun volumeChange(id: Int, context: Context) {
        val audioManager: AudioManager
        audioManager = context.applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val query = "SELECT volume FROM lists WHERE id = ${id};"
        val cursor = database.rawQuery(query, null)
        cursor.moveToNext()

        val vol = cursor.getString(cursor.getColumnIndex("volume")).toInt()
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