package com.hqnguyen.syl.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.viewpager2.widget.ViewPager2
import com.hqnguyen.syl.R
import com.hqnguyen.syl.base.BaseFragment
import com.hqnguyen.syl.databinding.FragmentHomeBinding
import com.hqnguyen.syl.service.LocationService

class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {
    override fun onViewCreated() {
        initView()
        initEvent()
    }

    private fun initView() {
        binding.viewpager.adapter = ViewPagerHomeAdapter(requireActivity())
        binding.viewpager.isUserInputEnabled = false
    }

    private fun initEvent() {
        with(binding) {
            viewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    bottomNavigationView.menu.getItem(position).isChecked = true
                }
            })

            bottomNavigationView.setOnItemSelectedListener {
                when (it.itemId) {
                    R.id.location -> {
                        viewpager.setCurrentItem(0, true)
                        return@setOnItemSelectedListener false
                    }
                    R.id.menu -> {
                        viewpager.setCurrentItem(1, true)
                        return@setOnItemSelectedListener false
                    }
                    else -> {
                        viewpager.setCurrentItem(0, true)
                        return@setOnItemSelectedListener true
                    }
                }
            }
        }
    }
}