package kr.sdbk.volumechanger.data.repository

import kr.sdbk.volumechanger.data.room.entity.LocationEntity

interface LocationRepository {
    suspend fun insertLocation(location: LocationEntity)
    suspend fun insertLocationList(locations: List<LocationEntity>)
    suspend fun getAllLocation(): List<LocationEntity>
    suspend fun deleteLocation(location: LocationEntity)
    suspend fun updateLocation(location: LocationEntity)
}