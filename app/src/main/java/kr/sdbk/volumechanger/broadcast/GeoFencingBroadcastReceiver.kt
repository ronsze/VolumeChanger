package kr.sdbk.volumechanger.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.sdbk.volumechanger.data.room.entity.LocationEntity
import kr.sdbk.volumechanger.util.Constants
import kr.sdbk.volumechanger.util.enums.BellVolume
import kr.sdbk.volumechanger.util.enums.MediaVolume

class GeoFencingBroadcastReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null || context == null) return
        val event = GeofencingEvent.fromIntent(intent) ?: return

        if (event.hasError()) {
            val errorMessage = GeofenceStatusCodes
                .getStatusCodeString(event.errorCode)
            Log.e("broadcast", errorMessage)
            return
        }

        val transition = event.geofenceTransition

        if (transition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            val locationEntity = intent.getSerializableExtra(Constants.LOCATION_ENTITY) as LocationEntity
            volumeChange(context, locationEntity)
        }
    }

    private fun volumeChange(context: Context, location: LocationEntity) {
        val audioManager: AudioManager = context.applicationContext?.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        CoroutineScope(Dispatchers.Main).launch {
            when (location.bellVolume) {
                BellVolume.MUTE.value -> {
                    audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
                }
                BellVolume.VIBRATION.value -> {
                    audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                }
                else -> {
                    audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
                    audioManager.setStreamVolume(
                        AudioManager.STREAM_RING,
                        (audioManager.getStreamMaxVolume(AudioManager.STREAM_RING) * (location.bellVolume / 100.0)).toInt(),
                        AudioManager.FLAG_PLAY_SOUND
                    )
                }
            }

            when (location.mediaVolume) {
                MediaVolume.MUTE.value -> {
                    audioManager.setStreamVolume(
                        AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_SHOW_UI
                    )
                }
                else -> {
                    audioManager.setStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        (audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * (location.mediaVolume / 100.0)).toInt(),
                        AudioManager.FLAG_SHOW_UI
                    )
                }
            }
        }
    }
}