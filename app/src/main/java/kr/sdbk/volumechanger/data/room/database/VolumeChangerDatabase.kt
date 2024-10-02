package kr.sdbk.volumechanger.data.room.database

import androidx.room.Database
import androidx.room.RoomDatabase
import kr.sdbk.volumechanger.data.room.dao.LocationDao
import kr.sdbk.volumechanger.data.room.entity.LocationEntity

@Database(entities = [LocationEntity::class], version = 1)
abstract class VolumeChangerDatabase: RoomDatabase() {
    abstract fun locationDao(): LocationDao
}