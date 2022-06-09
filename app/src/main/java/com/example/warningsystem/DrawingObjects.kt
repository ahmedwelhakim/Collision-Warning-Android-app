package com.example.warningsystem

import android.content.Context
import android.graphics.*
import android.os.Build
import android.view.WindowManager
import androidx.annotation.RequiresApi


abstract class DrawingObjects(context: Context) {
    var text: String = "Uninitialized Speed!!!!"
    var x: Float = 0F
    var y: Float = 0F
    var scaleX = 1F
    var scaleY = 1F
    val textPaint = Paint()
    private val display = (context.getSystemService(Context.WINDOW_SERVICE)) as WindowManager
    val screenHeight = display.defaultDisplay.height
    val screenWidth = display.defaultDisplay.width

    init {
        textPaint.color = Color.WHITE
        textPaint.isAntiAlias = true
        textPaint.strokeWidth = 1F
        textPaint.style = Paint.Style.FILL
        textPaint.textSize = 80F
    }

    abstract fun draw(c: Canvas?)
    fun setXY(x: Float, y: Float) {
        this.x = x
        this.y = y
    }
}

@RequiresApi(Build.VERSION_CODES.O)
class Speed(context: Context) : DrawingObjects(context) {
    private var oval: RectF
    private var arcPaint: Paint = Paint()
    private val maxSpeed = 300
    private var context: Context
    private val topMarginOffset = 300F
    private val colorMaxIntensity =200F


    init {
        scaleX = 1.2F
        scaleY = 1.2F
        textPaint.textSize = 90F
        this.context = context
        val textBound = Rect()
        textPaint.getTextBounds("999Km", 0, "999Km".length, textBound)
        x = (screenWidth / 2F) - (textBound.width() / 2)
        y = topMarginOffset

        val marginHorizontalOffset = 100F
        val arcHeight = 400F

        oval = RectF(
            ((x - marginHorizontalOffset * scaleX).toFloat()),
            ((y - (arcHeight / 2F) * scaleY).toFloat()),
            ((x + textBound.width() + (marginHorizontalOffset * scaleX)).toFloat()),
            ((y + (arcHeight / 2F * scaleY)).toFloat())
        )

        arcPaint.color = myRgb(0f, 255F, 0F)
        arcPaint.isAntiAlias = true
        arcPaint.strokeWidth = 50F
        arcPaint.style = Paint.Style.STROKE

    }

    override fun draw(c: Canvas?) {

        var speed = Integer.parseInt(text)
        speed = if (speed > maxSpeed) maxSpeed else speed
        val sweepAngle = if (speed < maxSpeed) ((speed.toFloat() / maxSpeed) * 180F) else 180F
        val colorRed = (speed / (maxSpeed.toFloat() )) * colorMaxIntensity
        val colorGreen = ((maxSpeed - speed) / (maxSpeed.toFloat() )) * colorMaxIntensity
        arcPaint.color = myRgb(colorRed,colorGreen,0F)
        c?.drawArc(oval, 180F, sweepAngle, false, arcPaint)
        c?.drawText(String.format("%03dKm",speed), x, y, textPaint)
    }

}


fun myRgb(red: Float, green: Float, blue: Float): Int {
    return -0x1000000 or
            ((red).toInt() shl 16) or
            ((green).toInt() shl 8) or (blue).toInt()
}
