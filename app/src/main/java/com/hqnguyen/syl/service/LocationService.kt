package com.hqnguyen.syl.service

import android.Manifest
import android.app.*
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.*
import com.hqnguyen.syl.MainActivity
import com.hqnguyen.syl.R
import com.mapbox.geojson.Point
import timber.log.Timber


class LocationService : Service() {

    companion object {
        const val CHANNEL_ID = "ForegroundServiceChannel"
    }

    private var fusedLocationClient: FusedLocationProviderClient? = null

    private var pointList = arrayListOf<Point>()

    private fun getLocation() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 700).build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                if (locationResult.lastLocation?.longitude == pointList[pointList.size - 1].longitude()
                    && locationResult.lastLocation?.latitude == pointList[pointList.size - 1].latitude()
                )
                    return
                Timber.d("doWork notification ${locationResult.lastLocation?.longitude}")
                pointList.add(
                    Point.fromLngLat(
                        locationResult.lastLocation?.longitude ?: 0.0,
                        locationResult.lastLocation?.latitude ?: 0.0
                    )
                )
            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext)

        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient?.requestLocationUpdates(
            locationRequest, locationCallback, Looper.getMainLooper()
        )
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(CHANNEL_ID, "Foreground Service Channel", NotificationManager.IMPORTANCE_DEFAULT)
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
    }

    private fun notificationToDisplayServiceInfo(): Notification {
        createNotificationChannel()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent: PendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getBroadcast(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
        } else {
            PendingIntent.getBroadcast(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SYL Service")
            .setContentText("App is running for get your location")
            .setSmallIcon(R.drawable.ic_location)
            .setContentIntent(pendingIntent)
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1, notificationToDisplayServiceInfo())
        val data = intent?.getSerializableExtra("pointList") as ArrayList<Point>
        Timber.d("onStartCommand ${data.size}")
        pointList = pointList.plus(data) as ArrayList<Point>
        getLocation()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        val intent = Intent("your_intent_filter")
        intent.putExtra("POINT_LIST", pointList)
        Timber.d("onStartCommand onDestroy${pointList.size}")
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
    }
}