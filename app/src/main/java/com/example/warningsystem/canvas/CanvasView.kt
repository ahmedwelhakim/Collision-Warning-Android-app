package com.example.warningsystem.canvas

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.*
import com.example.bluetooth.Bluetooth
import com.example.warningsystem.activities.BluetoothActivity
import com.example.warningsystem.datamanager.DataManager
import com.example.warningsystem.states.States
import org.json.JSONException
import org.json.JSONObject
import java.lang.Thread.sleep

import kotlin.concurrent.thread
import kotlin.properties.Delegates



class CanvasView : SurfaceView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )



    private lateinit var loopThread: CanvasThread
    private var viewWidth by Delegates.notNull<Int>()
    private var viewHeight by Delegates.notNull<Int>()


    init {
        this.setBackgroundColor(Color.TRANSPARENT)
        this.setZOrderOnTop(true)
        holder.setFormat(PixelFormat.TRANSPARENT)
        holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceDestroyed(holder: SurfaceHolder) {
                var retry = true
                loopThread.setRunning(false)
                while (retry) {
                    try {
                        loopThread.join()
                        retry = false
                    } catch (e: InterruptedException) {
                       // TODO()
                    }
                }
            }


            override fun surfaceCreated(holder: SurfaceHolder) {

                val screenHeight = height
                val screenWidth = width
                val param = layoutParams
                viewWidth = screenWidth
                viewHeight = screenHeight - y.toInt()
                param.height = viewHeight
                layoutParams = param
                Log.d(
                    "CanvasView",
                    "canvas view: viewHeight = $viewHeight,viewWidth = $viewWidth"
                )
                setBackgroundColor(Color.TRANSPARENT)
                loopThread = CanvasThread(this@CanvasView, screenWidth, viewHeight)
                loopThread.setRunning(true)
                loopThread.start()

                DataManager.putMapValue("speed", "0")
                DataManager.putMapValue("ttc", "100")

                var speed = 0f
                val maxTTC=10f
                var ttc = maxTTC

                thread {
                    if (!States.isDemo) {
                        var tmp: Pair<ByteArray, Boolean>?
                        var jsonResponse: JSONObject
                        var iteratorObj: Iterator<String>
                        var keyName: String
                        var valueName: String
                        val bluetooth = Bluetooth.getInstanceWithoutArg()
                        while (true) {
                            tmp = bluetooth!!.read()
                            if ( tmp.second) {
                                jsonResponse = try {
                                    JSONObject(String(tmp.first))
                                } catch (ex: JSONException) {
                                    States.isDataReceived = false
                                    JSONObject("{}")
                                }

                                iteratorObj = jsonResponse.keys()
                                if (iteratorObj.hasNext()) {
                                    States.isDataReceived = true
                                }
                                // write the data in the Global BluetoothHashMapReceive
                                while (iteratorObj.hasNext()) {
                                    keyName = iteratorObj.next()
                                    valueName = jsonResponse.getString(keyName)
                                    DataManager.putMapValue(keyName,valueName)
                                }
                            }

                        }
                    } else {
                        while (States.isDemo) {
                            /*if (MonitorActivity.Companion.BluetoothHashMapSend.containsKey("mSpeed")) {
                                speed =
                                    MonitorActivity.Companion.BluetoothHashMapSend.getMapValue("mSpeed")
                                        .toFloat() * 3.6f // to Km/hr
                            }else speed = 0f*/
                            speed+=0.5f
                            DataManager.putMapValue("speed", speed.toString())
                            DataManager.putMapValue("ttc", ttc.toString())
                            States.isDataReceived =true
                            ttc -= 0.03f
                            if (speed > 200) speed = 0f
                            if (ttc < 0) ttc = maxTTC
                            sleep(30)
                        }
                    }
                }
            }


            override fun surfaceChanged(
                holder: SurfaceHolder, format: Int,
                width: Int, height: Int
            ) {
            }
        })


    }

    /////////////----------------------------------------for Debugging Purpose-------------------------
    private val runnable =
        Runnable {
            //if (!BluetoothActivity.isDemo)
         States.isDebugging = States.isDebugging.not()
        }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                handler.postDelayed(runnable, 3000)
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                performClick()
            }
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        handler.removeCallbacks(runnable)
        return true
    }

    /////////////----------------------------------------for Debugging Purpose----------------------
    public override fun onDraw(canvas: Canvas) {

    }
}