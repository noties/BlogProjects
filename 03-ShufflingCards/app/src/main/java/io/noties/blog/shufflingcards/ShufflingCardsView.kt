package io.noties.blog.shufflingcards

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import io.noties.tumbleweed.Timeline
import io.noties.tumbleweed.Tween
import io.noties.tumbleweed.TweenCallback
import io.noties.tumbleweed.TweenDef
import io.noties.tumbleweed.android.ViewTweenManager
import io.noties.tumbleweed.android.kt.toFloatSeconds
import io.noties.tumbleweed.android.kt.tweenManager
import io.noties.tumbleweed.android.kt.whenReady
import io.noties.tumbleweed.android.types.Rotation
import io.noties.tumbleweed.android.types.Translation
import io.noties.tumbleweed.equations.Bounce

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
class ShufflingCardsView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    // when view is shown - animate when ready -> not 1000 delay
    // 
    // check the tween-manager... we use extension method in another extension method and
    //  it _might_ be that outer extension method uses View #tweenManager ShufflingCardsView's one

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
//        cardViews[INDEX_BACK].animateChildRotation(7f)
//        cardViews[INDEX_MIDDLE].animateChildRotation(-7f)
//        // OBSCURED matches FRONT to ensure that it's obscured when the color is suddenly set
//        cardViews[INDEX_OBSCURED].animateChildRotation(0f)
//        cardViews[INDEX_FRONT].animateChildRotation(0f)

        val duration = 300.toFloatSeconds()
//
//        Timeline.createParallel()
//                .push(cardRotationTween(cardViews[INDEX_BACK], 7F))
//                .push(cardRotationTween(cardViews[INDEX_MIDDLE], -7F))
//                .push(cardRotationTween(cardViews[INDEX_OBSCURED], 0F))
//                .push(cardRotationTween(cardViews[INDEX_FRONT], 0F))
//                .start(tweenManager(ViewTweenManager.KILL_ALL))
//
        Timeline.createParallel()
                .push(cardViews[INDEX_BACK].rotation(7F, duration))
                .push(cardViews[INDEX_MIDDLE].rotation(-7F, duration))
                .push(cardViews[INDEX_OBSCURED].rotation(0F, duration))
                .push(cardViews[INDEX_FRONT].rotation(0F, duration))
                .start(tweenManager(ViewTweenManager.KILL_ALL))
    }

//    private fun cardRotationTween(child: View, rotation: Float) =
//            Tween.to(child, Rotation.I, 0.3F).target(rotation)

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

//        if (cardDesigns.size == 2) {
//            fanTwoCardsOut()
//        } else {
//            fanThreeCardsOut()
//        }
        fanThreeCardsOut()
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
//        animate()
//                .rotation(45f)
//                .translationXBy(45.dp.toFloat())
//                .translationYBy(-120.dp.toFloat())
//                .setDuration(200)
////                .setInterpolator(FastOutSlowInInterpolator())
//                .setInterpolator(AccelerateDecelerateInterpolator())
//                .withEndAction { doOnEnd() }
//                .start()
//
        val duration = 200.toFloatSeconds()

        Timeline.createParallel()
//                .push(Tween.to(this, Rotation.I, duration).target(45F))
//                .push(Tween.to(this, Translation.XY, duration).target(45.dp.toFloat(), -120.dp.toFloat()))
                .push(rotation(45F, duration))
                .push(translationXY(45.dp.toFloat(), -120.dp.toFloat(), duration))
                .addCallback(TweenCallback.END) { _, _ -> doOnEnd() }
                .start(tweenManager(ViewTweenManager.KILL_ALL))
    }

    private fun View.animateCardFromPeakOutToIn() {
//        animate()
//                .rotation(0f)
//                .translationX(0f)
//                .translationY(0f)
//                .setDuration(300)
////                .setInterpolator(FastOutSlowInInterpolator())
//                .setInterpolator(AccelerateDecelerateInterpolator())
//                .start()

        val duration = 300.toFloatSeconds()

        Timeline.createParallel()
//                .push(Tween.to(this, Rotation.I, duration).target(0F))
//                .push(Tween.to(this, Translation.XY, duration).target(0F, 0F))
                .push(rotation(0F, duration))
                .push(translationXY(0F, 0F, duration))
                .start(tweenManager(ViewTweenManager.KILL_ALL))
    }

