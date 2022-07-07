package com.example.warningsystem.canvas

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff

import com.example.warningsystem.canvas.CanvasView.Companion.BluetoothHashMapReceive
import com.example.warningsystem.activities.MonitorActivity
import com.example.warningsystem.drawingobjects.*


class CanvasThread(private val view: CanvasView, canvasWidth: Int, canvasHeight: Int) : Thread() {
    companion object {
        var isDataReceived = true
    }


    private var running = true
    private var drawingSpeed1: Speedometer
    private var collisionWarning: TTCMeter
    private var imageDrawing: WarningSign

    ////--------------------------Debugging--Purpose------------------------------------------------
    private val paint = Paint()
    private var x = 70f
    private var y: Float =
        ((view.height) / BLOCKS) * (BLOCKS) -
                (BLOCKS - 2f) * (view.height) / BLOCKS
    private val paintBg = Paint()
    private val blockHeight: Int
    private val blockWidth: Int
    private val left: Float
    private val top: Float
    private val bottom: Float
    private val right: Float


    ////--------------------------------------------------------------------------------------------
    init {

        drawingSpeed1 = Speedometer(0, view.context, canvasWidth, canvasHeight)
        collisionWarning = TTCMeter(1, view.context, canvasWidth, canvasHeight)
        imageDrawing = WarningSign(2, view.context, canvasWidth, canvasHeight)

        //////-------------------------------Debugging Purpose-------------------------------
        blockHeight = (view.height / BLOCKS).toInt()
        blockWidth = view.width - 140

        left = x
        top = y
        bottom = y + view.height
        right = x + view.width
    }

    fun setRunning(run: Boolean) {
        running = run
    }


    override fun run() {
        var c: Canvas? = null
        while (running) {


            if (isDataReceived) {
                try {
                    c = view.holder.lockCanvas()

                    synchronized(view.holder) {
                        c?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                        if (!(CanvasView.isDebugging) && c != null) {
                            var speed = if (BluetoothHashMapReceive.getMapValue("speed")?.contains("[0-9]".toRegex())!!) (BluetoothHashMapReceive.getMapValue(
                                "speed"
                            )?.toFloat() ) else 0f
                            isDataReceived = false
                            //var speed = 0f
                           /* if (MonitorActivity.Companion.BluetoothHashMapSend.containsKey("mSpeed")) {
                                speed =
                                    MonitorActivity.Companion.BluetoothHashMapSend.getMapValue("mSpeed")
                                        .toFloat() * 3.6f // to Km/h
                            }*/
                            drawingSpeed1.textValue = speed.toString()
                            drawingSpeed1.draw(c)

                            collisionWarning.textValue = BluetoothHashMapReceive.getMapValue("ttc")!!
                            collisionWarning.draw(c)

                            imageDrawing.speed = speed!!

                            imageDrawing.ttc = BluetoothHashMapReceive.getMapValue("ttc")!!.toFloat()

                            imageDrawing.draw(c)
                        } else {
                            // -------------------------------------------------------------------------
                            // debugging text drawing should be removed lately
                            // -------------------------------------------------------------------------
                            isDataReceived = false
                            drawDebugTexts(c)
                            /////---------------------------------------------------------------------------
                        }
                    }
                } finally {
                    if (c != null) {
                        view.holder.unlockCanvasAndPost(c)
                    }

                }
            }
        }
    }


    private fun drawDebugTexts(c: Canvas) {
        paint.textSize = 34f
        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE

        x = 70f
        y = 0f
        //((view.height) / DrawingObjects.BLOCKS.toFloat()) * (DrawingObjects.BLOCKS) -
        // (DrawingObjects.BLOCKS - 2f) * (view.height) / DrawingObjects.BLOCKS.toFloat() + 200

        c.drawText("Debugging mode: . . . ", x, y + 50, paint)
        paintBg.color = Color.parseColor("#30000000")
        c.drawRect(x, y, (x + blockWidth), y + (bottom - top), paintBg)
        x += 50
        y += 100
        for (i in BluetoothHashMapReceive.toSortedMap()) {
            c.drawText("${i.key} = ${i.value}", x, y + 50, paint)
            y += paint.textSize * 1.5f
        }
        for (i in MonitorActivity.Companion.BluetoothHashMapSend.toSortedMap()) {
            c.drawText("${i.key} = ${i.value}", x, y + 50, paint)
            y += paint.textSize * 1.5f
        }
    }
}
