package io.noties.blog.shufflingcards

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewPropertyAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView

private const val INDEX_OBSCURED = 0
private const val INDEX_BACK = 1
private const val INDEX_MIDDLE = 2
private const val INDEX_FRONT = 3
private const val VISIBLE_CARDS_COUNT = 3

/**
 * Displays a "deck" of 2 or more Monzo cards. Use [setCards] to pass a list of (at least 2) [CardDesign]s.
 *
 * At most 3 cards will be shown in any settled state. If the selected card is not one of these 3, then you'll be able
 * to see a 4th card temporarily while it's animating from the back of the deck to the front.
 */
class ShufflingCardsView2(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    // when view is shown - animate when ready -> not 1000 delay

    private val cardViews = mutableListOf<CardView>()
    private var cardsAreFannedOut = false

    init {
        clipChildren = false
        val inflater = LayoutInflater.from(context)
        for (i in INDEX_OBSCURED..INDEX_FRONT) {
            val view = inflater.inflate(R.layout.view_card, this, false)
            cardViews.add(view as CardView)
            addView(view)
        }
        resetZIndexToMatchCardOrder()
    }

    /**
     * z-index matches the order of the cardViews in the list
     */
    private fun resetZIndexToMatchCardOrder() {
        for (i in INDEX_OBSCURED..INDEX_FRONT) {
            cardViews[i].z = i.toFloat()
        }
    }

    private fun resetInternalRotationToMatchCardOrder() {
        cardViews[INDEX_BACK].animateChildRotation(7f)
        cardViews[INDEX_MIDDLE].animateChildRotation(-7f)
        // OBSCURED matches FRONT to ensure that it's obscured when the color is suddenly set
        cardViews[INDEX_OBSCURED].animateChildRotation(0f)
        cardViews[INDEX_FRONT].animateChildRotation(0f)
    }

    fun setCards(cardDesigns: List<CardDesign>) {
        if (cardDesigns.isEmpty() || cardDesigns.size == 1) {
            throw IllegalArgumentException("Need at least 2 card(s), you provided: $cardDesigns")
        }

        // clear the colors from all the cardViews
        cardViews.forEach { it.setCard(null) }

        val visibleRectangles = cardDesigns.take(VISIBLE_CARDS_COUNT)
        visibleRectangles.forEachIndexed { index, rectangle ->
            // want the ones at the top of the list to be at the front of the `cardViews` "stack",
            // where a higher index indicates an elements is in front of another
            cardViews[VISIBLE_CARDS_COUNT - index].setCard(rectangle)
        }

        if (cardDesigns.size == 2) {
            fanTwoCardsOut()
        } else {
            fanThreeCardsOut()
        }
        cardsAreFannedOut = true
    }

    fun moveToFront(cardDesign: CardDesign) {
        if (cardsAreFannedOut) {
            collateCardsAndThen { moveSelectedCardToFront(cardDesign) }
            cardsAreFannedOut = false
        } else {
            moveSelectedCardToFront(cardDesign)
        }
    }

    private fun moveSelectedCardToFront(cardDesign: CardDesign) {

        if (cardViews[INDEX_FRONT].represents(cardDesign)) {
            return
        }

        val cardView = cardViews.find { it.represents(cardDesign) }
        // cardDesign isn’t a visible card - let’s set the color on the obscured card view and use that
                ?: cardViews[INDEX_OBSCURED].apply { setCard(cardDesign) }

        // we change the pivot so that the rotations are made around the correct fulcrum
        cardView.pivotX = cardView.width.toFloat()
        cardView.pivotY = cardView.height.toFloat()

        cardView.animateCardOutToPeakAndThen {

            // update list order, then recalculate the z-index and rotations
            cardViews.remove(cardView)
            cardViews.add(cardView)

            resetZIndexToMatchCardOrder()
            resetInternalRotationToMatchCardOrder()

            cardView.animateCardFromPeakOutToIn()
        }

    }

    private fun View.animateCardOutToPeakAndThen(doOnEnd: () -> Unit) {
        animate()
                .rotation(45f)
                .translationXBy(45.dp.toFloat())
                .translationYBy(-120.dp.toFloat())
                .setDuration(200)
//                .setInterpolator(FastOutSlowInInterpolator())
                .setInterpolator(AccelerateDecelerateInterpolator())
                .withEndAction { doOnEnd() }
                .start()
    }

    private fun View.animateCardFromPeakOutToIn() {
        animate()
                .rotation(0f)
                .translationX(0f)
                .translationY(0f)
                .setDuration(300)
//                .setInterpolator(FastOutSlowInInterpolator())
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
    }

    private fun fanTwoCardsOut() {
        cardViews[INDEX_MIDDLE].animate()
                .rotation(-17f)
                .translationYBy(-20.dp.toFloat())
                .startSpread()

        cardViews[INDEX_FRONT].animate()
                .rotation(17f)
                .translationYBy(20.dp.toFloat())
                .startSpread()
    }

    private fun fanThreeCardsOut() {
        cardViews[INDEX_BACK].animate()
                .rotation(-17f)
                .translationXBy(-12.dp.toFloat())
                .translationYBy(-60.dp.toFloat())
                .startSpread()

        cardViews[INDEX_MIDDLE].animate()
                .translationXBy(4.dp.toFloat())
                .startSpread()

        cardViews[INDEX_FRONT].animate()
                .rotation(17f)
                .translationYBy(60.dp.toFloat())
                .startSpread()
    }

    private fun collateCardsAndThen(doOnEnd: () -> Unit) {
        resetInternalRotationToMatchCardOrder()

        cardViews[INDEX_BACK].animate().withEndAction { doOnEnd() }.startUnspread()
        cardViews[INDEX_MIDDLE].animate().startUnspread()
        cardViews[INDEX_FRONT].animate().startUnspread()
    }

    private fun ViewPropertyAnimator.startSpread() {
        setDuration(600)
                .setStartDelay(1000)
                .setInterpolator(OvershootInterpolator(3f))
                .start()
    }

    private fun ViewPropertyAnimator.startUnspread() {
        setDuration(600)
                .setStartDelay(0)
//                .setInterpolator(FastOutSlowInInterpolator())
                .setInterpolator(AccelerateDecelerateInterpolator())
                .rotation(0f)
                .translationX(0f)
                .translationY(0f)
                .start()
    }

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density + 0.5F).toInt()
}

