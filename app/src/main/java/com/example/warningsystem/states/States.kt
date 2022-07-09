package com.example.warningsystem.states

import com.example.bluetooth.Bluetooth

object States {
    var isDemo = false
    var isDebugging = false
    var isDataReceived = false
    var isConnected:Boolean
        get(){ return Bluetooth.getInstanceWithoutArg()?.isConnected()!!} private set(_) {}
    var isEnabled:Boolean
        get(){ return Bluetooth.getInstanceWithoutArg()?.isEnabled()!!} private set(_) {}

    fun reset(){
        isDemo = false
        isDebugging = false
        isDataReceived = false
    }
}