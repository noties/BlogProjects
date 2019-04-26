package io.noties.blog.gradientmessenger

import android.graphics.*
import android.graphics.drawable.Drawable

class GradientDrawable(private val colors: IntArray) : Drawable() {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)

        paint.shader = LinearGradient(
                0.0F, 0.0F,
                0.0F, bounds.bottom.toFloat(),
                colors,
                null,
                Shader.TileMode.CLAMP
        )
    }

    override fun draw(canvas: Canvas) {
        canvas.drawRect(bounds, paint)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun getOpacity(): Int = PixelFormat.OPAQUE

    override fun setColorFilter(colorFilter: ColorFilter?) {
    }
}