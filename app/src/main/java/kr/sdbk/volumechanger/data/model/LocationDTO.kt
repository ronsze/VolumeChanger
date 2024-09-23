package kr.sdbk.volumechanger.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LocationDTO(
    val created: Long,
    val name: String,
    val location: String,
    val range: Int,
    @SerialName("bell_volume")
    val bellVolume: Int,
    @SerialName("media_volume")
    val mediaVolume: Int
)