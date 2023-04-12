package com.alim.letscode.Database

import android.content.Context

class OfflineData(private val context: Context) {

    private val DATA_NAME = "OFFLINE_DATA"

    fun getOffline(fro : String, pos: String): Boolean {
        val prefs = context.getSharedPreferences(DATA_NAME, 0)
        return prefs.getBoolean(OFFLINE+"_"+fro+"_"+pos, false)
    }

    fun getOfflineL(fro : String, pos: String): String {
        val prefs = context.getSharedPreferences(DATA_NAME, 0)
        return prefs.getString(OFFLINE+"_L_"+fro+"_"+pos, "").toString()
    }

    fun setOffline(fro : String, pos: String, loc: String, value: Boolean) {
        val sharedPref = context.getSharedPreferences(DATA_NAME, 0)
        val editor = sharedPref.edit()
        editor.putBoolean(OFFLINE+"_"+fro+"_"+pos,  value)
        editor.putString(OFFLINE+"_L_"+fro+"_"+pos,  loc)
        editor.apply()
    }

    fun setAll(lan: String, ans: Boolean) {
        val sharedPref = context.getSharedPreferences(DATA_NAME, 0)
        val editor = sharedPref.edit()
        editor.putBoolean(DOWNLOADED+"_"+lan, ans)
        editor.apply()
    }

    fun getAll(lan: String): Boolean {
        val prefs = context.getSharedPreferences(DATA_NAME, 0)
        return prefs.getBoolean(DOWNLOADED+"_"+lan, false)
    }

    companion object {
        private const val OFFLINE = "OFFLINE"
        private const val DOWNLOADED = "DOWNLOADED"
    }
}