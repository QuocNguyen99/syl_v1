package com.hqnguyen.syl.ui.menu

import android.annotation.SuppressLint
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.hqnguyen.syl.R
import com.hqnguyen.syl.base.BaseFragment
import com.hqnguyen.syl.utils.convertToAvatar
import com.hqnguyen.syl.databinding.FragmentMenuBinding
import com.hqnguyen.syl.ui.dialog.DialogUpdateImageFragment
import com.hqnguyen.syl.ui.login.UserViewModel
import com.hqnguyen.syl.ui.map.MapViewModel
import com.hqnguyen.syl.ui.map.MapViewModelFactory

class MenuFragment : BaseFragment<FragmentMenuBinding>(FragmentMenuBinding::inflate) {
    private val userVM: UserViewModel by activityViewModels()
    private lateinit var mapVM: MapViewModel

    override fun onViewCreated() {
        mapVM = ViewModelProvider(
            requireActivity(),
            MapViewModelFactory(context = requireContext())
        )[MapViewModel::class.java]

        initView()
        initEvent()
        initData()
    }

    private fun initView() {
        binding.header.viewBack.isVisible = false
    }

    private fun initEvent() {
        binding.cardAvatar.setOnClickListener {
            val dialog = DialogUpdateImageFragment()
            dialog.show(childFragmentManager, "")
        }

        binding.cardHistory.setOnClickListener {
            navigation(R.id.action_homeFragment_to_detailListHistoryFragment)
        }
    }

    private fun initData() {
        mapVM.getListLocationFormLocal()
    }

    @SuppressLint("SetTextI18n")
    override fun onObserverLiveData() {
        userVM.uriAvatarUser.observe(viewLifecycleOwner) {
            it?.let { uri ->
                val bitmap = uri.convertToAvatar(requireContext())
                binding.imgAvatar.setImageBitmap(bitmap)
            }
        }

        mapVM.listLocation.observe(viewLifecycleOwner) {
            binding.tvTotal.text = "Total: " + it.size.toString()
        }
    }
}