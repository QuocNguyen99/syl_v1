package com.hqnguyen.syl.ui.home

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.hqnguyen.syl.ui.map.MapFragment
import com.hqnguyen.syl.ui.menu.MenuFragment

class ViewPagerHomeAdapter(frg: FragmentActivity) : FragmentStateAdapter(frg) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> MapFragment()
            1 -> MenuFragment()
            else -> MapFragment()
        }
    }
}