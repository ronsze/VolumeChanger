package kr.sdbk.volumechanger.data.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kr.sdbk.volumechanger.data.room.entity.LocationEntity

@Dao
interface LocationDao {
    @Insert
    suspend fun insertLocation(location: LocationEntity)

    @Insert
    suspend fun insertLocationList(locationList: List<LocationEntity>)

    @Query("SELECT * from location")
    suspend fun getAllLocation(): List<LocationEntity>

    @Delete
    suspend fun deleteLocation(location: LocationEntity)

    @Update
    suspend fun updateLocation(location: LocationEntity)
}