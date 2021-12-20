package com.example.volumechanger

import android.app.Dialog
import android.content.Context
import android.view.WindowManager
import android.widget.Button
import com.example.volumechanger.databinding.LongClickDialogBinding

class LongClickDialog(context: Context) {
    private val dialog = Dialog(context)
    fun showDig(){
        dialog.setContentView(R.layout.long_click_dialog)
        dialog.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                                    WindowManager.LayoutParams.WRAP_CONTENT)
        val revise = dialog.findViewById<Button>(R.id.revise)
        val delete = dialog.findViewById<Button>(R.id.delete)

        revise.setOnClickListener {
            onClickedListenser.onClicked("revise")
            dialog.dismiss()
        }

        delete.setOnClickListener {
            onClickedListenser.onClicked("delete")
            dialog.dismiss()
        }
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)
        dialog.show()
    }

    interface ButtonClickListener{
        fun onClicked(select: String)
    }

    private lateinit var onClickedListenser: ButtonClickListener

    fun setOnClikedListenser(listener: ButtonClickListener){
        onClickedListenser = listener
    }
}