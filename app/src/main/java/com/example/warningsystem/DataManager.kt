package com.example.warningsystem

import com.example.bluetooth.Bluetooth
import kotlinx.coroutines.runBlocking
import org.json.JSONException
import org.json.JSONObject
import kotlin.concurrent.thread

object DataManager  {
    private val mHashMap:HashMap<String,String> = HashMap()


    init{
        mHashMap["speed"] = "0"
        mHashMap["ttc"] = "100"
        mHashMap["maxSpeed"] = "200"
        runBluetoothReadingThread()
    }


    fun putMapValue(key: String, value: String) = runBlocking {
        mHashMap[key] = value
    }

    fun getMapValue(key: String) = runBlocking {
        if(mHashMap.containsKey(key))
            return@runBlocking mHashMap[key] as String
        else
            return@runBlocking null
    }

    fun getSortedMap() = runBlocking {
        return@runBlocking mHashMap.toSortedMap()
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
            while (true) {
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
                        mHashMap[keyName] = valueName
                    }
                }
            }
        }
    }
}