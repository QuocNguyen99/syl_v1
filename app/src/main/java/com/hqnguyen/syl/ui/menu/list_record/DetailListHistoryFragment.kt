package com.hqnguyen.syl.ui.menu.list_record

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.hqnguyen.syl.base.BaseFragment
import com.hqnguyen.syl.databinding.FragmentDetailListHistoryBinding
import com.hqnguyen.syl.ui.map.MapViewModel
import com.hqnguyen.syl.ui.map.MapViewModelFactory


class DetailListHistoryFragment : BaseFragment<FragmentDetailListHistoryBinding>(FragmentDetailListHistoryBinding::inflate) {

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
        mapVM.listLocation.observe(viewLifecycleOwner) {
            recordAdapter.submitList(it)
            it.forEach { item ->
                currentTarget += item.distance
            }
            binding.processTarget.progress = (currentTarget * 0.001).toInt()
            binding.tvCurrentKM.text = String.format("%.2f", (currentTarget * 0.001)) + "Km"
        }
    }
}