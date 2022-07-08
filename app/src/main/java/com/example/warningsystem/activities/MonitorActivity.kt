package com.example.warningsystem.activities

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.warningsystem.sensors.compass.Compass
import com.example.warningsystem.R
import com.example.warningsystem.sensors.location.Location


class MonitorActivity : AppCompatActivity(){

    private lateinit var compass: Compass
    private lateinit var location: Location

    companion object {

        private var activityInstance: MonitorActivity? = null
        fun getInstance(): Activity? = activityInstance

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_monitor)
        activityInstance = this

        location = Location(this)
        compass = Compass(this)

    }

    override fun onResume() {

        super.onResume()
        location.startLocationUpdates()
        compass.startListener()
    }
    override fun onPause() {
        super.onPause()
        location.stopLocationUpdates()
        compass.stopListener()
    }



}

