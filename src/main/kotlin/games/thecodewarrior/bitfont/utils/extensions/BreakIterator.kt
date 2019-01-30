package games.thecodewarrior.bitfont.utils.extensions

import com.ibm.icu.text.BreakIterator
import kotlin.concurrent.getOrSet

private val characterBreakIteratorLocal = ThreadLocal<BreakIterator>()
val characterBreakIterator: BreakIterator
    get() = characterBreakIteratorLocal.getOrSet { BreakIterator.getCharacterInstance() }

private val titleBreakIteratorLocal = ThreadLocal<BreakIterator>()
val titleBreakIterator: BreakIterator
    get() = titleBreakIteratorLocal.getOrSet { BreakIterator.getTitleInstance() }

private val wordBreakIteratorLocal = ThreadLocal<BreakIterator>()
val wordBreakIterator: BreakIterator
    get() = wordBreakIteratorLocal.getOrSet { BreakIterator.getWordInstance() }

private val sentenceBreakIteratorLocal = ThreadLocal<BreakIterator>()
val sentenceBreakIterator: BreakIterator
    get() = sentenceBreakIteratorLocal.getOrSet { BreakIterator.getSentenceInstance() }

private val lineBreakIteratorLocal = ThreadLocal<BreakIterator>()
val lineBreakIterator: BreakIterator
    get() = lineBreakIteratorLocal.getOrSet { BreakIterator.getLineInstance() }
