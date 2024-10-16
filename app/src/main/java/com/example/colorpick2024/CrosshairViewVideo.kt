package com.example.colorpick2024

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.VectorDrawable
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.colorpick2024.R

class CrosshairViewVideo @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val crosshairDrawable: VectorDrawable?

    init {
        // Load the SVG as a VectorDrawable
        crosshairDrawable = ContextCompat.getDrawable(context, R.drawable.centre_crosshair) as? VectorDrawable
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        crosshairDrawable?.let {
            // Calculate the center position
            val left = (width - it.intrinsicWidth) / 2
            val top = (height - it.intrinsicHeight) / 2

            // Set bounds for the drawable
            it.setBounds(left, top, left + it.intrinsicWidth, top + it.intrinsicHeight)

            // Draw the drawable
            it.draw(canvas)
        }
    }
}
