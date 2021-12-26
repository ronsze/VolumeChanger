package com.example.volumechanger

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView

data class ListViewItem(val name: String, val point: String, val address: String)

class LocListAdapater (private val items: MutableList<ListViewItem>): BaseAdapter() {
    override fun getCount(): Int = items.size

    override fun getItem(position: Int): ListViewItem = items[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, view: View?, parent: ViewGroup?): View {
        var convertView = view

        if(convertView == null) convertView = LayoutInflater.from(parent?.context).inflate(R.layout.loc_list_custom, parent, false)
        val item: ListViewItem = items[position]
        val name: TextView? = convertView?.findViewById<TextView>(R.id.nametext)
        val address: TextView? = convertView?.findViewById<TextView>(R.id.addresstext)

        name?.setText(item.name)
        address?.setText(item.address)

        return convertView!!
    }

    inner class DeleteItem : View.OnClickListener{
        override fun onClick(v: View?) {

        }
    }
}