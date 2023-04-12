package com.alim.letscode.Adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.alim.letscode.Fragment.*

class PagerAdapter {
    class ViewPagerAdapter(fm: FragmentManager?) :
        FragmentPagerAdapter(fm!!) {
        override fun getItem(position: Int): Fragment {
            when (position) {
                0 -> return HomeFragment()
                1 -> return StatisticsFragment()
                2 -> return DownloadsFragment()
                3 -> return SettingsFragment()
            }
            return HomeFragment()
        }
        override fun getCount(): Int {
            return 4
        }
    }
}