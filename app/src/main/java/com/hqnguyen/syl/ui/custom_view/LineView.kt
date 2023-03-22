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


class LineView @JvmOverloads constructor(context: Context, attr: AttributeSet) : View(context, attr) {

    var list: ArrayList<Point> = arrayListOf()

    private val renderPath: Path = Path()

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        Timber.d("LineView ${list.size}")
        val p = Paint()
        p.color = resources.getColor(R.color.blue, null)
        p.strokeWidth = 5f
        p.style = Paint.Style.FILL
        if (canvas != null) {
            drawAxis(canvas,p)
        }
        canvas?.drawPath(renderPath, p)
    }

    private fun drawAxis(canvas: Canvas, paint: Paint) {
        var mPaint = Paint()
        var mPath = Path()
        var mWidth = width
        var mHeight = height

        var mXUnit = mWidth / 12 //for 10 plots on x axis, 2 kept for padding;

        var mYUnit = mHeight / 12
        var mOriginX = mXUnit
        var mOriginY = mHeight - mYUnit
        var mBlackPaint = Paint()
        var mIsInit = true
        canvas.drawLine(mXUnit.toFloat(), mYUnit.toFloat(), mXUnit.toFloat(), (mHeight - 10).toFloat(), paint) //y-axis
        canvas.drawLine(
            10f, (mHeight - mYUnit).toFloat(),
            (mWidth - mXUnit).toFloat(), (mHeight - mYUnit).toFloat(), paint
        ) //x-axis
    }

    fun setData(list: ArrayList<Point>) {
        val tetsList = arrayListOf<Float>(0.8f, 0.5f, 0.1f, 0.3f, 0.9f, 1f, 2.2f, 0.5f, 0.3f, 0.5f)
        if (tetsList.size == 0) return
        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()
        var maxX = tetsList[0]
        var maxY = tetsList[0]

        tetsList.forEach {
            if (maxX <= it) {
                maxX = it
            }
            if (maxY <= it) {
                maxY = it
            }
        }

        tetsList.forEachIndexed { index, point ->
            if (index == (list.size - 1)) return
            if (index == 0) {
                renderPath.moveTo((viewWidth * (point / maxX)).toFloat(), (viewHeight * (point / maxY)).toFloat())
            } else {
                renderPath.lineTo((viewWidth * (point / maxX)).toFloat(), (viewHeight * (point / maxY)).toFloat())
            }
        }
        invalidate()
    }
}