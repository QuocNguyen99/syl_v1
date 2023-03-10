package com.hqnguyen.syl.data.local.repository

import androidx.lifecycle.LiveData
import com.hqnguyen.syl.data.local.dao.LocationDAO
import com.hqnguyen.syl.data.local.entity.LocationEntity

class LocationRepository constructor(private val locationDAO: LocationDAO) {
    fun getListLocation(): LiveData<List<LocationEntity>> = locationDAO.getListLocation()

    suspend fun insertLocation(locationEntity: LocationEntity) = locationDAO.insertItemLocation(locationEntity)

}