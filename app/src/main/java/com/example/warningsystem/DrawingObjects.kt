package com.example.warningsystem

import android.graphics.*
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.dynamicanimation.animation.FlingAnimation
import java.text.DecimalFormat
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.properties.Delegates


abstract class DrawingObjects() {

    var textValue: String = "-1"
    var left: Float = 0F
    var top: Float = 0F
    var right: Float = 0F
    var bottom: Float = 0F
    private val horizontalMargin = 80F
    private val verticalMargin = 50F
    val textValuePaint = Paint()
    private lateinit var id: String
    protected var blockHeight: Int = 0
    protected var blockWidth: Int = 0
    private var blockNumber = -1
    private var textWidth: Int = 0
    var canvasHeight: Int = 0
    var canvasWidth: Int = 0
    private val lastBlockBottomMargin = 100

    companion object {
        const val BLOCKS = 4
    }

    constructor(blockPosition: Int, canvasWidth: Int, canvasHeight: Int) : this() {
        this.blockNumber = blockPosition
        this.canvasWidth = canvasWidth
        this.canvasHeight = canvasHeight
        constructorInit()
    }

    init {
        textValuePaint.color = Color.WHITE
        textValuePaint.isAntiAlias = true
        textValuePaint.strokeWidth = 1F
        textValuePaint.style = Paint.Style.FILL
        textValuePaint.textSize = 80F
    }

    private fun constructorInit() {
        blockHeight = ((canvasHeight - lastBlockBottomMargin) / BLOCKS.toFloat()).toInt()
        blockWidth = canvasWidth
        id = "objectId: $blockNumber"
        val textBound = Rect()
        textValuePaint.getTextBounds("999Km", 0, "999Km".length, textBound)
        textWidth = textBound.width()
        left = 0 + horizontalMargin
        right = canvasWidth - horizontalMargin
        top = ((blockNumber * blockHeight).toFloat()) + verticalMargin
        bottom = top + blockHeight - verticalMargin
    }

    open fun draw(c: Canvas?) {
        val background: RectF = RectF(
            left,
            top,
            right,
            bottom
        )
        val backgroundPaint = Paint()
        backgroundPaint.color = Color.parseColor("#10000000")
        backgroundPaint.style = Paint.Style.FILL
        c?.drawRoundRect(background, 100F, 100F, backgroundPaint)
    }
}

@Suppress("UselessCallOnNotNull")
@RequiresApi(Build.VERSION_CODES.O)
class Speed : DrawingObjects {
    //private var oval: RectF
    private var arcPaint: Paint = Paint()
    private val maxSpeed = 20f
    private val colorMaxIntensity = 170F
    private val arcHeight = 220F
    private val arcWidth = 450F
    private var textBound = Rect()
    private var textUnitBound = Rect()
    private var blockNumber by Delegates.notNull<Int>()
    private lateinit var oval: RectF
    private val textTopMargin = 30F
    private val textCenterX: Float
    private val textCenterY: Float

    private var speed by Delegates.notNull<Float>()

    private var sweepAngle by Delegates.notNull<Float>()
    private var colorRed by Delegates.notNull<Float>()
    private var colorGreen by Delegates.notNull<Float>()
    private var halfSpeed by Delegates.notNull<Float>()

    private var shadowPaint: Paint = Paint()
    private var strokePaint: Paint = Paint()
    private var textUnitPaint:Paint = Paint()
    private var textUnitCenterX:Float
    private var textUnitCenterY:Float

    @RequiresApi(Build.VERSION_CODES.S)
    private constructor() : super()
    constructor(canvasWidth: Int, canvasHeight: Int) : super(
        0,
        canvasWidth,
        canvasHeight
    )

    constructor(blockPosition: Int, canvasWidth: Int, canvasHeight: Int) : super(
        blockPosition,
        canvasWidth,
        canvasHeight
    ) {
        this.blockNumber = blockPosition
    }

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
        val marginBottom = 30F

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

