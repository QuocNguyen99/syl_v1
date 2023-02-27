package com.hqnguyen.syl.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.hqnguyen.syl.R
import com.mapbox.geojson.Point
import timber.log.Timber


class LocationService(private val context: Context) : Service() {

    companion object {
        const val NOTIFICATION_ID = 123
        const val CHANNEL_ID = "CHANNEL_ID"
    }

    private var fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    private var pointList = arrayListOf<Point>()

    private fun getLocation() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 700).build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                Timber.d("doWork notification ${locationResult.lastLocation?.longitude}")
                pointList.add(Point.fromLngLat(locationResult.lastLocation?.longitude ?: 0.0, locationResult.lastLocation?.latitude ?: 0.0))
            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest, locationCallback, Looper.getMainLooper()
        )
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createChannel() {
        // Create a Notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.app_name)
            val descriptionText = context.getString(R.string.app_name)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val name = context.getString(R.string.app_name)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel()
        }
//
//        val pendingIntent: PendingIntent =
//            Intent(this, MainActivity::class.java).let { notificationIntent ->
//                PendingIntent.getActivity(this, 0, notificationIntent,
//                    PendingIntent.FLAG_IMMUTABLE)
//            }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(name)
            .setTicker(name)
            .setContentText(name)
            .setSmallIcon(R.mipmap.logo)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)

        return START_STICKY
    }
}