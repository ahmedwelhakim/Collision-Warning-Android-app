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



    private  var loopThread = CanvasThread(this@CanvasView,width, height- y.toInt())
    private var viewWidth by Delegates.notNull<Int>()
    private var viewHeight by Delegates.notNull<Int>()
    private var isFirstTime = true

    init {
        this.setBackgroundColor(Color.TRANSPARENT)
        this.setZOrderOnTop(true)


        holder.setFormat(PixelFormat.TRANSPARENT)
        holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceDestroyed(holder: SurfaceHolder) {
                stopLoopThread()
            }


            override fun surfaceCreated(holder: SurfaceHolder) {
                if(isFirstTime) {
                    setCanvasHeight()
                    isFirstTime = false
                }
                setBackgroundColor(Color.TRANSPARENT)
                if(loopThread.isAlive)
                    stopLoopThread()
                loopThread = CanvasThread(this@CanvasView, viewWidth, viewHeight)
                loopThread.setRunning(true)
                loopThread.start()


            }

            override fun surfaceChanged(
                holder: SurfaceHolder, format: Int,
                width: Int, height: Int
            ) {
            }
        })


    }

    private fun stopLoopThread(){
        if(loopThread.isAlive) {
            var retry = true
            loopThread.setRunning(false)
            while (retry) {
                try {
                    loopThread.join()
                    retry = false
                } catch (e: InterruptedException) {
                    //
                }
            }
        }
    }

    private fun setCanvasHeight(){
        val screenHeight = height
        val screenWidth = width
        val param = layoutParams
        viewWidth = screenWidth
        viewHeight = screenHeight - y.toInt()
        param.height = viewHeight
        layoutParams = param
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