package com.example.warningsystem.drawingobjects

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import com.example.warningsystem.R
import com.example.warningsystem.constants.*
import kotlin.math.max
import kotlin.math.min

class WarningSign(blockPosition: Int,blocks:Float, context: Context, canvasWidth: Int, canvasHeight: Int) :
    DrawingObjects(blockPosition,blocks, context, canvasWidth, canvasHeight) {
    private val mCustomImage: Drawable
    private var sRed: Float = 0f
    private var sGreen: Float = GREEN_VALUE
    private var tRed: Float = 0f
    private var tGreen: Float = GREEN_VALUE
    private var paint: Paint
    private var mTop: Int = top.toInt()
    private var mLeft: Int = left.toInt()
    private var mRight: Int = right.toInt()
    private var mBitmap: Bitmap
    private var mShadowBitmap: Bitmap
    private var mDarkBitmap: Bitmap
    private val mWidth: Int = 450
    private val mHeight: Int = 350
    var speed: Float = 0f
    var ttc: Float = 10f

    private var bmWidthOffset =260
    private var bmHeightOffset =200
    init {
        mCustomImage = AppCompatResources.getDrawable(context,R.drawable.warning5_)!!

        mTop = (top + blockHeight / 2f - mHeight / 2f).toInt() - 50

        mLeft = (left + blockWidth / 2f - mWidth / 2f).toInt()
        mRight = (right - (blockWidth - mWidth) / 2f + (blockWidth - mWidth) / 4f).toInt()

        //colorFilter = LightingColorFilter(Color.GREEN, 255)
        paint = Paint()


        // mCustomImage.colorFilter = colorFilter

        mDarkBitmap = (mCustomImage as BitmapDrawable).bitmap.copy(Bitmap.Config.ARGB_8888, true)
        bmWidthOffset = max(min( (bmWidthOffset-(mWidth/2.0f)).toInt(),50),0)
        bmHeightOffset = max(min( (bmHeightOffset-(mHeight/2.0f)).toInt(),50),0)
        mDarkBitmap = Bitmap.createScaledBitmap(mDarkBitmap, mWidth-bmWidthOffset, mHeight-bmHeightOffset, true)
        medianFilter(mDarkBitmap,mDarkBitmap.width,mDarkBitmap.height)
        convertToBlack(mDarkBitmap,mDarkBitmap.width,mDarkBitmap.height)

        mBitmap = (mCustomImage).bitmap.copy(Bitmap.Config.ARGB_8888, true)
        mBitmap = Bitmap.createScaledBitmap(mBitmap, mWidth, mHeight, true)
        medianFilter(mBitmap,mBitmap.width,mBitmap.height)
        changeColor(mBitmap, myRgb(sRed, sGreen, 0f), mBitmap.width, mBitmap.height)

        mShadowBitmap = (mCustomImage ).bitmap.copy(Bitmap.Config.ARGB_8888, true)
        mShadowBitmap = Bitmap.createScaledBitmap(mShadowBitmap, mWidth-bmWidthOffset, mHeight-bmHeightOffset, true)
        medianFilter(mShadowBitmap,mShadowBitmap.width,mShadowBitmap.height)
        convertImageToShadow(mShadowBitmap,mShadowBitmap.width,mShadowBitmap.height)


    }

    override fun draw(c: Canvas?) {
        super.draw(c)
        if (c != null) {

            val sRedGreen = greenToRed(1,speed, MAX_SPEED)
            sRed = sRedGreen.first
            sGreen = sRedGreen.second

            val tRedGreen = greenToRed(-1,ttc,MAX_TTC)
            tRed = tRedGreen.first
            tGreen = tRedGreen.second



            if (tRed > sRed ||  tGreen<sGreen)
                changeColor(mBitmap, myRgb(tRed, tGreen, 0f), mBitmap.width, mBitmap.height)
            else
                changeColor(mBitmap, myRgb(sRed,sGreen,0f), mBitmap.width, mBitmap.height)

            c.drawBitmap(mDarkBitmap,mLeft+(bmWidthOffset/2f),mTop+(bmHeightOffset/2f),paint)
            c.drawBitmap(mShadowBitmap,mLeft+(bmWidthOffset/2f),mTop+(bmHeightOffset/2f),paint)
            c.drawBitmap(mBitmap, mLeft.toFloat(), mTop.toFloat(), paint)
        }
    }

}