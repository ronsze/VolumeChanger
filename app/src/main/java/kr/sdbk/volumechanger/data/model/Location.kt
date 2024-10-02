package kr.sdbk.volumechanger.data.model

import com.google.android.gms.maps.model.LatLng

data class Location(
    val created: Long,
    val name: String,
    val location: LatLng,
    val range: Int,
    val bellVolume: Int,
    val mediaVolume: Int,
    var enabled: Boolean = true
)