package io.noties.blog.shufflingcards

import android.content.Context
import android.util.AttributeSet
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView

internal class CardView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    private var cardDesign: CardDesign? = null

    private val imageView: ImageView = ImageView(context).apply {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        addView(this)
    }

    fun represents(cardDesign: CardDesign) = cardDesign == this.cardDesign

    fun setCard(cardDesign: CardDesign?) {
        this.cardDesign = cardDesign
        if (cardDesign != null) {
            val selectedCardRes = when (cardDesign) {
                CardDesign.PLUS_HOT_CORAL -> R.drawable.img_card_hot_coral
                CardDesign.PLUS_LAGOON_BLUE -> R.drawable.img_card_lagoon_blue
                CardDesign.PLUS_MIDNIGHT_SKY -> R.drawable.img_card_midnight_sky
                // ...
            }
            imageView.setImageResource(selectedCardRes)
        } else {
            imageView.setImageDrawable(null)
        }
    }

    fun animateChildRotation(rotation: Float) {
        imageView.animate()
                .setDuration(300)
//                .setInterpolator(FastOutSlowInInterpolator())
                .setInterpolator(AccelerateDecelerateInterpolator())
                .rotation(rotation)
                .start()
    }
}