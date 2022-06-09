package com.example.warningsystem

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.Window
import android.view.WindowManager
import android.view.WindowMetrics
import androidx.annotation.RequiresApi
import androidx.core.content.getSystemService

@RequiresApi(Build.VERSION_CODES.S)
class CanvasView : SurfaceView {
    constructor(context: Context) : super (context)
    constructor(context: Context,attrs:AttributeSet) : super(context,attrs)
    constructor(context: Context,attrs: AttributeSet,defStyle: Int) : super(context,attrs,defStyle)


    private val loopThread: CanvasThread = CanvasThread(this)
    private var x = 0
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
                loopThread.setRunning(true)
                loopThread.start()
                val display = (context.getSystemService(Context.WINDOW_SERVICE))as WindowManager
                val screenHeight = display.defaultDisplay.height
                val param = layoutParams
                param.height = screenHeight - y.toInt()
                layoutParams = param
                setBackgroundColor( Color.TRANSPARENT)
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