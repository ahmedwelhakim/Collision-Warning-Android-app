package com.example.warningsystem.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.warningsystem.R


class BluetoothListviewAdapter (context: Context, data: ArrayList<ArrayList<String>>) :
    BaseAdapter() {
    private var context: Context
    private var data: ArrayList<ArrayList<String>>
    private var inflater: LayoutInflater? = null

    init {
        this.context = context
        this.data = data
        inflater = context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater?
    }

    override fun getCount(): Int {
        return data.size
    }

    override fun getItem(position: Int): Any {
        return data[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }


    @SuppressLint("InflateParams")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var vi = convertView
        if (vi == null) vi = inflater?.inflate(R.layout.list_item, null)
        val tvBtName = vi?.findViewById<View>(R.id.bt_name) as TextView
        tvBtName.text = data[position][0]
        val tvBtAddress = vi.findViewById<View>(R.id.bt_address) as TextView
        tvBtAddress.text = data[position][1]
        return vi
    }
}



