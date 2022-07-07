package com.example.warningsystem

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.RequiresApi
import java.text.DecimalFormat
import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.properties.Delegates


abstract class DrawingObjects(context: Context) {
    lateinit var context: Context
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
    private var canvasHeight: Int = 0
    private var canvasWidth: Int = 0
    private val lastBlockBottomMargin = 100


    companion object {
        const val BLOCKS = 3.5f
        var BLACK = Color.parseColor("#70000000")
        var RED = 170f
        var GREEN = 170f
        var BG_COLOR = Color.parseColor("#10000000")
        var EMPTY_BAR_COLOR = Color.parseColor("#20000000")
    }

    constructor(blockPosition: Int, context: Context, canvasWidth: Int, canvasHeight: Int) : this(
        context
    ) {
        this.blockNumber = blockPosition
        this.canvasWidth = canvasWidth
        this.canvasHeight = canvasHeight
        this.context = context
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

        id = "objectId: $blockNumber"
        val textBound = Rect()
        textValuePaint.getTextBounds("999Km", 0, "999Km".length, textBound)
        textWidth = textBound.width()
        left = 0 + horizontalMargin
        right = canvasWidth - horizontalMargin
        top = ((blockNumber * blockHeight).toFloat()) + verticalMargin
        bottom = top + blockHeight - verticalMargin
        blockWidth = (right - left).toInt()
    }

    open fun draw(c: Canvas?) {
        val background: RectF = RectF(
            left,
            top,
            right,
            bottom
        )
        val backgroundPaint = Paint()
        backgroundPaint.color = BG_COLOR
        backgroundPaint.style = Paint.Style.FILL
        c?.drawRoundRect(background, 100F, 100F, backgroundPaint)
    }
}

@Suppress("UselessCallOnNotNull")
@RequiresApi(Build.VERSION_CODES.O)
class Speed : DrawingObjects {
    //private var oval: RectF
    private var arcPaint: Paint = Paint()
    private val maxSpeed = 200f
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
    private var textUnitPaint: Paint = Paint()
    private var textUnitCenterX: Float
    private var textUnitCenterY: Float
    private val endArcAngle: Float = 360f + 20f
    private val startAngle: Float = 180f - 20f
    private val maxArcAngle: Float = endArcAngle - startAngle

    @RequiresApi(Build.VERSION_CODES.S)
    private constructor(context: Context) : super(context)
    constructor(context: Context, canvasWidth: Int, canvasHeight: Int) : super(
        0, context,
        canvasWidth,
        canvasHeight
    )

