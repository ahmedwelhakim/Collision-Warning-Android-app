package com.example.warningsystem

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.os.Build
import android.os.SystemClock
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.bluetooth.Bluetooth
import com.example.warningsystem.CanvasView.Companion.bluetoothHashMap
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import kotlin.concurrent.thread


@RequiresApi(Build.VERSION_CODES.S)
class CanvasThread(private val view: CanvasView, canvasWidth: Int, canvasHeight: Int) : Thread() {
    companion object {
        var count = 0
        var isDataReceived = true
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private var running = true
    private lateinit var drawingSpeed1: Speed
    private lateinit var drawingTTC: TTCDrawing
    private val bluetooth: Bluetooth? = Bluetooth.getBluetoothInstanceWithoutContext(1)

    ////--------------------------Debugging--Purpose------------------------------------------------
    private val paint = Paint()
    private var x = 50f
    private var y =
        ((view.height) / DrawingObjects.BLOCKS.toFloat()) * (DrawingObjects.BLOCKS - 1f) + 80f

    ////--------------------------------------------------------------------------------------------
    init {

        drawingSpeed1 = Speed(0, canvasWidth, canvasHeight)
        drawingTTC = TTCDrawing(1, canvasWidth, canvasHeight)
    }

    fun setRunning(run: Boolean) {
        running = run
    }


    override fun run() {

        while (running) {
            if (isDataReceived) {
                var c: Canvas? = null
                try {
                    c = view.holder.lockCanvas()

                    synchronized(view.holder) {
                        isDataReceived = false
                        c?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)


                        drawingSpeed1.textValue = bluetoothHashMap["speed"].toString()
                        drawingSpeed1.draw(c)

                        drawingTTC.textValue = bluetoothHashMap["ttc"].toString()
                        drawingTTC.draw(c)

                        // -------------------------------------------------------------------------
                        // debugging text drawing should be removed lately
                        // -------------------------------------------------------------------------
                        paint.textSize = 30f
                        paint.style = Paint.Style.FILL
                        paint.color = Color.WHITE
                        x = 70f
                        y =
                            ((view.height) / DrawingObjects.BLOCKS.toFloat()) * (DrawingObjects.BLOCKS) -
                                    (DrawingObjects.BLOCKS - 2f) * (view.height) / DrawingObjects.BLOCKS.toFloat()
                        for (i in bluetoothHashMap.toSortedMap()) {
                            if (i.key != "speed" && i.key != "ttc") {
                                c.drawText("${i.key} = ${i.value}", x, y, paint)
                                y += 55
                            }
                        }
                        /////---------------------------------------------------------------------------

                    }
                } finally {
                    if (c != null) {
                        view.holder.unlockCanvasAndPost(c)
                    }
                }
            }
        }
    }
}
