package com.example.warningsystem.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatButton
import com.example.warningsystem.R
import com.example.warningsystem.states.States

class MainActivity : AppCompatActivity(),View.OnClickListener {
    private lateinit var connectBtn:AppCompatButton
    private lateinit var demoBtn:AppCompatButton
    private lateinit var speedometerBtn:AppCompatButton
    private lateinit var nextIntent: Intent
    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        connectBtn = findViewById(R.id.connect_btn)
        demoBtn = findViewById(R.id.demo_btn)
        speedometerBtn = findViewById(R.id.speedometer_btn)
        connectBtn.setOnClickListener(this)
        demoBtn.setOnClickListener(this)
        speedometerBtn.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
       when(v?.id){
           R.id.connect_btn ->{
               States.setMonitorMode()
               nextIntent = Intent(applicationContext,BluetoothActivity::class.java)
               startActivity(nextIntent)
           }
           R.id.demo_btn ->{
               States.setDemoMode()
               nextIntent = Intent(applicationContext,MonitorActivity::class.java)
               startActivity(nextIntent)
           }
           R.id.speedometer_btn ->{
               States.setSpeedometerMode()
               nextIntent = Intent(applicationContext,MonitorActivity::class.java)
               startActivity(nextIntent)
           }
       }
    }
}