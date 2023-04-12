package com.alim.letscode.Database

import android.content.Context

class MainDB(private val context: Context) {

    fun getTitle(lan: String, pos: Int): String {
        val prefs = context.getSharedPreferences(DATA_NAME_TITLE+lan, 0)
        return prefs.getString("$pos", "").toString()
    }

    fun setTitle(lan: String, pos: Int, value: String) {
        val sharedPref = context.getSharedPreferences(DATA_NAME_TITLE+lan, 0)
        val editor = sharedPref.edit()
        editor.putString("$pos",  value)
        editor.apply()
    }

    fun getLink(lan: String, pos: Int): String {
        val prefs = context.getSharedPreferences(DATA_NAME_LINK+lan, 0)
        return prefs.getString("$pos", "").toString()
    }

    fun setLink(lan: String, pos: Int, value: String) {
        val sharedPref = context.getSharedPreferences(DATA_NAME_LINK+lan, 0)
        val editor = sharedPref.edit()
        editor.putString("$pos",  value)
        editor.apply()
    }

    fun getThumb(lan: String, pos: Int): String {
        val prefs = context.getSharedPreferences(DATA_NAME_THUMB+lan, 0)
        return prefs.getString("$pos", "").toString()
    }

    fun setThumb(lan: String, pos: Int, value: String) {
        val sharedPref = context.getSharedPreferences(DATA_NAME_THUMB+lan, 0)
        val editor = sharedPref.edit()
        editor.putString("$pos",  value)
        editor.apply()
    }

    companion object {
        private const val DATA_NAME_LINK = "MAIN_DATA_LINK_"
        private const val DATA_NAME_TITLE = "MAIN_DATA_TITLE_"
        private const val DATA_NAME_THUMB = "MAIN_DATA_THUMB_"
    }
}