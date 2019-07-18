package io.noties.blog.shufflingcards

import android.app.Activity
import android.os.Bundle
import android.view.View

class MainActivity : Activity() {

    // https://medium.com/monzo-bank/pick-a-card-any-card-implementing-the-monzo-plus-card-selection-animation-on-android-536e5622ada9

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val view = findViewById<ShufflingCardsView>(R.id.shuffling_cards)
        view.setCards(CardDesign.values().toList())

        arrayOf(
                Pair(CardDesign.PLUS_MIDNIGHT_SKY, R.id.midnight_sky),
                Pair(CardDesign.PLUS_LAGOON_BLUE, R.id.lagoon_blue),
                Pair(CardDesign.PLUS_HOT_CORAL, R.id.hot_coral)
        ).forEach {
            findViewById<View>(it.second).setOnClickListener { _ ->
                view.moveToFront(it.first)
            }
        }
    }
}
