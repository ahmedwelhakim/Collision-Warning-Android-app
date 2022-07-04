package com.example.warningsystem

import android.R
import android.app.Activity
import android.content.Context.LOCATION_SERVICE
import android.content.Context.SENSOR_SERVICE
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.LocationManager
import android.os.Build
import android.util.Log
import android.view.Surface
import androidx.annotation.RequiresApi
import com.example.bluetooth.Bluetooth
import com.google.gson.GsonBuilder
import java.lang.String
import kotlin.FloatArray
import kotlin.Int
import kotlin.TODO
import kotlin.properties.Delegates


const val TAG = "Compass"

class Compass : SensorEventListener {
    private val activity: Activity
    var compassLastMeasuredBearing: Float = 0f
    private val SMOOTHING_FACTOR_COMPASS = 0.1f
    private var current_measured_bearing by Delegates.notNull<Float>()
    private val bluetooth:Bluetooth?
    private lateinit var mSensorManager: SensorManager
    var mGravity:FloatArray? = null
    var mMagnetic:FloatArray? = null
    constructor(activity: Activity) {
        this.activity = activity
        initSensors()
        bluetooth=Bluetooth.getBluetoothInstance(1,activity)
        mSensorManager = activity.getSystemService(SENSOR_SERVICE) as SensorManager

    }
     fun startListener(){
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), MonitorActivity.MIN_TIME_INTERVAL *1000,
            MonitorActivity.MAX_TIME_INTERVAL *1000)
         mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY), MonitorActivity.MIN_TIME_INTERVAL *1000,
             MonitorActivity.MAX_TIME_INTERVAL *1000)
    }
    fun stopListener(){
        mSensorManager.unregisterListener(this,mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD))
        mSensorManager.unregisterListener(this,mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY))
    }
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onSensorChanged(event: SensorEvent?) {


        if (event?.sensor?.getType() == Sensor.TYPE_GRAVITY) {

             mGravity = event?.values.clone()

        } else if (event?.sensor?.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mMagnetic = event?.values.clone()
        }

        if (mGravity != null && mMagnetic != null) {
            /* Create rotation Matrix */
            val rotationMatrix = FloatArray(9)
            if (SensorManager.getRotationMatrix(rotationMatrix, null, mGravity, mMagnetic)) {
                /* Compensate device orientation */
                // http://android-developers.blogspot.de/2010/09/one-screen-turn-deserves-another.html
                val remappedRotationMatrix = FloatArray(9)
                when (activity.getWindowManager().getDefaultDisplay()
                    .getRotation()) {
                    Surface.ROTATION_0 -> SensorManager.remapCoordinateSystem(
                        rotationMatrix,
                        SensorManager.AXIS_X, SensorManager.AXIS_Y,
                        remappedRotationMatrix
                    )
                    Surface.ROTATION_90 -> SensorManager.remapCoordinateSystem(
                        rotationMatrix,
                        SensorManager.AXIS_Y,
                        SensorManager.AXIS_MINUS_X,
                        remappedRotationMatrix
                    )
                    Surface.ROTATION_180 -> SensorManager.remapCoordinateSystem(
                        rotationMatrix,
                        SensorManager.AXIS_MINUS_X,
                        SensorManager.AXIS_MINUS_Y,
                        remappedRotationMatrix
                    )
                    Surface.ROTATION_270 -> SensorManager.remapCoordinateSystem(
                        rotationMatrix,
                        SensorManager.AXIS_MINUS_Y,
                        SensorManager.AXIS_X, remappedRotationMatrix
                    )
                }

                /* Calculate Orientation */
                val results = FloatArray(3)
                SensorManager.getOrientation(
                    remappedRotationMatrix,
                    results
                )

                /* Get measured value */
                     current_measured_bearing = (results[0] * 180 / Math.PI).toFloat()
                if (current_measured_bearing < 0) {
                    current_measured_bearing += 360f
                }

                /* Smooth values using a 'Low Pass Filter' */


                current_measured_bearing =
                    (current_measured_bearing
                                + SMOOTHING_FACTOR_COMPASS * (current_measured_bearing - compassLastMeasuredBearing))

                /*
                 * Update variables for next use (Required for Low Pass
                 * Filter)
                 */compassLastMeasuredBearing = current_measured_bearing

                /*
                 * Write the heading in the BluetoothHashmap send
                 */
                MonitorActivity.Companion.BluetoothHashMapSend.putMapValue("mHeading",current_measured_bearing.toString())
                CanvasThread.isDataReceived = true
                val gsonMapBuilder = GsonBuilder()
                val gsonObject = gsonMapBuilder.create()
                val jsonString =gsonObject.toJson(MonitorActivity.Companion.BluetoothHashMapSend.toSortedMap())
                bluetooth?.send(jsonString.toByteArray())
            }
        }


    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        //TODO("Not yet implemented")
    }

    private fun initSensors() {
        val locationManager = activity.getSystemService(LOCATION_SERVICE) as LocationManager?
        val sensorManager = activity.getSystemService(SENSOR_SERVICE) as SensorManager?
        val mSensorGravity = sensorManager!!.getDefaultSensor(Sensor.TYPE_GRAVITY)
        val mSensorMagneticField = sensorManager
            .getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        /* Initialize the gravity sensor */if (mSensorGravity != null) {
            Log.i(TAG, "Gravity sensor available. (TYPE_GRAVITY)")
            sensorManager.registerListener(
                this, mSensorGravity, SensorManager.SENSOR_DELAY_GAME)
        } else {
            Log.i(TAG, "Gravity sensor unavailable. (TYPE_GRAVITY)")
        }

        /* Initialize the magnetic field sensor */if (mSensorMagneticField != null) {
            Log.i(TAG, "Magnetic field sensor available. (TYPE_MAGNETIC_FIELD)")
            sensorManager.registerListener(
                this, mSensorMagneticField, SensorManager.SENSOR_DELAY_GAME)
        } else {
            Log.i(TAG, "Magnetic field sensor unavailable. (TYPE_MAGNETIC_FIELD)")
        }
    }
}
