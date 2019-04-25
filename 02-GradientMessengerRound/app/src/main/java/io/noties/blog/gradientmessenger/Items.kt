package io.noties.blog.gradientmessenger

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.noties.adapt.Item

class Me(private val message: CharSequence) : Item<Me.Holder>(message.hashCode().toLong()) {

    override fun createHolder(inflater: LayoutInflater, parent: ViewGroup): Holder {
        return Holder(inflater.inflate(R.layout.item_me, parent, false))
    }

    override fun render(holder: Holder) {
        holder.textView.text = message
    }

    class Holder(view: View) : Item.Holder(view), TextViewHolder {
        override val textView = requireView<TextView>(R.id.text)
    }
}

class You(private val message: CharSequence) : Item<You.Holder>(message.hashCode().toLong()) {

    override fun createHolder(inflater: LayoutInflater, parent: ViewGroup): Holder {
        return Holder(inflater.inflate(R.layout.item_you, parent, false))
    }

    override fun render(holder: Holder) {
        holder.textView.text = message
    }

    class Holder(view: View) : Item.Holder(view), TextViewHolder {
        override val textView = requireView<TextView>(R.id.text)
    }
}

// we can go with any view, as our decoration actually does not require (yet?) actual TextView,
// but just a generic View to obtain it's position
interface TextViewHolder {
    val textView: TextView
}