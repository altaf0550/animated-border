package com.losersteam.animatedborder

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView

class BorderAnimator {
    fun animateCardBorder(cardView: MaterialCardView, context: Context): ValueAnimator {
        // Set the stroke width
        cardView.strokeWidth = 2

        // Create a custom drawable for the border
        val borderDrawable = TravelingBorderDrawable(cardView.radius, context)

        // Set the drawable as the foreground of the card
        cardView.foreground = borderDrawable

        // Start the animation
        val animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 4000 // 4 seconds for one full cycle
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()

            addUpdateListener { animation ->
                val position = animation.animatedValue as Float
                borderDrawable.setTravelPosition(position)
                cardView.invalidate() // Request redraw
            }
        }

        animator.start()
        return animator
    }

    // Custom drawable to draw a traveling highlight on the border
    private inner class TravelingBorderDrawable(
        private val cornerRadius: Float,
        context: Context
    ) : Drawable() {
        private val borderPaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
            color = Color.GRAY // Base border color
            isAntiAlias = true
        }

        private val highlightPaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 3f
            color = ContextCompat.getColor(context, R.color.red) // Using R.color.red
            isAntiAlias = true
        }

        private var travelPosition = 0f
        private val path = Path()
        private val measure = PathMeasure()

        fun setTravelPosition(position: Float) {
            travelPosition = position
            invalidateSelf()
        }

        override fun draw(canvas: Canvas) {
            val bounds = bounds

            // Draw the base border
            path.reset()
            path.addRoundRect(
                RectF(
                    bounds.left.toFloat() + 1f,
                    bounds.top.toFloat() + 1f,
                    bounds.right.toFloat() - 1f,
                    bounds.bottom.toFloat() - 1f
                ),
                cornerRadius,
                cornerRadius,
                Path.Direction.CW
            )
            canvas.drawPath(path, borderPaint)

            // Calculate the position of the traveling highlight
            measure.setPath(path, false)
            val pathLength = measure.length
            val highlightLength = pathLength / 4 // Highlight covers 1/4 of the perimeter

            // Calculate start and end points for the highlight
            val startDistance = (travelPosition * pathLength) % pathLength
            val endDistance = (startDistance + highlightLength) % pathLength

            // Draw the traveling highlight
            val highlightPath = Path()
            if (endDistance > startDistance) {
                // Simple case - highlight doesn't wrap around the end
                measure.getSegment(startDistance, endDistance, highlightPath, true)
            } else {
                // Highlight wraps around - need two segments
                measure.getSegment(startDistance, pathLength, highlightPath, true)
                measure.getSegment(0f, endDistance, highlightPath, true)
            }

//            canvas.drawPath(highlightPath, highlightPaint)
            // Get the positions at the start, middle, and end of the highlight segment
            val posStart = FloatArray(2)
            val posMid = FloatArray(2)
            val posEnd = FloatArray(2)

            val midDistance = (startDistance + (highlightLength / 2)) % pathLength

            measure.getPosTan(startDistance, posStart, null)
            measure.getPosTan(midDistance, posMid, null)
            measure.getPosTan(endDistance, posEnd, null)

            // Apply a symmetric gradient: transparent ➜ color ➜ transparent
            highlightPaint.shader = android.graphics.LinearGradient(
                posStart[0], posStart[1], posEnd[0], posEnd[1],
                intArrayOf(Color.TRANSPARENT, highlightPaint.color, Color.TRANSPARENT),
                floatArrayOf(0f, 0.5f, 1f),
                android.graphics.Shader.TileMode.CLAMP
            )

            canvas.drawPath(highlightPath, highlightPaint)

            // Clear shader after draw to avoid affecting next draw cycle
            highlightPaint.shader = null


        }

        override fun setAlpha(alpha: Int) {
            borderPaint.alpha = alpha
            highlightPaint.alpha = alpha
        }

        override fun setColorFilter(colorFilter: ColorFilter?) {
            borderPaint.colorFilter = colorFilter
            highlightPaint.colorFilter = colorFilter
        }

        @Deprecated(
            "Deprecated in Java",
            ReplaceWith("PixelFormat.TRANSLUCENT", "android.graphics.PixelFormat")
        )
        override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
    }
}