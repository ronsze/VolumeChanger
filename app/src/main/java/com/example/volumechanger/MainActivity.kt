package com.example.volumechanger

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.volumechanger.databinding.ActivityMainBinding
import com.naver.maps.geometry.LatLng
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    lateinit var dbHelper: DBHelper
    lateinit var database: SQLiteDatabase
    lateinit var items: MutableList<ListViewItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        dbHelper = DBHelper(this, "newdb.db", null, 1)
        database = dbHelper.writableDatabase

        getPermission()

        items = initList()

        val intent = Intent(this, MapActivity::class.java)

        binding.locList.setOnItemClickListener { parent, view, position, id ->
            Log.e("리스트","${items}")
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
            val name = cursor.getString(cursor.getColumnIndex("name"))
            val latLng = cursor.getString(cursor.getColumnIndex("point")).split(",")
            val lat = latLng[0].toDouble()
            val lng = latLng[1].toDouble()
            val address = getAddress(LatLng(lat, lng)).substring(4)
            items.add(ListViewItem(name, cursor.getString(cursor.getColumnIndex("point")), address))
        }
        val adapter = LocListAdapater(items)
        binding.locList.adapter = adapter

        return items
    }

    private fun getAddress(latLng: LatLng): String{
        var mGeoCoder = Geocoder(applicationContext, Locale.KOREA)
        var mResultList: List<Address>? = null
        try{
            mResultList = mGeoCoder.getFromLocation(
                    latLng.latitude, latLng.longitude, 1
            )
        }catch (e: IOException){
            e.printStackTrace()
        }
        if(mResultList != null){
            return mResultList[0].getAddressLine(0)
        }else{
            return "null"
        }
    }

    private fun getPermission(){
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            Log.e("인증", "방해금지3")
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 100)
        }

        Log.e("인증", "방해금지2")
        var notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if(Build.VERSION.SDK_INT >= 23){
            if(!notificationManager.isNotificationPolicyAccessGranted){
                Log.e("인증", "방해금지")
                this.startActivity(Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
            }
        }


    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode === 100){
            if(grantResults.size > 0){
                for(grant in grantResults){
                    if(grant != PackageManager.PERMISSION_GRANTED){
                        Log.e("인증", "${grant}")
                        System.exit(0)
                    }
                }
            }
        }
    }

    override fun onResume() {
        items = initList()
        super.onResume()
    }
}