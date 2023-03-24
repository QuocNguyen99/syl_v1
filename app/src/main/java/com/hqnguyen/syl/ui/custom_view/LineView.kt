package com.hqnguyen.syl.ui.custom_view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import com.hqnguyen.syl.R
import com.mapbox.geojson.Point
import timber.log.Timber
import kotlin.math.*


class LineView @JvmOverloads constructor(context: Context, attr: AttributeSet) : View(context, attr) {

    var list: ArrayList<Point> = arrayListOf()

    private val renderPath: Path = Path()
    var mWidth = 0
    var mHeight = 0

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        mWidth = width
        mHeight = height
        val p = Paint()
        p.color = resources.getColor(R.color.blue, null)
        p.strokeWidth = 5f
        p.style = Paint.Style.FILL

        if (list.size == 0) return

        val listUTM = list.map { Deg2UTM(it.latitude(), it.longitude()) }

        val listScale = listScaleUTMToWidthHeight(listUTM, width, height).distinct()
        Timber.d("NewData")
        listScale.forEachIndexed { index, item ->
            if (listScale.size-1 == index) return
//            if (index == 0) {
//                renderPath.moveTo(item.first.toFloat(), item.second.toFloat())
//            } else {
//                renderPath.lineTo(item.first.toFloat(), item.second.toFloat())
            canvas?.drawLine(item.first.toFloat(), item.second.toFloat(),listScale[index+1].first.toFloat(),listScale[index+1].second.toFloat(),p)
//            }
        }

        canvas?.drawPath(renderPath, p)
    }

    fun setData(points: ArrayList<Point>) {
        if (points.size == 0) return
        list = points
        invalidate()
    }

    private fun listScaleUTMToWidthHeight(listUTM: List<Deg2UTM>, viewWidth: Int, viewHeight: Int): List<Pair<Double, Double>> {
        var minEast = listUTM[0].Easting
        var maxEast = listUTM[1].Easting

        var minNorth = listUTM[0].Northing
        var maxNorth = listUTM[1].Northing

        listUTM.forEach {
            if (minEast >= it.Easting) {
                minEast = it.Easting
            }
            if (maxEast <= it.Easting) {
                maxEast = it.Easting
            }

            if (minNorth >= it.Northing) {
                minNorth = it.Northing
            }

            if (maxNorth <= it.Northing) {
                maxNorth = it.Northing
            }
        }

        return listUTM.map {
            val rangeEastUTM = maxEast - minEast
            val rangeWidth = viewWidth - 0
            val ratioScaleWidth = rangeWidth / rangeEastUTM
            val x = ratioScaleWidth * (it.Easting - minEast)

            val rangeNorthUTM = maxNorth - minNorth
            val rangeHeight = viewHeight - 0
            val ratioScaleHeight = rangeHeight / rangeNorthUTM
            val y = ratioScaleHeight * (it.Northing - minNorth)
            Pair(x, y)
        }
    }
}

class Deg2UTM constructor(Lat: Double, Lon: Double) {
    var Easting: Double
    var Northing: Double
    var Zone: Int
    var Letter = 0.toChar()

    init {
        Zone = floor(Lon / 6 + 31).toInt()
        Letter =
            if (Lat < -72) 'C'
            else if (Lat < -64) 'D'
            else if (Lat < -56) 'E'
            else if (Lat < -48) 'F'
            else if (Lat < -40) 'G' else if (Lat < -32) 'H'
            else if (Lat < -24) 'J' else if (Lat < -16) 'K' else if (Lat < -8) 'L' else if (Lat < 0) 'M'
            else if (Lat < 8) 'N' else if (Lat < 16) 'P' else if (Lat < 24) 'Q' else if (Lat < 32) 'R'
            else if (Lat < 40) 'S' else if (Lat < 48) 'T' else if (Lat < 56) 'U' else if (Lat < 64) 'V'
            else if (Lat < 72) 'W' else 'X'

        Easting = 0.5 * ln(
            (1 + cos(Lat * Math.PI / 180) * sin(Lon * Math.PI / 180 - (6 * Zone - 183) * Math.PI / 180)) / (1 - cos(Lat * Math.PI / 180) * sin(
                Lon * Math.PI / 180 - (6 * Zone - 183) * Math.PI / 180
            ))
        ) * 0.9996 * 6399593.62 / (1 + 0.0820944379.pow(2.0) * cos(Lat * Math.PI / 180).pow(2.0)).pow(0.5) * (1 + 0.0820944379.pow(2.0) / 2 * (0.5 * ln(
            (1 + cos(Lat * Math.PI / 180) * sin(Lon * Math.PI / 180 - (6 * Zone - 183) * Math.PI / 180)) / (1 - cos(Lat * Math.PI / 180) * sin(
                Lon * Math.PI / 180 - (6 * Zone - 183) * Math.PI / 180
            ))
        )).pow(2.0) * cos(Lat * Math.PI / 180).pow(2.0) / 3) + 500000
        Easting = (Easting * 100).roundToInt() * 0.01
        Northing =
            (atan(tan(Lat * Math.PI / 180) / cos(Lon * Math.PI / 180 - (6 * Zone - 183) * Math.PI / 180)) - Lat * Math.PI / 180) * 0.9996 * 6399593.625 / sqrt(
                1 + 0.006739496742 * cos(Lat * Math.PI / 180).pow(2.0)
            ) * (1 + 0.006739496742 / 2 * (0.5 * ln(
                (1 + cos(Lat * Math.PI / 180) * sin(
                    Lon * Math.PI / 180 - (6 * Zone - 183) * Math.PI / 180
                )) / (1 - cos(Lat * Math.PI / 180) * sin(Lon * Math.PI / 180 - (6 * Zone - 183) * Math.PI / 180))
            )).pow(2.0) * cos(Lat * Math.PI / 180).pow(2.0)) + 0.9996 * 6399593.625 * (Lat * Math.PI / 180 - 0.005054622556 * (Lat * Math.PI / 180 + sin(2 * Lat * Math.PI / 180) / 2) + 4.258201531e-05 * (3 * (Lat * Math.PI / 180 + sin(
                2 * Lat * Math.PI / 180
            ) / 2) + sin(2 * Lat * Math.PI / 180) * cos(Lat * Math.PI / 180).pow(2.0)) / 4 - 1.674057895e-07 * (5 * (3 * (Lat * Math.PI / 180 + sin(2 * Lat * Math.PI / 180) / 2) + sin(2 * Lat * Math.PI / 180) * cos(Lat * Math.PI / 180).pow(2.0)) / 4 + sin(2 * Lat * Math.PI / 180) * cos(Lat * Math.PI / 180).pow(
                2.0
            ) * cos(Lat * Math.PI / 180).pow(2.0)) / 3)
        if (Letter < 'M') Northing += 10_000_000
        Timber.d("Easting: $Easting            Northing:$Northing")
        Northing = (Northing * 100).roundToInt() * 0.01

    }
}
