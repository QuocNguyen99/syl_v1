package com.hqnguyen.syl

import android.graphics.*


fun Bitmap.addBorder(borderSize: Float, color: Int): Bitmap? {
    val w: Float = this.width.toFloat()
    val h: Float = this.height.toFloat()

    val radius = (h / 2).coerceAtMost(w / 2)
    val output = Bitmap.createBitmap((w + 8).toInt(), (h + 8).toInt(), Bitmap.Config.ARGB_8888)

    val p = Paint()
    p.isAntiAlias = true

    val c = Canvas(output)
    c.drawARGB(0, 0, 0, 0)
    p.style = Paint.Style.FILL

    c.drawCircle((w / 2 + 4), (h / 2 + 4), radius, p)

    p.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

    c.drawBitmap(this, 4f, 4f, p)
    p.xfermode = null
    p.style = Paint.Style.STROKE
    p.color = color
    p.strokeWidth = borderSize
    c.drawCircle((w / 2 + 4), (h / 2 + 4), radius, p)

    return output
}

fun Bitmap.getCroppedBitmap(): Bitmap? {
    val output = Bitmap.createBitmap(
        this.width,
        this.height, Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(output)
    val color = -0xbdbdbe
    val paint = Paint()
    val rect = Rect(0, 0, this.width, this.height)
    paint.isAntiAlias = true
    canvas.drawARGB(0, 0, 0, 0)
    paint.color = color
    canvas.drawCircle(
        (this.width / 2).toFloat(), (this.height / 2).toFloat(),
        (this.width / 2).toFloat(), paint
    )
    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    canvas.drawBitmap(this, rect, rect, paint)
    return output
}
