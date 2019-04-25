package io.noties.blog.gradientmessenger

import android.app.Activity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.noties.adapt.Adapt
import io.noties.adapt.Item
import io.noties.debug.AndroidLogDebugOutput
import io.noties.debug.Debug
import java.util.*

class MainActivity : Activity() {

    companion object {
        init {
            Debug.init(AndroidLogDebugOutput(true))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // okay, `item_me` & `item_you` remove backgrounds and vertical padding
        // rename decoration to modify both types
        // add item offsets handling to decoration
        // add padding to recycler (with clipToPadding=false)
        // introduce common holder type for Me & You

        // textColor to config?
        // change me background to drawable also

        // create adapt instance
        val adapt = Adapt.create()

        // initialize RecyclerView
        findViewById<RecyclerView>(R.id.recycler_view).apply {

            layoutManager = LinearLayoutManager(this@MainActivity)
            setHasFixedSize(true)
            adapter = adapt

            val r = resources
            val config = Config(
                    r.getDimensionPixelSize(R.dimen.message_grouped_vertical_padding),
                    r.getDimensionPixelSize(R.dimen.message_regular_vertical_padding),
                    r.getDimensionPixelSize(R.dimen.message_grouped_corner_radius),
                    r.getDimensionPixelSize(R.dimen.message_regular_corner_radius),
                    0xFFdddddd.toInt(),
                    GradientDrawable(r.getIntArray(R.array.list_gradient_color)))

            addItemDecoration(MessageDecoration(
                    config,
                    Item.generatedViewType(Me::class.java),
                    Item.generatedViewType(You::class.java)))
        }

        // set items
        adapt.setItems(items())
    }

    private fun items(): List<Item<*>> {
        val fakeMessage = FakeMessage(Random(43L))
        return 0.until(100)
                .map { fakeMessage.create() }
    }
}
