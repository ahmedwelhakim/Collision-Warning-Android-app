package com.example.warningsystem.activities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.bluetooth.Bluetooth
import com.example.warningsystem.canvas.CanvasThread
import com.example.warningsystem.compass.Compass
import com.example.warningsystem.R
import com.google.android.gms.location.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.runBlocking



class MonitorActivity : AppCompatActivity(){

    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationManager: LocationManager
    private lateinit var bluetooth:Bluetooth
    private lateinit var compass: Compass


    companion object {

        private var activityInstance: MonitorActivity? = null
        fun getInstance(): Activity? = activityInstance
        const val LOCATION_PERMISSION_REQ_CODE =  355

        const val MAX_TIME_INTERVAL = 100
        const val MIN_TIME_INTERVAL = 50

        private val bluetoothHashMapSend: HashMap<String, String> = HashMap()
        class BluetoothHashMapSend {

            companion object {
                fun putMapValue(key: String, value: String) = runBlocking {
                    bluetoothHashMapSend[key] = value

                }

                fun getMapValue(key: String) = runBlocking {
                    return@runBlocking bluetoothHashMapSend[key] as String
                }

                fun toSortedMap() = runBlocking {
                    return@runBlocking bluetoothHashMapSend.toSortedMap()
                }
                fun containsKey(k:String):Boolean = runBlocking {
                    return@runBlocking bluetoothHashMapSend.containsKey(k)
                }

            }

        }


    }

    private val requestingLocationUpdates = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_monitor)
        activityInstance = this


        bluetooth = Bluetooth.getInstance( this@MonitorActivity)
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

                    data class JsonDataParser(
                        @SerializedName("mLon") val id: Double,
                        @SerializedName("mLat") val name: Double,
                        @SerializedName("mSpeed") val image: Float,
                        @SerializedName("mHeading") val description: Float
                    )

                    val gson = Gson()
                    val json = gson.toJson(JsonDataParser(lon, lat, speed, heading))
                    Log.d("Location", json)
                    bluetooth.send(json.toByteArray())
                }
            }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val lon:Double = locationResult.lastLocation?.longitude!!
                val lat:Double = locationResult.lastLocation?.latitude!!
                val speed:Float = locationResult.lastLocation?.speed!!
                val lonAccuracy = locationResult.lastLocation?.accuracy
                val speedAccuracy = locationResult.lastLocation?.speedAccuracyMetersPerSecond

                BluetoothHashMapSend.putMapValue("mLat", lat.toString())
                BluetoothHashMapSend.putMapValue("mLon", lon.toString())
                BluetoothHashMapSend.putMapValue("mSpeed", speed.toString())
                BluetoothHashMapSend.putMapValue("mSpeedAccuracy", speedAccuracy.toString())
                BluetoothHashMapSend.putMapValue("mGpsAccuracy", lonAccuracy.toString())
                CanvasThread.isDataReceived = true

                val gsonMapBuilder =GsonBuilder()
                val gsonObject = gsonMapBuilder.create()
                val jsonString =gsonObject.toJson(BluetoothHashMapSend.toSortedMap())
                bluetooth.send(jsonString.toByteArray())
                Log.d("MonitorActivity",jsonString)
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

