package com.hqnguyen.syl.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.hqnguyen.syl.data.local.entity.LocationEntity

@Dao
interface LocationDAO {
    @Insert
    suspend fun insertItemLocation(locationLocal: LocationEntity)

    @Query("SELECT * FROM location")
    suspend fun getListLocation(): List<LocationEntity>
}