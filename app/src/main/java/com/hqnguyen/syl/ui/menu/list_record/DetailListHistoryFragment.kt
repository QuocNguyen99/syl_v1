package com.hqnguyen.syl.ui.menu.list_record

import android.annotation.SuppressLint
import android.location.Location
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.hqnguyen.syl.base.BaseFragment
import com.hqnguyen.syl.data.ListLocation
import com.hqnguyen.syl.data.local.entity.LocationEntity
import com.hqnguyen.syl.databinding.FragmentDetailListHistoryBinding
import com.hqnguyen.syl.ui.map.MapViewModel
import com.hqnguyen.syl.ui.map.MapViewModelFactory
import com.mapbox.geojson.Point


class DetailListHistoryFragment :
    BaseFragment<FragmentDetailListHistoryBinding>(FragmentDetailListHistoryBinding::inflate) {

    private lateinit var mapVM: MapViewModel

    private val recordAdapter = RecordAdapter()

    private var currentTarget = 0.0

    override fun onViewCreated() {
        mapVM = ViewModelProvider(
            requireActivity(),
            MapViewModelFactory(context = requireContext())
        )[MapViewModel::class.java]

        initView()
        initEvent()
    }

    private fun initEvent() {
        binding.header.viewBack.setOnClickListener {
            navController.popBackStack()
        }
    }

    private fun initView() {
        binding.header.tvTitleScreen.text = "List record"
        binding.rv.apply {
            adapter = recordAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        val dividerItemDecoration = DividerItemDecoration(binding.rv.context, LinearLayoutManager(requireContext()).orientation)
        binding.rv.addItemDecoration(dividerItemDecoration)
        binding.processTarget.max = 50
    }

    private fun convertData(data: List<LocationEntity>): ArrayList<ListLocation> {
        var timeStart = ""
        val sameTimeList = arrayListOf<Point>()
        val newList = arrayListOf<ListLocation>()
        data.forEach {
            if (it.timeStart == timeStart) {
                sameTimeList.add(Point.fromLngLat(it.lng, it.lat))
            } else {
                timeStart = it.timeStart
                if (!timeStart.isNullOrEmpty()) {
                    newList.add(
                        ListLocation(
                            timeStart,
                            ArrayList(sameTimeList.map { point -> point }),
                            calculator(ArrayList(sameTimeList.map { point -> point }))
                        )
                    )
                    sameTimeList.clear()
                } else {
                    sameTimeList.add(Point.fromLngLat(it.lng, it.lat))
                }
            }
        }
        return newList
    }

    private fun calculator(list: ArrayList<Point>): Double {
        var totalKM = 0f
        var preItem: Point? = null
        list.forEach { point ->
            if (preItem == null) {
                preItem = point
            } else {
                val locationA = Location("").apply {
                    latitude = preItem!!.latitude()
                    longitude = preItem!!.longitude()
                }
                val locationB = Location("").apply {
                    latitude = point.latitude()
                    longitude = point.longitude()
                }
                totalKM += locationA.distanceTo(locationB)
                preItem = point
            }
        }
        return totalKM.toDouble()
    }


    @SuppressLint("SetTextI18n")
    override fun onObserverLiveData() {
        mapVM.listLocation.observe(viewLifecycleOwner) {
            it?.let {
                val data = convertData(it)
                recordAdapter.submitList(data)
                data.forEach { item ->
                    currentTarget += item.distance
                }
                binding.processTarget.progress = (currentTarget * 0.001).toInt()
                binding.tvCurrentKM.text = String.format("%.2f", (currentTarget * 0.001)) + "Km"
            }
        }
    }
}