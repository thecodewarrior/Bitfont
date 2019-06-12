package games.thecodewarrior.bitfont.editor.mode

import com.ibm.icu.text.BreakIterator
import games.thecodewarrior.bitfont.editor.Editor
import games.thecodewarrior.bitfont.editor.Key
import games.thecodewarrior.bitfont.editor.Modifier
import games.thecodewarrior.bitfont.editor.ModifierPattern
import games.thecodewarrior.bitfont.editor.MouseButton
import games.thecodewarrior.bitfont.typesetting.TypesetString
import games.thecodewarrior.bitfont.utils.ExperimentalBitfont
import games.thecodewarrior.bitfont.utils.Vec2i
import games.thecodewarrior.bitfont.utils.extensions.BreakType
import games.thecodewarrior.bitfont.utils.extensions.endExclusive
import games.thecodewarrior.bitfont.utils.extensions.replace

@ExperimentalBitfont
class MacEditorMode(editor: Editor): DefaultEditorMode(editor) {

    init {
        val optionalShift = ModifierPattern.optional(Modifier.SHIFT)

        keyAction(Key.H, Modifier.CONTROL, { delete(cursor.index-1) })
        keyAction(Key.BACKSPACE, { delete(cursor.index-1) })
        keyAction(Key.BACKSPACE, Modifier.ALT, { delete(prevWordStart(contents.plaintext, cursor.index)) })

        keyAction(Key.D, Modifier.CONTROL, { delete(cursor.index+1) })
        keyAction(Key.DELETE, { delete(cursor.index+1) })
        keyAction(Key.DELETE, Modifier.ALT, { delete(nextWordEnd(contents.plaintext, cursor.index)) })

        keyAction(Key.K, Modifier.CONTROL, { delete(nextParagraphEnd(contents.plaintext, cursor.index).index) })

        keyAction(Key.ENTER, ::enter)
        keyAction(Key.O, Modifier.CONTROL, {
            val cursor = cursor
            insert("\n")
            resetCursor(cursor)
        })

        keyAction(Key.LEFT, optionalShift, { moveBackward(BreakType.CHARACTER) })
        keyAction(Key.RIGHT, optionalShift, { moveForward(BreakType.CHARACTER) })
        keyAction(Key.B, optionalShift.require(Modifier.CONTROL), { moveBackward(BreakType.CHARACTER) })
        keyAction(Key.F, optionalShift.require(Modifier.CONTROL), { moveForward(BreakType.CHARACTER) })

        keyAction(Key.LEFT, optionalShift.require(Modifier.ALT), { moveTo(CursorPosition(prevWordStart(contents.plaintext, cursor.index), false)) })
        keyAction(Key.RIGHT, optionalShift.require(Modifier.ALT), { moveTo(CursorPosition(nextWordEnd(contents.plaintext, cursor.index), true)) })

        keyAction(Key.A, optionalShift.require(Modifier.CONTROL), {
            if(cursor.index > 0 && contents.plaintext[cursor.index-1] !in TypesetString.newlines)
                moveTo(prevParagraphStart(contents.plaintext, cursor.index))
        })
        keyAction(Key.E, optionalShift.require(Modifier.CONTROL), {
            if(contents.plaintext[cursor.index] !in TypesetString.newlines)
                moveTo(nextParagraphEnd(contents.plaintext, cursor.index))
        })
        keyAction(Key.UP, optionalShift.require(Modifier.ALT), { moveTo(prevParagraphStart(contents.plaintext, cursor.index)) })
        keyAction(Key.DOWN, optionalShift.require(Modifier.ALT), { moveTo(nextParagraphEnd(contents.plaintext, cursor.index)) })

        keyAction(Key.UP, optionalShift, ::moveUp)
        keyAction(Key.DOWN, optionalShift, ::moveDown)
        keyAction(Key.P, optionalShift.require(Modifier.CONTROL), ::moveUp)
        keyAction(Key.N, optionalShift.require(Modifier.CONTROL), ::moveDown)

        keyAction(Key.UP, optionalShift.require(Modifier.SUPER), { moveTo(CursorPosition(0, false)) })
        keyAction(Key.DOWN, optionalShift.require(Modifier.SUPER), { moveTo(CursorPosition(contents.length, true)) })
        keyAction(Key.LEFT, optionalShift.require(Modifier.SUPER), ::moveToLineStart)
        keyAction(Key.RIGHT, optionalShift.require(Modifier.SUPER), ::moveToLineEnd)

        mouseAction(MouseButton.LEFT, optionalShift, 1, ::jumpToMouse, {}, ::normalMouseDrag)
        mouseAction(MouseButton.LEFT, ModifierPattern.NONE, 2, ::selectWord, {}, ::dragSelectWord)
        mouseAction(MouseButton.LEFT, ModifierPattern.NONE, 3, ::selectParagraph, {}, ::dragSelectParagraph)

        keyAction(Key.V, Modifier.SUPER, ::paste)
        keyAction(Key.C, Modifier.SUPER, ::copy)
        keyAction(Key.X, Modifier.SUPER, ::cut)
        keyAction(Key.A, Modifier.SUPER, ::selectAll)
    }

