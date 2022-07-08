package com.example.warningsystem.activities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.warningsystem.canvas.CanvasThread
import com.example.warningsystem.compass.Compass
import com.example.warningsystem.R
import com.example.warningsystem.datamanager.DataManager
import com.google.android.gms.location.*


class MonitorActivity : AppCompatActivity(){

    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationManager: LocationManager
    private lateinit var compass: Compass


    companion object {

        private var activityInstance: MonitorActivity? = null
        fun getInstance(): Activity? = activityInstance
        const val LOCATION_PERMISSION_REQ_CODE =  355

        const val MAX_TIME_INTERVAL = 100
        const val MIN_TIME_INTERVAL = 50


    }

    private val requestingLocationUpdates = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_monitor)
        activityInstance = this


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager


        locationRequest = LocationRequest.create()
        locationRequest.interval = MAX_TIME_INTERVAL.toLong()
        locationRequest.fastestInterval = MIN_TIME_INTERVAL.toLong()
        locationRequest.priority = Priority.PRIORITY_HIGH_ACCURACY
        compass = Compass(this)


        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        LOCATION_PERMISSION_REQ_CODE
            )
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQ_CODE
            )
            return
        }


        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                if(location != null) {
                    val lon: Double = location.longitude
                    val lat: Double = location.latitude
                    val speed: Float = location.speed
                    val heading: Float = location.bearing


                    DataManager.putMapValue("longitude",lon.toString())
                    DataManager.putMapValue("latitude",lat.toString())
                    DataManager.putMapValue("speed",speed.toString())
                    DataManager.putMapValue("heading",heading.toString())
                }
            }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {

                val lon:Double = locationResult.lastLocation?.longitude!!
                val lat:Double = locationResult.lastLocation?.latitude!!
                val speed:Float = locationResult.lastLocation?.speed!!
                val lonAccuracy = locationResult.lastLocation?.accuracy
                val speedAccuracy = locationResult.lastLocation?.speedAccuracyMetersPerSecond
                val heading = locationResult.lastLocation?.bearing
                DataManager.putMapValue("longitude",lon.toString())
                DataManager.putMapValue("latitude",lat.toString())
                DataManager.putMapValue("speed",speed.toString())
                DataManager.putMapValue("heading",heading.toString())
                DataManager.putMapValue("speedAccuracy", speedAccuracy.toString())
                DataManager.putMapValue("gpsAccuracy", lonAccuracy.toString())
                CanvasThread.isDataReceived = true

                DataManager.sendDataWithBluetooth()

            }

        }
        startLocationUpdates()
    }
    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQ_CODE
            )
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQ_CODE
            )
            return
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper())
    }
    override fun onResume() {

        super.onResume()
        if (requestingLocationUpdates) startLocationUpdates()
        compass.startListener()
    }
    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
        compass.stopListener()
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

}

