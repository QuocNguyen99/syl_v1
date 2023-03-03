package com.hqnguyen.syl.base.config

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.hqnguyen.syl.data.local.dao.LocationDAO
import com.hqnguyen.syl.data.local.entity.LocationEntity

@Database(entities = [LocationEntity::class], version = 1)
abstract class LocalDatabase : RoomDatabase() {
    abstract fun locationDao(): LocationDAO

    companion object {
        private const val DB_NAME = "Local_Database"
        private var databaseManager: LocalDatabase? = null

        fun getInstance(context: Context): LocalDatabase {
            if (databaseManager == null) {
                databaseManager = Room.databaseBuilder(context, LocalDatabase::class.java, DB_NAME)
                    .fallbackToDestructiveMigration()
                    .build()
            }
            return databaseManager!!
        }
    }
}