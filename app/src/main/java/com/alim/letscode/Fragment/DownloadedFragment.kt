package com.alim.letscode.Fragment

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alim.letscode.Adapter.OfflineAdapter
import com.alim.letscode.Class.GlobalVariable
import com.alim.letscode.Database.ApplicationData
import com.alim.letscode.Database.OfflineData
import com.alim.letscode.Interface.ClickInterface
import com.alim.letscode.Interface.NavigationInterface
import com.alim.letscode.PlayerActivity
import com.alim.letscode.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File

class DownloadedFragment : Fragment() {

    var chipC = 0
    var chipCP = 0
    var chipJ = 0
    var chipP = 0
    var chipK = 0
    lateinit var delete: FloatingActionButton
    lateinit var cancel: FloatingActionButton
    var fName = "C"
    var theme = false
    private val MY_PERMISSIONS = 101
    lateinit var noFile: TextView
    lateinit var offlineData: OfflineData
    private lateinit var recyclerView: RecyclerView
    private lateinit var mAdapter: RecyclerView.Adapter<*>
    private var title: ArrayList<String> = ArrayList()
    private var link: ArrayList<String> = ArrayList()
    private lateinit var layoutManager: RecyclerView.LayoutManager

    val thread = Thread{
        recyclerView.stopScroll()
        title.clear()
        link.clear()
        val limit = when(fName) {
            "C_P" -> 149
            "J" -> 92
            "P" ->   110
            "K" -> 47
            else -> 176
        }

        for (x in 0..limit) {
            try {
                if (offlineData.getOffline(fName, x.toString())) {
                    val l = offlineData.getOfflineL(fName, x.toString())
                    link.add(l)
                    title.add(l.substring(l.lastIndexOf("/") + 1, l.length - 5))
                    GlobalVariable().adSelectedSingle(false)
                }
            } catch (e: Exception) {
                Log.println(Log.ASSERT,"Exception",e.toString())
    }
}
        try {
            activity!!.runOnUiThread {
                if (title.size==0)
                    noFile.visibility = View.VISIBLE
                else
                    noFile.visibility = View.GONE
                mAdapter.notifyDataSetChanged()
            }
        } catch(e: Exception) {
            Log.println(Log.ASSERT,"Exception",e.toString())}
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_downloaded, container, false)

        val chipGroup = rootView.findViewById<ChipGroup>(R.id.chip_offline)
        offlineData = OfflineData(activity!!)

        //val applicationData = ApplicationData(activity!!)
        //val name = rootView.findViewById<TextView>(R.id.name)
        //name.text = "OKK"//applicationData.name

        delete = rootView.findViewById<FloatingActionButton>(R.id.delete_fab)
        cancel = rootView.findViewById<FloatingActionButton>(R.id.cancel_fab)
        chipC = R.id.chip_c
        chipCP = R.id.chip_c_p
        chipJ = R.id.chip_j
        chipP = R.id.chip_p
        chipK = R.id.chip_k

        noFile = rootView.findViewById(R.id.no_text)

        if (GlobalVariable.choice) {
            delete.show()
            cancel.show()
        } else {
            delete.hide()
            cancel.hide()
        }

        cancel.setOnClickListener {
           cancelClick()
        }

        delete.setOnClickListener {
            delete.hide()
            cancel.hide()
            GlobalVariable.choice = false
            AlertDialog.Builder(activity!!)
                .setTitle("Delete")
                .setMessage("Are you sure want to delete selected videos")
                .setNegativeButton("Cancle") { dialog, _ ->
                    dialog.dismiss()
                    delete.show()
                    cancel.show()
                }
                .setPositiveButton("Yes") { _, _ ->
                    deleteLoop()
                    offlineData.setAll(fName, false)
                    Log.println(Log.ASSERT,"Downloaded Frag Adapter",offlineData.getAll(fName).toString())
                    Thread(thread).start()
                }
                .show()
        }
        rootView.findViewById<Chip>(chipC).isClickable = false
        chipGroup.setOnCheckedChangeListener { _, checkedId ->
            ChipManager(rootView, checkedId)
        }

