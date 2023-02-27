package com.hqnguyen.syl.ui.map

import android.Manifest
import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.google.android.gms.location.*
import com.hqnguyen.syl.*
import com.hqnguyen.syl.R
import com.hqnguyen.syl.base.BaseFragment
import com.hqnguyen.syl.databinding.FragmentMapBinding
import com.hqnguyen.syl.service.LocationService
import com.hqnguyen.syl.ui.login.UserViewModel
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin
import com.mapbox.maps.plugin.annotation.AnnotationConfig
import com.mapbox.maps.plugin.annotation.AnnotationPlugin
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.*
import com.mapbox.maps.plugin.locationcomponent.location
import timber.log.Timber


class MapFragment : BaseFragment<FragmentMapBinding>(FragmentMapBinding::inflate) {

    companion object {
        private const val LAYER_ID = "line_layer"
        private const val SOURCE_ID = "line_source"
        private const val LAYER_MARKER_ID = "line_layer"
    }

    private val userVM: UserViewModel by activityViewModels()

    private var isRecording = false


    private var longitude: Double = 0.0
    private var latitude: Double = 0.0
    private var preLongitude: Double = 0.0
    private var preLatitude: Double = 0.0
    private var avatarMarker: Bitmap? = null
    private var animator: ValueAnimator? = null
    private var currentPoint: Point? = null
    private var prePoint: Point? = null
    private var pointList = arrayListOf<Point>()

    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var annotationApi: AnnotationPlugin? = null

    private var pointAnnotationManager: PointAnnotationManager? = null
    private var pointAnnotationOptions: PointAnnotationOptions? = null

    private var polylineAnnotationManager: PolylineAnnotationManager? = null
    private var polylineAnnotationOptions: PolylineAnnotationOptions? = null
    private var pointAnnotation: PointAnnotation? = null
    private var polylineAnnotation: PolylineAnnotation? = null
    var isCreatedPolylineManager = false

    override fun onViewCreated() {
        initMapbox()
        intEvent()
        createLocationRequest()
    }

    override fun onObserverLiveData() {
        userVM.uriAvatarUser.observe(viewLifecycleOwner) {
            it?.let { uri ->
                avatarMarker = uri.convertToAvatar(requireContext())
                initMapbox()
                avatarMarker?.let { avatar -> createAnnotationMarker(avatar) }
            }
        }


    }

    private fun intEvent() {
        binding.fabRecordLocation.setOnClickListener {
            if (isRecording) {
                binding.fabRecordLocation.setImageResource(R.drawable.ic_play)
            } else {
                binding.fabRecordLocation.setImageResource(R.drawable.ic_resume)
                createService()
            }
            isRecording = !isRecording
        }
    }

    private fun createService() {
        val intent = Intent(context, LocationService.javaClass) // Build the intent for the service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context?.startForegroundService(intent)
        }
    }

    private fun initMapbox() {
        binding.mapView.getMapboxMap().loadStyleUri(
            Style.MAPBOX_STREETS
        ) {
            binding.mapView.location.updateSettings {
                enabled = avatarMarker == null
                pulsingEnabled = avatarMarker == null
            }
            if (longitude != 0.0 && latitude != 0.0) {
                updatePositionCamera()
            }
        }
    }

    private fun createLocationRequest() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            //Will show popup
            return
        }

        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 700).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                if (locationResult.lastLocation?.longitude == longitude &&
                    locationResult.lastLocation?.latitude == latitude
                )
                    return
                preLongitude = longitude
                preLatitude = latitude
                longitude = locationResult.lastLocation?.longitude ?: 0.0
                latitude = locationResult.lastLocation?.latitude ?: 0.0
                pointList.add(Point.fromLngLat(longitude, latitude))
                Timber.d("onLocationResult longitude: $longitude -- latitude: $latitude ")
                animationWhenMoving()
                if (pointList.size >= 2) {
                    if (!isCreatedPolylineManager) {
                        isCreatedPolylineManager = true
                        createPolyLine()
                    } else {
                        updatePolyline()
                    }
                }
            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        fusedLocationClient.requestLocationUpdates(
            locationRequest, locationCallback, Looper.getMainLooper()
        )

        annotationApi = binding.mapView.annotations
        polylineAnnotationManager = annotationApi!!.createPolylineAnnotationManager(
            annotationConfig = AnnotationConfig(LAYER_MARKER_ID, LAYER_ID, SOURCE_ID)
        )
    }

    private fun createPolyLine() {
        polylineAnnotationOptions = PolylineAnnotationOptions()
            .withPoints(pointList)
            .withLineColor(ContextCompat.getColor(requireContext(), R.color.blue))
            .withLineWidth(8.0)
            .withLineJoin(LineJoin.ROUND)
            .withLineBlur(2.0)

        polylineAnnotation = polylineAnnotationManager!!.create(polylineAnnotationOptions!!)
    }

    private fun updatePolyline() {
        polylineAnnotation?.points = pointList
        polylineAnnotationManager?.update(polylineAnnotation!!)
    }

    private fun createAnnotationMarker(bitmap: Bitmap) {
        pointAnnotationManager = pointAnnotationManager ?: annotationApi!!.createPointAnnotationManager(
            annotationConfig = AnnotationConfig()
        )
        pointAnnotationOptions = pointAnnotationOptions ?: PointAnnotationOptions()
            .withPoint(Point.fromLngLat(longitude, latitude))
            .withIconImage(bitmap)
        if (pointAnnotation != null) {
            pointAnnotation?.iconImageBitmap = bitmap
            pointAnnotation?.let { pointAnnotationManager!!.update(it) }
        } else {
            pointAnnotation = pointAnnotationManager?.create(pointAnnotationOptions!!)
        }
    }

    private fun updateAnnotationMarker() {
        pointAnnotation?.point = Point.fromLngLat(longitude, latitude)
        pointAnnotation?.let { pointAnnotationManager!!.update(it) }
    }

    private fun updatePositionCamera(point: Point? = null) {
        binding.mapView.getMapboxMap().loadStyleUri(
            Style.MAPBOX_STREETS
        ) {
            binding.mapView.getMapboxMap().setCamera(
                CameraOptions.Builder().center(point ?: Point.fromLngLat(longitude, latitude))
                    .zoom(14.0)
                    .build()
            )
        }
    }

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
                updatePositionCamera()
            }
            duration = 1000
            start()
        }
    }
}