package com.example.proyectopenka

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.view.animation.LinearInterpolator

class ScrollingBackgroundDrawable(context: Context) : Drawable() {

    private val paint = Paint(Paint.FILTER_BITMAP_FLAG)
    private val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.tilebg)
    private var offsetX = 0f
    private var offsetY = 0f

    private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 50000L
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener {
            val progress = it.animatedValue as Float
            offsetX = progress * bitmap.width * 3
            offsetY = progress * bitmap.width
            invalidateSelf()
        }
    }

    override fun draw(canvas: Canvas) {
        val b = bounds
        val bh = bitmap.height.toFloat()
        val bw = bitmap.width.toFloat()

        val startX = -(offsetX % bw)
        val startY = -(offsetY % bh)

        var y = startY - bh
        while (y < b.height() + bh) {
            var x = startX - bw
            while (x < b.width() + bw) {
                canvas.drawBitmap(bitmap, x, y, paint)
                x += bw
            }
            y += bh
        }
    }

    override fun setAlpha(alpha: Int) { paint.alpha = alpha }
    override fun setColorFilter(cf: ColorFilter?) { paint.colorFilter = cf }
    override fun getOpacity() = PixelFormat.OPAQUE

    fun start() = animator.start()
    fun stop() = animator.cancel()
}
