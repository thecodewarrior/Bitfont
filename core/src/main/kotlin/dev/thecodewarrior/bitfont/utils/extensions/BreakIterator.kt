@file:JvmName("BreakIteratorUtils")
package dev.thecodewarrior.bitfont.utils.extensions

import com.ibm.icu.text.BreakIterator
import java.util.EnumMap
import kotlin.concurrent.getOrSet

private val characterBreakIteratorLocal = ThreadLocal<BreakIterator>()
internal val characterBreakIterator: BreakIterator
    get() = characterBreakIteratorLocal.getOrSet { BreakIterator.getCharacterInstance() }

private val titleBreakIteratorLocal = ThreadLocal<BreakIterator>()
internal val titleBreakIterator: BreakIterator
    get() = titleBreakIteratorLocal.getOrSet { BreakIterator.getTitleInstance() }

private val wordBreakIteratorLocal = ThreadLocal<BreakIterator>()
internal val wordBreakIterator: BreakIterator
    get() = wordBreakIteratorLocal.getOrSet { BreakIterator.getWordInstance() }

private val sentenceBreakIteratorLocal = ThreadLocal<BreakIterator>()
internal val sentenceBreakIterator: BreakIterator
    get() = sentenceBreakIteratorLocal.getOrSet { BreakIterator.getSentenceInstance() }

private val lineBreakIteratorLocal = ThreadLocal<BreakIterator>()
internal val lineBreakIterator: BreakIterator
    get() = lineBreakIteratorLocal.getOrSet { BreakIterator.getLineInstance() }

private val breakIterators = EnumMap<BreakType, ThreadLocal<BreakIterator>>(
    BreakType.values().associate { it to ThreadLocal<BreakIterator>() }
)

enum class BreakType(private val constructor: () -> BreakIterator) {
    CHARACTER({ BreakIterator.getCharacterInstance() }),
    TITLE({ BreakIterator.getTitleInstance() }),
    WORD({ BreakIterator.getWordInstance() }),
    SENTENCE({ BreakIterator.getSentenceInstance() }),
    LINE({ BreakIterator.getLineInstance() });

    fun get(): BreakIterator {
        return breakIterators.getValue(this).getOrSet(constructor)
    }
}
