package io.noties.blog.gradientmessenger

import android.graphics.Canvas
import android.graphics.Path
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.noties.debug.Debug

class YouMessageDecoration(
        recyclerView: RecyclerView,
        private val itemViewType: Int
) : RecyclerView.ItemDecoration() {

    private val drawable: Drawable

//    private val path = Path()

    private val timer = Timer(256)

    init {
        drawable = GradientDrawable(recyclerView.resources.getIntArray(R.array.list_gradient_color))
                .also { initDrawableBounds(recyclerView, it) }
    }

//    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) = timer.step {

//        // a range of integers representing view indexes
//        0.until(parent.childCount)
//                // convert index to view
//                .map { parent.getChildAt(it) }
//                // convert to Holder (if found)
//                .mapNotNull { parent.findContainingViewHolder(it) }
//                // keep only holders of You type
//                .filter { it.itemViewType == itemViewType }
//                // cast left holders (at this point they must be of our type)
//                .map { it as You.Holder }
//                .forEach {
//
//                    // obtain TextView. We cannot use the whole itemView as it fills the parent
//                    val textView = it.textView
//
//                    // it's required for us to have x,y coordinates _relative_ to RecyclerView
//                    val (x, y) = textView.relativeTo(parent)
//
//                    // save current canvas state
//                    with(c.save()) {
//
//                        // clip our TextView, everything else except clipped area will be ignored
//                        c.clipRect(x, y, x + textView.width, y + textView.height)
//
//                        // draw our drawable
//                        drawable.draw(c)
//
//                        // restore canvas state
//                        c.restoreToCount(this)
//                    }
//                }
//    }

//    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) = timer.step {
//
//        // reset before (can still hold values from previous iteration)
////        path.reset()
//        path.rewind()
//
//        // a range of integers representing view indexes
//        0.until(parent.childCount)
//                // convert index to view
//                .map { parent.getChildAt(it) }
//                // convert to Holder (if found)
//                .mapNotNull { parent.findContainingViewHolder(it) }
//                // keep only holders of You type
//                .filter { it.itemViewType == itemViewType }
//                // cast left holders (at this point they must be of our type)
//                .map { it as You.Holder }
//                .forEach {
//
//                    // obtain TextView. We cannot use the whole itemView as it fills the parent
//                    val textView = it.textView
//
//                    // it's required for us to have x,y coordinates _relative_ to RecyclerView
//                    val (x, y) = textView.relativeTo(parent)
//
//                    path.addRect(
//                            x.toFloat(),
//                            y.toFloat(),
//                            x + textView.width.toFloat(),
//                            y + textView.height.toFloat(),
//                            Path.Direction.CCW)
//                }
//
//        // let's check if our path is not empty
//        if (!path.isEmpty) {
//            with(c.save()) {
//                c.clipPath(path)
//                drawable.draw(c)
//                c.restoreToCount(this)
//            }
//        }
//    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) = timer.step {

        var view: View
        var holder: RecyclerView.ViewHolder

        // wat? a for loop?
        for (i in 0 until parent.childCount) {

            view = parent.getChildAt(i)
            holder = parent.findContainingViewHolder(view) ?: continue

            if (itemViewType == holder.itemViewType) {

                val textView = (holder as You.Holder).textView

                val (x, y) = textView.relativeTo(parent)

                c.withSave {
                    clipRect(x, y, x + textView.width, y + textView.height)
                    drawable.draw(this)
                }
            }
        }
    }

    private companion object {

        // as we are dealing with View, which is implicitly single-threaded, we can
        // reuse this value for all calculations
        private val _POINT = Point()

        // we must listen for supplied view layout events to change gradient bounds
        // as gradient drawable won't be actually added to any view, but will be
        // used directly by this item decoration
        private fun initDrawableBounds(view: View, drawable: Drawable) {
            view.viewTreeObserver.addOnGlobalLayoutListener {
                drawable.setBounds(0, 0, view.width, view.height)
            }
        }

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

        // additionally, let's define _destruction_ operators for Point,
        // so we can do `val (x, y) = point`
        private operator fun Point.component1() = this.x

        private operator fun Point.component2() = this.y

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
}