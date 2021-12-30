package com.example.volumechanger

import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.example.volumechanger.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    lateinit var dbHelper: DBHelper
    lateinit var database: SQLiteDatabase
    var permissions = arrayOf(android.Manifest.permission.ACCESS_NOTIFICATION_POLICY, android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)

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
        val query = "SELECT name, point FROM lists;"
        val cursor = database.rawQuery(query, null)
        while(cursor.moveToNext()){
            items.add(ListViewItem(cursor.getString(0), cursor.getString(1), cursor.getString(1)))
        }
        val adapter = LocListAdapater(items)
        binding.locList.adapter = adapter

        return items
    }

    private fun getPermission(){
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_NOTIFICATION_POLICY) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, permissions, 100)
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