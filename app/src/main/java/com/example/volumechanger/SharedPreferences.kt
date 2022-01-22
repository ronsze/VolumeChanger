package com.example.volumechanger

import android.content.Context
import android.content.SharedPreferences

class SharedPreferences(context: Context) {
    private val prefsFilename = "prefs"
    private val prefs: SharedPreferences = context.getSharedPreferences(prefsFilename, 0)
}