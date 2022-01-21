package com.example.volumechanger

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.WindowManager
import android.widget.*

class MarkerDialog (context: Context){
    private val dialog = Dialog(context)

    fun showDia(){
        dialog.setContentView(R.layout.create_marker_custom_dialog)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.setLayout(WindowManager.LayoutParams.WRAP_CONTENT,
                                    WindowManager.LayoutParams.WRAP_CONTENT)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)

        val nameEdt = dialog.findViewById<EditText>(R.id.nameEdit)
        val volBar = dialog.findViewById<SeekBar>(R.id.volume_bar)
        val spin = dialog.findViewById<Spinner>(R.id.spinner)
        val okBtn = dialog.findViewById<Button>(R.id.ok)
        val cancelBtn = dialog.findViewById<Button>(R.id.cancel)
        val vibBtn = dialog.findViewById<Button>(R.id.vol_vib)
        val muteBtn = dialog.findViewById<Button>(R.id.vol_mute)
        val maxBtn = dialog.findViewById<Button>(R.id.vol_max)
        var muteFlag = false ; var vibFlag = false
        var vol = 0 ; var range = 0

        val adapter = ArrayAdapter.createFromResource(dialog.context, R.array.rangeList, R.layout.spinner_item)
        adapter.setDropDownViewResource(R.layout.spinner_item)
        spin.adapter = adapter

        okBtn.setOnClickListener {
            if(muteFlag) vol = 0
            else if(vibFlag)  vol = -1
            else vol = volBar.progress

            if(spin.selectedItem == "100m") range = 100
            else if(spin.selectedItem == "200m") range = 200
            else if(spin.selectedItem == "300m") range = 300
            else if(spin.selectedItem == "500m") range = 500

            onClikedListener.onClicked(nameEdt.text.toString(), range, vol)
            dialog.dismiss()
        }

        cancelBtn.setOnClickListener {
            dialog.dismiss()
        }

        vibBtn.setOnClickListener {
            volBar.setProgress(0)
            vibFlag = !vibFlag
            muteFlag = false
        }

        muteBtn.setOnClickListener {
            volBar.setProgress(0)
            vibFlag = false
            muteFlag = !muteFlag
        }

        maxBtn.setOnClickListener {
            volBar.setProgress(volBar.max)
            vibFlag = false
            muteFlag = false
        }

        volBar.setOnClickListener {
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