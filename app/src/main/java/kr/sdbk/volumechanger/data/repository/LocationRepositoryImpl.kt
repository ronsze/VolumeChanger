package kr.sdbk.volumechanger.data.repository

import kr.sdbk.volumechanger.data.room.dao.LocationDao
import kr.sdbk.volumechanger.data.room.entity.LocationEntity

class LocationRepositoryImpl(
    private val localDataSource: LocationDao
): LocationRepository {
    override suspend fun insertLocation(location: LocationEntity) = localDataSource.insertLocation(location)

    override suspend fun insertLocationList(locations: List<LocationEntity>) = localDataSource.insertLocationList(locations)

    override suspend fun getAllLocation(): List<LocationEntity>  = localDataSource.getAllLocation()

    override suspend fun deleteLocation(location: LocationEntity) = localDataSource.deleteLocation(location)

    override suspend fun updateLocation(location: LocationEntity) = localDataSource.updateLocation(location)
}