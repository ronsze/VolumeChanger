package kr.sdbk.volumechanger.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.sdbk.volumechanger.data.repository.LocationRepository
import kr.sdbk.volumechanger.data.repository.LocationRepositoryImpl
import kr.sdbk.volumechanger.data.room.dao.LocationDao
import kr.sdbk.volumechanger.util.modules.GeofenceModule
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {
    @Provides
    @Singleton
    fun providesLocationRepository(
        locationDao: LocationDao,
        geofenceModule: GeofenceModule
    ): LocationRepository = LocationRepositoryImpl(locationDao, geofenceModule)
}