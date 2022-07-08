package com.example.warningsystem.datamanager

import com.example.bluetooth.Bluetooth
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import org.json.JSONException
import org.json.JSONObject
import kotlin.concurrent.thread

object DataManager  {
    private val mHashMap:HashMap<String,String> = HashMap()

    init{
        mHashMap["speed"] = "0"
        mHashMap["ttc"] = "100"
        mHashMap["speedLimit"] = "100"
        runBluetoothReadingThread()
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
    private fun runBluetoothReadingThread(){
        // Reading received data from Bluetooth
        thread {
            var receivedData:ByteArray
            var receivedStatus:Boolean
            var jsonResponse: JSONObject
            var iteratorObj: Iterator<String>
            var keyName: String
            var valueName: String
            val bluetooth:Bluetooth = Bluetooth.getInstanceWithoutArg()!!
            while (bluetooth.isConnected()) {
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
}