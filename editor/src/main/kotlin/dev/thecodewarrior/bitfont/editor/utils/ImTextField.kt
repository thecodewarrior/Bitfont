package dev.thecodewarrior.bitfont.editor.utils

import kotlin.reflect.KMutableProperty0
/*
class ImTextField(val idString: String) {
    val id = idString.hashCode()

    val autoSelectAll = false
    var text: String = ""
        set(value) {
            field = value
            fixCursor()
        }

    fun draw(
        label: String,
        textPtr: KMutableProperty0<String>,
        sizeIn: Vec2,
        validation: (String) -> String = { it }
    ): Boolean = with(ImGui) {
        val field_ = this@ImTextField

//        fun inputTextEx(label: String, buf: CharArray/*, bufSize: Int*/, sizeArg: Vec2, flags: InputTextFlags,
//            callback: InputTextCallback? = null, callbackUserData: Any? = null): Boolean {

        val window = currentWindow
        if (window.skipItems) return false

        val labelSize = calcTextSize(label, 0, true)
        val size = calcItemSize(sizeIn, calcItemWidth(),
            // Arbitrary default of 8 lines high for multi-line
            labelSize.y + style.framePadding.y * 2f)
        val frameBb = Rect(window.dc.cursorPos, window.dc.cursorPos + size)
        val totalBb = Rect(frameBb.min, frameBb.max + Vec2(if (labelSize.x > 0f) style.itemInnerSpacing.x + labelSize.x else 0f, 0f))

        var drawWindow = window
        itemSize(totalBb, style.framePadding.y)
        if (!itemAdd(totalBb, id, frameBb)) return false

        val hovered = itemHoverable(frameBb, id)
        if (hovered) g.mouseCursor = MouseCursor.TextInput

        // Using completion callback disable keyboard tabbing
        val tabStop = flags hasnt (Itf.CallbackCompletion or Itf.AllowTabInput)
        val focusRequested = focusableItemRegister(window, id, tabStop)
        val focusRequestedByCode = focusRequested && window.focusIdxAllCounter == window.focusIdxAllRequestCurrent
        val focusRequestedByTab = focusRequested && !focusRequestedByCode

        val userClicked = hovered && io.mouseClicked[0]
        val userNavInputStart = g.activeId != id && (g.navInputId == id || (g.navActivateId == id && g.navInputSource == InputSource.NavKeyboard))

        var valueChanged = false
        var enterPressed = false
        var backupCurrentTextLength = 0

        if (g.activeId == id) {

            if (!field_.isEditable && !g.activeIdIsJustActivated) {
                field_.text = textPtr.get()
            }

            /*  Although we are active we don't prevent mouse from hovering other elements unless we are interacting
                right now with the widget.
                Down the line we should have a cleaner library-wide concept of Selected vs Active.  */
            g.activeIdAllowOverlap = !io.mouseDown[0]
            g.wantTextInputNextFrame = 1

            // Edit in progress
            val mouseX = io.mousePos.x - frameBb.min.x - style.framePadding.x + field_.scrollX
            val mouseY =
                if (isMultiline)
                    io.mousePos.y - drawWindow.dc.cursorPos.y - style.framePadding.y
                else g.fontSize * 0.5f

            // OS X style: Double click selects by word instead of selecting whole text
            val isOsx = io.configMacOSXBehaviors
            if (selectAll || (hovered && !isOsx && io.mouseDoubleClicked[0])) {
                editState.selectAll()
                editState.selectedAllMouseLock = true
            } else if (hovered && isOsx && io.mouseDoubleClicked[0]) {
                // Double-click select a word only, OS X style (by simulating keystrokes)
                editState.onKeyPressed(K.WORDLEFT)
                editState.onKeyPressed(K.WORDRIGHT or K.SHIFT)
            } else if (io.mouseClicked[0] && !editState.selectedAllMouseLock) {
                if (hovered) {
                    editState.click(mouseX, mouseY)
                    editState.cursorAnimReset()
                }
            } else if (io.mouseDown[0] && !editState.selectedAllMouseLock && io.mouseDelta anyNotEqual 0f) {
                editState.state.selectStart = editState.state.cursor
                editState.state.selectEnd = editState.locateCoord(mouseX, mouseY)
                editState.cursorFollow = true
                editState.cursorAnimReset()
            }
            if (editState.selectedAllMouseLock && !io.mouseDown[0])
                editState.selectedAllMouseLock = false

            if (io.inputCharacters[0] != NUL) {
                /*  Process text input (before we check for Return because using some IME will effectively send a
                    Return?)
                    We ignore CTRL inputs, but need to allow ALT+CTRL as some keyboards (e.g. German) use AltGR
                    (which _is_ Alt+Ctrl) to input certain characters. */
                val ignoreInputs = (io.keyCtrl && !io.keyAlt) || (isOsx && io.keySuper)
                if (!ignoreInputs && isEditable && !userNavInputStart)
                    io.inputCharacters.filter { it != NUL }.map {
                        withChar { c ->
                            // Insert character if they pass filtering
                            if (inputTextFilterCharacter(c.apply { set(it) }, flags, callback, callbackUserData))
                                editState.onKeyPressed(c().i)
                        }
                    }
                // Consume characters
                io.inputCharacters.fill(NUL)
            }
        }

        // Handle key-presses
        val kMask = if (io.keyShift) K.SHIFT else 0
        val isOsx = io.configMacOSXBehaviors
        // OS X style: Shortcuts using Cmd/Super instead of Ctrl
        val isShortcutKey = (if (isOsx) io.keySuper && !io.keyCtrl else io.keyCtrl && !io.keySuper) && !io.keyAlt && !io.keyShift
        val isOsxShiftShortcut = isOsx && io.keySuper && io.keyShift && !io.keyCtrl && !io.keyAlt
        val isWordmoveKeyDown = if (isOsx) io.keyAlt else io.keyCtrl // OS X style: Text editing cursor movement using Alt instead of Ctrl
        // OS X style: Line/Text Start and End using Cmd+Arrows instead of Home/End
        val isStartendKeyDown = isOsx && io.keySuper && !io.keyCtrl && !io.keyAlt
        val isCtrlKeyOnly = io.keyCtrl && !io.keyShift && !io.keyAlt && !io.keySuper
        val isShiftKeyOnly = io.keyShift && !io.keyCtrl && !io.keyAlt && !io.keySuper

        val isCut = ((isShortcutKey && Key.X.isPressed) || (isShiftKeyOnly && Key.Delete.isPressed)) && isEditable && !isPassword && (!isMultiline || editState.hasSelection)
        val isCopy = ((isShortcutKey && Key.C.isPressed) || (isCtrlKeyOnly && Key.Insert.isPressed)) && !isPassword && (!isMultiline || editState.hasSelection)
        val isPaste = ((isShortcutKey && Key.V.isPressed) || (isShiftKeyOnly && Key.Insert.isPressed)) && isEditable
        val isUndo = ((isShortcutKey && Key.Z.isPressed) && isEditable && isUndoable)
        val isRedo = ((isShortcutKey && Key.Y.isPressed) || (isOsxShiftShortcut && Key.Z.isPressed)) && isEditable && isUndoable

        when {
            Key.LeftArrow.isPressed -> editState.onKeyPressed(when {
                isStartendKeyDown -> K.LINESTART
                isWordmoveKeyDown -> K.WORDLEFT
                else -> K.LEFT
            } or kMask)
            Key.RightArrow.isPressed -> editState.onKeyPressed(when {
                isStartendKeyDown -> K.LINEEND
                isWordmoveKeyDown -> K.WORDRIGHT
                else -> K.RIGHT
            } or kMask)
            Key.UpArrow.isPressed && isMultiline ->
                if (io.keyCtrl)
                    drawWindow.setScrollY(glm.max(drawWindow.scroll.y - g.fontSize, 0f))
                else
                    editState.onKeyPressed((if (isStartendKeyDown) K.TEXTSTART else K.UP) or kMask)
            Key.DownArrow.isPressed && isMultiline ->
                if (io.keyCtrl)
                    drawWindow.setScrollY(glm.min(drawWindow.scroll.y + g.fontSize, scrollMaxY))
                else
                    editState.onKeyPressed((if (isStartendKeyDown) K.TEXTEND else K.DOWN) or kMask)
            Key.Home.isPressed -> editState.onKeyPressed((if (io.keyCtrl) K.TEXTSTART else K.LINESTART) or kMask)
            Key.End.isPressed -> editState.onKeyPressed((if (io.keyCtrl) K.TEXTEND else K.LINEEND) or kMask)
            Key.Delete.isPressed && isEditable -> editState.onKeyPressed(K.DELETE or kMask)
            Key.Backspace.isPressed && isEditable -> {
                if (!editState.hasSelection)
                    if (isWordmoveKeyDown)
                        editState.onKeyPressed(K.WORDLEFT or K.SHIFT)
                    else if (isOsx && io.keySuper && !io.keyAlt && !io.keyCtrl)
                        editState.onKeyPressed(K.LINESTART or K.SHIFT)
                editState.onKeyPressed(K.BACKSPACE or kMask)
            }
            Key.Enter.isPressed -> {
                val ctrlEnterForNewLine = flags has Itf.CtrlEnterForNewLine
                if (!isMultiline || (ctrlEnterForNewLine && !io.keyCtrl) || (!ctrlEnterForNewLine && io.keyCtrl)) {
                    clearActiveId = true
                    enterPressed = true
                } else if (isEditable)
                    withChar('\n') { c ->
                        // Insert new line
                        if (inputTextFilterCharacter(c, flags, callback, callbackUserData))
                            editState.onKeyPressed(c().i)
                    }
            }
            flags has Itf.AllowTabInput && Key.Tab.isPressed && !io.keyCtrl && !io.keyShift && !io.keyAlt && isEditable ->
                withChar('\t') { c ->
                    // Insert TAB
                    if (inputTextFilterCharacter(c, flags, callback, callbackUserData))
                        editState.onKeyPressed(c().i)
                }
            Key.Escape.isPressed -> {
                cancelEdit = true
                clearActiveId = true
            }
            isUndo || isRedo -> {
                editState.onKeyPressed(if (isUndo) K.UNDO else K.REDO)
                editState.clearSelection()
            }
            isShortcutKey && Key.A.isPressed -> {
                editState.selectAll()
                editState.cursorFollow = true
            }
            isCut || isCopy -> {
                // Cut, Copy
                val min = min(editState.state.selectStart, editState.state.selectEnd)
                val max = max(editState.state.selectStart, editState.state.selectEnd)

                val copy = String(editState.textW, min, max - editState.state.cursor)//for some reason this is needed.

                if (copy.isNotEmpty()) {
                    val stringSelection = StringSelection(copy)
                    val clpbrd = Toolkit.getDefaultToolkit().systemClipboard
                    clpbrd.setContents(stringSelection, null)
                }
                if (isCut) {
                    if (!editState.hasSelection)
                        editState.selectAll()
                    System.arraycopy(editState.textW, max, editState.textW, min, max - min)
                    editState.deleteChars(editState.state.cursor, max - min)
                    editState.state.cursor = min
                    editState.clearSelection()
                }
            }
            isPaste -> {
                if (editState.hasSelection)
                    editState.deleteSelection()
                val data = Toolkit.getDefaultToolkit().systemClipboard.getData(DataFlavor.stringFlavor) as? String
                data?.let {
                    editState.insertChars(editState.state.cursor, data.toCharArray(), 0, data.toCharArray().size)
                    editState.state.cursor += data.length
                }
            }
        }
        if (clearActiveId && g.activeId == id) clearActiveId()
        // ------------------------- Render -------------------------
        /*  Select which buffer we are going to display. When ImGuiInputTextFlags_NoLiveEdit is set 'buf' might still
            be the old value. We set buf to NULL to prevent accidental usage from now on.         */
        val bufDisplay = if (g.activeId == id && isEditable) editState.tempBuffer else buf

        /*  Set upper limit of single-line InputTextEx() at 2 million characters strings. The current pathological worst case is a long line
            without any carriage return, which would makes ImFont::RenderText() reserve too many vertices and probably crash. Avoid it altogether.
            Note that we only use this limit on single-line InputText(), so a pathologically large line on a InputTextMultiline() would still crash. */
        val bufDisplayMaxLength = 2 * 1024 * 1024

        if (!isMultiline) {
            renderNavHighlight(frameBb, id)
            renderFrame(frameBb.min, frameBb.max, Col.FrameBg.u32, true, style.frameRounding)
        }

        val clipRect = Vec4(frameBb.min, frameBb.min + size) // Not using frameBb.Max because we have adjusted size
        val renderPos = if (isMultiline) Vec2(drawWindow.dc.cursorPos) else frameBb.min + style.framePadding
        val textSize = Vec2()
        val isCurrentlyScrolling = editState.id == id && isMultiline && g.activeId == drawWindow.getIdNoKeepAlive("#SCROLLY")
        if (g.activeId == id || isCurrentlyScrolling) {

            editState.cursorAnim += io.deltaTime

            /*  This is going to be messy. We need to:
                    - Display the text (this alone can be more easily clipped)
                    - Handle scrolling, highlight selection, display cursor (those all requires some form of 1d->2d
                        cursor position calculation)
                    - Measure text height (for scrollbar)
                We are attempting to do most of that in **one main pass** to minimize the computation cost
                (non-negligible for large amount of text) + 2nd pass for selection rendering (we could merge them by an
                extra refactoring effort)   */
            val text = editState.textW
            val cursorOffset = Vec2()
            val selectStartOffset = Vec2()

            run {
                // Count lines + find lines numbers straddling 'cursor' and 'select_start' position.
                val searchesInputPtr = intArrayOf(0 + editState.state.cursor, -1)
                var searchesRemaining = 1
                val searchesResultLineNumber = intArrayOf(-1, -999)
                if (editState.state.selectStart != editState.state.selectEnd) {
                    searchesInputPtr[1] = glm.min(editState.state.selectStart, editState.state.selectEnd)
                    searchesResultLineNumber[1] = -1
                    searchesRemaining++
                }

                // Iterate all lines to find our line numbers
                // In multi-line mode, we never exit the loop until all lines are counted, so add one extra to the searchesRemaining counter.
                if (isMultiline) searchesRemaining++
                var lineCount = 0
                var s = 0
                while (s < text.size && text[s] != NUL)
                    if (text[s++] == '\n') {
                        lineCount++
                        if (searchesResultLineNumber[0] == -1 && s >= searchesInputPtr[0]) {
                            searchesResultLineNumber[0] = lineCount
                            if (--searchesRemaining <= 0) break
                        }
                        if (searchesResultLineNumber[1] == -1 && s >= searchesInputPtr[1]) {
                            searchesResultLineNumber[1] = lineCount
                            if (--searchesRemaining <= 0) break
                        }
                    }
                lineCount++
                if (searchesResultLineNumber[0] == -1) searchesResultLineNumber[0] = lineCount
                if (searchesResultLineNumber[1] == -1) searchesResultLineNumber[1] = lineCount

                // Calculate 2d position by finding the beginning of the line and measuring distance
                var start = text.beginOfLine(searchesInputPtr[0])
                cursorOffset.x = inputTextCalcTextSizeW(text, start, searchesInputPtr[0]).x
                cursorOffset.y = searchesResultLineNumber[0] * g.fontSize
                if (searchesResultLineNumber[1] >= 0) {
                    start = text.beginOfLine(searchesInputPtr[1])
                    selectStartOffset.x = inputTextCalcTextSizeW(text, start, searchesInputPtr[1]).x
                    selectStartOffset.y = searchesResultLineNumber[1] * g.fontSize
                }

                // Store text height (note that we haven't calculated text width at all, see GitHub issues #383, #1224)
                if (isMultiline)
                    textSize.put(size.x, lineCount * g.fontSize)
            }

            // Scroll
            if (editState.cursorFollow) {
                // Horizontal scroll in chunks of quarter width
                if (flags hasnt Itf.NoHorizontalScroll) {
                    val scrollIncrementX = size.x * 0.25f
                    if (cursorOffset.x < editState.scrollX)
                        editState.scrollX = (glm.max(0f, cursorOffset.x - scrollIncrementX)).i.f
                    else if (cursorOffset.x - size.x >= editState.scrollX)
                        editState.scrollX = (cursorOffset.x - size.x + scrollIncrementX).i.f
                } else
                    editState.scrollX = 0f

                // Vertical scroll
                if (isMultiline) {
                    var scrollY = drawWindow.scroll.y
                    if (cursorOffset.y - g.fontSize < scrollY)
                        scrollY = glm.max(0f, cursorOffset.y - g.fontSize)
                    else if (cursorOffset.y - size.y >= scrollY)
                        scrollY = cursorOffset.y - size.y
                    drawWindow.dc.cursorPos.y += drawWindow.scroll.y - scrollY   // To avoid a frame of lag
                    drawWindow.scroll.y = scrollY
                    renderPos.y = drawWindow.dc.cursorPos.y
                }
            }
            editState.cursorFollow = false
            val renderScroll = Vec2(editState.scrollX, 0f)

            // Draw selection
            if (editState.state.selectStart != editState.state.selectEnd) {

                val textSelectedBegin = glm.min(editState.state.selectStart, editState.state.selectEnd)
                val textSelectedEnd = glm.max(editState.state.selectStart, editState.state.selectEnd)

                val bgOffYUp = if (isMultiline) 0f else -1f
                val bgOffYDn = if (isMultiline) 0f else 2f
                val bgColor = Col.TextSelectedBg.u32
                val rectPos = renderPos + selectStartOffset - renderScroll
                var p = textSelectedBegin
                while (p < textSelectedEnd) {
                    if (rectPos.y > clipRect.w + g.fontSize) break
                    if (rectPos.y < clipRect.y) {
                        while (p < textSelectedEnd)
                            if (text[p++] == '\n')
                                break
                    } else {
                        val rectSize = withInt {
                            inputTextCalcTextSizeW(text, p, textSelectedEnd, it, stopOnNewLine = true).also { p = it() }
                        }
                        // So we can see selected empty lines
                        if (rectSize.x <= 0f) rectSize.x = (g.font.getCharAdvance(' ') * 0.5f).i.f
                        val rect = Rect(rectPos + Vec2(0f, bgOffYUp - g.fontSize), rectPos + Vec2(rectSize.x, bgOffYDn))
                        val clipRect_ = Rect(clipRect)
                        rect clipWith clipRect_
                        if (rect overlaps clipRect_)
                            drawWindow.drawList.addRectFilled(rect.min, rect.max, bgColor)
                    }
                    rectPos.x = renderPos.x - renderScroll.x
                    rectPos.y += g.fontSize
                }
            }

            val bufDisplayLen = editState.curLenA
            if (isMultiline || bufDisplayLen < bufDisplayMaxLength)
                drawWindow.drawList.addText(g.font, g.fontSize, renderPos - renderScroll, Col.Text.u32, bufDisplay, bufDisplayLen, 0f, clipRect.takeIf { isMultiline })

            // Draw blinking cursor
            val cursorIsVisible = !io.configCursorBlink || g.inputTextState.cursorAnim <= 0f || glm.mod(g.inputTextState.cursorAnim, 1.2f) <= 0.8f
            val cursorScreenPos = renderPos + cursorOffset - renderScroll
            val cursorScreenRect = Rect(cursorScreenPos.x, cursorScreenPos.y - g.fontSize + 0.5f, cursorScreenPos.x + 1f, cursorScreenPos.y - 1.5f)
            if (cursorIsVisible && cursorScreenRect overlaps Rect(clipRect))
                drawWindow.drawList.addLine(cursorScreenRect.min, cursorScreenRect.bl, Col.Text.u32)

            /*  Notify OS of text input position for advanced IME (-1 x offset so that Windows IME can cover our cursor.
                Bit of an extra nicety.)             */
            if (isEditable)
                g.platformImePos = Vec2(cursorScreenPos.x - 1, cursorScreenPos.y - g.fontSize)
        } else {
            // Render text only
            val bufEnd = IntArray(1)
            if (isMultiline)
            // We don't need width
                textSize.put(size.x, inputTextCalcTextLenAndLineCount(bufDisplay.contentToString(), bufEnd) * g.fontSize)
            else
                bufEnd[0] = bufDisplay.strlen
            if (isMultiline || bufEnd[0] < bufDisplayMaxLength)
                drawWindow.drawList.addText(g.font, g.fontSize, renderPos, Col.Text.u32, bufDisplay, bufEnd[0], 0f, clipRect.takeIf { isMultiline })
        }

    }
}
    */