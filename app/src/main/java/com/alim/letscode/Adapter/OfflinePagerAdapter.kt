package com.alim.letscode.Adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.alim.letscode.Fragment.*

class OfflinePagerAdapter {
    class ViewPagerAdapter(fm: FragmentManager?) :
        FragmentPagerAdapter(fm!!) {
        override fun getItem(position: Int): Fragment {
            when (position) {
                0 -> return DownloadingFragment()
                1 -> return DownloadedFragment()
            }
            return DownloadingFragment()
        }
        override fun getCount(): Int {
            return 2
        }
    }
}