package com.example.volumechanger

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.databinding.DataBindingUtil
import com.example.volumechanger.databinding.CreateMarkerCustomDialogBinding

class MarkerDialog (context: Context): View.OnClickListener{
    private lateinit var binding: CreateMarkerCustomDialogBinding
    private val dialog = Dialog(context)
    private val diaContext = context

    private var muteFlag = false ; var vibFlag = false
    private var vol = 0 ; var range = 0

    fun showDia(){
        binding = DataBindingUtil.inflate(LayoutInflater.from(diaContext), R.layout.create_marker_custom_dialog, null, false)

        with(dialog){
            setContentView(binding.root)
            window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window!!.setLayout(WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT)
            setCanceledOnTouchOutside(true)
            setCancelable(true)
        }

        val adapter = ArrayAdapter.createFromResource(dialog.context, R.array.rangeList, R.layout.spinner_item)
        adapter.setDropDownViewResource(R.layout.spinner_item)

        val clickListener = this

        with(binding){
            spinner.adapter = adapter
            ok.setOnClickListener(clickListener)
            cancel.setOnClickListener(clickListener)
            volVib.setOnClickListener(clickListener)
            volMute.setOnClickListener(clickListener)
            volMax.setOnClickListener(clickListener)
        }

        dialog.show()
    }

    override fun onClick(view: View?) {
        with(binding){
            when(view){
                ok -> {
                    if(volumeBar.progress <= 0){
                        if(muteFlag) vol = 0
                        else if(vibFlag)  vol = -1
                    }else{
                        vol = volumeBar.progress
                    }

                    when(spinner.selectedItem){
                        "100m" -> range = 100
                        "200m" -> range = 200
                        "300m" -> range = 300
                        "500m" -> range = 500
                    }

                    onClikedListener.onClicked(nameEdit.text.toString(), range, vol)
                    dialog.dismiss()
                }
                cancel -> {
                    dialog.dismiss()
                }
                volVib -> {
                    volumeBar.setProgress(0)
                    vibFlag = true
                    muteFlag = false
                }
                volMute -> {
                    volumeBar.setProgress(0)
                    vibFlag = false
                    muteFlag = true
                }
                volMax -> {
                    volumeBar.setProgress(volumeBar.max)
                    vibFlag = false
                    muteFlag = false
                }
            }
        }
    }

    interface ButtonOnClickLister{
        fun onClicked(name: String, range: Int, volume: Int)
    }

    private lateinit var onClikedListener: ButtonOnClickLister

    fun setOnClickListener(listener: ButtonOnClickLister){
        onClikedListener = listener
    }
}