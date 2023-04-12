package com.alim.letscode.Database

import android.content.Context

class LearningData(private val context: Context) {

    private val DATA_NAME = "C_CODE_DATA"

    fun setSession(pos: String, num:Int) {
        val sharedPref = context.getSharedPreferences(DATA_NAME, 0)
        val editor = sharedPref.edit()
        editor.putInt(SESSION+"_"+pos,  num)
        editor.apply()
    }

    fun getSession(pos: String): Int {
        val prefs = context.getSharedPreferences(DATA_NAME, 0)
        return prefs.getInt(SESSION+"_"+pos, -1)
    }

    fun setPosition(lan: String, pos: String) {
        val sharedPref = context.getSharedPreferences(DATA_NAME, 0)
        val editor = sharedPref.edit()
        editor.putBoolean(POSITION+"_"+lan+"_"+pos,  true)
        editor.apply()
    }

    fun getPosition(lan: String, pos: String): Boolean {
        val prefs = context.getSharedPreferences(DATA_NAME, 0)
        return prefs.getBoolean(POSITION+"_"+lan+"_"+pos, false)
    }

    fun setWatchTime(pos: String, num: Int) {
        val sharedPref = context.getSharedPreferences(DATA_NAME, 0)
        val editor = sharedPref.edit()
        editor.putInt(WATCH+"_"+pos,  num)
        editor.apply()
    }

    fun getWatchTime(pos: String): Int {
        val prefs = context.getSharedPreferences(DATA_NAME, 0)
        return prefs.getInt(WATCH+"_"+pos, 0)
    }

    companion object {
        private const val SESSION = "SESSION"
        private const val NAME = "NAME"
        private const val WATCH = "WATCH"
        private const val POSITION = "POSITION"
    }

}