package com.hqnguyen.syl.ui.map

import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.*
import com.hqnguyen.syl.R
import com.hqnguyen.syl.base.BaseFragment
import com.hqnguyen.syl.base.PermissionHelper
import com.hqnguyen.syl.base.PermissionHelper.permissionControl
import com.hqnguyen.syl.utils.convertToAvatar
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
    private var locationBroadCast: BroadcastReceiver? = null

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
    private var isCreatedPolylineManager = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stopService()
    }
    override fun onViewCreated() {
        annotationApi = binding.mapView.annotations
        initMapbox()
        intEvent()
        initBroadCast()
    }

    override fun onResume() {
        super.onResume()
        locationBroadCast?.let { LocalBroadcastManager.getInstance(requireContext()).registerReceiver(it, IntentFilter("your_intent_filter")) }
        createLocationRequest()
    }

    override fun onDestroy() {
        super.onDestroy()
        locationBroadCast?.let { LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(it) }
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

    private fun initBroadCast() {
        locationBroadCast = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                Timber.d("onStartCommand initBroadCast ")
                intent.extras?.let {
                    var data :ArrayList<Point>
                    when {
                        Build.VERSION.SDK_INT >= 33 -> data = it.getSerializable("POINT_LIST", ArrayList::class.java) as ArrayList<Point>
                        else -> @Suppress("DEPRECATION") data = it.getSerializable("POINT_LIST") as ArrayList<Point>
                    }
                    Timber.d("onStartCommand initBroadCast: ${data.size}")
                    pointList = data.plus(pointList) as ArrayList<Point>
                }
            }
        }
    }

    private fun intEvent() {
        binding.fabRecordLocation.setOnClickListener {
            if (PermissionHelper.isGranted(requireActivity(), *PermissionHelper.locationPermission)) {
                if (isRecording) {
                    binding.fabRecordLocation.setImageResource(R.drawable.ic_play)
                    stopService()
                } else {
                    binding.fabRecordLocation.setImageResource(R.drawable.ic_resume)
                    startService()
                }
                isRecording = !isRecording
            } else {
                createLocationRequest()
            }
        }
    }

    private fun startService() {
        val serviceIntent = Intent(context, LocationService::class.java)
        serviceIntent.putExtra("pointList", pointList)
        requireContext().startForegroundService(serviceIntent)
    }

    private fun stopService() {
        val serviceIntent = Intent(context, LocationService::class.java)
        requireContext().stopService(serviceIntent)
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

    @SuppressLint("MissingPermission")
    private fun createLocationRequest() {
        permissionControl(
            activity = requireActivity(),
            onGranted = {
                locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 700).build()

                locationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        super.onLocationResult(locationResult)
                        if (locationResult.lastLocation?.longitude == longitude && locationResult.lastLocation?.latitude == latitude)
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
            }
        )
    }

    private fun createPolyLine() {
        polylineAnnotationManager = polylineAnnotationManager ?: annotationApi!!.createPolylineAnnotationManager(
            annotationConfig = AnnotationConfig(LAYER_MARKER_ID, LAYER_ID, null)
        )
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
        pointAnnotationManager =
            pointAnnotationManager ?: annotationApi!!.createPointAnnotationManager(
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