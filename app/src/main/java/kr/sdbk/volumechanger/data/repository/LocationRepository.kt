package kr.sdbk.volumechanger.data.repository

import kr.sdbk.volumechanger.data.model.Location

interface LocationRepository {
    suspend fun insertLocation(location: Location)
    suspend fun insertLocationList(locations: List<Location>)
    suspend fun getAllLocation(): List<Location>
    suspend fun deleteLocation(location: Location)
    suspend fun updateLocation(location: Location)
}