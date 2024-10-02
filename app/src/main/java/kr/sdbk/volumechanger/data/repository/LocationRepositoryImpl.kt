package kr.sdbk.volumechanger.data.repository

import kr.sdbk.volumechanger.data.mapper.LocationMapper.toData
import kr.sdbk.volumechanger.data.mapper.LocationMapper.toEntity
import kr.sdbk.volumechanger.data.model.Location
import kr.sdbk.volumechanger.data.room.dao.LocationDao
import kr.sdbk.volumechanger.util.modules.GeofenceModule
import javax.inject.Inject

class LocationRepositoryImpl @Inject constructor(
    private val localDataSource: LocationDao,
    private val geofenceModule: GeofenceModule
): LocationRepository {
    override suspend fun insertLocation(location: Location) {
        localDataSource.insertLocation(location.toEntity())
        geofenceModule.addGeofencing(listOf(location))
    }

    override suspend fun insertLocationList(locations: List<Location>) {
        localDataSource.insertLocationList(locations.map { it.toEntity() })
        geofenceModule.addGeofencing(locations)
    }

    override suspend fun getAllLocation(): List<Location> = localDataSource.getAllLocation().map { it.toData() }

    override suspend fun deleteLocation(location: Location) {
        localDataSource.deleteLocation(location.toEntity())
        geofenceModule.removeGeofencing(listOf(location))
    }

    override suspend fun updateLocation(location: Location) {
        localDataSource.updateLocation(location.toEntity())
        geofenceModule.addGeofencing(listOf(location))
    }
}