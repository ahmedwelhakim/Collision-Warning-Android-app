package com.example.warningsystem

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.warningsystem.CanvasView.Companion.BluetoothHashMapReceive


@RequiresApi(Build.VERSION_CODES.S)
class CanvasThread(private val view: CanvasView, canvasWidth: Int, canvasHeight: Int) : Thread() {
    companion object {
        var isDataReceived = false
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private var running = true
    private var drawingSpeed1: Speed
    private var drawingTTC: TTCDrawing
    private var imageDrawing: ImageDrawing

    ////--------------------------Debugging--Purpose------------------------------------------------
    private val paint = Paint()
    private var x = 70f
    private var y: Float =
        ((view.height) / DrawingObjects.BLOCKS.toFloat()) * (DrawingObjects.BLOCKS) -
                (DrawingObjects.BLOCKS - 2f) * (view.height) / DrawingObjects.BLOCKS.toFloat()
    private val paintBg = Paint()
    private val blockHeight: Int
    private val blockWidth: Int
    private val left: Float
    private val top: Float
    private val bottom: Float
    private val right: Float
    private val debugPaint = Paint()

    ////--------------------------------------------------------------------------------------------
    init {

        drawingSpeed1 = Speed(0, view.context, canvasWidth, canvasHeight)
        drawingTTC = TTCDrawing(1, view.context, canvasWidth, canvasHeight)
        imageDrawing = ImageDrawing(2, view.context, canvasWidth, canvasHeight)

        //////-------------------------------Debugging Purpose-------------------------------
        blockHeight = (view.height / DrawingObjects.BLOCKS.toFloat()).toInt()
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

        while (running) {

            var c: Canvas? = null
            try {
                if (isDataReceived) {
                    c = view.holder.lockCanvas()

                    synchronized(view.holder) {


                        c?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                        if (!(CanvasView.isDebugging)) {

                            isDataReceived = false
                            var speed = 0f
                            if (MonitorActivity.Companion.BluetoothHashMapSend.containsKey("mSpeed")) {
                               speed =
                                    MonitorActivity.Companion.BluetoothHashMapSend.getMapValue("mSpeed")
                                        .toFloat() * 3.6f // to Kmph
                            }
                            drawingSpeed1.textValue = speed.toString()
                            drawingSpeed1.draw(c)

                            drawingTTC.textValue = BluetoothHashMapReceive.getMapValue("ttc")
                            drawingTTC.draw(c)

                            imageDrawing.speed =
                                if (drawingSpeed1.textValue.contains("[0-9]".toRegex())) (BluetoothHashMapReceive.getMapValue(
                                    "speed"
                                ).toFloat()) else -1f
                            imageDrawing.ttc =
                                if (drawingTTC.textValue.contains("[0-9]".toRegex())) (BluetoothHashMapReceive.getMapValue(
                                    "ttc"
                                ).toFloat()) else -1f
                            imageDrawing.draw(c)
                        } else {
                            // -------------------------------------------------------------------------
                            // debugging text drawing should be removed lately
                            // -------------------------------------------------------------------------
                            drawDebugTexts(c)
                            /////---------------------------------------------------------------------------
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
        for(i in MonitorActivity.Companion.BluetoothHashMapSend.toSortedMap()){
            c.drawText("${i.key} = ${i.value}", x, y + 50, paint)
            y += paint.textSize * 1.5f
        }
    }
}
