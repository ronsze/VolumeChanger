package kr.sdbk.volumechanger.data.room.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import kr.sdbk.volumechanger.data.mapper.LocationConverter
import kr.sdbk.volumechanger.data.room.dao.LocationDao
import kr.sdbk.volumechanger.data.room.entity.LocationEntity

@Database(entities = [LocationEntity::class], version = 1)
@TypeConverters(LocationConverter::class)
abstract class VolumeChangerDatabase: RoomDatabase() {
    abstract fun locationDao(): LocationDao
}