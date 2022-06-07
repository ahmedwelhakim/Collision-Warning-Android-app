package com.example.warningsystem

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MonitorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_monitor)
        activityInstance = this
    }
    companion object{
        private var activityInstance: MonitorActivity? = null
        fun getInstance(): Activity? = activityInstance
    }
}