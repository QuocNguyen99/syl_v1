package com.hqnguyen.syl.utils

import android.location.Location
import com.hqnguyen.syl.data.ListLocation
import com.hqnguyen.syl.data.local.entity.LocationEntity
import com.mapbox.geojson.Point

class DataHelper {

    companion object {
        private var sInstance: DataHelper? = null

        fun getInstance(): DataHelper {
            if (sInstance == null) sInstance = DataHelper()
            return sInstance ?: throw IllegalStateException("")
        }
    }

    fun convertData(data: List<LocationEntity>): ArrayList<ListLocation> {
        var timeStart = ""
        val sameTimeList = arrayListOf<Point>()
        val newList = arrayListOf<ListLocation>()
        data.forEach {
            if (it.timeStart == timeStart) {
                sameTimeList.add(Point.fromLngLat(it.lng, it.lat))
            } else {
                timeStart = it.timeStart
                if (!timeStart.isNullOrEmpty()) {
                    newList.add(
                        ListLocation(
                            timeStart,
                            ArrayList(sameTimeList.map { point -> point }),
                            calculator(ArrayList(sameTimeList.map { point -> point }))
                        )
                    )
                    sameTimeList.clear()
                } else {
                    sameTimeList.add(Point.fromLngLat(it.lng, it.lat))
                }
            }
        }
        return newList
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