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

        keyAction(Key.H, Modifier.CONTROL, { delete(cursor-1) })
        keyAction(Key.BACKSPACE, { delete(cursor-1) })
        keyAction(Key.BACKSPACE, Modifier.ALT, { delete(prevWordStart(contents.plaintext, cursor)) })

        keyAction(Key.D, Modifier.CONTROL, { delete(cursor+1) })
        keyAction(Key.DELETE, { delete(cursor+1) })
        keyAction(Key.DELETE, Modifier.ALT, { delete(nextWordEnd(contents.plaintext, cursor)) })

        keyAction(Key.K, Modifier.CONTROL, { delete(nextParagraphEnd(contents.plaintext, cursor)) })

        keyAction(Key.ENTER, ::enter)
        keyAction(Key.O, Modifier.CONTROL, {
            val cursor = cursor
            insert("\n")
            this.cursor = cursor
        })

        keyAction(Key.LEFT, optionalShift, { moveBackward(BreakType.CHARACTER) })
        keyAction(Key.RIGHT, optionalShift, { moveForward(BreakType.CHARACTER) })
        keyAction(Key.B, optionalShift.require(Modifier.CONTROL), { moveBackward(BreakType.CHARACTER) })
        keyAction(Key.F, optionalShift.require(Modifier.CONTROL), { moveForward(BreakType.CHARACTER) })

        keyAction(Key.LEFT, optionalShift.require(Modifier.ALT), { moveTo(prevWordStart(contents.plaintext, cursor)) })
        keyAction(Key.RIGHT, optionalShift.require(Modifier.ALT), { moveTo(nextWordEnd(contents.plaintext, cursor)) })

        keyAction(Key.A, optionalShift.require(Modifier.CONTROL), {
            if(cursor > 0 && contents.plaintext[cursor-1] !in TypesetString.newlines)
                moveTo(prevParagraphStart(contents.plaintext, cursor))
        })
        keyAction(Key.E, optionalShift.require(Modifier.CONTROL), {
            if(contents.plaintext[cursor] !in TypesetString.newlines)
                moveTo(nextParagraphEnd(contents.plaintext, cursor))
        })
        keyAction(Key.UP, optionalShift.require(Modifier.ALT), { moveTo(prevParagraphStart(contents.plaintext, cursor)) })
        keyAction(Key.DOWN, optionalShift.require(Modifier.ALT), { moveTo(nextParagraphEnd(contents.plaintext, cursor)) })

        keyAction(Key.UP, optionalShift, ::moveUp)
        keyAction(Key.DOWN, optionalShift, ::moveDown)
        keyAction(Key.P, optionalShift.require(Modifier.CONTROL), ::moveUp)
        keyAction(Key.N, optionalShift.require(Modifier.CONTROL), ::moveDown)

        keyAction(Key.UP, optionalShift.require(Modifier.SUPER), { moveTo(0) })
        keyAction(Key.DOWN, optionalShift.require(Modifier.SUPER), { moveTo(contents.length) })

        mouseAction(MouseButton.LEFT, optionalShift, 1, ::jumpToMouse, {}, ::normalMouseDrag)
        mouseAction(MouseButton.LEFT, ModifierPattern.NONE, 2, ::selectWord, {}, ::dragSelectWord)
        mouseAction(MouseButton.LEFT, ModifierPattern.NONE, 3, ::selectParagraph, {}, ::dragSelectParagraph)

        keyAction(Key.V, Modifier.SUPER, ::paste)
        keyAction(Key.C, Modifier.SUPER, ::copy)
        keyAction(Key.X, Modifier.SUPER, ::cut)
        keyAction(Key.A, Modifier.SUPER, ::selectAll)
    }

    fun selectAll() {
        cursor = contents.length
        selectionStart = 0
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

    fun prevParagraphStart(text: String, pos: Int): Int {
        if(pos == 0) return 0
        var startSearch = false
        for(i in pos-1 downTo 0) {
            startSearch = startSearch || text[i] !in TypesetString.newlines
            if(startSearch && text[i] in TypesetString.newlines) {
                return i+1
            }
        }
        return 0
    }

    fun nextParagraphEnd(text: String, pos: Int): Int {
        if(pos == text.length) return text.length
        var startSearch = false
        for(i in pos until text.length) {
            startSearch = startSearch || text[i] !in TypesetString.newlines
            if(startSearch && text[i] in TypesetString.newlines) {
                return i
            }
        }
        return text.length
    }

    var originalParagraph = 0 until 0
    var originalParagraphStart = 0
    var originalParagraphCursor = 0

    fun selectParagraph() {
        val underCursor = internals.typesetString.containingCharacter(mousePos)
        val start = prevParagraphStart(contents.plaintext, underCursor)
        val end = nextParagraphEnd(contents.plaintext, underCursor)
        originalParagraph = start until end

        val isStartCloser = cursor - start < end - cursor
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
        if(mouseOver > originalParagraph.endInclusive) {
            cursor = nextParagraphEnd(contents.plaintext, mouseOver)
            selectionStart = originalParagraph.start
        } else if(mouseOver < originalParagraph.start) {
            cursor = prevParagraphStart(contents.plaintext, mouseOver)
            selectionStart = originalParagraph.endExclusive
        } else {
            cursor = originalParagraphCursor
            selectionStart = originalParagraphStart
        }
    }
}