//internal class CardView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
//
//    private var cardDesign: CardDesign? = null
//
//    private val imageView: ImageView = ImageView(context).apply {
//        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
//        addView(this)
//    }
//
//    fun represents(cardDesign: CardDesign) = cardDesign == this.cardDesign
//
//    fun setCard(cardDesign: CardDesign?) {
//        this.cardDesign = cardDesign
//        if (cardDesign != null) {
//            val selectedCardRes = when (cardDesign) {
//                CardDesign.PLUS_HOT_CORAL -> R.drawable.img_card_hot_coral
//                CardDesign.PLUS_LAGOON_BLUE -> R.drawable.img_card_lagoon_blue
//                CardDesign.PLUS_MIDNIGHT_SKY -> R.drawable.img_card_midnight_sky
//                // ...
//            }
//            imageView.setImageResource(selectedCardRes)
//        } else {
//            imageView.setImageDrawable(null)
//        }
//    }
//
//    fun animateChildRotation(rotation: Float) {
//        imageView.animate()
//                .setDuration(300)
////                .setInterpolator(FastOutSlowInInterpolator())
//                .setInterpolator(AccelerateDecelerateInterpolator())
//                .rotation(rotation)
//                .start()
//    }
//}
//
//enum class CardDesign {
//    PLUS_HOT_CORAL,
//    PLUS_LAGOON_BLUE,
//    PLUS_MIDNIGHT_SKY
//}
