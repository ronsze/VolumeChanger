package com.example.volumechanger

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.example.volumechanger.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        getPermission()

        val items = mutableListOf<ListViewItem>()

        items.add(ListViewItem("집", "집집"))

        val adapter = LocListAdapater(items)
        binding.locList.adapter = adapter

        val intent = Intent(this, MapActivity::class.java)

        binding.locList.setOnItemClickListener { parent, view, position, id ->
            intent.putExtra("select", "item")
            startActivity(intent)
        }

        binding.mapBtn.setOnClickListener {
            intent.putExtra("select", "button")
            startActivity(intent)
        }
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
}