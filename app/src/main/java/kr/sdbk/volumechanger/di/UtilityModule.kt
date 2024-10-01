package kr.sdbk.volumechanger.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kr.sdbk.volumechanger.util.modules.GeofenceModule
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class UtilityModule {
    @Provides
    @Singleton
    fun providesGeofenceModule(@ApplicationContext context: Context) = GeofenceModule(context)
}