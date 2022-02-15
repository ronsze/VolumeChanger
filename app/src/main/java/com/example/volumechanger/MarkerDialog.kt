package com.example.volumechanger

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.*
import androidx.databinding.DataBindingUtil
import com.example.volumechanger.databinding.CreateMarkerCustomDialogBinding

class MarkerDialog (context: Context){
    private lateinit var binding: CreateMarkerCustomDialogBinding
    private val dialog = Dialog(context)
    private val diaContext = context

    fun showDia(){
        binding = DataBindingUtil.inflate(LayoutInflater.from(diaContext), R.layout.create_marker_custom_dialog, null, false)
        dialog.setContentView(binding.root)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.setLayout(WindowManager.LayoutParams.WRAP_CONTENT,
                                    WindowManager.LayoutParams.WRAP_CONTENT)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)

        var muteFlag = false ; var vibFlag = false
        var vol = 0 ; var range = 0

        val adapter = ArrayAdapter.createFromResource(dialog.context, R.array.rangeList, R.layout.spinner_item)
        adapter.setDropDownViewResource(R.layout.spinner_item)
        binding.spinner.adapter = adapter

        binding.ok.setOnClickListener {
            if(binding.volumeBar.progress <= 0){
                if(muteFlag) vol = 0
                else if(vibFlag)  vol = -1
            }else{
                vol = binding.volumeBar.progress
            }

            when(binding.spinner.selectedItem){
                "100m" -> range = 100
                "200m" -> range = 200
                "300m" -> range = 300
                "500m" -> range = 500
            }

            onClikedListener.onClicked(binding.nameEdit.text.toString(), range, vol)
            dialog.dismiss()
        }

        binding.cancel.setOnClickListener {
            dialog.dismiss()
        }

        binding.volVib.setOnClickListener {
            binding.volumeBar.setProgress(0)
            vibFlag = true
            muteFlag = false
        }

        binding.volMute.setOnClickListener {
            binding.volumeBar.setProgress(0)
            vibFlag = false
            muteFlag = true
        }

        binding.volMax.setOnClickListener {
            binding.volumeBar.setProgress(binding.volumeBar.max)
            vibFlag = false
            muteFlag = false
        }

        dialog.show()
    }

    interface ButtonOnClickLister{
        fun onClicked(name: String, range: Int, volume: Int)
    }

    private lateinit var onClikedListener: ButtonOnClickLister

    fun setOnClickListener(listener: ButtonOnClickLister){
        onClikedListener = listener
    }
}