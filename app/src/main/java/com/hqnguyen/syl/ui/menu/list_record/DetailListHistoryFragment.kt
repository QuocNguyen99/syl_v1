package com.hqnguyen.syl.ui.menu.list_record

import android.annotation.SuppressLint
import android.location.Location
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.hqnguyen.syl.base.BaseFragment
import com.hqnguyen.syl.data.ListLocation
import com.hqnguyen.syl.data.local.entity.LocationEntity
import com.hqnguyen.syl.databinding.FragmentDetailListHistoryBinding
import com.hqnguyen.syl.ui.map.MapViewModel
import com.hqnguyen.syl.ui.map.MapViewModelFactory
import com.hqnguyen.syl.utils.DataHelper
import com.mapbox.geojson.Point
import kotlinx.coroutines.launch


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

    @SuppressLint("SetTextI18n")
    override fun onObserverLiveData() {
        lifecycleScope.launch {
            mapVM.getListLocationFormLocal().observe(viewLifecycleOwner) {
                it?.let {
                    val data = DataHelper.getInstance().convertData(it)
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
}