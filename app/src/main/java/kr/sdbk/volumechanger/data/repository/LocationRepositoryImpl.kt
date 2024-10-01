package kr.sdbk.volumechanger.data.repository

import kr.sdbk.volumechanger.data.room.dao.LocationDao
import kr.sdbk.volumechanger.data.room.entity.LocationEntity
import kr.sdbk.volumechanger.util.modules.GeofenceModule
import javax.inject.Inject

class LocationRepositoryImpl @Inject constructor(
    private val localDataSource: LocationDao,
    private val geofenceModule: GeofenceModule
): LocationRepository {
    override suspend fun insertLocation(location: LocationEntity) {
        localDataSource.insertLocation(location)
        geofenceModule.addGeofencing(listOf(location))
    }

    override suspend fun insertLocationList(locations: List<LocationEntity>) {
        localDataSource.insertLocationList(locations)
        geofenceModule.addGeofencing(locations)
    }

    override suspend fun getAllLocation(): List<LocationEntity> = localDataSource.getAllLocation()

    override suspend fun deleteLocation(location: LocationEntity) {
        localDataSource.deleteLocation(location)
        geofenceModule.removeGeofencing(listOf(location))
    }

    override suspend fun updateLocation(location: LocationEntity) {
        localDataSource.updateLocation(location)
        geofenceModule.addGeofencing(listOf(location))
    }
}