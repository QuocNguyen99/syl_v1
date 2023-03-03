package com.hqnguyen.syl.ui.map

import android.content.Context
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hqnguyen.syl.base.config.LocalDatabase
import com.hqnguyen.syl.data.ListLocation
import com.hqnguyen.syl.data.local.LocationRepository
import com.hqnguyen.syl.data.local.entity.LocationEntity
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
        getListLocationFormLocal()
    }

    private val _listLocation = MutableLiveData<List<ListLocation>>(listOf())
    val listLocation: LiveData<List<ListLocation>> = _listLocation

    fun saveLocationToLocal(point: Point, timeStart: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repoLocation.insertLocation(LocationEntity(lng = point.longitude(), lat = point.latitude(), timeStart = timeStart))
            } catch (ex: java.lang.Exception) {
                Timber.e(ex.message)
            }
        }
    }

    fun getListLocationFormLocal() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repoLocation.getListLocation()
                var timeStart = ""
                val sameTimeList = arrayListOf<Point>()
                val listLocationAfterParse = arrayListOf<ListLocation>()
                response.forEachIndexed { index, it ->
                    if (index == response.size - 1) {
                        sameTimeList.add(Point.fromLngLat(it.lng, it.lat))
                        listLocationAfterParse.add(
                            ListLocation(timeStart, ArrayList(sameTimeList), calculator(ArrayList(sameTimeList)))
                        )
                        return@forEachIndexed
                    }
                    if (it.timeStart == timeStart) {
                        sameTimeList.add(Point.fromLngLat(it.lng, it.lat))
                    } else {
                        if (timeStart.isNotEmpty()) {
                            listLocationAfterParse.add(
                                ListLocation(timeStart, ArrayList(sameTimeList), calculator(ArrayList(sameTimeList)))
                            )
                            sameTimeList.clear()
                        } else {
                            sameTimeList.add(Point.fromLngLat(it.lng, it.lat))
                        }
                        timeStart = it.timeStart
                    }
                }
                _listLocation.postValue(listLocationAfterParse)
            } catch (ex: java.lang.Exception) {
                Timber.e(ex.message)
            }
        }
    }

    private fun calculator(list: ArrayList<Point>): Double {
        var totalKM = 0f
        var preItem: Point? = null
        list.forEach { point ->
            if (preItem == null) {
                preItem = point
            } else {
                val locationA = Location("").apply {
                    latitude = preItem!!.latitude()
                    longitude = preItem!!.longitude()
                }
                val locationB = Location("").apply {
                    latitude = point.latitude()
                    longitude = point.longitude()
                }
                totalKM += locationA.distanceTo(locationB)
                preItem = point
            }
        }
        return totalKM.toDouble()
    }
}