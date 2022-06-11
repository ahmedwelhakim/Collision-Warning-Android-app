package com.example.warningsystem

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import androidx.annotation.RequiresApi
import androidx.core.content.getSystemService
import android.view.View.AUTOFILL_FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
import com.example.bluetooth.Bluetooth
import org.json.JSONException
import org.json.JSONObject
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
        val bluetoothHashMap: HashMap<String, String> = HashMap()
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

                bluetoothHashMap["speed"] = "0"
                bluetoothHashMap["ttc"] = "100"
                thread {

                    var tmp: Pair<ByteArray, Boolean>?
                    var jsonResponse: JSONObject
                    var iteratorObj: Iterator<String>
                    var keyName: String
                    var valueName: String
                    var stringJson: String
                    val bluetooth = Bluetooth.getBluetoothInstanceWithoutContext(1)
                    while (true){
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
                            if(iteratorObj.hasNext()){
                                CanvasThread.isDataReceived = true
                            }

                            while (iteratorObj.hasNext()) {
                                keyName = iteratorObj.next()
                                valueName = jsonResponse.getString(keyName)
                                bluetoothHashMap[keyName] = valueName
                                 }
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


    public override fun onDraw(canvas: Canvas) {

    }
}