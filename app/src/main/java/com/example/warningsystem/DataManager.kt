package com.example.warningsystem

import kotlinx.coroutines.runBlocking

class DataManager {
    private val mHashMap:HashMap<String,String> = HashMap()
    private constructor(){
        mHashMap["speed"] = "0"
        mHashMap["ttc"] = "100"
        mHashMap["maxSpeed"] = "200"

    }
    companion object{
        private val instance = DataManager()
        fun getInstance():DataManager{
            return instance
        }
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
}