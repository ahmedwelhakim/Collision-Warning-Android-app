package com.example.warningsystem.states

import com.example.bluetooth.Bluetooth

object States {
    enum class Mode{
        DEMO,
        MONITOR,
        SPEEDOMETER,
        DEBUG,
        MAIN
    }
    var mode:Mode = Mode.MAIN
        private set
    private var nonDebugMode = Mode.MAIN
    var isDataReceived = true
    var isConnected:Boolean
        get(){ return Bluetooth.getInstanceWithoutArg()?.isConnected()!!} private set(_) {}
    var isEnabled:Boolean
        get(){ return Bluetooth.getInstanceWithoutArg()?.isEnabled()!!} private set(_) {}

    fun reset(){
        mode = Mode.MAIN
        isDataReceived = true
    }
    fun setDemoMode(){
        mode = Mode.DEMO
        nonDebugMode = Mode.DEMO
    }
    fun setMonitorMode(){
        mode = Mode.MONITOR
        nonDebugMode = Mode.MONITOR
    }
    fun setSpeedometerMode(){
        mode = Mode.SPEEDOMETER
        nonDebugMode = Mode.SPEEDOMETER
    }
    fun toggleDebugMode(){
        mode = if(mode == Mode.DEBUG){
            nonDebugMode
        }else{
            Mode.DEBUG
        }
    }

}