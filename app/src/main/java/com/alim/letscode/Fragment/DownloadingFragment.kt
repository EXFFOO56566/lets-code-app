package com.alim.letscode.Fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alim.letscode.Adapter.DownloadProgressAdapter
import com.alim.letscode.Class.GlobalVariable
import com.alim.letscode.Database.MainDB
import com.alim.letscode.Database.OfflineData
import com.alim.letscode.Interface.ProgressInterface
import com.alim.letscode.R
import com.bumptech.glide.Glide

@SuppressLint("SetTextI18n")
class DownloadingFragment : Fragment() {

    companion object {
        var task = ""
        var Name = ""
        var Thumb = ""
        var Position = 0
        var save = false
    }

    var first = true
    lateinit var title: TextView
    private lateinit var offlineData: OfflineData
    lateinit var prog: ProgressBar
    lateinit var textView: TextView
    lateinit var mainDB: MainDB
    lateinit var thumbnail: ImageView
    var globalVariable = GlobalVariable()
    lateinit var linearlayout: LinearLayout

    private var titleS: MutableList<String> = ArrayList()
    private var thumbS: MutableList<String> = ArrayList()

    private lateinit var adapter: DownloadProgressAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager

    val thread = Thread {
        titleS.clear()
        thumbS.clear()
        val loop = when (task) {
            "C" -> 176
            "C_P" -> 149
            "J" -> 92
            "P" -> 110
            "K" -> 47
            else -> 0 }
        for (x in Position+1 until loop) {
            if (!offlineData.getOffline(task, x.toString())) {
                titleS.add("${x + 1}. " + mainDB.getTitle(task, x))
                thumbS.add(mainDB.getThumb(task, x))
            }
        }
        activity!!.runOnUiThread { adapter.notifyDataSetChanged() }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_downloading, container, false)

        mainDB = MainDB(activity!!)
        offlineData = OfflineData(activity!!)
        prog = rootView.findViewById(R.id.prog)
        title = rootView.findViewById(R.id.title)
        textView = rootView.findViewById(R.id.no_text)
        thumbnail = rootView.findViewById(R.id.thumbnail)
        linearlayout = rootView.findViewById(R.id.downloading)
        recyclerView = rootView.findViewById(R.id.recycle_view)
        recyclerView.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(activity)

        task = globalVariable.getTask()

        if (Name.isEmpty()) { setName() }
        title.text = "${Position+1}. $Name"
        Glide.with(activity!!).load(Thumb).centerCrop().into(thumbnail)


        if (Position> 0) {
            textView.visibility = View.GONE
            linearlayout.visibility = View.VISIBLE
        }

        /*textView.visibility = View.GONE
        linearlayout.visibility = View.VISIBLE*/

        globalVariable.registerInterface(object : ProgressInterface {
            override fun Status(running: Boolean, pos: Int) {
                if (running) {
                    save = true
                    task = globalVariable.getTask()
                    Name = mainDB.getTitle(task, pos)
                    Position = pos
                    prog.progress = 0
                    title.text = "${pos+1}. $Name"
                    Glide.with(activity!!).load(mainDB.getThumb(task, pos)).centerCrop().into(thumbnail)
                    Thread(thread).start()
                } else {
                    if (save)
                        OfflineData(GlobalVariable.context).setAll(task, true)
                    textView.visibility = View.VISIBLE
                    linearlayout.visibility = View.GONE
                }
                Log.println(Log.ASSERT,"End Downloading",running.toString())
            }

            override fun Progress(total: Int, current: Int) {
                prog.max = total
                prog.progress = current
                if (first) {
                    first = false
                    task = globalVariable.getTask()
                    textView.visibility = View.GONE
                    linearlayout.visibility = View.VISIBLE
                    setName()
                }
            }
        })

        recyclerView.layoutManager = layoutManager
        adapter = DownloadProgressAdapter(titleS, thumbS, activity!!)
        recyclerView.adapter = adapter

        //Thread(thread).start()

        return rootView
    }

    fun setName() {
        try {
            Log.println(Log.ASSERT,"Task",task)
            Position = globalVariable.getDownloadingPos()
            Name = mainDB.getTitle(task, Position)
            Thumb = mainDB.getThumb(task, Position)
            title.text = "${Position+1}. $Name"
            Glide.with(activity!!).load(mainDB.getThumb(task, Position)).centerCrop().into(thumbnail)
            Thread(thread).start()
        } catch (e: Exception) {
            Log.println(Log.ASSERT,"Exception", e.toString())
        }
    }
}
