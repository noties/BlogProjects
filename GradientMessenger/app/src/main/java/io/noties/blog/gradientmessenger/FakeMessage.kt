package io.noties.blog.gradientmessenger

import io.noties.adapt.Item
import java.util.*

class FakeMessage(private val random: Random) {

    private companion object {
        // a set of emojis
        val EMOJIS = listOf(
                String(Character.toChars(0x1F601)),
                String(Character.toChars(0x1F602)),
                String(Character.toChars(0x1F603)),
                String(Character.toChars(0x1F604)),
                String(Character.toChars(0x1F605)),
                String(Character.toChars(0x1F606)),
                String(Character.toChars(0x1F607)))

        const val MAX_WORDS = 19
        const val MAX_CHARS_IN_WORD = 9
    }

    fun create(): Item<*> {

        val words = random.nextInt(MAX_WORDS)
        val builder = StringBuilder()

        if (words > 0) {

            for (word in 0 until words) {

                if (word > 0) {
                    builder.append(' ')
                }

                val length = random.nextInt(MAX_CHARS_IN_WORD) + 1
                val array = CharArray(length)

                for (char in 0 until length) {
                    array[char] = (97 + random.nextInt(26)).toChar()
                }

                builder.append(array)
            }
        }

        // add emoji
        if (words == 0 || random.nextBoolean()) {
            val length = random.nextInt(4) + 1
            for (i in 0 until length) {
                builder.append(EMOJIS[random.nextInt(EMOJIS.size)])
            }
        }

        val message = builder.toString()

        return when (random.nextBoolean()) {
            true -> Me(message)
            else -> You(message)
        }
    }
}