package io.noties.blog.gradientmessenger

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.noties.adapt.Item

class Me(private val message: CharSequence) : Item<Me.Holder>(message.hashCode().toLong()) {

    override fun createHolder(inflater: LayoutInflater, parent: ViewGroup): Holder {
        return Holder(inflater.inflate(R.layout.item_me, parent, false))
    }

    override fun render(holder: Holder) {
        holder.textView.text = message
    }

    class Holder(view: View) : Item.Holder(view) {
        val textView = requireView<TextView>(R.id.text)
    }
}

class You(private val message: CharSequence) : Item<You.Holder>(message.hashCode().toLong()) {

    override fun createHolder(inflater: LayoutInflater, parent: ViewGroup): Holder {
        return Holder(inflater.inflate(R.layout.item_you, parent, false))
    }

    override fun render(holder: Holder) {
        holder.textView.text = message
    }

    override fun recyclerDecoration(recyclerView: RecyclerView): RecyclerView.ItemDecoration? {
        return YouMessageDecoration(recyclerView, viewType())
    }

    class Holder(view: View) : Item.Holder(view) {
        val textView = requireView<TextView>(R.id.text)
    }
}