package kr.sdbk.volumechanger.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity("location")
@Serializable
data class LocationEntity(
    @PrimaryKey val created: Long,
    val name: String,
    val location: Pair<Double, Double>,
    val range: Int,
    @ColumnInfo("bell_volume") val bellVolume: Int,
    @ColumnInfo("media_volume") val mediaVolume: Int,
    var enabled: Boolean = true
)