@file:Suppress("DEPRECATION")

package com.hqnguyen.syl.utils

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import com.hqnguyen.syl.R
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*


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

fun Bitmap.saveImage(context: Context) {
    var savedImagePath: String? = ""
    val imageFileName = "JPEG_" + Calendar.getInstance().timeInMillis.toString() + ".jpg"
    val storageDir = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            .toString() + "/avatar"
    )
    Timber.d("storageDir: $storageDir")
    var success = true
    if (!storageDir.exists()) {
        success = storageDir.mkdirs()
    }
    if (success) {
        val imageFile = File(storageDir, imageFileName)
        savedImagePath = imageFile.absolutePath
        try {
            val fOut: OutputStream = FileOutputStream(imageFile)
            this.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
            fOut.close()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
    savedImagePath?.let {
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val f = File(it)
        val contentUri = Uri.fromFile(f)
        mediaScanIntent.data = contentUri
        context.sendBroadcast(mediaScanIntent)
    }
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
    val avatarMarker = this.convertToBitmap(context)
    avatarMarker?.let { bitmap ->
        val bitmapHaveSize = bitmap.let { Bitmap.createScaledBitmap(it, 80, 80, false) }
        return bitmapHaveSize?.addBorder(5F, ContextCompat.getColor(context, R.color.blue))
    }
    return null
}

fun String.getDate(dateFormat: String = "dd/MM/yyyy hh:mm"): String {
    val formatter = SimpleDateFormat(dateFormat)
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = this.toLong()
    return formatter.format(calendar.time)
}