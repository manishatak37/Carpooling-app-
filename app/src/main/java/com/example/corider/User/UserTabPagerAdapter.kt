package com.example.corider.User


import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.corider.Fragment.UserScheduledFragment
import com.example.corider.Fragment.UserCompletedFragment
import com.example.corider.Fragment.UserCancelledFragment


class UserTabPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 3 // Number of tabs

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> UserScheduledFragment()
            1 -> UserCompletedFragment()
            2 -> UserCancelledFragment()
            else -> UserScheduledFragment()
        }
    }
}

