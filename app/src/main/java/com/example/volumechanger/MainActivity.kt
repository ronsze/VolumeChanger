package com.example.volumechanger

import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.volumechanger.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    lateinit var dbHelper: DBHelper
    lateinit var database: SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        dbHelper = DBHelper(this, "newdb.db", null, 1)
        database = dbHelper.writableDatabase

        getPermission()

        val items: MutableList<ListViewItem> = initList()

        val intent = Intent(this, MapActivity::class.java)

        binding.locList.setOnItemClickListener { parent, view, position, id ->
            intent.putExtra("select", "item")
            intent.putExtra("point", items[position].point)
            startActivity(intent)
        }

        binding.mapBtn.setOnClickListener {
            intent.putExtra("select", "button")
            startActivity(intent)
        }
    }

    private fun initList(): MutableList<ListViewItem>{
        val items = mutableListOf<ListViewItem>()
        var query = "SELECT name, point FROM lists;"
        var cursor = database.rawQuery(query, null)
        while(cursor.moveToNext()){
            items.add(ListViewItem(cursor.getString(0), cursor.getString(1), cursor.getString(1)))
        }
        val adapter = LocListAdapater(items)
        binding.locList.adapter = adapter

        return items
    }

    private fun getPermission(){
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_NOTIFICATION_POLICY) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_NOTIFICATION_POLICY), 100)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode === 100){
            if(grantResults.size > 0){
                for(grant in grantResults){
                    if(grant != PackageManager.PERMISSION_GRANTED) System.exit(0)
                }
            }
        }
    }

    override fun onResume() {
        initList()
        super.onResume()
    }
}