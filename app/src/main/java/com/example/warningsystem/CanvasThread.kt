package com.example.warningsystem

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.bluetooth.Bluetooth
import kotlin.concurrent.thread

@RequiresApi(Build.VERSION_CODES.S)
class CanvasThread(private val view: CanvasView) : Thread() {
    @RequiresApi(Build.VERSION_CODES.S)
    private var running = false
    private var drawingSpeed: Speed = Speed(view.context)
    private val bluetoothHashMap: HashMap<String, String> = HashMap()
    private val bluetooth:Bluetooth? = Bluetooth.getBluetoothInstanceWithoutContext(1)

    init {
            thread {
                var run = true
                while (run) {
                    val tmp = bluetooth?.read()
                    run = tmp?.second ?: false
                    if (tmp != null) {
                        if (tmp.first != null) {
                            bluetoothHashMap["speed"] = String(tmp.first)
                        }else{
                            bluetoothHashMap["speed"] = "-1"
                        }
                    }
                }
            }
        }
    fun setRunning(run: Boolean) {
        running = run
    }


    override fun run() {
        while (running) {
            var c: Canvas? = null
            try {
                c = view.holder.lockCanvas()

                synchronized(view.holder) { //view.draw(c)

                    c?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                    drawingSpeed.text = bluetoothHashMap["speed"].toString()
                    drawingSpeed.draw(c)
                }

            } finally {
                if (c != null) {
                    view.holder.unlockCanvasAndPost(c)
                }
            }
        }
    }



}
