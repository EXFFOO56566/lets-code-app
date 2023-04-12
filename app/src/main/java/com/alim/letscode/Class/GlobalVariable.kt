package com.alim.letscode.Class

import android.content.Context
import android.util.Log
import com.alim.letscode.Interface.NavigationInterface
import com.alim.letscode.Interface.ProgressInterface

class GlobalVariable {

    companion object {
        var posi = 0
        var total = 0
        var task = "C"
        var progress = 0
        var bottomNav = 0
        var choice = false
        var downloadingPos = 0
        var downloading = false
        lateinit var context: Context
        lateinit var progressInterface : ProgressInterface
        lateinit var navigationInterface: NavigationInterface
        lateinit var navigationInterface2: NavigationInterface
        lateinit var navigationInterface3: NavigationInterface
        private var selected: ArrayList<Boolean> = ArrayList()
    }

    fun adSelectedSingle(mSelected: Boolean) {
        selected.add(mSelected)
    }

    fun setSelectedSingle(pos: Int, mSelected: Boolean) {
        selected[pos] = mSelected
    }

    fun getSelectedSingle(pos : Int): Boolean {
        return selected[pos]
    }

    fun setSelected(mSelected: ArrayList<Boolean>) {
        selected = mSelected
    }

    fun getSelected(): ArrayList<Boolean> {
        return selected
    }

    fun setPosition(pos: Int) { posi = pos }

    fun getPosition(): Int { return posi }

    fun setTotal(pos: Int) { total = pos }

    fun getTotal(): Int { return total }

    fun setTask(tas: String) { task = tas }

    fun getTask(): String { return task }

    fun setProgress(pos: Int) {
        progress = pos
        try {
            progressInterface.Progress(total, progress)
        } catch (e: Exception){
            Log.println(Log.ASSERT,"Error",e.toString())
        }
    }

    fun setDownloading(start: Boolean) {
        downloading = start
        try {
            progressInterface.Status(downloading, downloadingPos)
        } catch (e: Exception) {
            Log.println(Log.ASSERT,"Exception",e.toString())
        }
    }

    fun getDownloading(): Boolean {
        return downloading
    }

    fun setDownloadingPos(pos: Int) {
        downloadingPos = pos
        try {
            progressInterface.Status(downloading, pos)
        } catch (e: Exception) {
            Log.println(Log.ASSERT,"Exception",e.toString())}
    }

    fun setBottomNav(p: Int) {
        bottomNav = p
        try {
            when (p) {
                1 -> navigationInterface.Position(1)
                2 -> {
                    setDownloadingPos(downloadingPos)
                    navigationInterface3.Position(2)
                }
                3 -> navigationInterface2.Position(3)
            }
        } catch (e: Exception) {
            Log.println(Log.ASSERT,"Exception",e.toString())}
    }

    fun getDownloadingPos(): Int { return downloadingPos }

    fun registerInterface(inter: ProgressInterface) {
        progressInterface = inter
    }

    fun registerNavigaionInterface(inter: NavigationInterface) {
        navigationInterface = inter
    }

    fun registerNavigaionInterface2(inter: NavigationInterface) {
        navigationInterface2= inter
    }

    fun registerNavigaionInterface3(inter: NavigationInterface) {
        navigationInterface3= inter
    }
}