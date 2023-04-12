package com.alim.letscode.Fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.alim.letscode.Class.GlobalVariable
import com.alim.letscode.Database.*
import com.alim.letscode.Interface.NavigationInterface
import com.alim.letscode.R
import java.lang.Exception

class StatisticsFragment : Fragment() {

    private lateinit var cText: TextView
    private lateinit var cWText: TextView
    private lateinit var cRound: ProgressBar
    private lateinit var cPercentage: TextView

    private lateinit var cpText: TextView
    private lateinit var cpWText: TextView
    private lateinit var cpRound: ProgressBar
    private lateinit var cpPercentage: TextView

    private lateinit var jText: TextView
    private lateinit var jWText: TextView
    private lateinit var jRound: ProgressBar
    private lateinit var jPercentage: TextView

    private lateinit var pText: TextView
    private lateinit var pWText: TextView
    private lateinit var pRound: ProgressBar
    private lateinit var pPercentage: TextView

    private lateinit var kText: TextView
    private lateinit var kWText: TextView
    private lateinit var kRound: ProgressBar
    private lateinit var kPercentage: TextView

    private lateinit var cProg: ProgressBar
    private lateinit var jProg: ProgressBar
    private lateinit var pProg: ProgressBar
    private lateinit var kProg: ProgressBar
    private lateinit var cpProg: ProgressBar

    private var globalVariable = GlobalVariable()
    private lateinit var learningData: LearningData

    @SuppressLint("SetTextI18n")
    private val overAll = Thread{
        var c = 0
        var cp = 0
        var j = 0
        var p = 0
        var k = 0
        for (x in 0..176) {
            try {
                val pos = x.toString()
                if (learningData.getPosition("C",pos))
                    c++
                if (learningData.getPosition("C_P",pos))
                    cp++
                if (learningData.getPosition("J",pos))
                    j++
                if (learningData.getPosition("P",pos))
                    p++
                if (learningData.getPosition("K",pos))
                    k++
            } catch (e: Exception) {
                Log.println(Log.ASSERT,"Exception",e.toString())
            }
        }
        var cTimeText = "Sec"
        var cTime = (learningData.getWatchTime("C")/1000)

        var cpTimeText = "Sec"
        var cpTime = (learningData.getWatchTime("C_P")/1000)

        var jTimeText = "Sec"
        var jTime = (learningData.getWatchTime("J")/1000)

        var pTimeText = "Sec"
        var pTime = (learningData.getWatchTime("P")/1000)

        var kTimeText = "Sec"
        var kTime = (learningData.getWatchTime("K")/1000)

        if (cTime>59) {
            cTimeText = "Min"
            cTime /= 60
            if (cTime>59) {
                cTimeText = "Hour"
                cTime /= 60
            }
        }

        if (cpTime>59) {
            cpTimeText = "Min"
            cpTime /= 60
            if (cpTime>59) {
                cpTimeText = "Hour"
                cpTime /= 60
            }
        }

        if (jTime>59) {
            jTimeText = "Min"
            jTime /= 60
            if (jTime>59) {
                jTimeText = "Hour"
                jTime /= 60
            }
        }

        if (pTime>59) {
            pTimeText = "Min"
            pTime /= 60
            if (pTime>59) {
                pTimeText = "Hour"
                pTime /= 60
            }
        }

        if (kTime>59) {
            kTimeText = "Min"
            kTime /= 60
            if (kTime>59) {
                kTimeText = "Hour"
                kTime /= 60
            }
        }

        activity!!.runOnUiThread {
            cProg.progress = c
            cRound.progress = c
            cPercentage.text = "${(c*100)/176}%"
            cText.text = "Watched Video's : $c"
            cWText.text = "Time Watched : $cTime $cTimeText"

            cpProg.progress = cp
            cpRound.progress = cp
            cpPercentage.text = "${(cp*100)/149}%"
            cpText.text = "Watched Video's : $cp"
            cpWText.text = "Time Watched : $cpTime $cpTimeText"

            jProg.progress = j
            jRound.progress = j
            jPercentage.text = "${(j*100)/92}%"
            jText.text = "Watched Video's : $j"
            jWText.text = "Time Watched : $jTime $jTimeText"

            pProg.progress = p
            pRound.progress = p
            pPercentage.text = "${(p*100)/110}%"
            pText.text = "Watched Video's : $p"
            pWText.text = "Time Watched : $pTime $pTimeText"

            kProg.progress = k
            kRound.progress = k
            kPercentage.text = "${(k*100)/47}%"
            kText.text = "Watched Video's : $k"
            kWText.text = "Time Watched : $kTime $kTimeText"
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_statistics, container, false)

        learningData = LearningData(activity!!)

        cText = rootView.findViewById(R.id.c_watch)
        cWText = rootView.findViewById(R.id.c_watch_time)
        cRound = rootView.findViewById(R.id.c_round)
        cPercentage = rootView.findViewById(R.id.c_percentage)

        cpText = rootView.findViewById(R.id.cp_watch)
        cpWText = rootView.findViewById(R.id.cp_watch_time)
        cpRound = rootView.findViewById(R.id.cp_round)
        cpPercentage = rootView.findViewById(R.id.cp_percentage)

        jText = rootView.findViewById(R.id.j_watch)
        jWText = rootView.findViewById(R.id.j_watch_time)
        jRound = rootView.findViewById(R.id.j_round)
        jPercentage = rootView.findViewById(R.id.j_percentage)

        pText = rootView.findViewById(R.id.p_watch)
        pWText = rootView.findViewById(R.id.p_watch_time)
        pRound = rootView.findViewById(R.id.p_round)
        pPercentage = rootView.findViewById(R.id.p_percentage)

        kText = rootView.findViewById(R.id.k_watch)
        kWText = rootView.findViewById(R.id.k_watch_time)
        kRound = rootView.findViewById(R.id.k_round)
        kPercentage = rootView.findViewById(R.id.k_percentage)

        cProg = rootView.findViewById(R.id.c_prog)
        cpProg = rootView.findViewById(R.id.cp_prog)
        jProg = rootView.findViewById(R.id.j_prog)
        pProg = rootView.findViewById(R.id.p_prog)
        kProg = rootView.findViewById(R.id.k_prog)

        val applicationData = ApplicationData(activity!!)
        val name = rootView.findViewById<TextView>(R.id.name)
        name.text = applicationData.name

        globalVariable.registerNavigaionInterface(object : NavigationInterface{
            override fun Position(pos: Int) {
                Handler().postDelayed({
                    Thread(overAll).start()
                },500)
            }
        })

        return rootView
    }
}