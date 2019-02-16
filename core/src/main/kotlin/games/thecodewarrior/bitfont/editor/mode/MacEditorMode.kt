package games.thecodewarrior.bitfont.editor.mode

import com.ibm.icu.lang.UCharacter
import com.ibm.icu.text.BreakIterator
import games.thecodewarrior.bitfont.editor.Editor
import games.thecodewarrior.bitfont.editor.Key
import games.thecodewarrior.bitfont.editor.Modifier
import games.thecodewarrior.bitfont.editor.ModifierPattern
import games.thecodewarrior.bitfont.editor.MouseButton
import games.thecodewarrior.bitfont.utils.Vec2i
import games.thecodewarrior.bitfont.utils.extensions.BreakType
import games.thecodewarrior.bitfont.utils.extensions.endExclusive
import games.thecodewarrior.bitfont.utils.extensions.replace
import kotlin.math.abs

class MacEditorMode(editor: Editor): DefaultEditorMode(editor) {

    init {
        val optionalShift = ModifierPattern.optional(Modifier.SHIFT)

        keyAction(Key.BACKSPACE, ::backspace)
        keyAction(Key.DELETE, ::delete)
        keyAction(Key.ENTER, ::enter)

        keyAction(Key.LEFT, optionalShift, { moveBackward(BreakType.CHARACTER) })
        keyAction(Key.RIGHT, optionalShift, { moveForward(BreakType.CHARACTER) })
        keyAction(Key.LEFT, optionalShift.require(Modifier.ALT), { moveTo(prevWordStart(contents.plaintext, cursor)) })
        keyAction(Key.RIGHT, optionalShift.require(Modifier.ALT), { moveTo(nextWordEnd(contents.plaintext, cursor)) })

        keyAction(Key.UP, optionalShift, ::moveUp)
        keyAction(Key.DOWN, optionalShift, ::moveDown)

        mouseAction(MouseButton.LEFT, optionalShift, 1, ::jumpToMouse, {}, ::normalMouseDrag)
        mouseAction(MouseButton.LEFT, optionalShift, 2, ::selectWord, {}, ::dragSelectWord)

        keyAction(Key.V, Modifier.SUPER, ::paste)
        keyAction(Key.C, Modifier.SUPER, ::copy)
        keyAction(Key.X, Modifier.SUPER, ::cut)
    }

    fun nextWordEnd(text: String, pos: Int): Int {
        val wordIter = BreakType.WORD.get()
        wordIter.setText(text)
        val charIter = BreakType.CHARACTER.get()
        charIter.setText(text)

        var wordBreak = wordIter.following(pos)
        while (true) {
            if (wordBreak == BreakIterator.DONE) {
                return text.length
            }
            if(wordIter.ruleStatus != BreakIterator.WORD_NONE) {
                return wordBreak
            }

            wordBreak = wordIter.next()
        }
    }

    fun prevWordStart(text: String, pos: Int): Int {
        val wordIter = BreakType.WORD.get()
        wordIter.setText(text)
        val charIter = BreakType.CHARACTER.get()
        charIter.setText(text)

        wordIter.isBoundary(pos)
        var isInWord = wordIter.ruleStatus != BreakIterator.WORD_NONE

        while (true) {
            val wordBreak = wordIter.previous()
            if (wordBreak == BreakIterator.DONE) {
                return 0
            }
            if(isInWord) {
                return wordBreak
            }
            if(wordIter.ruleStatus != BreakIterator.WORD_NONE) {
                isInWord = true
            }
        }
    }


    var originalWord = 0 until 0
    var originalWordStart = 0
    var originalWordCursor = 0


    fun selectWord() {
        val wordIter = BreakType.WORD.get()
        wordIter.setText(contents.plaintext)
        val underCursor = internals.typesetString.containingCharacter(mousePos)
        val end = wordIter.following(underCursor).replace(BreakIterator.DONE, contents.length)
        val start = wordIter.previous().replace(BreakIterator.DONE, 0)
        originalWord = start until end

        val isStartCloser = cursor - start < end - cursor
        if(isStartCloser) {
            cursor = start
            originalWordCursor = start
            selectionStart = end
            originalWordStart = end
        } else {
            cursor = end
            originalWordCursor = end
            selectionStart = start
            originalWordStart = start
        }
    }

    fun dragSelectWord(pos: Vec2i) {
        val mouseOver = internals.typesetString.containingCharacter(mousePos)
        if(mouseOver > originalWord.endInclusive) {
            val wordIter = BreakType.WORD.get()
            wordIter.setText(contents.plaintext)

            val wordBreak = wordIter.following(mouseOver)
            if (wordBreak == BreakIterator.DONE) {
                cursor = contents.length
                selectionStart = originalWord.start
            } else {
                cursor = wordBreak
                selectionStart = originalWord.start
            }
        } else if(mouseOver < originalWord.start) {
            val wordIter = BreakType.WORD.get()
            wordIter.setText(contents.plaintext)

            val wordBreak = wordIter.preceding(mouseOver)
            if (wordBreak == BreakIterator.DONE) {
                cursor = contents.length
                selectionStart = originalWord.endExclusive
            } else {
                cursor = wordBreak
                selectionStart = originalWord.endExclusive
            }
        } else {
            cursor = originalWordCursor
            selectionStart = originalWordStart
        }
    }
}