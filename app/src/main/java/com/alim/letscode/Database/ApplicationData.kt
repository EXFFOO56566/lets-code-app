package com.alim.letscode.Database

import android.content.Context

class ApplicationData(private val context: Context) {

    private val DATA_NAME = "APP_DATA"

    var session: Boolean
        get() {
            val prefs = context.getSharedPreferences(DATA_NAME, 0)
            return prefs.getBoolean(SESSION, false)
        }
        set(value) {
            val sharedPref = context.getSharedPreferences(DATA_NAME, 0)
            val editor = sharedPref.edit()
            editor.putBoolean(SESSION, value)
            editor.apply()
        }

    var image: String
        get() {
            val prefs = context.getSharedPreferences(DATA_NAME, 0)
            return prefs.getString(IMAGE, "").toString()
        }
        set(value) {
            val sharedPref = context.getSharedPreferences(DATA_NAME, 0)
            val editor = sharedPref.edit()
            editor.putString(IMAGE, value)
            editor.apply()
        }

    var name: String
        get() {
            val prefs = context.getSharedPreferences(DATA_NAME, 0)
            return prefs.getString(NAME, "").toString()
        }
        set(value) {
            val sharedPref = context.getSharedPreferences(DATA_NAME, 0)
            val editor = sharedPref.edit()
            editor.putString(NAME, value)
            editor.apply()
        }

    var theme: Int
        get() {
            val prefs = context.getSharedPreferences(DATA_NAME, 0)
            return prefs.getInt(THEME, 0)
        }
        set(value) {
            val sharedPref = context.getSharedPreferences(DATA_NAME, 0)
            val editor = sharedPref.edit()
            editor.putInt(THEME, value)
            editor.apply()
        }


    companion object {
        private const val NAME = "NAME"
        private const val THEME = "THEME"
        private const val IMAGE = "IMAGE"
        private const val SESSION = "SESSION"
    }
}