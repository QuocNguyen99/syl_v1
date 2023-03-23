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
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.roundToInt
import kotlin.math.sin


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
        Timber.d("LineView $width $mHeight")
        val p = Paint()
        p.color = resources.getColor(R.color.blue, null)
        p.strokeWidth = 5f
        p.style = Paint.Style.FILL

        if (list.size == 0) return

        val listUTM = list.map { Deg2UTM(it.latitude(), it.longitude()) }

        var maxX = listUTM[0].Easting
        var maxY = listUTM[0].Northing

        var minX = listUTM[0].Easting
        var minY = listUTM[0].Northing

        listUTM.forEach {
            if (maxX <= it.Easting) {
                maxX = it.Easting
            }
            if (maxY <= it.Northing) {
                maxY = it.Northing
            }

            if (minX >= it.Easting) {
                minX = it.Easting
            }
            if (minY >= it.Northing) {
                minY = it.Northing
            }
        }

        listUTM.forEachIndexed { index, point ->

            if (index == (listUTM.size - 1)) return
            Timber.d(
                "rotation: ${(maxX - minX)}    " +
                        " X: ${((maxX - minX) / list.size)}   " +
                        "Y: ${((maxY - minY) / list.size)}"
            )
            if (index == 0) {
                renderPath.moveTo(
                    ((mWidth / ((maxX - minX) / list.size)) + (maxX - point.Easting)).toFloat(),
                    ((mHeight / ((maxY - minY) / list.size)) + (maxY - point.Northing)).toFloat()
                )
            } else {
                renderPath.lineTo(
                    ((mWidth / ((maxX - minX) / list.size)) + (maxX - point.Easting)).toFloat(),
                    ((mHeight / ((maxY - minY) / list.size)) + (maxY - point.Northing)).toFloat()
                )
            }
        }

        canvas?.drawPath(renderPath, p)
    }

    fun setData(points: ArrayList<Point>) {
        if (points.size == 0) return
        list = points
        invalidate()
    }
}

class Deg2UTM constructor(Lat: Double, Lon: Double) {
    var Easting: Double
    var Northing: Double
    var Zone: Int
    var Letter = 0.toChar()

    init {
        Zone = Math.floor(Lon / 6 + 31).toInt()
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
            (1 + cos(Lat * Math.PI / 180) * sin(Lon * Math.PI / 180 - (6 * Zone - 183) * Math.PI / 180)) / (1 - Math.cos(Lat * Math.PI / 180) * Math.sin(
                Lon * Math.PI / 180 - (6 * Zone - 183) * Math.PI / 180
            ))
        ) * 0.9996 * 6399593.62 / Math.pow(
            1 + Math.pow(0.0820944379, 2.0) * Math.pow(Math.cos(Lat * Math.PI / 180), 2.0), 0.5
        ) * (1 + Math.pow(0.0820944379, 2.0) / 2 * Math.pow(
            0.5 * Math.log(
                (1 + Math.cos(Lat * Math.PI / 180) * sin(Lon * Math.PI / 180 - (6 * Zone - 183) * Math.PI / 180)) / (1 - Math.cos(Lat * Math.PI / 180) * Math.sin(
                    Lon * Math.PI / 180 - (6 * Zone - 183) * Math.PI / 180
                ))
            ),
            2.0
        ) * Math.pow(
            Math.cos(Lat * Math.PI / 180), 2.0
        ) / 3) + 500000
        Easting = Math.round(Easting * 100) * 0.01
        Northing =
            (Math.atan(Math.tan(Lat * Math.PI / 180) / Math.cos(Lon * Math.PI / 180 - (6 * Zone - 183) * Math.PI / 180)) - Lat * Math.PI / 180) * 0.9996 * 6399593.625 / Math.sqrt(
                1 + 0.006739496742 * Math.pow(Math.cos(Lat * Math.PI / 180), 2.0)
            ) * (1 + 0.006739496742 / 2 * Math.pow(
                0.5 * Math.log(
                    (1 + Math.cos(Lat * Math.PI / 180) * Math.sin(
                        Lon * Math.PI / 180 - (6 * Zone - 183) * Math.PI / 180
                    )) / (1 - Math.cos(Lat * Math.PI / 180) * Math.sin(Lon * Math.PI / 180 - (6 * Zone - 183) * Math.PI / 180))
                ), 2.0
            ) * Math.pow(
                Math.cos(Lat * Math.PI / 180),
                2.0
            )) + 0.9996 * 6399593.625 * (Lat * Math.PI / 180 - 0.005054622556 * (Lat * Math.PI / 180 + Math.sin(2 * Lat * Math.PI / 180) / 2) + 4.258201531e-05 * (3 * (Lat * Math.PI / 180 + Math.sin(
                2 * Lat * Math.PI / 180
            ) / 2) + Math.sin(2 * Lat * Math.PI / 180) * Math.pow(
                Math.cos(Lat * Math.PI / 180), 2.0
            )) / 4 - 1.674057895e-07 * (5 * (3 * (Lat * Math.PI / 180 + Math.sin(2 * Lat * Math.PI / 180) / 2) + Math.sin(2 * Lat * Math.PI / 180) * Math.pow(
                Math.cos(Lat * Math.PI / 180),
                2.0
            )) / 4 + Math.sin(2 * Lat * Math.PI / 180) * Math.pow(Math.cos(Lat * Math.PI / 180), 2.0) * Math.pow(
                Math.cos(Lat * Math.PI / 180), 2.0
            )) / 3)
        if (Letter < 'M') Northing += 10_000_000
        Timber.d("Easting: $Easting  Northing:$Northing")
        Northing = (Northing * 100).roundToInt() * 0.01

    }
}
