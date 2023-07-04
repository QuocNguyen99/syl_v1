package com.hqnguyen.syl.ui.map

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hqnguyen.syl.base.config.LocalDatabase
import com.hqnguyen.syl.data.local.entity.LocationEntity
import com.hqnguyen.syl.data.local.repository.LocationRepository
import com.mapbox.geojson.Point
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class MapViewModel(context: Context) : ViewModel() {

    private var repoLocation: LocationRepository

    init {
        val dbManager = LocalDatabase.getInstance(context)
        val locationDao = dbManager.locationDao()
        repoLocation = LocationRepository(locationDao)
    }

    fun saveLocationToLocal(point: Point, timeStart: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repoLocation.insertLocation(LocationEntity(lng = point.longitude(), lat = point.latitude(), timeStart = timeStart))
                repoLocation.insertLocation(LocationEntity(lng = point.longitude(), lat = point.latitude(), timeStart = timeStart))
            } catch (ex: java.lang.Exception) {
                Timber.e(ex.message)
            }
        }
    }

    fun getListLocationFormLocal():LiveData<List<LocationEntity>> {
        return repoLocation.getListLocation()
    }
}