    constructor(blockPosition: Int, context: Context, canvasWidth: Int, canvasHeight: Int) : super(
        blockPosition,
        context,
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


        strokePaint.color = BLACK
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
        sweepAngle = if (speed < maxSpeed) ((speed / maxSpeed) * maxArcAngle) else maxArcAngle
        colorRed = 0F
        colorGreen = 0F
        halfSpeed = maxSpeed / 2F
        if (speed <= halfSpeed) {
            colorRed = min(
                ((RED * ((speed / halfSpeed.toDouble()).pow(0.7)))).toFloat(),
                RED.toFloat()
            )
            colorGreen = GREEN.toFloat()
        } else {
            colorGreen = (GREEN.toFloat() * ((1F - ((speed - halfSpeed) / halfSpeed))))
            colorRed = RED.toFloat()
        }

        c?.drawArc(oval, startAngle, sweepAngle, false, strokePaint)
        arcPaint.color = myRgb(colorRed, colorGreen, 0F)
        c?.drawArc(oval, startAngle, sweepAngle, false, arcPaint)

        c?.drawArc(oval, startAngle, maxArcAngle, false, shadowPaint)


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

class CollisionWarningDrawer(blockPosition: Int, context: Context, canvasWidth: Int, canvasHeight: Int) :
    DrawingObjects(blockPosition, context, canvasWidth, canvasHeight) {
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
    private var halfTTC = maxTTC / 2F
    private var r: Float = 0f
    private var g: Float = 0f
    private var strokePaint: Paint = Paint()
    private val colorOffset = 10
    private var ttc = 10f
    private val df: DecimalFormat = DecimalFormat("0.00")

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
        strokePaint.color = BLACK
        strokePaint.style = Paint.Style.FILL
        strokePaint.strokeCap = Paint.Cap.ROUND
        strokePaint.strokeWidth = linePaint.strokeWidth + (linePaint.strokeWidth * 0.3f)


    }

    override fun draw(c: Canvas?) {
        super.draw(c)

        c?.drawText(titleText, titleX, titleY, titlePaint)
        ttc = textValue.toFloat()
        ttc = min(maxTTC,ttc)
        ttc = max(0f,ttc)




        halfTTC = maxTTC / 2F

        if (ttc > maxTTC / 2f) {
            r = min(((maxTTC - ttc) / maxTTC) * 2f * RED, RED.toFloat())
            g = min((ttc / maxTTC) * 2f * GREEN, GREEN.toFloat())
        } else {
            g = min((ttc / maxTTC) * 2.5f * GREEN, GREEN.toFloat())
            r = min(((maxTTC - ttc) / maxTTC) * 2f * RED, RED.toFloat())
        }




        lineStopX = max(lineStartX + (1 - ttc / maxTTC) * lineWidth,lineStartX)
        linePaint.color =  myRgb(max(r-colorOffset,0f), max(g-colorOffset,0f), 0f)

        c?.drawLine(lineStartX, lineStartY, lineStopX, lineStopY, strokePaint)
        c?.drawLine(lineStartX, lineStartY, lineStopX, lineStopY, linePaint)
        c?.drawLine(lineStartX, lineStartY, lineStartX + lineWidth, lineStopY, shadowPaint)
    }
}

@SuppressLint("UseCompatLoadingForDrawables")

class ImageDrawing(blockPosition: Int, context: Context, canvasWidth: Int, canvasHeight: Int) :
    DrawingObjects(blockPosition, context, canvasWidth, canvasHeight) {
    private val mCustomImage: Drawable
    private var sRed: Float = 0f
    private var sGreen: Float = GREEN.toFloat()
    private var tRed: Float = 0f
    private var tGreen: Float = GREEN.toFloat()
    private var paint: Paint
    private val hMargin: Int = 150
    private val vMargin: Int = 100
    private var mTop: Int = top.toInt()
    private var mBottom: Int = bottom.toInt()
    private var mLeft: Int = left.toInt()
    private var mRight: Int = right.toInt()
    private var mBitmap: Bitmap
    private var mShadowBitmap: Bitmap
    private var mDarkBitmap: Bitmap
    private val mWidth: Int = 450
    private val mHeight: Int = 350
    var speed: Float = 0f
    var ttc: Float = 10f
    val maxSpeed = 200
    val maxTTC = 10f
    var bmWidthOffset =260
    var bmHeightOffset =200
    init {
        mCustomImage = context.getDrawable(R.drawable.warning5_)!!

        mTop = (top + blockHeight / 2f - mHeight / 2f).toInt() - 50

        mLeft = (left + blockWidth / 2f - mWidth / 2f).toInt()
        mRight = (right - (blockWidth - mWidth) / 2f + (blockWidth - mWidth) / 4f).toInt()

        //colorFilter = LightingColorFilter(Color.GREEN, 255)
        paint = Paint()


        // mCustomImage.colorFilter = colorFilter

        mDarkBitmap = (mCustomImage as BitmapDrawable).bitmap.copy(Bitmap.Config.ARGB_8888, true)
        bmWidthOffset =max(min( (bmWidthOffset-(mWidth/2.0f)).toInt(),50),0)
        bmHeightOffset =max(min( (bmHeightOffset-(mHeight/2.0f)).toInt(),50),0)
        mDarkBitmap = Bitmap.createScaledBitmap(mDarkBitmap, mWidth-bmWidthOffset, mHeight-bmHeightOffset, true)
        medianFilter(mDarkBitmap,mDarkBitmap.width,mDarkBitmap.height)
        convertToBlack(mDarkBitmap,mDarkBitmap.width,mDarkBitmap.height)

        mBitmap = (mCustomImage as BitmapDrawable).bitmap.copy(Bitmap.Config.ARGB_8888, true)
        mBitmap = Bitmap.createScaledBitmap(mBitmap, mWidth, mHeight, true)
        medianFilter(mBitmap,mBitmap.width,mBitmap.height)
        changeColor(mBitmap, myRgb(sRed, sGreen, 0f), mBitmap.width, mBitmap.height)

        mShadowBitmap = (mCustomImage as BitmapDrawable).bitmap.copy(Bitmap.Config.ARGB_8888, true)
        mShadowBitmap = Bitmap.createScaledBitmap(mShadowBitmap, mWidth-bmWidthOffset, mHeight-bmHeightOffset, true)
        medianFilter(mShadowBitmap,mShadowBitmap.width,mShadowBitmap.height)
        convertImageToShadow(mShadowBitmap,mShadowBitmap.width,mShadowBitmap.height)


    }

    override fun draw(c: Canvas?) {
        super.draw(c)
        if (c != null) {

            if (speed <= maxSpeed / 2f) {
                sRed = min(
                    ((RED * ((speed / (maxSpeed / 2f).toDouble()).pow(0.7)))).toFloat(),
                    RED.toFloat()
                )
                sGreen = GREEN.toFloat()
            } else {
                sGreen = (GREEN * ((1F - ((speed - (maxSpeed / 2f)) / (maxSpeed / 2f)))))
                sRed = RED.toFloat()
            }
            val halfTTC = maxTTC/2.0f
            if (ttc > maxTTC / 2f) {
                tRed = min(((maxTTC - ttc) / maxTTC) * 2f * RED, RED.toFloat())
                tGreen = min((ttc / maxTTC) * 2f * GREEN, GREEN.toFloat())
            } else {
                tGreen = min((ttc / maxTTC) * 2.5f * GREEN, GREEN.toFloat())
                tRed = min(((maxTTC - ttc) / maxTTC) * 2f * RED, RED.toFloat())
            }


            val offset = 30
            if (tRed > sRed)
                changeColor(mBitmap, myRgb(max(tRed-offset,0f), max(tGreen-offset,0f), 0f), mBitmap.width, mBitmap.height)
            else
                changeColor(mBitmap, myRgb(max(sRed-offset,0f), max(sGreen-offset,0f), 0f), mBitmap.width, mBitmap.height)

            c.drawBitmap(mDarkBitmap,mLeft+(bmWidthOffset/2f),mTop+(bmHeightOffset/2f),paint)
            c.drawBitmap(mBitmap, mLeft.toFloat(), mTop.toFloat(), paint)
            c.drawBitmap(mShadowBitmap,mLeft+(bmWidthOffset/2f),mTop+(bmHeightOffset/2f),paint)
        }
    }

}

fun changeColor(bitmap: Bitmap?, color: Int, width: Int, height: Int) {
    if (bitmap == null) {
        return
    }
    val size = width * height
    val allPixels = IntArray(size)
    var red: Int
    var green: Int
    var blue: Int
    var alpha: Int
    bitmap.getPixels(allPixels, 0, width, 0, 0, width, height)
    var pixel: Int
    for (i in 0 until size) {
        pixel = allPixels[i]
        red = getRed(pixel)
        green = getGreen(pixel)
        blue = getBlue(pixel)
        alpha = getAlpha(pixel)

        // black pixels
        if (red+green+blue<1 && alpha>0) {
           pixel = Color.parseColor("#30101010")
        }
        else if((green+red+blue)>(250*3)){
            pixel = Color.parseColor("#caffffff")
        }
        else if(alpha>200 ) {
             pixel =  color
        }
    allPixels[i] = pixel
    }
    bitmap.setPixels(allPixels, 0, width, 0, 0, width, height)
}

fun convertToBlack (bitmap: Bitmap?, width: Int, height: Int) {
    if (bitmap == null) {
        return
    }
    val size = width * height
    val allPixels = IntArray(size)
    var red: Int
    var green: Int
    var blue: Int
    var alpha: Int
    bitmap.getPixels(allPixels, 0, width, 0, 0, width, height)
    var pixel: Int
    for (i in 0 until size) {
        pixel = allPixels[i]
        red = getRed(pixel)
        green = getGreen(pixel)
        blue = getBlue(pixel)
        alpha = getAlpha(pixel)

        // black pixels
        if (alpha>0) {
            pixel = Color.parseColor("#03000000")
        }

        allPixels[i] = pixel
    }
    bitmap.setPixels(allPixels, 0, width, 0, 0, width, height)

}
fun convertImageToShadow(bitmap: Bitmap?, width: Int, height: Int) {
    if (bitmap == null) {
        return
    }
    val size = width * height
    val allPixels = IntArray(size)
    var red: Int
    var green: Int
    var blue: Int
    var alpha: Int
    bitmap.getPixels(allPixels, 0, width, 0, 0, width, height)
    var pixel: Int
    for (i in 0 until size) {
        pixel = allPixels[i]
        red = getRed(pixel)
        green = getGreen(pixel)
        blue = getBlue(pixel)
        alpha = getAlpha(pixel)

        // black pixels
        if (red+green+blue>10 || (red+green+blue <10 && alpha>100)) {
            pixel = Color.parseColor("#10000000")
        }

        allPixels[i] = pixel
    }
    bitmap.setPixels(allPixels, 0, width, 0, 0, width, height)
}
fun medianFilter(bitmap: Bitmap?, width: Int, height: Int) {
    if (bitmap == null) {
        return
    }
    val size = width * height
    val allPixels = IntArray(size)
    var pixel: Int
    var left: Int
    var right: Int
    var top: Int
    var bottom: Int
    var alpha: Int
    var red: Int
    var green: Int
    var blue: Int
    bitmap.getPixels(allPixels, 0, width, 0, 0, width, height)
    for (i in 0 until size - 1) {
        pixel = allPixels[i]
        if (pixel != 0) {
            left = allPixels[max(i - 1, 0)]
            right = allPixels[min(i + 1, size - 1)]
            top = allPixels[max(i - width, 0)]
            bottom = allPixels[min(i + width, size - 1)]
            alpha = median(arrayOf(getAlpha(left),getAlpha(right),getAlpha(top),getAlpha(bottom),getAlpha(pixel)))
            red = median(arrayOf(getRed(left),getRed(right),getRed(top),getRed(bottom),getRed(pixel)))
            green =median(arrayOf(getGreen(left),getGreen(right),getGreen(top),getGreen(bottom),getGreen(pixel)))
            blue = median(arrayOf(getBlue(left),getBlue(right),getBlue(top),getBlue(bottom),getBlue(pixel)))

            // removing the noises in black part of image
            if(red+green+blue<100*3){
                alpha=255
                red =0
                green = 0
                blue = 0
            }
            pixel = myArgb(alpha, red, green, blue)
            allPixels[i] = pixel
        }
    }
    bitmap.setPixels(allPixels, 0, width, 0, 0, width, height)
}

fun median(numArray: Array<Int>) :Int{
    Arrays.sort(numArray)
    val median: Double =
        if (numArray.size % 2 == 0)
                (numArray[numArray.size / 2].toDouble() + numArray[numArray.size / 2 - 1].toDouble()) / 2
        else
            numArray[numArray.size / 2].toDouble()

    return median.toInt()
}

fun myRgb(red: Float, green: Float, blue: Float): Int {
    return -0x1000000 or
            ((red).toInt() shl 16) or
            ((green).toInt() shl 8) or (blue).toInt()
}

fun getRed(color: Int): Int {
    return (color and 0xff0000) shr 16
}

fun getGreen(color: Int): Int {
    return (color and 0x00ff00) shr 8
}

fun getBlue(color: Int): Int {
    return (color and 0x0000ff)
}

fun getAlpha(color: Int): Int {
    return ((color.toLong() and 0xff000000) shr 24).toInt()
}

fun myRgb(red: Int, green: Int, blue: Int): Int {
    return -0x1000000 or
            ((red).toInt() shl 16) or
            ((green).toInt() shl 8) or (blue).toInt()
}

fun myArgb(alpha: Int, red: Int, green: Int, blue: Int): Int {
    return ((alpha).toInt() shl 24) or
            ((red).toInt() shl 16) or
            ((green).toInt() shl 8) or (blue).toInt()
}
fun myArgb(alpha: Float, red: Float, green: Float, blue: Float): Int {
    return ((alpha).toInt() shl 24) or
            ((red).toInt() shl 16) or
            ((green).toInt() shl 8) or (blue).toInt()
}