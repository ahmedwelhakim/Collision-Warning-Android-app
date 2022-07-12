package com.example.warningsystem.canvas

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import com.example.warningsystem.constants.*
import com.example.warningsystem.datamanager.DataManager
import com.example.warningsystem.drawingobjects.*
import com.example.warningsystem.states.States



class CanvasThread(private val view: CanvasView, canvasWidth: Int, canvasHeight: Int) : Thread() {



    private var running = true
    private var monitorSpeedometerDrawing: Speedometer
    private var ttcDrawing: TTCMeter
    private var warningDrawing: WarningSign
    private var speedometer:Speedometer

    ////--------------------------Debugging--Purpose------------------------------------------------
    private val paint = Paint()
    private var x = 70f
    private var y: Float = 0f
    private val paintBg = Paint()
    private var blockHeight: Int =0
    private var blockWidth: Int =0
    private var left: Float = 0f
    private var top: Float = 0f
    private var bottom: Float = 0f
    private var right: Float = 0f
    private var ttc = 100f
    private var speed = 0f
    private var demoTTC = 100f
    private var demoSpeed = 0f
    private val blocks = 3.5f
    private val speedometerBlocks = 2.7f
    ////--------------------------------------------------------------------------------------------
    init {

        // for both Demo and Monitor Mode
        monitorSpeedometerDrawing = Speedometer(0,blocks ,view.context, canvasWidth, canvasHeight)
        ttcDrawing = TTCMeter(1,blocks ,view.context, canvasWidth, canvasHeight)
        warningDrawing = WarningSign(2,blocks, view.context, canvasWidth, canvasHeight)

        // for speedometer Mode
        speedometer =  Speedometer(0,speedometerBlocks ,view.context, canvasWidth, canvasHeight)

        // for Debug Mode
        debugInit()

    }

    fun setRunning(run: Boolean) {
        running = run
    }


    override fun run() {
        var c: Canvas? = null

        while (running) {


            if (States.isDataReceived) {
                try {
                    c = view.holder.lockCanvas()

                    synchronized(view.holder) {
                        if (c != null) {
                            c.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                            if (States.mode != States.Mode.DEBUG) {
                                when (States.mode) {
                                    States.Mode.DEMO -> demoMode(c)
                                    States.Mode.MONITOR -> monitorMode(c)
                                    States.Mode.SPEEDOMETER -> speedometerMode(c)
                                    else -> {/* Do Nothing */}
                                }

                            } else {
                                // debugging text drawing
                                States.isDataReceived = false
                                drawDebugTexts(c)

                            }
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

    private fun speedometerMode(c: Canvas) {
        // update ------------------------------------------------
        States.isDataReceived = false
        speed = DataManager.getMapValueAsFloat("speed") * 3.6f;
        speedometer.textValue = speed.toString()
        // Draw -------------------------------------------------
        speedometer.draw(c)

    }

    private fun monitorMode(c:Canvas){

        // update ------------------------------------------------
        States.isDataReceived = false
        speed = DataManager.getMapValueAsFloat("speed") * 3.6f;
        monitorSpeedometerDrawing.textValue = speed.toString()

        ttc = DataManager.getMapValueAsFloat("ttc")
        ttcDrawing.ttc = ttc

        warningDrawing.speed = speed
        warningDrawing.ttc = ttc

        // Draw ---------------------------------------------------
        ttcDrawing.draw(c)
        monitorSpeedometerDrawing.draw(c)
        warningDrawing.draw(c)
    }
    private fun demoMode(c:Canvas){

        // update ------------------------------------------------
        States.isDataReceived = false
        demoSpeed = DataManager.getMapValueAsFloat("demoSpeed")
        monitorSpeedometerDrawing.textValue = demoSpeed.toString()

        demoTTC = DataManager.getMapValueAsFloat("demoTTC")
        ttcDrawing.ttc = demoTTC

        warningDrawing.speed = demoSpeed
        warningDrawing.ttc = demoTTC

        // Draw ---------------------------------------------------
        ttcDrawing.draw(c)
        monitorSpeedometerDrawing.draw(c)
        warningDrawing.draw(c)
    }
    private fun debugInit(){

        blockWidth = view.width - 140

        left = x
        top = y
        bottom = y + view.height
        right = x + view.width
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
        for (i in DataManager.getSortedMap()) {
            c.drawText("${i.key} = ${i.value}", x, y + 50, paint)
            y += paint.textSize * 1.5f
        }
    }
}
