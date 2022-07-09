package com.example.warningsystem.activities

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.warningsystem.sensors.compass.Compass
import com.example.warningsystem.R
import com.example.warningsystem.datamanager.DataManager
import com.example.warningsystem.sensors.location.Location
import com.example.warningsystem.states.States

class MonitorActivity : AppCompatActivity(){

    private lateinit var compass: Compass
    private lateinit var location: Location

    companion object {

        private var activityInstance: MonitorActivity? = null
        fun getInstance(): Activity? = activityInstance
        const val TAG = "MonitorActivity"

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_monitor)
        activityInstance = this
        location = Location(this)
        compass = Compass(this)
        DataManager.start()
    }

    override fun onStart() {
        super.onStart()
        DataManager.start()
    }
    override fun onRestart() {
        super.onRestart()
        DataManager.start()
    }
    override fun onResume() {
        super.onResume()
        location.startLocationUpdates()
        compass.startListener()
        DataManager.start()
    }
    override fun onPause() {
        super.onPause()
        location.stopLocationUpdates()
        compass.stopListener()
        DataManager.stop()
        Log.d(TAG,"onPause")
    }

    override fun onStop() {
        super.onStop()
        DataManager.stop()
        Log.d(TAG,"onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        DataManager.stop()
        Log.d(TAG,"onDestroy")
        States.reset()
    }


}

