package com.hqnguyen.syl.ui.menu.list_record

import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.hqnguyen.syl.base.BaseFragment
import com.hqnguyen.syl.databinding.FragmentDetailListHistoryBinding
import com.hqnguyen.syl.ui.map.MapViewModel
import com.hqnguyen.syl.ui.map.MapViewModelFactory
import timber.log.Timber

class DetailListHistoryFragment : BaseFragment<FragmentDetailListHistoryBinding>(FragmentDetailListHistoryBinding::inflate) {

    private lateinit var mapVM: MapViewModel

    private val recordAdapter = RecordAdapter()

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
    }

    override fun onObserverLiveData() {
        mapVM.listLocation.observe(viewLifecycleOwner) {
            Timber.d("listLocation ${it.size}")
            recordAdapter.submitList(it)
        }
    }
}