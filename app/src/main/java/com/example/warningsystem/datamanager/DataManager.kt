package com.example.warningsystem.datamanager

import com.example.bluetooth.Bluetooth
import com.example.warningsystem.constants.MAX_SPEED
import com.example.warningsystem.constants.MAX_TTC
import com.example.warningsystem.states.States
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.lang.Float.min
import kotlin.concurrent.thread
import kotlin.math.max

object DataManager  {
    private val mHashMap:HashMap<String,String> = HashMap()
    private var btReadingThread = BluetoothReadingThread()
    private var startBtReading = false
    private var startDemo = false
    private var demoThread = DemoThread()
    init{
        mHashMap["speed"] = "0"
        mHashMap["ttc"] = "100"
        mHashMap["demoSpeed"] = "0"
        mHashMap["demoTTC"] = "100"
        mHashMap["speedLimit"] = "100"
    }

    fun start(){
        if(States.mode == States.Mode.DEMO){
            startDemoThread()
        }else if(States.mode == States.Mode.MONITOR){
            startBluetoothReading()
        }
    }
    fun stop(){
        if(States.mode == States.Mode.DEMO){
            stopDemoThread()
        }else if(States.mode == States.Mode.MONITOR){
            stopBluetoothReading()
        }
    }
    fun putMapValue(key: String, value: String) = runBlocking {
        mHashMap[key] = value
    }
    fun sendDataWithBluetooth():Boolean{
        val bluetooth = Bluetooth.getInstanceWithoutArg()
        return if(bluetooth?.isConnected() == true){
            bluetooth.send(getJSONDataAsByteArray())
            true
        } else
            false
    }

    private fun getMapValue(key: String) = runBlocking {
        if(mHashMap.containsKey(key))
            return@runBlocking mHashMap[key]
        else
            return@runBlocking null
    }
    fun getMapValueAsString(key: String):String {
        return getMapValue(key) ?: ""
    }
    fun getMapValueAsFloat(key: String):Float {
        val mapVal = getMapValue(key)
        return if(mapVal != null && mapVal.contains("[0-9]".toRegex()))
            mapVal.toFloat()
        else
            0f
    }

    fun getSortedMap() = runBlocking {
        return@runBlocking mHashMap.toSortedMap()
    }
    private fun getJSONDataAsByteArray():ByteArray{
        return Gson().toJson(mHashMap).toString().toByteArray()
    }

    private fun startBluetoothReading(){
        if(!btReadingThread.isAlive) {
            startBtReading = true
            btReadingThread = BluetoothReadingThread()
            btReadingThread.start()
        }
    }
    private fun stopBluetoothReading(){
        if(btReadingThread.isAlive) {
            startBtReading = false
            try {
                btReadingThread.join()
            } catch (e: InterruptedException) {
                //
            }
        }
    }
    private fun startDemoThread(){
        if(!demoThread.isAlive) {
            startDemo = true
            demoThread = DemoThread()
            demoThread.start()
        }
    }
    private fun stopDemoThread(){
        if(demoThread.isAlive) {
            startDemo = false
            try {
                demoThread.join()
            } catch (e: InterruptedException) {
                //
            }
        }
    }
    private class BluetoothReadingThread : Thread(){

        override fun run() {
            var receivedData:ByteArray
            var receivedStatus:Boolean
            var jsonResponse: JSONObject
            var iteratorObj: Iterator<String>
            var keyName: String
            var valueName: String
            val bluetooth:Bluetooth = Bluetooth.getInstanceWithoutArg()!!

            while (startBtReading && States.isConnected) {
                receivedData = bluetooth.read().first
                receivedStatus = bluetooth.read().second
                if ( receivedStatus) {
                    jsonResponse = try {
                        JSONObject(String(receivedData))
                    } catch (ex: JSONException) {
                        JSONObject("{}")
                    }
                    iteratorObj = jsonResponse.keys()
                    // write the data in the Global BluetoothHashMapReceive
                    while (iteratorObj.hasNext()) {
                        keyName = iteratorObj.next()
                        valueName = jsonResponse.getString(keyName)
                        putMapValue(keyName,valueName)
                    }
                }
            }
        }
    }
    private class DemoThread : Thread(){

        override fun run() {
            var speed = 0f
            var ttc = MAX_TTC
            while (startDemo && (States.mode == States.Mode.DEMO || States.mode == States.Mode.DEBUG )){
                mHashMap["demoSpeed"] = speed.toString()
                mHashMap["demoTTC"] = ttc.toString()
                speed += 0.5f
                ttc -= 0.03f
                ttc = if(ttc<0) MAX_TTC else ttc
                speed =if(speed> MAX_SPEED)0f else speed
                States.isDataReceived = true
                sleep(30)
            }
        }
    }
}