package com.example.volumechanger

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.NotificationManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.example.volumechanger.databinding.ActivityMainBinding
import androidx.databinding.DataBindingUtil

@SuppressLint("Range")
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    lateinit var dbHelper: DBHelper
    lateinit var database: SQLiteDatabase
    lateinit var items: MutableList<ListViewItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.lifecycleOwner = this

        dbHelper = DBHelper(this, "newdb.db", null, 1)
        database = dbHelper.writableDatabase

        getPermission()

        items = updateList()

        val mapIntent = Intent(this, MapActivity::class.java)

        binding.locList.setOnItemClickListener { parent, view, position, id ->
            mapIntent.putExtra("select", "item")
            mapIntent.putExtra("location", items[position].location)
            startActivity(mapIntent)
        }

        binding.addBtn.setOnClickListener {
            mapIntent.putExtra("select", "button")
            startActivity(mapIntent)
        }
    }
//ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡPermissionㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
    private fun getPermission(){
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_BACKGROUND_LOCATION), 100)
        }

        val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if(Build.VERSION.SDK_INT >= 23){
            if(!notificationManager.isNotificationPolicyAccessGranted){
                val builder = AlertDialog.Builder(this)
                    .setTitle("권한 요청")
                    .setMessage("볼륨 조절을 위해\n방해 금지 권한이\n필요합니다.")
                    .setPositiveButton("확인",
                        DialogInterface.OnClickListener{ dialog, which ->
                            this.startActivity(Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
                        })
                    .setNegativeButton("취소",
                        DialogInterface.OnClickListener{ dialog, which ->
                            System.exit(0)
                        })
                    .show()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 100){
            if(grantResults.size > 0){
                for(grant in grantResults){
                    if(grant != PackageManager.PERMISSION_GRANTED){
                        System.exit(0)
                    }
                }
            }
        }
    }

    fun updateList(): MutableList<ListViewItem>{
        val items = mutableListOf<ListViewItem>()
        val query = "SELECT name, location FROM lists;"
        val cursor = database.rawQuery(query, null)

        while(cursor.moveToNext()){
            val name = cursor.getString(cursor.getColumnIndex("name"))
            items.add(ListViewItem(name, cursor.getString(cursor.getColumnIndex("location"))))
        }

        val adapter = LocListAdapater(items)
        binding.locList.adapter = adapter

        return items
    }

    override fun onResume() {
        items = updateList()
        super.onResume()
    }
}