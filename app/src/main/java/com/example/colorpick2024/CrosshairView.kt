package com.example.colorpick2024

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
class CrosshairView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var crosshairDrawable: Drawable? = null
    private var lastX = 0f
    private var lastY = 0f
    private var imageView: ImageView? = null
    private var colorInfoLayout: LinearLayout? = null
    private var colorCodeTextView: TextView? = null

    init {
        // Load the VectorDrawable
        crosshairDrawable = ContextCompat.getDrawable(context, R.drawable.centre_crosshair)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Draw the crosshair icon at the center of the view
        crosshairDrawable?.let {
            val left = 0
            val top = 0
            val right = width
            val bottom = height
            it.setBounds(left, top, right, bottom)
            it.draw(canvas)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                // Update the position
                val parentWidth = (parent as View).width
                val parentHeight = (parent as View).height

                // Adjust the x and y based on the size of the crosshair
                val crosshairOffsetX = width / 2f
                val crosshairOffsetY = height / 2f

                lastX = event.rawX - crosshairOffsetX
                lastY = event.rawY - crosshairOffsetY

                // Ensure the crosshair doesn't move out of bounds
                if (lastX < 0) lastX = 0f
                if (lastY < 0) lastY = 0f
                if (lastX + width > parentWidth) lastX = (parentWidth - width).toFloat()
                if (lastY + height > parentHeight) lastY = (parentHeight - height).toFloat()

                // Update the view position
                this.x = lastX
                this.y = lastY
                invalidate()

                // Get the color under the crosshair
                imageView?.let {
                    val bitmap = getBitmapFromImageView(it)
                    // Sample color from the center of the crosshair
                    val centerX = (lastX + crosshairOffsetX).toInt()
                    val centerY = (lastY + crosshairOffsetY).toInt()
                    val color = bitmap?.let { bmp -> getColorFromBitmap(bmp, centerX, centerY) }
                    color?.let { updateColorInfo(color) }
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    fun setImageView(imageView: ImageView) {
        this.imageView = imageView
    }

    fun setColorInfoViews(colorInfoLayout: LinearLayout, colorCodeTextView: TextView) {
        this.colorInfoLayout = colorInfoLayout
        this.colorCodeTextView = colorCodeTextView
    }

    private fun getBitmapFromImageView(imageView: ImageView): Bitmap? {
        return (imageView.drawable as? BitmapDrawable)?.bitmap
    }

    private fun getColorFromBitmap(bitmap: Bitmap, x: Int, y: Int): Int? {
        return if (x in 0 until bitmap.width && y in 0 until bitmap.height) {
            bitmap.getPixel(x, y)
        } else {
            null
        }
    }

    private fun updateColorInfo(color: Int) {
        val hexColor = String.format("#%06X", 0xFFFFFF and color)
        colorCodeTextView?.text = hexColor
        colorInfoLayout?.setBackgroundColor(color)
    }
}
