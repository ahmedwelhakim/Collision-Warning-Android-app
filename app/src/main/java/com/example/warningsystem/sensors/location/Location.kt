package com.example.warningsystem.sensors.location

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.example.warningsystem.canvas.CanvasThread
import com.example.warningsystem.constants.*
import com.example.warningsystem.datamanager.DataManager
import com.example.warningsystem.states.States
import com.google.android.gms.location.*



class Location(private val activity: Activity) {
    private var locationCallback: LocationCallback
    private var fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity)
    private var locationRequest: LocationRequest = LocationRequest.create()

    init {

        locationRequest.interval = MAX_TIME_INTERVAL.toLong()
        locationRequest.fastestInterval = MIN_TIME_INTERVAL.toLong()
        locationRequest.priority = Priority.PRIORITY_HIGH_ACCURACY

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
                DataManager.putMapValue("gpsHeading",heading.toString())
                DataManager.putMapValue("speedAccuracy", speedAccuracy.toString())
                DataManager.putMapValue("gpsAccuracy", lonAccuracy.toString())
                States.isDataReceived = true
                DataManager.sendDataWithBluetooth()

            }
        }


        startLocationUpdates()
    }
    fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQ_CODE
            )
            ActivityCompat.requestPermissions(activity,
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

    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

}