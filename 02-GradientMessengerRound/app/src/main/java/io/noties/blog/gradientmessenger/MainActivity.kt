package io.noties.blog.gradientmessenger

import android.app.Activity
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.noties.adapt.Adapt
import io.noties.adapt.Item
import io.noties.debug.AndroidLogDebugOutput
import io.noties.debug.Debug
import java.util.Random

class MainActivity : Activity() {

    companion object {
        init {
            Debug.init(AndroidLogDebugOutput(true))
        }
    }

    private val fakeMessage = FakeMessage(Random(43L))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // create adapt instance
        // * if diff-util is used -> then decorations are not updated, so there will be gaps between
        // items that should not be. Using regular data-set-changed handler seems to work with that better
//        val adapt = Adapt.create(DiffUtilDataSetChanged.create(true))
        val adapt = Adapt.create()

        // initialize RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view).apply {

            layoutManager = LinearLayoutManager(this@MainActivity)
            setHasFixedSize(true)
            adapter = adapt

            val r = resources
            val config = Config(
                    r.getDimensionPixelSize(R.dimen.message_grouped_vertical_padding),
                    r.getDimensionPixelSize(R.dimen.message_regular_vertical_padding),
                    r.getDimensionPixelSize(R.dimen.message_grouped_corner_radius),
                    r.getDimensionPixelSize(R.dimen.message_regular_corner_radius),
                    ColorDrawable(r.getColor(R.color.message_me_background)),
                    GradientDrawable(r.getIntArray(R.array.list_gradient_color))
            )

            addItemDecoration(
                    MessageDecoration(
                            config,
                            Item.generatedViewType(Me::class.java),
                            Item.generatedViewType(You::class.java)
                    )
            )
        }

        findViewById<View>(R.id.app_bar_add).setOnClickListener {

            val items = adapt.currentItems.toMutableList()
                    .apply {
                        // add as the last item
                        this.add(fakeMessage.create())
                    }

            adapt.setItems(items)

            recyclerView.post {
                recyclerView.smoothScrollToPosition(items.size)
            }
        }

        // set items
        adapt.setItems(items())
    }

    private fun items(): List<Item<*>> {
        return 0.until(100)
                .map { fakeMessage.create() }
    }
}
