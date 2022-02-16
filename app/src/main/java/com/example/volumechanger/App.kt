package com.example.volumechanger

import android.annotation.SuppressLint
import android.app.Application
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

class App: Application() {
    companion object{
        lateinit var dbHelper: DBHelper
        lateinit var database: SQLiteDatabase
        val items = mutableListOf<ListViewItem>()
    }

    override fun onCreate(){
        dbHelper = DBHelper(this, "newdb.db", null, 1)
        database = dbHelper.writableDatabase
        super.onCreate()
    }
}