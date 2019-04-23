package io.noties.blog.gradientmessenger

import android.app.Activity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.noties.adapt.Adapt
import io.noties.adapt.Item
import ru.noties.debug.AndroidLogDebugOutput
import ru.noties.debug.Debug
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

        // create adapt instance
        val adapt = Adapt.create()

        // initialize RecyclerView
        findViewById<RecyclerView>(R.id.recycler_view).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            setHasFixedSize(true)
            adapter = adapt
//            background = GradientDrawable(resources.getIntArray(R.array.list_gradient_color))
//            addItemDecoration(YouMessageDecoration(
//                    this,
//                    // we can use _default_ generated itemViewType
//                    // as long as we do not specify in explicitly
//                    Item.generatedViewType(You::class.java)))
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
