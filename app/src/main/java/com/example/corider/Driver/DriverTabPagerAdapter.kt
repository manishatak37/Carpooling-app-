package com.example.corider.Driver


import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.corider.Fragment.DriverScheduledFragment
import com.example.corider.Fragment.DriverCompletedFragment
import com.example.corider.Fragment.DriverCancelledFragment


class DriverTabPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 3 // Number of tabs

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> DriverScheduledFragment()
            1 -> DriverCompletedFragment()
            2 -> DriverCancelledFragment()
            else -> DriverScheduledFragment()
        }
    }
}
