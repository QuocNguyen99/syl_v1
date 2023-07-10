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
import android.os.CountDownTimer
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.*
import com.hqnguyen.syl.R
import com.hqnguyen.syl.base.BaseFragment
import com.hqnguyen.syl.base.PermissionHelper
import com.hqnguyen.syl.base.PermissionHelper.permissionControl
import com.hqnguyen.syl.databinding.FragmentMapBinding
import com.hqnguyen.syl.service.LocationService
import com.hqnguyen.syl.ui.login.UserViewModel
import com.hqnguyen.syl.utils.convertToAvatar
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
import java.util.*
import kotlin.math.roundToInt

class MapFragment : BaseFragment<FragmentMapBinding>(FragmentMapBinding::inflate) {

    companion object {
        private const val LAYER_ID = "line_layer"
        private const val SOURCE_ID = "line_source"
        private const val LAYER_MARKER_ID = "line_layer"
    }

    private val userVM: UserViewModel by activityViewModels()
    private lateinit var mapVM: MapViewModel

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
    private var timeStart: String = ""

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

    private var countTimer : CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stopService()
    }

    override fun onViewCreated() {
        annotationApi = binding.mapView.annotations
        mapVM = ViewModelProvider(
            requireActivity(),
            MapViewModelFactory(context = requireContext())
        )[MapViewModel::class.java]

        initMapbox()
        intEvent()
        initBroadCast()
        userVM.getTokenAndCheckWasLogin()
    }

    override fun onResume() {
        super.onResume()
        locationBroadCast?.let {
            LocalBroadcastManager.getInstance(requireContext())
                .registerReceiver(it, IntentFilter("your_intent_filter"))
        }
        createLocationRequest()
    }

    override fun onDestroy() {
        super.onDestroy()
        locationBroadCast?.let {
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(it)
        }
    }

    override fun onObserver() {
        userVM.uriAvatarUser.observe(viewLifecycleOwner) {
            it?.let { uri ->
                avatarMarker = uri.convertToAvatar(requireContext())
                initMapbox()
                avatarMarker?.let { avatar -> createAnnotationMarker(avatar) }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun initBroadCast() {
        locationBroadCast = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                intent.extras?.let {
                    val data: ArrayList<Point> = when {
                        Build.VERSION.SDK_INT >= 33 -> it.getSerializable(
                            "POINT_LIST",
                            ArrayList::class.java
                        ) as ArrayList<Point>

                        else -> it.getSerializable("POINT_LIST") as ArrayList<Point>
                    }
                    pointList = data
                }
            }
        }
    }

    private fun intEvent() {
        binding.btnRecordLocation.setOnClickListener {
            if (PermissionHelper.isGranted(requireActivity(), *PermissionHelper.locationPermission)
            ) {
                if (binding.layoutInfo.isVisible) {
                    binding.layoutInfo.isVisible = false
                    binding.btnRecordLocation.text = "Start"
                    stopService()
                    countTimer?.cancel()
                    countTimer = null
                } else {
                    binding.layoutInfo.isVisible = true
                    binding.btnRecordLocation.text = "Resume"
                    startService()
                    countTimer = object :CountDownTimer(200,1000){
                        override fun onTick(millisUntilFinished: Long) {
                            val hours = (millisUntilFinished / (1000 * 60 * 60)).toInt()
                            val minutes = ((millisUntilFinished % (1000 * 60 * 60)) / (1000 * 60)).toInt()
                            val seconds = ((millisUntilFinished % (1000 * 60)) / 1000).toInt()
                            Timber.d("millisUntilFinished: $millisUntilFinished - housr: $hours - minus: $minutes - seconds: $seconds")
                            binding.tvContentTime.text = "$hours:$minutes:$seconds"
                        }

                        override fun onFinish() {
                        }
                    }
                    countTimer?.start()
                }
                isRecording = !isRecording
                timeStart = Calendar.getInstance().timeInMillis.toString()
            } else {
                createLocationRequest()
            }
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

    private fun startService() {
        try {
            val serviceIntent = Intent(context, LocationService::class.java)
            serviceIntent.putExtra("pointList", pointList)

            requireContext().startForegroundService(serviceIntent)
        } catch (ex: Exception) {
            Timber.e("startService fail: ${ex.message}")
        }
    }

    private fun stopService() {
        val serviceIntent = Intent(context, LocationService::class.java)
        requireContext().stopService(serviceIntent)
    }

    @SuppressLint("MissingPermission")
    private fun createLocationRequest() {
        permissionControl(
            activity = requireActivity(),
            onGranted = {
                locationRequest =
                    LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 700).build()

                locationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        super.onLocationResult(locationResult)
                        Timber.d("onLocationResult longitude: $longitude -- latitude: $latitude ")
                        if (locationResult.lastLocation?.longitude == longitude && locationResult.lastLocation?.latitude == latitude)
                            return
                        binding.tvContentSpeed.text = "${locationResult.lastLocation?.speed?.times(3.6)?.roundToInt()} km/h"
                        preLongitude = longitude
                        preLatitude = latitude
                        longitude = locationResult.lastLocation?.longitude ?: 0.0
                        latitude = locationResult.lastLocation?.latitude ?: 0.0
                        val point = Point.fromLngLat(longitude, latitude)
                        pointList.add(point)

                        animationWhenMoving()
                        if (isRecording) {
                            mapVM.saveLocationToLocal(point, timeStart)
                        }
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

                fusedLocationClient =
                    LocationServices.getFusedLocationProviderClient(requireContext())

                fusedLocationClient.requestLocationUpdates(
                    locationRequest, locationCallback, Looper.getMainLooper()
                )
            }
        )
    }

    private fun createPolyLine() {
        polylineAnnotationManager =
            polylineAnnotationManager ?: annotationApi!!.createPolylineAnnotationManager(
                annotationConfig = AnnotationConfig(LAYER_MARKER_ID, LAYER_ID, null)
            )
        polylineAnnotationOptions = PolylineAnnotationOptions()
            .withPoints(ArrayList(pointList))
            .withLineColor(ContextCompat.getColor(requireContext(), R.color.blue))
            .withLineWidth(8.0)
            .withLineJoin(LineJoin.ROUND)
            .withLineBlur(2.0)

        polylineAnnotation = polylineAnnotationManager!!.create(polylineAnnotationOptions!!)
    }

    private fun updatePolyline() {
        polylineAnnotation?.points = ArrayList(pointList)
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
            duration = 600
            start()
        }
    }

    override fun onDestroyView() {
        binding.mapView.onDestroy()
        super.onDestroyView()
    }

}