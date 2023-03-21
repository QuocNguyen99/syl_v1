package com.hqnguyen.syl.ui.custom_view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.View
import com.hqnguyen.syl.R
import com.mapbox.geojson.Point


class LineView @JvmOverloads constructor(context: Context, attr: AttributeSet) : View(context, attr) {

    var list: ArrayList<Point> = arrayListOf()

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()

        val p = Paint()
        canvas!!.drawColor(Color.BLUE, PorterDuff.Mode.CLEAR)
        if (list.size > 0) {
            p.color = resources.getColor(R.color.blue, null)
            p.strokeWidth = 2f
            p.style = Paint.Style.FILL

            list.forEachIndexed { index, point ->
                if(index == (list.size -1)) return
                canvas.drawLine(
                    viewWidth * (point.latitude() / 1000).toInt(), viewHeight - (viewHeight * (point.longitude() / 100).toInt()),
                    viewWidth * (list[index + 1].latitude() / 1000).toInt(), viewHeight - (viewHeight * (list[index + 1].latitude() / 100).toInt()),
                    p
                )
            }
        }
    }

    fun setData(data: ArrayList<Point>) {
        list = data
        invalidate()
    }
}