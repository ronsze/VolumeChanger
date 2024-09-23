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
    fun insertLocation(location: LocationEntity)

    @Insert
    fun insertLocationList(locationList: List<LocationEntity>)

    @Query("SELECT * from location")
    fun getAllLocation(): LocationEntity

    @Delete
    fun deleteLocation(location: LocationEntity)

    @Update
    fun updateLocation(location: LocationEntity)
}