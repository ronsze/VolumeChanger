package com.example.volumechanger

import android.annotation.SuppressLint
import android.app.Application
import android.database.sqlite.SQLiteDatabase

class App: Application() {
    companion object{
        private var isFirst = false
        val items = mutableListOf<ListViewItem>()
        lateinit var model: Model

        lateinit var dbHelper: DBHelper
        lateinit var database: SQLiteDatabase

        fun firstCheck(): Boolean {
            return isFirst
        }

        fun endHowTo(){
            isFirst = true
        }
    }

    override fun onCreate(){
        super.onCreate()
        model = Model()
        dbHelper = DBHelper(this, "newdb.db", null, 1)
        database = dbHelper.writableDatabase
    }
}