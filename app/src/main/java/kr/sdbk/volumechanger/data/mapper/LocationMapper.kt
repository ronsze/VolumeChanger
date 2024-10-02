package kr.sdbk.volumechanger.data.mapper

import com.google.android.gms.maps.model.LatLng
import kr.sdbk.volumechanger.data.model.Location
import kr.sdbk.volumechanger.data.room.entity.LocationEntity

object LocationMapper {
    fun LocationEntity.toData() = Location(
        created = created,
        name = name,
        location = location.convertLatLng(),
        range = range,
        bellVolume = bellVolume,
        mediaVolume = mediaVolume,
        enabled = enabled
    )

    fun Location.toEntity() = LocationEntity(
        created = created,
        name = name,
        location = location.convertString(),
        range = range,
        bellVolume = bellVolume,
        mediaVolume = mediaVolume,
        enabled = enabled
    )

    fun LatLng.convertString() = "${latitude}/${longitude}"

    fun String.convertLatLng(): LatLng {
        val split = split("/")
        return LatLng(split[0].toDouble(), split[1].toDouble())
    }
}