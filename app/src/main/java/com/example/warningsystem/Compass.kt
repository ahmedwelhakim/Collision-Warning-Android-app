package com.example.warningsystem


import android.app.Activity
import android.content.Context.SENSOR_SERVICE
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.util.Log
import android.view.Surface
import androidx.annotation.RequiresApi
import com.example.bluetooth.Bluetooth
import com.google.gson.GsonBuilder

import kotlin.FloatArray
import kotlin.Int

import kotlin.properties.Delegates




class Compass(private val activity: Activity) : SensorEventListener {

    private var compassLastMeasuredBearing: Float = 0f
    private var currentMeasuredBearing by Delegates.notNull<Float>()
    private val bluetooth:Bluetooth?
    private var mSensorManager: SensorManager
    private var mGravity:FloatArray? = null
    private var mMagnetic:FloatArray? = null
    companion object{
        private const val TAG = "Compass"
        private const val SMOOTHING_FACTOR_COMPASS = 0.1f
    }

    init {
        initSensors()
        bluetooth=Bluetooth.getInstance(activity)
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


        if (event?.sensor?.type == Sensor.TYPE_GRAVITY) {

             mGravity = event.values.clone()

        } else if (event?.sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) {
            mMagnetic = event.values.clone()
        }

        if (mGravity != null && mMagnetic != null) {
            /* Create rotation Matrix */
            val rotationMatrix = FloatArray(9)
            if (SensorManager.getRotationMatrix(rotationMatrix, null, mGravity, mMagnetic)) {
                /* Compensate device orientation */
                val remappedRotationMatrix = FloatArray(9)
                when (activity.display?.rotation) {
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
                     currentMeasuredBearing = (results[0] * 180 / Math.PI).toFloat()
                if (currentMeasuredBearing < 0) {
                    currentMeasuredBearing += 360f
                }

                /* Smooth values using a 'Low Pass Filter' */


                currentMeasuredBearing =
                    (currentMeasuredBearing
                                + SMOOTHING_FACTOR_COMPASS * (currentMeasuredBearing - compassLastMeasuredBearing))

                /*
                 * Update variables for next use (Required for Low Pass
                 * Filter)
                 */compassLastMeasuredBearing = currentMeasuredBearing

                /*
                 * Write the heading in the BluetoothHashmap send
                 */
                MonitorActivity.Companion.BluetoothHashMapSend.putMapValue("mHeading",currentMeasuredBearing.toString())
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
        val sensorManager = activity.getSystemService(SENSOR_SERVICE) as SensorManager?
        val mSensorGravity = sensorManager?.getDefaultSensor(Sensor.TYPE_GRAVITY)
        val mSensorMagneticField = sensorManager
            ?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

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
