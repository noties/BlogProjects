package io.noties.blog.emotionlayout

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ScrollView
import androidx.annotation.RequiresApi
import ru.noties.scrollable.ScrollableLayout

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val scrollable = findViewById<ScrollableLayout>(R.id.scrollable_layout)
        val scrollView = findViewById<ScrollView>(R.id.scroll_view)
        val image = findViewById<View>(R.id.image)

        // _hook-up_ ScrollableLayout with our scrolling content
        scrollable.setCanScrollVerticallyDelegate {
            scrollView.canScrollVertically(it)
        }

        val drawingOrder = DrawingOrder.create(image).also { it.init() }

        val interpolator = AccelerateDecelerateInterpolator()

        scrollable.addOnScrollChangedListener { y, _, maxY ->

            // when reached half of possible scroll we _flip_ view order and animate the other way
            val half = maxY / 2F

            if (y <= half) {
                drawingOrder.bringToFront()
            } else {
                drawingOrder.sendToBack()
            }

            val height = image.height

            image.translationY = if (y <= half) {
                // negative translationY (moving to the top of the screen) multiplied by distance ratio
                val ratio = interpolator.getInterpolation(y / half)
                -(height / 2F) * ratio
            } else {
                // initial starting position is -(height / 2F) - which first half is finishing at
                // then we multiple view height (no half of it to faster movement) by distance ratio
                val ratio = interpolator.getInterpolation((y - half) / half)
                -(height / 2F) + (height * ratio)
            }
        }
    }

    interface DrawingOrder {

        fun init()

        fun bringToFront()

        fun sendToBack()

        companion object {
            fun create(view: View): DrawingOrder {
                return if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                    DrawingOrderPre21(view)
                } else {
                    DrawingOrder21(view)
                }
            }
        }

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        private class DrawingOrder21(private val view: View) : DrawingOrder {

            override fun init() {
                val parent = view.parent as ViewGroup
                // initially our view is at front and others behind it
                for (i in 0 until parent.childCount) {
                    parent.getChildAt(i).run {
                        translationZ = if (view == this) 1F else 0F
                    }
                }
            }

            override fun bringToFront() {
                view.translationZ = 1F
            }

            override fun sendToBack() {
                view.translationZ = -1F
            }
        }

        private class DrawingOrderPre21(private val view: View) : DrawingOrder {

            private val parent = view.parent as ViewGroup

            override fun init() {
                if (!isAtFront()) {
                    view.bringToFront()
                }
            }

            override fun bringToFront() {
                if (!isAtFront()) {
                    view.bringToFront()
                }
            }

            override fun sendToBack() {
                if (!isAtBack()) {
                    // small optimization if there are only 2 views in the parent
                    if (parent.childCount == 2) {
                        parent.getChildAt(0).bringToFront()
                    } else {
                        parent.removeView(view)
                        parent.addView(view, 0)
                    }
                }
            }

            private fun isAtFront() = view == parent.getChildAt(parent.childCount - 1)

            private fun isAtBack() = view == parent.getChildAt(0)
        }
    }
}