    fun selectAll() {
        cursor = CursorPosition(contents.length, true)
        selectionStart = CursorPosition(0, false)
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


    var originalWord = CursorRange(CursorPosition(0, false), CursorPosition(0, false))
    var originalWordStart = CursorPosition(0, false)
    var originalWordCursor = CursorPosition(0, false)

    fun selectWord() {
        val wordIter = BreakType.WORD.get()
        wordIter.setText(contents.plaintext)
        val underCursor = internals.typesetString.containingCharacter(mousePos)
        val end = CursorPosition(wordIter.following(underCursor).replace(BreakIterator.DONE, contents.length), true)
        val start = CursorPosition(wordIter.previous().replace(BreakIterator.DONE, 0), false)
        originalWord = CursorRange(start, end)

        val isStartCloser = cursor.index - start.index < end.index - cursor.index
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
        if(mouseOver > originalWord.endInclusive.index) {
            val wordIter = BreakType.WORD.get()
            wordIter.setText(contents.plaintext)

            val wordBreak = wordIter.following(mouseOver)
            if (wordBreak == BreakIterator.DONE) {
                cursor = CursorPosition(contents.length, true)
                selectionStart = originalWord.start
            } else {
                cursor = CursorPosition(wordBreak, true)
                selectionStart = originalWord.start
            }
        } else if(mouseOver < originalWord.start.index) {
            val wordIter = BreakType.WORD.get()
            wordIter.setText(contents.plaintext)

            val wordBreak = wordIter.preceding(mouseOver)
            if (wordBreak == BreakIterator.DONE) {
                cursor = CursorPosition(contents.length, false)
                selectionStart = originalWord.endExclusive
            } else {
                cursor = CursorPosition(wordBreak, false)
                selectionStart = originalWord.endExclusive
            }
        } else {
            cursor = originalWordCursor
            selectionStart = originalWordStart
        }
    }

    fun prevParagraphStart(text: String, pos: Int): CursorPosition {
        if(pos == 0) return CursorPosition(0, false)
        var startSearch = false
        for(i in pos-1 downTo 0) {
            startSearch = startSearch || text[i] !in TypesetString.newlines
            if(startSearch && text[i] in TypesetString.newlines) {
                return CursorPosition(i+1, false)
            }
        }
        return CursorPosition(0, false)
    }

    fun nextParagraphEnd(text: String, pos: Int): CursorPosition {
        if(pos == text.length) return CursorPosition(text.length, true)
        var startSearch = false
        for(i in pos until text.length) {
            startSearch = startSearch || text[i] !in TypesetString.newlines
            if(startSearch && text[i] in TypesetString.newlines) {
                return CursorPosition(i, true)
            }
        }
        return CursorPosition(text.length, true)
    }

    var originalParagraph = CursorRange(CursorPosition(0, false), CursorPosition(0, false))
    var originalParagraphStart = CursorPosition(0, false)
    var originalParagraphCursor = CursorPosition(0, false)

    fun selectParagraph() {
        val underCursor = internals.typesetString.containingCharacter(mousePos)
        val start = prevParagraphStart(contents.plaintext, underCursor)
        val end = nextParagraphEnd(contents.plaintext, underCursor)
        originalParagraph = CursorRange(start, end)

        val isStartCloser = cursor.index - start.index < end.index - cursor.index
        if(isStartCloser) {
            cursor = start
            originalParagraphCursor = start
            selectionStart = end
            originalParagraphStart = end
        } else {
            cursor = end
            originalParagraphCursor = end
            selectionStart = start
            originalParagraphStart = start
        }
    }

    fun dragSelectParagraph(pos: Vec2i) {
        val mouseOver = internals.typesetString.containingCharacter(mousePos)
        if(mouseOver > originalParagraph.endInclusive.index) {
            cursor = nextParagraphEnd(contents.plaintext, mouseOver)
            selectionStart = originalParagraph.start
        } else if(mouseOver < originalParagraph.start.index) {
            cursor = prevParagraphStart(contents.plaintext, mouseOver)
            selectionStart = originalParagraph.endExclusive
        } else {
            cursor = originalParagraphCursor
            selectionStart = originalParagraphStart
        }
    }
}