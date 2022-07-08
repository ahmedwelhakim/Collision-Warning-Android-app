package com.example.warningsystem.drawingobjects

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import kotlin.math.max
import kotlin.math.min
import kotlin.properties.Delegates

class TTCMeter (blockPosition: Int, context: Context, canvasWidth: Int, canvasHeight: Int) :
    DrawingObjects(blockPosition, context, canvasWidth, canvasHeight){
    private var titleX: Float = 0F
    private var titleY: Float = 0F
    private val titlePaint: Paint = Paint()
    private var valueX: Float = 0F
    private var valueY: Float = 0F
    private val titleText: String = "Time to Collision"
    private var titleTopMargin = blockHeight / 4F
    private var valueTopMargin = blockHeight / 2F
    private var lineStartX by Delegates.notNull<Float>()
    private var lineStartY by Delegates.notNull<Float>()
    private var lineStopX by Delegates.notNull<Float>()
    private var lineStopY by Delegates.notNull<Float>()
    private var linePaint: Paint
    private var shadowPaint: Paint
    private val lineWidth = 550f
    private var r: Float = 0f
    private var g: Float = 0f
    private var strokePaint: Paint = Paint()
    private val colorOffset = 10
    var ttc = 10f

    init {
        // Title initialization---------------------------------------------------------------------
        titlePaint.style = Paint.Style.FILL
        titlePaint.textSize = 84F
        titlePaint.color = Color.WHITE
        titleTopMargin = blockHeight / 4F
        valueTopMargin = (3 * blockHeight / 4F) + 20

        val titleTextBound = Rect()
        titlePaint.getTextBounds(titleText, 0, titleText.length, titleTextBound)
        titleX = left + (right - left) / 2F - titleTextBound.width() / 2F
        titleY = top + titleTopMargin

        // Value initialization -------------------------------------------------------------------
        textValuePaint.style = Paint.Style.FILL
        textValuePaint.textSize = 50f
        textValuePaint.color = Color.WHITE
        val valueTextBound = Rect()
        textValuePaint.getTextBounds("0.00 s", 0, "0.00 s".length, valueTextBound)
        valueX = left + (right - left) / 2F - valueTextBound.width() / 2F
        valueY = top + valueTopMargin


        // Bar initialization-----------------------------------------------------------------------
        lineStartX = left + (right - left - lineWidth) / 2f
        lineStartY = top + ((bottom - top) / 2f) + 20
        lineStopX = lineStartX
        lineStopY = top + ((bottom - top) / 2f) + 20
        linePaint = Paint()
        linePaint.style = Paint.Style.FILL
        linePaint.strokeCap = Paint.Cap.ROUND
        linePaint.strokeWidth = 60f
        linePaint.color = myRgb(0f, 255f, 0f)

        // shadow Bar Initialization----------------------------------------------------------------
        shadowPaint = Paint()
        shadowPaint.color = EMPTY_BAR_COLOR
        shadowPaint.style = Paint.Style.FILL
        shadowPaint.strokeCap = Paint.Cap.ROUND
        shadowPaint.strokeWidth = linePaint.strokeWidth + (linePaint.strokeWidth * 0.3f)

        //Stroke Paint ----------------------------------------------------------------------------
        strokePaint
        strokePaint.color = BLACK_COLOR
        strokePaint.style = Paint.Style.FILL
        strokePaint.strokeCap = Paint.Cap.ROUND
        strokePaint.strokeWidth = linePaint.strokeWidth + (linePaint.strokeWidth * 0.3f)


    }

    override fun draw(c: Canvas?) {
        super.draw(c)

        c?.drawText(titleText, titleX, titleY, titlePaint)
        ttc = min(MAX_TTC,ttc)
        ttc = max(0f,ttc)




        val redGreen = greenToRed(-1,ttc,MAX_TTC)
        r = redGreen.first
        g = redGreen.second




        lineStopX = max(lineStartX + (1 - ttc / MAX_TTC) * lineWidth,lineStartX)
        linePaint.color =  myRgb(max(r-colorOffset,0f), max(g-colorOffset,0f), 0f)

        c?.drawLine(lineStartX, lineStartY, lineStopX, lineStopY, strokePaint)

        c?.drawLine(lineStartX, lineStartY, lineStartX + lineWidth, lineStopY, shadowPaint)
        c?.drawLine(lineStartX, lineStartY, lineStopX, lineStopY, linePaint)
    }
}