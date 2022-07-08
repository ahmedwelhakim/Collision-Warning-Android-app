package com.example.warningsystem.drawingobjects

import android.content.Context
import android.graphics.*
import com.example.warningsystem.constants.*
import kotlin.math.min
import kotlin.properties.Delegates

class Speedometer(blockPosition: Int, context: Context, canvasWidth: Int, canvasHeight: Int) :
    DrawingObjects(
        blockPosition,
        context,
        canvasWidth,
        canvasHeight
    ) {
    //private var oval: RectF
    private var arcPaint: Paint = Paint()
    private var textBound = Rect()
    private var textUnitBound = Rect()
    private var oval: RectF
    private val textTopMargin = 30F
    private val textCenterX: Float
    private val textCenterY: Float

    private var speed by Delegates.notNull<Float>()

    private var sweepAngle by Delegates.notNull<Float>()
    private var colorRed by Delegates.notNull<Float>()
    private var colorGreen by Delegates.notNull<Float>()
    private var shadowPaint: Paint = Paint()
    private var strokePaint: Paint = Paint()
    private var textUnitPaint: Paint = Paint()
    private var textUnitCenterX: Float
    private var textUnitCenterY: Float
    private val endArcAngle: Float = 360f + 20f
    private val startAngle: Float = 180f - 20f
    private val maxArcAngle: Float = endArcAngle - startAngle


    init {

        textValuePaint.textSize = 150F
        textValuePaint.getTextBounds("99", 0, "99".length, textBound)


        textValuePaint.isAntiAlias = true
        textValuePaint.style = Paint.Style.FILL
        textUnitPaint.textSize = 70F
        textUnitPaint.color = Color.WHITE
        textUnitPaint.getTextBounds("Km/hr", 0, "Km/hr".length, textUnitBound)

        arcPaint.color = myRgb(0f, 255F, 0F)
        arcPaint.isAntiAlias = true
        arcPaint.strokeWidth = 60F
        arcPaint.style = Paint.Style.STROKE
        arcPaint.strokeCap = Paint.Cap.ROUND

        val horizontalDistance = right - left
        val verticalDistance = bottom - top

        val desiredArcHeight = min(300F, verticalDistance)
        val desiredArcWidth = min(600F, horizontalDistance)
        val marginBottom = 55F

        val arcLeft = left + arcPaint.strokeWidth / 2
        val arcRight = right - arcPaint.strokeWidth / 2
        val arcTop = top + arcPaint.strokeWidth / 2
        val arcBottom = bottom + desiredArcHeight - arcPaint.strokeWidth

        val arcHalfRemainingHorizontalDistance = (horizontalDistance - desiredArcWidth) / 2F
        val arcHalfRemainingVerticalDistance = (verticalDistance - desiredArcHeight) / 2F
        val finalTop = arcTop + arcHalfRemainingVerticalDistance - marginBottom
        val finalBottom = arcBottom - arcHalfRemainingVerticalDistance - marginBottom
        val finalLeft = arcLeft + arcHalfRemainingHorizontalDistance
        val finalRight = arcRight - arcHalfRemainingHorizontalDistance

        oval = RectF(
            finalLeft,
            finalTop,
            finalRight,
            finalBottom
        )

        textCenterX = left + ((right - left) / 2) - (textBound.width() / 2f) - 20
        textCenterY = top + ((bottom - top) / 2) + (textBound.height() / 2f) + textTopMargin

        textUnitCenterX = left + ((right - left) / 2) - (textUnitBound.width() / 2)
        textUnitCenterY = textCenterY + 100F


        ////-------- Shadow Paint

        shadowPaint.color = EMPTY_BAR_COLOR
        shadowPaint.isAntiAlias = true
        shadowPaint.strokeWidth = arcPaint.strokeWidth + (arcPaint.strokeWidth * 0.3f)
        shadowPaint.style = Paint.Style.STROKE
        shadowPaint.strokeCap = Paint.Cap.ROUND


        strokePaint.color = BLACK_COLOR
        strokePaint.isAntiAlias = true
        strokePaint.strokeWidth = shadowPaint.strokeWidth
        strokePaint.style = Paint.Style.STROKE
        strokePaint.strokeCap = Paint.Cap.ROUND
    }

    override fun draw(c: Canvas?) {
        super.draw(c)
        speed =
            if (textValue.contains("[0-9]".toRegex())) (textValue).toFloat() else -1f
        speed = if (speed > MAX_SPEED) MAX_SPEED else speed
        sweepAngle = if (speed < MAX_SPEED) ((speed / MAX_SPEED) * maxArcAngle) else maxArcAngle

        val redGreen = greenToRed(1,speed,MAX_SPEED)
        colorRed = redGreen.first
        colorGreen = redGreen.second


        c?.drawArc(oval, startAngle, sweepAngle, false, strokePaint)
        arcPaint.color = myRgb(colorRed, colorGreen, 0F)


        c?.drawArc(oval, startAngle, maxArcAngle, false, shadowPaint)
        c?.drawArc(oval, startAngle, sweepAngle, false, arcPaint)


        c?.drawText(
            String.format("%02.0f", speed),
            textCenterX,
            textCenterY,
            textValuePaint
        )
        c?.drawText(
            "Km/hr",
            textUnitCenterX,
            textUnitCenterY,
            textUnitPaint
        )
    }
}