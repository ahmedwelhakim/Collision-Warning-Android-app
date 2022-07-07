package com.example.warningsystem.drawingobjects


import android.content.Context
import android.graphics.*
import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow


abstract class DrawingObjects(
    blockPosition: Int,
    var context: Context,
    private var canvasWidth: Int,
    private var canvasHeight: Int
) {
    var textValue: String = ""
    var left: Float = 0F
    var top: Float = 0F
    var right: Float = 0F
    var bottom: Float = 0F
    private val horizontalMargin = 80F
    private val verticalMargin = 50F
    val textValuePaint = Paint()
    protected var blockHeight: Int = 0
    protected var blockWidth: Int = 0
    private var blockNumber = blockPosition
    private var textWidth: Int = 0
    private val lastBlockBottomMargin = 100



    init {
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
        blockHeight = ((canvasHeight - lastBlockBottomMargin) / BLOCKS).toInt()


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
        val background = RectF(
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

    protected fun greenToRed(slope:Int,value:Float, maxVal:Float,limit:Float=maxVal/2f):Pair<Float,Float>{
        val valueTimesSlope = if(slope>0) value*slope else maxVal + slope*value
        val redVal: Float
        val greenVal: Float
        if (valueTimesSlope <= limit) {
            greenVal = GREEN_VALUE
            redVal = max(min(((RED_VALUE * ((valueTimesSlope / limit).pow(0.7f)))), RED_VALUE),0f)

        } else {
            greenVal = max(min((GREEN_VALUE * (1F - min(((valueTimesSlope - limit) / limit).pow(0.7f),1f))),
                GREEN_VALUE),0f)
            redVal = RED_VALUE
        }
        return Pair(redVal,greenVal)
    }
    protected fun changeColor(bitmap: Bitmap?, color: Int, width: Int, height: Int) {
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

    protected fun convertToBlack (bitmap: Bitmap?, width: Int, height: Int) {
        if (bitmap == null) {
            return
        }
        val size = width * height
        val allPixels = IntArray(size)
        var alpha: Int
        bitmap.getPixels(allPixels, 0, width, 0, 0, width, height)
        var pixel: Int
        for (i in 0 until size) {
            pixel = allPixels[i]
            alpha = getAlpha(pixel)

            // black pixels
            if (alpha>0) {
                pixel = Color.parseColor("#03000000")
            }

            allPixels[i] = pixel
        }
        bitmap.setPixels(allPixels, 0, width, 0, 0, width, height)

    }
    protected fun convertImageToShadow(bitmap: Bitmap?, width: Int, height: Int) {
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
    protected fun medianFilter(bitmap: Bitmap?, width: Int, height: Int) {
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

    protected fun median(numArray: Array<Int>) :Int{
        Arrays.sort(numArray)
        val median: Double =
            if (numArray.size % 2 == 0)
                (numArray[numArray.size / 2].toDouble() + numArray[numArray.size / 2 - 1].toDouble()) / 2
            else
                numArray[numArray.size / 2].toDouble()

        return median.toInt()
    }

    protected fun myRgb(red: Float, green: Float, blue: Float): Int {
        return -0x1000000 or
                ((red).toInt() shl 16) or
                ((green).toInt() shl 8) or (blue).toInt()
    }

    protected fun getRed(color: Int): Int {
        return (color and 0xff0000) shr 16
    }

    protected fun getGreen(color: Int): Int {
        return (color and 0x00ff00) shr 8
    }

    protected fun getBlue(color: Int): Int {
        return (color and 0x0000ff)
    }

    protected fun getAlpha(color: Int): Int {
        return ((color.toLong() and 0xff000000) shr 24).toInt()
    }

    protected fun myRgb(red: Int, green: Int, blue: Int): Int {
        return -0x1000000 or
                ((red) shl 16) or
                ((green) shl 8) or (blue)
    }

    protected fun myArgb(alpha: Int, red: Int, green: Int, blue: Int): Int {
        return (alpha shl 24) or
                (red shl 16) or
                (green shl 8) or
                (blue)
    }
    protected fun myArgb(alpha: Float, red: Float, green: Float, blue: Float): Int {
        return ((alpha).toInt() shl 24) or
                ((red).toInt() shl 16) or
                ((green).toInt() shl 8) or (blue).toInt()
    }
}


