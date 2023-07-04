package com.hqnguyen.syl.ui.home

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.hqnguyen.syl.ui.map.MapFragment
import com.hqnguyen.syl.ui.menu.MenuFragment
import com.hqnguyen.syl.ui.record.list_record.ListRecordFragment

class ViewPagerHomeAdapter(frg: FragmentActivity) : FragmentStateAdapter(frg) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ListRecordFragment()
            1 -> MapFragment()
            2 -> MenuFragment()
            else -> ListRecordFragment()
        }
    }
}