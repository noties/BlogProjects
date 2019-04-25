package io.noties.blog.gradientmessenger

import android.graphics.*
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.recyclerview.widget.RecyclerView
import io.noties.debug.Debug

// we need configuration:
// * actual drawable that we will use from You message
// * both itemViewTypes for Me & You messages
// * background for Me message
// * regular padding
// * grouped padding (when multiple messages are grouped by type)
// * corner radius

class Config(
        @Px val groupedPadding: Int,
        @Px val regularPadding: Int,
        @Px val groupedCornerRadius: Int,
        @Px val regularCornerRadius: Int,
        @ColorInt val meBackgroundColor: Int,
        val youBackgroundDrawable: Drawable)

class MessageDecoration(
        private val config: Config,
        private val meItemViewType: Int,
        private val youItemViewType: Int
) : RecyclerView.ItemDecoration() {

//    private val drawable: Drawable

    private val path = Path()
    private val rectF = RectF()

    private val groupedCornerRadiusF = config.groupedCornerRadius.toFloat()
    private val regularCornerRadiusF = config.regularCornerRadius.toFloat()

//    private val timer = Timer(256)

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {

        // we need adapter to check for neighbor items
        val adapter = parent.adapter ?: return

        // ensure drawable bounds
        // previously we were listening for RecyclerView onGlobalLayout events,
        // but as drawable bounds are lazy (aka if they haven't changed nothing will be triggered)
        config.youBackgroundDrawable.setBounds(0, 0, parent.width, parent.height)

        var view: View
        var holder: RecyclerView.ViewHolder
        var itemViewType: Int
        var position: Int

        var previousItemTheSameType: Boolean
        var nextItemTheSameType: Boolean

        for (i in 0 until parent.childCount) {

            view = parent.getChildAt(i)
            holder = parent.findContainingViewHolder(view) ?: continue
            itemViewType = holder.itemViewType

            if (itemViewType != meItemViewType && itemViewType != youItemViewType) {
                continue
            }

            val textView = (holder as TextViewHolder).textView

            // it's required for us to have x,y coordinates _relative_ to RecyclerView
            // convert to floats
            val (x, y) = textView.relativeTo(parent)
                    .let { Pair(it.x.toFloat(), it.y.toFloat()) }

            position = holder.adapterPosition

            // now, check if we have previous item of our type
            // then check if next one is of our type

            // should apply rounding to top
            previousItemTheSameType = position > 0
                    && adapter.getItemViewType(position - 1) == itemViewType

            // should apply rounding to bottom
            nextItemTheSameType = position < (adapter.itemCount - 1)
                    && adapter.getItemViewType(position + 1) == itemViewType

            // reset path
            path.rewind()

            // okay, each item has different far-end
            // for Me -> right
            // for You -> left
            // far-end is always rounded with regular padding

            rectF.set(x, y, x + textView.width, y + textView.height)

            if (!previousItemTheSameType && !nextItemTheSameType) {

                // just a regular rounded rect for all corners
                path.addRoundRect(rectF, regularCornerRadiusF, regularCornerRadiusF, Path.Direction.CCW)

            } else {

                // move path to {x, half-height}, as our method to round corners draws line
                // and to do so we must initialize position of path (first corner to draw
                // will be LEFT_TOP, so we must place path _somewhere_ in-between LEFT_TOP & LEFT_BOTTOM)
                path.moveTo(rectF.left, rectF.top + (rectF.height() / 2))

                // also, we could evaluate layout direction and swap far-ends for RTL
                val corners = if (itemViewType == meItemViewType) {
                    Corners(
                            previousItemTheSameType.elvis(groupedCornerRadiusF, regularCornerRadiusF),
                            regularCornerRadiusF,
                            regularCornerRadiusF,
                            nextItemTheSameType.elvis(groupedCornerRadiusF, regularCornerRadiusF))
                } else {
                    Corners(
                            regularCornerRadiusF,
                            previousItemTheSameType.elvis(groupedCornerRadiusF, regularCornerRadiusF),
                            nextItemTheSameType.elvis(groupedCornerRadiusF, regularCornerRadiusF),
                            regularCornerRadiusF)
                }

                path.apply {
                    roundCorner(Corner.LEFT_TOP, corners.leftTop, rectF)
                    roundCorner(Corner.TOP_RIGHT, corners.topRight, rectF)
                    roundCorner(Corner.BOTTOM_RIGHT, corners.bottomRight, rectF)
                    roundCorner(Corner.LEFT_BOTTOM, corners.leftBottom, rectF)
                }

                // although it's not required, let's still close the path
                path.close()
            }

            c.withSave {

                c.clipPath(path)

                if (itemViewType == meItemViewType) {
                    c.drawColor(config.meBackgroundColor)
                } else {
                    config.youBackgroundDrawable.draw(c)
                }
            }
        }
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {

        // clear offsets first
        outRect.set(0, 0, 0, 0)

        val adapter = parent.adapter ?: return
        val holder = parent.findContainingViewHolder(view) ?: return

        val itemViewType = holder.itemViewType
        if (itemViewType != meItemViewType && itemViewType != youItemViewType) {
            return
        }

        // if previous the same -> grouped padding-top else regular
        // if next the same -> grouped padding-bottom else regular

        // we will use adapter position to detect next & previous items
        // (they can be absent from layout at this point)
        val position = holder.adapterPosition

        if (position > 0 && itemViewType == adapter.getItemViewType(position - 1)) {
            outRect.top = config.groupedPadding
        } else {
            outRect.top = config.regularPadding
        }

        if (position < (adapter.itemCount) - 1 && itemViewType == adapter.getItemViewType(position + 1)) {
            outRect.bottom = config.groupedPadding
        } else {
            outRect.bottom = config.regularPadding
        }
    }

    private companion object {

        // as we are dealing with View, which is implicitly single-threaded, we can
        // reuse this value for all calculations
        private val _POINT = Point()
        private val _RECT_F = RectF()

        private fun View.relativeTo(group: ViewGroup): Point {

            val point = _POINT

            // as we are reusing point instance between multiple calls, it's important to clear
            // previous values with our current ones
            point.set(this.left, this.top)

            var parent = this.parent
            var view: View?

            while (parent != null && parent != group) {
                view = parent as View
                point.x += view.left
                point.y += view.top
                parent = view.parent
            }

            return point
        }

        private inline fun Canvas.withSave(action: Canvas.() -> Unit) {
            val save = this.save()
            try {
                action()
            } finally {
                this.restoreToCount(save)
            }
        }
    }

    private class Timer(private val count: Int) {

        fun step(action: () -> Unit) {
            val start = System.nanoTime()
            try {
                action()
            } finally {
                val end = System.nanoTime()
                steps.add(end - start)
                if (steps.size == count) {
                    val min = steps.min()
                    val max = steps.max()
                    val avg = steps.average()
                    Debug.i("min: %d, max: %d, avg: %s", min, max, avg)
                }
            }
        }

        private val steps = mutableListOf<Long>()
    }

    private enum class Corner {
        LEFT_TOP,
        TOP_RIGHT,
        BOTTOM_RIGHT,
        LEFT_BOTTOM
    }

    private class Corners(
            val leftTop: Float,
            val topRight: Float,
            val bottomRight: Float,
            val leftBottom: Float)

    private fun <T> Boolean.elvis(left: T, right: T) = if (this) left else right

    // Please note that his method assumes the movement LEFT->TOP->RIGHT->BOTTOM
    private fun Path.roundCorner(corner: Corner, radius: Float, bounds: RectF) {

        val rectF = _RECT_F
        val diameter = radius * 2

        when (corner) {

            Corner.LEFT_TOP -> {

                // |x| | |
                // | | | |
                rectF.set(
                        bounds.left,
                        bounds.top,
                        bounds.left + diameter,
                        bounds.top + diameter)

                this.lineTo(bounds.left, bounds.top + radius)
                this.arcTo(rectF, 180.0F, 90.0F)
            }

            Corner.TOP_RIGHT -> {

                // | | |x|
                // | | | |
                rectF.set(
                        bounds.right - diameter,
                        bounds.top,
                        bounds.right,
                        bounds.top + diameter
                )

                this.lineTo(bounds.right - radius, bounds.top)
                this.arcTo(rectF, 270.0F, 90.0F)
            }

            Corner.BOTTOM_RIGHT -> {

                // | | | |
                // | | |x|
                rectF.set(
                        bounds.right - diameter,
                        bounds.bottom - diameter,
                        bounds.right,
                        bounds.bottom)

                this.lineTo(bounds.right, bounds.bottom - radius)
                this.arcTo(rectF, 0.0F, 90.0F)
            }

            Corner.LEFT_BOTTOM -> {

                // | | | |
                // |x| | |
                rectF.set(
                        bounds.left,
                        bounds.bottom - diameter,
                        bounds.left + diameter,
                        bounds.bottom)

                this.lineTo(bounds.right - radius, bounds.bottom)
                this.arcTo(rectF, 90.0F, 90.0F)
            }
        }
    }
}