//    private fun fanTwoCardsOut() {
//        cardViews[INDEX_MIDDLE].animate()
//                .rotation(-17f)
//                .translationYBy(-20.dp.toFloat())
//                .startSpread()
//
//        cardViews[INDEX_FRONT].animate()
//                .rotation(17f)
//                .translationYBy(20.dp.toFloat())
//                .startSpread()
//    }

    private fun fanThreeCardsOut() {
//        cardViews[INDEX_BACK].animate()
//                .rotation(-17f)
//                .translationXBy(-12.dp.toFloat())
//                .translationYBy(-60.dp.toFloat())
//                .startSpread()
//
//        cardViews[INDEX_MIDDLE].animate()
//                .translationXBy(4.dp.toFloat())
//                .startSpread()
//
//        cardViews[INDEX_FRONT].animate()
//                .rotation(17f)
//                .translationYBy(60.dp.toFloat())
//                .startSpread()
//
        whenReady {

            val duration = 600.toFloatSeconds()
            val ease = Bounce.OUT

            Timeline.createParallel()
//                    .push(Tween.to(cardViews[INDEX_BACK], Rotation.I, duration).target(-17F).ease(ease))
//                    .push(Tween.to(cardViews[INDEX_BACK], Translation.XY, duration).target(-12.dp.toFloat(), -60.dp.toFloat()).ease(ease))
//                    .push(Tween.to(cardViews[INDEX_MIDDLE], Translation.X, duration).target(4.dp.toFloat()).ease(ease))
//                    .push(Tween.to(cardViews[INDEX_FRONT], Rotation.I, duration).target(17F).ease(ease))
//                    .push(Tween.to(cardViews[INDEX_FRONT], Translation.Y, duration).target(60.dp.toFloat()).ease(ease))
                    .push(cardViews[INDEX_BACK].rotation(-17F, duration).ease(ease))
                    .push(cardViews[INDEX_BACK].translationXY(-12.dp.toFloat(), -60.dp.toFloat(), duration).ease(ease))
                    .push(cardViews[INDEX_MIDDLE].translationX(4.dp.toFloat(), duration).ease(ease))
                    .push(cardViews[INDEX_FRONT].rotation(17F, duration).ease(ease))
                    .push(cardViews[INDEX_FRONT].translationY(60.dp.toFloat(), duration).ease(ease))
                    .delay(1F)
                    .start(tweenManager(ViewTweenManager.KILL_ALL))
        }

        /*
                setDuration(600)
                .setStartDelay(1000)
                .setInterpolator(OvershootInterpolator(3f))
                .start()
         */
    }

    private fun collateCardsAndThen(doOnEnd: () -> Unit) {
        resetInternalRotationToMatchCardOrder()

//        cardViews[INDEX_BACK].animate().withEndAction { doOnEnd() }.startUnspread()
//        cardViews[INDEX_MIDDLE].animate().startUnspread()
//        cardViews[INDEX_FRONT].animate().startUnspread()

        Timeline.createParallel()
//                .push(cardUnspreadTween(cardViews[INDEX_BACK]))
//                .push(cardUnspreadTween(cardViews[INDEX_MIDDLE]))
//                .push(cardUnspreadTween(cardViews[INDEX_FRONT]))
                .push(cardViews[INDEX_BACK].unspread())
                .push(cardViews[INDEX_MIDDLE].unspread())
                .push(cardViews[INDEX_FRONT].unspread())
                .addCallback(TweenCallback.END) { _, _ -> doOnEnd() }
                .start(tweenManager(ViewTweenManager.KILL_ALL))
    }

//    private fun cardUnspreadTween(child: View) = Timeline.createParallel()
//            .push(Tween.to(child, Rotation.I, 600.toFloatSeconds()).target(0F))
//            .push(Tween.to(child, Translation.XY, 600.toFloatSeconds()).target(0F, 0F))

//    private fun ViewPropertyAnimator.startSpread() {
//        setDuration(600)
//                .setStartDelay(1000)
//                .setInterpolator(OvershootInterpolator(3f))
//                .start()
//    }

//    private fun ViewPropertyAnimator.startUnspread() {
//        setDuration(600)
//                .setStartDelay(0)
////                .setInterpolator(FastOutSlowInInterpolator())
//                .setInterpolator(AccelerateDecelerateInterpolator())
//                .rotation(0f)
//                .translationX(0f)
//                .translationY(0f)
//                .start()
//    }

    // _core_ tweens
    private fun View.rotation(value: Float, duration: Float): TweenDef<View> =
            Tween.to(this, Rotation.I, duration).target(value)

    private fun View.translationX(value: Float, duration: Float): TweenDef<View> =
            Tween.to(this, Translation.X, duration).target(value)

    private fun View.translationY(value: Float, duration: Float): TweenDef<View> =
            Tween.to(this, Translation.Y, duration).target(value)

    private fun View.translationXY(x: Float, y: Float, duration: Float): TweenDef<View> =
            Tween.to(this, Translation.XY, duration).target(x, y)

    // custom _composite_ tweens
    private fun View.unspread() = Timeline.createParallel()
            .push(rotation(0F, 0.6F))
            .push(translationXY(0F, 0F, 0.6F))

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density + 0.5F).toInt()
}

