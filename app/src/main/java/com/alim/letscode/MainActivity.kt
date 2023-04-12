package com.alim.letscode

import android.app.AlertDialog
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.alim.letscode.Adapter.PagerAdapter
import com.alim.letscode.Class.GlobalVariable
import com.alim.letscode.Class.NetWork
import com.alim.letscode.Database.ApplicationData
import com.fxn.BubbleTabBar
import com.fxn.OnBubbleClickListener

class MainActivity : AppCompatActivity() {

    private val globalVariable = GlobalVariable()
    private lateinit var viewPager: ViewPager
    private lateinit var bubbleTabBar: BubbleTabBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val applicationData = ApplicationData(this)
        when (applicationData.theme) {
            0 -> {
                when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                    Configuration.UI_MODE_NIGHT_YES -> setTheme(R.style.AppThemeDark)
                    Configuration.UI_MODE_NIGHT_NO -> setTheme(R.style.AppTheme)
                }
            }
            1 -> setTheme(R.style.AppTheme)
            2 -> setTheme(R.style.AppThemeDark)
        }
        setContentView(R.layout.activity_main)

        viewPager = findViewById(R.id.view_pager)
        bubbleTabBar = findViewById(R.id.navigation_view)
        viewPager.adapter = PagerAdapter.ViewPagerAdapter(supportFragmentManager)

        GlobalVariable.context = this

        bubbleTabBar.setupBubbleTabBar(viewPager)

        viewPager.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(
                position: Int, positionOffset: Float,
                positionOffsetPixels: Int) {
                Log.println(Log.ASSERT,"View Pager", "onPageScrolled")
            }

            override fun onPageSelected(position: Int) {
                globalVariable.setBottomNav(position)
            }

            override fun onPageScrollStateChanged(state: Int) {
                Log.println(Log.ASSERT,"View Pager", "onPageScrollStateChanged")
            }
        })


        bubbleTabBar.addBubbleListener(object : OnBubbleClickListener{
            override fun onBubbleClick(id: Int) {
                var pos = 0
                when (id) {
                    R.id.home -> { pos = 0 }
                    R.id.statistics -> { pos = 1 }
                    R.id.downloads -> { pos = 2 }
                    R.id.settings -> { pos = 3 }
                }
                viewPager.currentItem = pos
            }
        })

        if (intent.getStringExtra("FROM") == "SETTINGS")
            viewPager.currentItem = 3
        else if (!NetWork().isConnected(this))
            viewPager.currentItem = 2
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setTitle("Exit")
            .setMessage("Are you sure want to exit ?")
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .setPositiveButton("Yes") { _, _ -> super.onBackPressed() }
            .show()
    }
}
