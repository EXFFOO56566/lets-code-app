package com.alim.letscode.Fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.viewpager.widget.ViewPager
import com.alim.letscode.Adapter.OfflinePagerAdapter
import com.alim.letscode.Database.ApplicationData
import com.alim.letscode.R
import com.google.android.material.tabs.TabLayout

class DownloadsFragment : Fragment() {

    private lateinit var textName: TextView
    private lateinit var viewPager: ViewPager
    private lateinit var tabLayout: TabLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_downloads, container, false)

        tabLayout = rootView.findViewById(R.id.tabs)
        viewPager = rootView.findViewById(R.id.view_pager)
        viewPager.adapter = OfflinePagerAdapter.ViewPagerAdapter(childFragmentManager)

        textName = rootView.findViewById(R.id.name)
        textName.text = ApplicationData(activity!!).name

        tabLayout.addTab(tabLayout.newTab().setText("Downloading"))
        tabLayout.addTab(tabLayout.newTab().setText("Downloaded"))
        tabLayout.tabGravity = TabLayout.GRAVITY_FILL

        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager.currentItem = tab.position
            }
            override fun onTabReselected(tab: TabLayout.Tab) {
                Log.println(Log.ASSERT,"Tab Reselected","Reselected")
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                Log.println(Log.ASSERT,"Tab Unselected","Unselected")
            }
        })

        return rootView
    }

}
