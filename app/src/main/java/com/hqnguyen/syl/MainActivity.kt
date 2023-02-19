package com.hqnguyen.syl

import android.Manifest
import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.hqnguyen.syl.databinding.ActivityMainBinding
import com.mapbox.geojson.Point
import com.mapbox.maps.*
import com.mapbox.maps.plugin.annotation.AnnotationPlugin
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.locationcomponent.location
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var longitude: Double = 0.0
    private var latitude: Double = 0.0
    private var preLongitude: Double = 0.0
    private var preLatitude: Double = 0.0
    private var avatarMarker: Bitmap? = null

    private var requestLocationUpdates = true
    private val locationRequest = LocationRequest.create()?.apply {
        interval = 1000
        fastestInterval = 1000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                longitude = it.longitude
                latitude = it.latitude
            }
        }
        binding.mapView!!.getMapboxMap().loadStyleUri(
            Style.MAPBOX_STREETS
        ) {
            binding.mapView?.location?.updateSettings {
                enabled = false
                pulsingEnabled = false
            }
        }
        binding.btnImage.setOnClickListener {
            getImageInLocal()
        }
    }

    override fun onResume() {
        super.onResume()
        if (requestLocationUpdates) startLocationUpdates()
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            preLongitude = longitude
            preLatitude = latitude
            longitude = locationResult.lastLocation?.longitude ?: 0.0
            latitude = locationResult.lastLocation?.latitude ?: 0.0
            setMarkerCustom(avatarMarker)
            super.onLocationResult(locationResult)
        }
    }

    private fun getImageInLocal() {
        val photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        activityResultLauncher.launch(photoPickerIntent)
    }

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                it.data?.data?.let { uri ->
                    customMarker(uri)
                }
            }
        }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        if (locationRequest != null) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest, locationCallback, Looper.getMainLooper()
            )
        }
    }

    private fun customMarker(uri: Uri) {
        avatarMarker = Bitmap.createScaledBitmap(
            MediaStore.Images.Media.getBitmap(this.contentResolver, uri), 70, 70, false
        )
        avatarMarker = avatarMarker!!.getCroppedBitmap()
        avatarMarker = avatarMarker!!.addBorder(6F, Color.GREEN)
        setMarkerCustom(avatarMarker)
    }

    private fun setMarkerCustom(bitmap: Bitmap?) {
        bitmap?.let {
            if (!isFirstShow) {
                isFirstShow = true
            createAnnotationMarker()
            updatePositionCamera()
            animationWhenMoving()
            } else {
                animationWhenMoving()
            }
        }
    }

    private fun updatePositionCamera(point: Point? = null) {
        binding.mapView?.getMapboxMap()?.loadStyleUri(
            Style.MAPBOX_STREETS
        ) {

            binding.mapView.getMapboxMap().setCamera(
                CameraOptions.Builder().center(point ?: Point.fromLngLat(longitude, latitude))
                    .zoom(14.0)
                    .build()
            )
        }
    }

    private var pointAnnotationOptions: PointAnnotationOptions? = null
    var pointAnnotation: PointAnnotation? = null
    var pointAnnotationManager: PointAnnotationManager? = null
    var isFirstShow = false
    var annotationApi: AnnotationPlugin? = null

    private fun createAnnotationMarker(): PointAnnotation {
        var annotationApi = annotationApi ?: binding.mapView.annotations
        pointAnnotationManager =
            pointAnnotationManager ?: annotationApi.createPointAnnotationManager()
        pointAnnotationOptions =
            PointAnnotationOptions()
                .withPoint(Point.fromLngLat(longitude, latitude))
                .withIconImage(avatarMarker!!)
        pointAnnotation = pointAnnotationManager?.create(pointAnnotationOptions!!)
        return pointAnnotation!!
    }

    private fun updateAnnotationMarker() {
        pointAnnotation?.point = currentPoint!!
        pointAnnotation?.point = Point.fromLngLat(longitude, latitude)
        pointAnnotation?.let {
            pointAnnotationManager!!.update(it)
        }
    }

    private var animator: ValueAnimator? = null
    private var currentPoint: Point? = null
    private var prePoint: Point? = null

    private fun animationWhenMoving() {
        prePoint = Point.fromLngLat(preLongitude, preLatitude)
        currentPoint = Point.fromLngLat(longitude, latitude)
        animator?.let {
            if (it.isStarted) {
                prePoint = Point.fromLngLat(
                    (it.animatedValue as Point).longitude(),
                    (it.animatedValue as Point).latitude()
                )
                it.removeAllListeners()
                it.cancel()
            }
        }
        val pointEvaluator = TypeEvaluator<Point> { fraction, startValue, endValue ->
            Point.fromLngLat(
                startValue.longitude() + fraction * (endValue.longitude() - startValue.longitude()),
                startValue.latitude() + fraction * (endValue.latitude() - startValue.latitude())
            )
        }

        animator = ValueAnimator().apply {
            setObjectValues(prePoint, currentPoint)
            setEvaluator(pointEvaluator)
            addUpdateListener {
                val point = it.animatedValue as Point
                longitude = point.longitude()
                latitude = point.latitude()
                updateAnnotationMarker()
            }
            duration = 6000
            start()
        }


    }
}