package com.example.warningsystem

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.view.ViewCompat.setLayerType
import com.example.bluetooth.Bluetooth

class MonitorActivity : AppCompatActivity() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        private var activityInstance: MonitorActivity? = null
        fun getInstance(): Activity? = activityInstance
    }

    lateinit var tv_test: TextView

    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_monitor)
        activityInstance = this
        val bluetooth: Bluetooth = Bluetooth.getBluetoothInstance(1,this@MonitorActivity)!!

    }

}