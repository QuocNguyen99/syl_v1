package com.hqnguyen.syl.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "location")
data class LocationEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int? = null,
    @ColumnInfo(name = "lng")
    val lng: Double,
    @ColumnInfo(name = "lat")
    val lat: Double,
    @ColumnInfo(name = "timeStart")
    val timeStart: String,
)