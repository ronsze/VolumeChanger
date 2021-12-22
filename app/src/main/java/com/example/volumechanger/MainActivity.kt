package com.example.volumechanger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.volumechanger.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
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
}