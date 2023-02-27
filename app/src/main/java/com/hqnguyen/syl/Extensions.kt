package com.hqnguyen.syl

import android.content.Context
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.ContextCompat


fun Bitmap.addBorder(borderSize: Float, color: Int): Bitmap? {
    val bitmap = this.copy(Bitmap.Config.ARGB_8888, true)
    val w: Float = bitmap.width.toFloat()
    val h: Float = bitmap.height.toFloat()

    val radius = (h / 2).coerceAtMost(w / 2)
    val output = Bitmap.createBitmap((w + 8).toInt(), (h + 8).toInt(), Bitmap.Config.ARGB_8888)

    val p = Paint()
    p.isAntiAlias = true

    val c = Canvas(output)
    c.drawARGB(0, 0, 0, 0)
    p.style = Paint.Style.FILL

    c.drawCircle((w / 2 + 4), (h / 2 + 4), radius, p)

    p.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)


    c.drawBitmap(bitmap, 4f, 4f, p)
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

    val bitmap = this.copy(Bitmap.Config.ARGB_8888, true)

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
    canvas.drawBitmap(bitmap, rect, rect, paint)
    return output
}

fun Uri.convertToBitmap(context: Context): Bitmap? {
    try {
        this.let {
            if (Build.VERSION.SDK_INT < 28) {
                return MediaStore.Images.Media.getBitmap(context.contentResolver, this)
            } else {
                val source = ImageDecoder.createSource(context.contentResolver, this)
                return ImageDecoder.decodeBitmap(source)
            }
        }
    } catch (e: Exception) {
        return null
    }
}

fun Uri.convertToAvatar(context: Context): Bitmap? {
    var avatarMarker = this.convertToBitmap(context)
    avatarMarker?.let { bitmap ->
        val bitmapHaveSize = bitmap?.let { Bitmap.createScaledBitmap(it, 80, 80, false) }
        return bitmapHaveSize?.addBorder(5F, ContextCompat.getColor(context, R.color.blue))
    }
    return null
}
