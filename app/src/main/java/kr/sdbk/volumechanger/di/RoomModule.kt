package kr.sdbk.volumechanger.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kr.sdbk.volumechanger.data.room.database.VolumeChangerDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RoomModule {
    @Provides
    @Singleton
    fun providesDatabase(@ApplicationContext applicationContext: Context): VolumeChangerDatabase =
        Room.databaseBuilder(
            applicationContext,
            VolumeChangerDatabase::class.java,
            "volume-changer"
        ).build()

    @Provides
    @Singleton
    fun providesLocationDao(database: VolumeChangerDatabase) = database.locationDao()
}