        textCenterX = left + ((right - left) / 2) - (textBound.width() / 2f) -20
        textCenterY = top + ((bottom - top) / 2) + (textBound.height() / 2f) + textTopMargin

        textUnitCenterX = left + ((right - left) / 2) - (textUnitBound.width() / 2)
        textUnitCenterY = textCenterY + 100F


        ////-------- Shadow Paint

        shadowPaint.color = Color.parseColor("#20000000")
        shadowPaint.isAntiAlias = true
        shadowPaint.strokeWidth = arcPaint.strokeWidth + (arcPaint.strokeWidth*0.3f)
        shadowPaint.style = Paint.Style.STROKE
        shadowPaint.strokeCap = Paint.Cap.ROUND


        strokePaint.color = Color.parseColor("#70000000")
        strokePaint.isAntiAlias = true
        strokePaint.strokeWidth = shadowPaint.strokeWidth
        strokePaint.style = Paint.Style.STROKE
        strokePaint.strokeCap = Paint.Cap.ROUND
    }

    override fun draw(c: Canvas?) {
        super.draw(c)
        speed =
            if (textValue.contains("[0-9]".toRegex())) (textValue).toFloat() else -1f
        speed = if (speed > maxSpeed) maxSpeed else speed
        sweepAngle = if (speed < maxSpeed) ((speed / maxSpeed) * 180F) else 180F
        colorRed = 0F
        colorGreen = 0F
        halfSpeed = maxSpeed / 2F
        if (speed <= halfSpeed) {
            colorRed = min(
                ((colorMaxIntensity * ((speed / halfSpeed.toDouble()).pow(0.7)))).toFloat(),
                colorMaxIntensity
            )
            colorGreen = colorMaxIntensity
        } else {
            colorGreen = (colorMaxIntensity * ((1F - ((speed - halfSpeed) / halfSpeed))))
            colorRed = colorMaxIntensity
        }

        c?.drawArc(oval, 180F, sweepAngle, false, strokePaint)
        arcPaint.color = myRgb(colorRed, colorGreen, 0F)
        c?.drawArc(oval, 180F, sweepAngle, false, arcPaint)

        c?.drawArc(oval, 180F, 180f, false, shadowPaint)


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

class TTCDrawing(blockPosition: Int, canvasWidth: Int, canvasHeight: Int) :
    DrawingObjects(blockPosition, canvasWidth, canvasHeight) {
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
    private var maxTTC = 10F
    private var maxColorIntensity = 180F
    private var halfTTC = maxTTC / 2F
    private var r: Float = 0f
    private var g: Float = 0f
    private var strokePaint: Paint = Paint()

    private var ttc by Delegates.notNull<Float>()
    private val df :DecimalFormat = DecimalFormat("0.00")
    init {
        // Title initialization---------------------------------------------------------------------
        titlePaint.style = Paint.Style.FILL
        titlePaint.textSize = 84F
        titlePaint.color = Color.WHITE
        titleTopMargin = blockHeight / 4F
        valueTopMargin = (3*blockHeight / 4F ) +20

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
        lineStartY = top +  ((bottom - top) / 2f ) + 20
        lineStopX = lineStartX
        lineStopY = top +  ((bottom - top) / 2f ) +20
        linePaint = Paint()
        linePaint.style = Paint.Style.FILL
        linePaint.strokeCap = Paint.Cap.ROUND
        linePaint.strokeWidth = 60f
        linePaint.color = myRgb(0f, 255f, 0f)

        // shadow Bar Initialization----------------------------------------------------------------
        shadowPaint = Paint()
        shadowPaint.color = Color.parseColor("#20000000")
        shadowPaint.style = Paint.Style.FILL
        shadowPaint.strokeCap = Paint.Cap.ROUND
        shadowPaint.strokeWidth = linePaint.strokeWidth + (linePaint.strokeWidth*0.3f)

        //Stroke Paint ----------------------------------------------------------------------------
        strokePaint
        strokePaint.color = Color.parseColor("#70000000")
        strokePaint.style = Paint.Style.FILL
        strokePaint.strokeCap = Paint.Cap.ROUND
        strokePaint.strokeWidth = linePaint.strokeWidth + (linePaint.strokeWidth*0.3f)



    }

    override fun draw(c: Canvas?) {
        super.draw(c)

        c?.drawText(titleText, titleX, titleY, titlePaint)
        ttc = textValue.toFloat()


        c?.drawText(df.format(ttc)+" s", valueX, valueY, textValuePaint)
        maxTTC = 10F
        maxColorIntensity = 180F
        halfTTC = maxTTC / 2F

        if (ttc > maxTTC / 2f) {
            r = min(((maxTTC - ttc) / maxTTC) * 2f * maxColorIntensity, maxColorIntensity)
            g = min((ttc / maxTTC) * 2f * maxColorIntensity, maxColorIntensity)
        } else {
            g = min((ttc / maxTTC) * 1.8f * maxColorIntensity, maxColorIntensity)
            r = min(((maxTTC - ttc) / maxTTC) * 2f * maxColorIntensity, maxColorIntensity)
        }

        lineStopX = lineStartX + (1 - ttc / maxTTC) * lineWidth
        linePaint.color = myRgb(r, g, 0f)

        c?.drawLine(lineStartX, lineStartY, lineStopX, lineStopY, strokePaint)
        c?.drawLine(lineStartX, lineStartY, lineStopX, lineStopY, linePaint)
        c?.drawLine(lineStartX,lineStartY,lineStartX+lineWidth,lineStopY,shadowPaint)
    }
}
class TextObject(
    title: String,
    private val maxValue: String,
    private val unit: String,
    blockPosition: Int,
    canvasWidth: Int,
    canvasHeight: Int
) :
    DrawingObjects(blockPosition, canvasWidth, canvasHeight) {
    private var titleX: Float = 0F
    private var titleY: Float = 0F
    private val titlePaint: Paint = Paint()
    private var valueX: Float = 0F
    private var valueY: Float = 0F
    private val titleText: String
    private val titleTopMargin = blockHeight / 4F + 70
    private val valueTopMargin = blockHeight / 2F + 40

    init {
        // Title initialization---------------------------------------------------------------------
        titleText = title
        titlePaint.style = Paint.Style.FILL
        titlePaint.textSize = 100F
        titlePaint.color = Color.WHITE
        val titleTextBound = Rect()
        titlePaint.getTextBounds(title, 0, title.length, titleTextBound)
        titleX = left + (right - left) / 2F - titleTextBound.width() / 2F
        titleY = top + titleTopMargin

        // Value initialization -------------------------------------------------------------------
        textValuePaint.style = Paint.Style.FILL
        textValuePaint.textSize = 80F
        textValuePaint.color = Color.WHITE

        val valueTextBound = Rect()
        textValuePaint.getTextBounds(maxValue + unit, 0, (maxValue + unit).length, valueTextBound)
        valueX = left + (right - left) / 2F - valueTextBound.width() / 2F
        valueY = top + valueTopMargin

    }

    override fun draw(c: Canvas?) {
        super.draw(c)
        c?.drawText(titleText, titleX, titleY, titlePaint)
        c?.drawText(
            String.format("%0${maxValue.length}f$unit", textValue.toFloat()),
            valueX,
            valueY,
            textValuePaint
        )
    }
}

fun myRgb(red: Float, green: Float, blue: Float): Int {
    return -0x1000000 or
            ((red).toInt() shl 16) or
            ((green).toInt() shl 8) or (blue).toInt()
}

fun myRgb(red: Int, green: Int, blue: Int): Int {
    return -0x1000000 or
            ((red).toInt() shl 16) or
            ((green).toInt() shl 8) or (blue).toInt()
}