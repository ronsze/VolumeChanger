package com.example.volumechanger

import android.app.Application
import android.database.sqlite.SQLiteDatabase

class App: Application() {
    companion object{
        lateinit var dbHelper: DBHelper
        lateinit var database: SQLiteDatabase
    }

    override fun onCreate(){
        dbHelper = DBHelper(this, "newdb.db", null, 1)
        database = dbHelper.writableDatabase
        super.onCreate()
    }
}