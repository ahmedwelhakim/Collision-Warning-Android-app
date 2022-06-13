package com.example.warningsystem

import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.*
import androidx.annotation.RequiresApi
import com.example.bluetooth.Bluetooth
import kotlinx.coroutines.runBlocking
import org.json.JSONException
import org.json.JSONObject
import java.lang.Thread.sleep

import kotlin.concurrent.thread
import kotlin.properties.Delegates


@RequiresApi(Build.VERSION_CODES.S)
class CanvasView : SurfaceView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )


    companion object {
        private var instance: CanvasView? = null
        fun getViewInstance(): CanvasView? {
            return instance
        }

        var isDebugging = false
        private val bluetoothHashMapReceive: HashMap<String, String> = HashMap()

        class BluetoothHashMapReceive {

            companion object {
                fun putMapValue(key: String, value: String) = runBlocking {
                    bluetoothHashMapReceive[key] = value

                }

                fun getMapValue(key: String) = runBlocking {
                    return@runBlocking bluetoothHashMapReceive[key] as String
                }

                fun toSortedMap() = runBlocking {
                    return@runBlocking bluetoothHashMapReceive.toSortedMap()
                }

            }

        }

    }

    private lateinit var loopThread: CanvasThread
    private var x = 0
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
                        TODO()
                    }
                }
            }


            override fun surfaceCreated(holder: SurfaceHolder) {

                val display = (context.getSystemService(Context.WINDOW_SERVICE)) as WindowManager
                val screenHeight = display.defaultDisplay.height
                val screenWidth = display.defaultDisplay.width
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

                BluetoothHashMapReceive.putMapValue("speed","0")
                BluetoothHashMapReceive.putMapValue("ttc","10")

                var speed = 0f
                var ttc = 10f

                thread {
                    if (!BluetoothActivity.isDemo) {
                        var tmp: Pair<ByteArray, Boolean>?
                        var jsonResponse: JSONObject
                        var iteratorObj: Iterator<String>
                        var keyName: String
                        var valueName: String
                        var stringJson: String
                        val bluetooth = Bluetooth.getBluetoothInstanceWithoutContext(1)
                        var count = 0f
                        var ttc = 10f
                        while (true) {
                            tmp = bluetooth?.read()
                            if (tmp?.first != null && tmp.second) {
                                stringJson = String(tmp.first)
                                jsonResponse = try {
                                    JSONObject(String(tmp.first))
                                } catch (ex: JSONException) {
                                    CanvasThread.isDataReceived = false
                                    JSONObject("{}")
                                }

                                iteratorObj = jsonResponse.keys()
                                if (iteratorObj.hasNext()) {
                                    CanvasThread.isDataReceived = true
                                }

                                while (iteratorObj.hasNext()) {
                                    keyName = iteratorObj.next()
                                    valueName = jsonResponse.getString(keyName)
                                    bluetoothHashMapReceive[keyName] = valueName
                                }
                            }

                        }
                    } else {
                        while (true) {
                            BluetoothHashMapReceive.putMapValue("speed",speed.toString())
                            BluetoothHashMapReceive.putMapValue("ttc",ttc.toString())
                            speed += 0.17f
                            ttc -= 0.05f
                            if (speed > 20) speed = 0f
                            if (ttc < 0) ttc = 10f
                            CanvasThread.isDataReceived = true
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
         isDebugging = isDebugging.not()
        }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                handler.postDelayed(runnable, 3000)
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                handler.removeCallbacks(runnable)
            }
        }
        return true
    }

    /////////////----------------------------------------for Debugging Purpose----------------------
    public override fun onDraw(canvas: Canvas) {

    }
}