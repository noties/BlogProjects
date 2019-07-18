package io.noties.blog.gradientmessenger

import android.graphics.*
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Px
import androidx.recyclerview.widget.RecyclerView

// NB, if you are using a drawable from resources, you must mutate it.
class Config(
        @Px val groupedMargin: Int,
        @Px val regularMargin: Int,
        @Px val groupedCornerRadius: Int,
        @Px val regularCornerRadius: Int,
        val meBackgroundDrawable: Drawable,
        val youBackgroundDrawable: Drawable
)

class MessageDecoration(
        private val config: Config,
        private val meItemViewType: Int,
        private val youItemViewType: Int
) : RecyclerView.ItemDecoration() {

    // path for items area (that will be clipped)
    private val path = Path()

    // rect to hold view dimensions
    private val rectF = RectF()

    // we cannot use @Px annotation with Floats, so make an explicit conversion
    private val groupedCornerRadiusF = config.groupedCornerRadius.toFloat()
    private val regularCornerRadiusF = config.regularCornerRadius.toFloat()

//    private val timer = Timer(256)

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {

        // we need adapter to check for neighbor items
        val adapter = parent.adapter ?: return

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

            // process only message items
            if (itemViewType != meItemViewType && itemViewType != youItemViewType) {
                continue
            }

            val textView = (holder as TextViewHolder).textView

            // it's required for us to have x,y coordinates _relative_ to RecyclerView
            // convert to floats
            // apply translationY for item animations (as we know that we operate on a vertical list)
            // if we would operate on a grid then translationX should also be taken into account
            val (x, y) = textView.relativeTo(parent)
//                .let { Pair(it.x.toFloat(), it.y.toFloat() }
                    .let { Pair(it.x.toFloat(), it.y.toFloat() + view.translationY) }

            // position to obtain neighbors
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

            // apply view bounds
            rectF.set(x, y, x + textView.width, y + textView.height)

            // a single message in a group
            if (!previousItemTheSameType && !nextItemTheSameType) {

                // just a regular rounded rect for all corners
                path.addRoundRect(rectF, regularCornerRadiusF, regularCornerRadiusF, Path.Direction.CCW)

            } else {

                if (itemViewType == meItemViewType) {
                    // our extension method
                    path.addRoundRect(
                            rectF,
                            // `ternary` is a simple extension function on Boolean
                            previousItemTheSameType.ternary(groupedCornerRadiusF, regularCornerRadiusF),
                            regularCornerRadiusF,
                            regularCornerRadiusF,
                            nextItemTheSameType.ternary(groupedCornerRadiusF, regularCornerRadiusF))
                } else {
                    // our extension method
                    path.addRoundRect(
                            rectF,
                            regularCornerRadiusF,
                            previousItemTheSameType.ternary(groupedCornerRadiusF, regularCornerRadiusF),
                            nextItemTheSameType.ternary(groupedCornerRadiusF, regularCornerRadiusF),
                            regularCornerRadiusF)
                }

                // although it's not required, let's still close the path
                path.close()
            }

            // draw item
            c.withSave {

                // clip prepared path
                c.clipPath(path)

                // we
                val drawable = if (itemViewType == meItemViewType) {
                    config.meBackgroundDrawable
                } else {
                    config.youBackgroundDrawable
                }

                // ensure drawable bounds
                // previously we were listening for RecyclerView onGlobalLayout events,
                // but as drawable bounds are lazy (it checks if bounds have changed internally)
                // we are safe to set them each time
                drawable.setBounds(0, 0, parent.width, parent.height)

                // calculate alpha that will be applied to items (for appear/disappear animations)
                drawable.alpha = (view.alpha * 255.0F + 0.5F).toInt()

                // draw it
                drawable.draw(c)
            }
        }
    }

    override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State) {

        // clear offsets first
        outRect.set(0, 0, 0, 0)

        // we must have adapter in order to detect neighbors
        val adapter = parent.adapter ?: return
        val holder = parent.findContainingViewHolder(view) ?: return

        val itemViewType = holder.itemViewType

        // we will process only items that we are interested in
        if (itemViewType != meItemViewType && itemViewType != youItemViewType) {
            return
        }

        // if previous the same -> grouped margin-top else regular
        // if next the same -> grouped margin-bottom else regular

        // we will use adapter position to detect next & previous items
        // (they can be absent from layout at this point)
        val position = holder.adapterPosition

        outRect.top = if (position > 0
                && itemViewType == adapter.getItemViewType(position - 1)) {
            config.groupedMargin
        } else {
            config.regularMargin
        }

        outRect.bottom = if (position < (adapter.itemCount) - 1
                && itemViewType == adapter.getItemViewType(position + 1)) {
            config.groupedMargin
        } else {
            config.regularMargin
        }
    }

    private companion object {

        // as we are dealing with View, which is implicitly single-threaded, we can
        // reuse these values for all calculations
        private val POINT = Point()
        private val RECT_F = RectF()

        private fun View.relativeTo(group: ViewGroup): Point {

            val point = POINT

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

        // extension method that _picks_ value. If boolean is true -> left is picked, else right
        private fun <T> Boolean.ternary(left: T, right: T) = if (this) left else right

        private inline fun Canvas.withSave(action: Canvas.() -> Unit) {
            val save = this.save()
            try {
                action()
            } finally {
                this.restoreToCount(save)
            }
        }

        private fun Path.addRoundRect(
                bounds: RectF,
                leftTopRadius: Float,
                topRightRadius: Float,
                bottomRightRadius: Float,
                bottomLeftRadius: Float) {

            // we will be drawing from left-top
            // we must init position to be between left-top & bottom-left (x=0,y=height/2)

            moveTo(bounds.left, bounds.top + (bounds.height() / 2.0F))

            // the same for all corners
            val sweepAngle = 90.0F

            // inner helper function to add an arc starting at [x,y]
            fun arc(
                    rectF: RectF,
                    startAngle: Float,
                    x: Float,
                    y: Float) {
                this.lineTo(x, y)
                this.arcTo(rectF, startAngle, sweepAngle)
            }

            // anonymous lambda w/ immediate execution, please note that semicolon is required
            // after the execution call
            //
            // left-top
            {
                // |x| | |
                // | | | |
                val rectF = RECT_F.apply {
                    val diameter = leftTopRadius * 2.0F
                    set(
                            bounds.left,
                            bounds.top,
                            bounds.left + diameter,
                            bounds.top + diameter)
                }
                arc(rectF, 180.0F, rectF.left, rectF.top + leftTopRadius)
            }();

            // top-right
            {
                // | | |x|
                // | | | |
                val rectF = RECT_F.apply {
                    val diameter = topRightRadius * 2.0F
                    set(
                            bounds.right - diameter,
                            bounds.top,
                            bounds.right,
                            bounds.top + diameter)
                }
                arc(rectF, 270.0F, rectF.left + leftTopRadius, rectF.top)
            }();

            // bottom-right
            {
                // | | | |
                // | | |x|
                val rectF = RECT_F.apply {
                    val diameter = bottomRightRadius * 2.0F
                    set(
                            bounds.right - diameter,
                            bounds.bottom - diameter,
                            bounds.right,
                            bounds.bottom)
                }
                arc(rectF, 0.0F, rectF.right, rectF.top + bottomRightRadius)
            }();

            // bottom-left
            {
                // | | | |
                // |x| | |
                val rectF = RECT_F.apply {
                    val diameter = bottomLeftRadius * 2.0F
                    set(
                            bounds.left,
                            bounds.bottom - diameter,
                            bounds.left + diameter,
                            bounds.bottom)
                }
                arc(rectF, 90.0F, rectF.left + bottomLeftRadius, rectF.bottom)
            }()
        }
    }

//    private class Timer(private val count: Int) {
//
//        fun step(action: () -> Unit) {
//            val start = System.nanoTime()
//            try {
//                action()
//            } finally {
//                val end = System.nanoTime()
//                steps.add(end - start)
//                if (steps.size == count) {
//                    val min = steps.min()
//                    val max = steps.max()
//                    val avg = steps.average()
//                    Debug.i("min: %d, max: %d, avg: %s", min, max, avg)
//                }
//            }
//        }
//
//        private val steps = mutableListOf<Long>()
//    }
}