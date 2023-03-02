package com.hqnguyen.syl.data

import com.mapbox.geojson.Point

data class ListLocation(
    val startTime: String,
    val list: ArrayList<Point>,
    val distance: Double
)