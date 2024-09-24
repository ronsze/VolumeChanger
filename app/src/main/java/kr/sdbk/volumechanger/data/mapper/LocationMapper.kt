package kr.sdbk.volumechanger.data.mapper

import androidx.room.TypeConverter
import kr.sdbk.volumechanger.data.mapper.LocationConverter.locationStringToPair
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

    fun LocationEntity.toDTO() = LocationDTO(
        created = created,
        name = name,
        location = "${location.first}/${location.second}",
        range = range,
        bellVolume = bellVolume,
        mediaVolume = mediaVolume
    )
}

object LocationConverter {
    @TypeConverter
    fun locationPairToString(value: Pair<Double, Double>): String = "${value.first}/${value.second}"

    @TypeConverter
    fun locationStringToPair(value: String): Pair<Double, Double> {
        val split = value.split("/")
        return Pair(split[0].toDouble(), split[1].toDouble())
    }
}