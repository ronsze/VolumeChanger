package kr.sdbk.volumechanger.data.mapper

import kr.sdbk.volumechanger.data.model.LocationDTO
import kr.sdbk.volumechanger.data.room.entity.LocationEntity

object LocationMapper {
    fun LocationDTO.toEntity() = LocationEntity(
        created = created,
        name = name,
        location = locationStringToPair(location),
        range = range,
        bellVolume = bellVolume,
        mediaVolume = mediaVolume
    )

    private fun locationStringToPair(location: String): Pair<Double, Double> {
        val split = location.split("/")
        return Pair(split[0].toDouble(), split[1].toDouble())
    }

    fun LocationEntity.toDTO() = LocationDTO(
        created = created,
        name = name,
        location = "${location.first}/${location.second}",
        range = range,
        bellVolume = bellVolume,
        mediaVolume = mediaVolume
    )
}