        /*ActivityCompat.requestPermissions(activity!!,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), MY_PERMISSIONS)*/

        theme = when (ApplicationData(activity!!).theme) {
            0 -> {
                when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                    Configuration.UI_MODE_NIGHT_YES -> true
                    Configuration.UI_MODE_NIGHT_NO -> false
                    else -> false
                }
            }
            1 -> false
            2 -> true
            else -> false
        }

        recyclerView = rootView.findViewById(R.id.recycle_view)
        mAdapter = OfflineAdapter(theme, title, link, activity!!, object : ClickInterface {
            override fun Click(link: String, pos: Int, task: String) {
                when (task) {
                    "NOTIFY_DATA_SET_CHANGED" ->
                        mAdapter.notifyDataSetChanged()
                    "DELETE" -> {
                        offlineData.setAll(fName, false)
                        Log.println(Log.ASSERT,"Downloaded Frag Adapter",offlineData.getAll(fName).toString())
                        offlineData.setOffline(fName, (title[pos].substring(0,3).toInt()).toString(),"",false)
                        Thread(thread).start()
                    }
                    "LONG_CLICK" -> {
                        delete.show()
                        cancel.show()
                    }
                    else -> {
                        val intent = Intent(activity!!, PlayerActivity::class.java)
                        intent.putExtra("VIDEO_ID", link)
                        intent.putExtra("VIDEO_POS", pos)
                        intent.putExtra("DOWNLOADED", true)
                        intent.putExtra("MY_LOCATION", fName)
                        startActivity(intent)
                    }
                }
            }
        })

        recyclerView.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(GlobalVariable.context)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = mAdapter
        Thread(thread).start()

        GlobalVariable().registerNavigaionInterface3(object : NavigationInterface {
            override fun Position(pos: Int) {
                if (pos==2) {
                    Handler().postDelayed({
                        Thread(thread).start()
                    }, 500)
                }
            }
        })

        return rootView
    }

    private fun deleteLoop() {
        for (x in 0 until  GlobalVariable().getSelected().size) {
            if (GlobalVariable().getSelectedSingle(x)) {
                val file = File(link[x])
                if (file.exists())
                    file.delete()
                GlobalVariable().setSelectedSingle(x,false)
                offlineData.setOffline(fName, (title[x].substring(0,3).toInt()).toString(),"",false)
            }
        }
    }

    private fun cancelClick() {
        val limit = when(fName) {
            "C_P" -> 149
            "J" -> 92
            "P" ->   110
            "K" -> 47
            else -> 176
        }
        for (x in 0 until limit) {
            try {
                GlobalVariable().setSelectedSingle(x, false)
            } catch (e: Exception) {
                Log.println(Log.ASSERT,"Exception",e.toString())}
        }
        GlobalVariable.choice = false
        delete.hide()
        cancel.hide()
        mAdapter.notifyDataSetChanged()
    }

    private fun ChipManager(rootView: View,checkedId: Int) {
        rootView.findViewById<Chip>(chipC).isClickable = true
        rootView.findViewById<Chip>(chipCP).isClickable = true
        rootView.findViewById<Chip>(chipJ).isClickable = true
        rootView.findViewById<Chip>(chipP).isClickable = true
        rootView.findViewById<Chip>(chipK).isClickable = true
        when(checkedId) {
            chipC -> {
                fName = "C"
                Thread(thread).start()
                rootView.findViewById<Chip>(chipC).isClickable = false
            } chipCP -> {
            fName = "C_P"
            Thread(thread).start()
            rootView.findViewById<Chip>(chipCP).isClickable = false
        } chipJ -> {
            fName = "J"
            Thread(thread).start()
            rootView.findViewById<Chip>(chipJ).isClickable = false
        } chipP -> {
            fName = "P"
            Thread(thread).start()
            rootView.findViewById<Chip>(chipP).isClickable = false
        } chipK -> {
            fName = "K"
            Thread(thread).start()
            rootView.findViewById<Chip>(chipK).isClickable = false
        }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) Thread(thread).start()
                else Toast.makeText(activity!!,"Permission Denied",Toast.LENGTH_LONG).show()
                return
            }
        }
    }
}