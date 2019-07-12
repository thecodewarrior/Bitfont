@file:Suppress("unused")

package games.thecodewarrior.bitfont.editor.imgui

import games.thecodewarrior.bitfont.editor.utils.delegate
import games.thecodewarrior.bitfont.editor.utils.math.Rect
import games.thecodewarrior.bitfont.editor.utils.math.Vec2
import games.thecodewarrior.bitfont.editor.utils.math.rect
import games.thecodewarrior.bitfont.editor.utils.math.vec
import org.ice1000.jimgui.*
import org.ice1000.jimgui.util.JImGuiUtil
import org.jetbrains.annotations.Contract
import java.lang.ref.WeakReference

class ImGui(val wrapped: JImGui) {

    var background: JImVec4
        get() = wrapped.background
        set(value) {
            wrapped.background = value
        }

    val style: JImStyle
        get() = wrapped.style

    val font: JImFont
        get() = wrapped.font

    val windowDrawList: ImDrawList
        get() = ImDrawList.wrap(wrapped.windowDrawList)

    val foregroundDrawList: ImDrawList
        get() = ImDrawList.wrap(wrapped.foregroundDrawList)

    val isDisposed: Boolean
        get() = wrapped.isDisposed

    var platformWindowSizeX: Float
        get() = wrapped.platformWindowSizeX
        set(value) {
            wrapped.platformWindowSizeX = value
        }

    var platformWindowSizeY: Float
        get() = wrapped.platformWindowSizeY
        set(value) {
            wrapped.platformWindowSizeY = value
        }
    var platformWindowPosX: Float
        get() = wrapped.platformWindowPosX
        set(value) {
            wrapped.platformWindowPosX = value
        }
    var platformWindowPosY: Float
        get() = wrapped.platformWindowPosY
        set(value) {
            wrapped.platformWindowPosY = value
        }

    var clipboardText: String
        get() = wrapped.clipboardText
        set(value) {
            wrapped.clipboardText = value
        }
    //endregion

    val floatFmt: ByteArray
        get() = JImGuiGen.FLOAT_FMT

    val doubleFmt: ByteArray
        get() = JImGuiGen.FLOAT_FMT

    val intFmt: ByteArray
        get() = JImGuiGen.FLOAT_FMT

    /**is current window focused? or its root|child, depending on flags. see flags for options. */
    val isWindowFocused: Boolean
        get() = wrapped.isWindowFocused
    /**is current window hovered (and typically: not blocked by a popup|modal)? see flags for options. NB: If you are trying to check whether your mouse should be dispatched to imgui or to your app, you should use the 'io.WantCaptureMouse' boolean for that! Please read the FAQ! */
    val isWindowHovered: Boolean
        get() = wrapped.isWindowHovered
    /**is mouse button held (0=left, 1=right, 2=middle) */
    val isMouseDown: Boolean
        get() = wrapped.isMouseDown
    /**did mouse button clicked (went from !Down to Down) (0=left, 1=right, 2=middle) */
    val isMouseClicked: Boolean
        get() = wrapped.isMouseClicked
    /**did mouse button double-clicked. a double-click returns false in IsMouseClicked(). uses io.MouseDoubleClickTime. */
    val isMouseDoubleClicked: Boolean
        get() = wrapped.isMouseDoubleClicked
    /**did mouse button released (went from Down to !Down) */
    val isMouseReleased: Boolean
        get() = wrapped.isMouseReleased
    /**is mouse dragging. if lock_threshold < -1.0f uses io.MouseDraggingThreshold */
    val isMouseDragging: Boolean
        get() = wrapped.isMouseDragging
    /**get column width (in pixels). pass -1 to use current column */
    val columnWidth: Float
        get() = wrapped.columnWidth
    /**get position of column line (in pixels, from the left side of the contents region). pass -1 to use current column, otherwise 0..GetColumnsCount() inclusive. column 0 is typically 0.0f */
    val columnOffset: Float
        get() = wrapped.columnOffset
    /**is the last item hovered? (and usable, aka not blocked by a popup, etc.). See ImGuiHoveredFlags for more options. */
    val isItemHovered: Boolean
        get() = wrapped.isItemHovered
    /**is the last item clicked? (e.g. button|node just clicked on) == IsMouseClicked(mouse_button) && IsItemHovered() */
    val isItemClicked: Boolean
        get() = wrapped.isItemClicked

    fun deallocateNativeObject() = wrapped.deallocateNativeObject()

    fun close() = wrapped.close()

    private val _io = ImGuiIO(wrapped.io)

    /**
     * Call this only if you expect a nullable result.
     *
     * @return same as [.getIO]
     */
    fun findIO(): ImGuiIO? = if(wrapped.findIO() == null) null else _io

    val io: ImGuiIO
        get() {
            wrapped.io // to throw an error
            return _io
        }

    /**
     * Call this only if you expect a nullable result.
     *
     * @return same as [.getWindowDrawList]
     */
    fun findWindowDrawList(): JImDrawList? = wrapped.findWindowDrawList()

    /**
     * Call this only if you expect a nullable result.
     *
     * @return same as [.getForegroundDrawList]
     */
    fun findForegroundDrawList(): JImDrawList? = wrapped.findForegroundDrawList()

    /**
     * Call this only if you expect a nullable result.
     *
     * @return same as [.getStyle], don't call [JImStyle.deallocateNativeObject]
     */
    @Contract(pure = true)
    fun findStyle(): JImStyle? = wrapped.findStyle()

    /**
     * Call this only if you expect a nullable result.
     *
     * @return same as [.getFont], don't call [JImStyle.deallocateNativeObject]
     */
    @Contract(pure = true)
    fun findFont(): JImFont? = wrapped.findFont()
    //endregion

    fun initBeforeMainLoop() = wrapped.initBeforeMainLoop()
    fun setPlatformWindowSize(newX: Float, newY: Float) = wrapped.setPlatformWindowSize(newX, newY)
    fun setPlatformWindowPos(newX: Float, newY: Float) = wrapped.setPlatformWindowPos(newX, newY)

    /** alias to [JImGuiGen.textUnformatted]  */
    fun text(text: String) = wrapped.text(text)

    fun textColored(color: JImVec4, text: String) = wrapped.textColored(color, text)

    fun textDisabled(text: String) = wrapped.textDisabled(text)

    fun progressBar(fraction: Float, overlay: String?) = wrapped.progressBar(fraction, overlay)

    /**
     * @param label        label text
     * @param values       plot values
     * @param valuesOffset offset in [values]
     * @param valuesLength length in [values]
     */
    @JvmOverloads  fun plotLines(label: String, values: FloatArray, valuesOffset: Int = 0, valuesLength: Int = values.size) = wrapped.plotLines(label, values, valuesOffset, valuesLength)

    /**
     * @param label        label text
     * @param values       plot values
     * @param valuesOffset offset in [values]
     * @param valuesLength length in [values]
     * @param overlayText  tooltip text when plot is hovered
     */
    fun plotLines(label: String, values: FloatArray, valuesOffset: Int, valuesLength: Int, overlayText: String) = wrapped.plotLines(label, values, valuesOffset, valuesLength, overlayText)

    /**
     * @param label       label text
     * @param values      plot values
     * @param overlayText tooltip text when plot is hovered
     */
    fun plotLines(label: String, values: FloatArray, overlayText: String) = wrapped.plotLines(label, values, overlayText)

    /**
     * @param label        label text
     * @param values       plot values
     * @param valuesOffset offset in [values]
     * @param valuesLength length in [values]
     * @param overlayText  tooltip text when plot is hovered
     */
    fun plotLines(label: String, values: FloatArray, valuesOffset: Int, valuesLength: Int, overlayText: String, graphWidth: Float, graphHeight: Float) = wrapped.plotLines(label, values, valuesOffset, valuesLength, overlayText, graphWidth, graphHeight)

    /**
     * @param label        label text
     * @param values       plot values
     * @param valuesOffset offset in [values]
     * @param valuesLength length in [values]
     * @param overlayText  tooltip text when plot is hovered
     */
    fun plotLines(label: String, values: FloatArray, valuesOffset: Int, valuesLength: Int, overlayText: String, scaleMin: Float, scaleMax: Float, graphWidth: Float, graphHeight: Float) = wrapped.plotLines(label, values, valuesOffset, valuesLength, overlayText, scaleMin, scaleMax, graphWidth, graphHeight)

    /**
     * @param label  label text
     * @param values plot values
     */
    fun plotHistogram(label: String, values: FloatArray) = wrapped.plotHistogram(label, values)

    /**
     * @param label       label text
     * @param values      plot values
     * @param overlayText tooltip text when plot is hovered
     */
    fun plotHistogram(label: String, values: FloatArray, overlayText: String) = wrapped.plotHistogram(label, values, overlayText)

    // TODO doc
    @JvmOverloads  fun plotHistogram(label: String, values: FloatArray, valuesOffset: Int, valuesLength: Int, overlayText: String, scaleMin: Float = JImGuiUtil.FLT_MAX, scaleMax: Float = JImGuiUtil.FLT_MAX, graphWidth: Float = 0f, graphHeight: Float = 0f) = wrapped.plotHistogram(label, values, valuesOffset, valuesLength, overlayText, scaleMin, scaleMax, graphWidth, graphHeight)

    /**
     * @param styleVar should be a value from [JImStyleVars]
     * @param value    the value to set
     */
    fun pushStyleVar(styleVar: JImStyleVar<Float>, value: Float) = wrapped.pushStyleVar(styleVar, value)

    /**
     * @param styleVar should be a value from [JImStyleVars]
     * @param valueX   the first value of ImVec2 to set
     * @param valueY   the second value of ImVec2 to set
     */
    fun pushStyleVar(styleVar: JImStyleVar<Void>, valueX: Float, valueY: Float) = wrapped.pushStyleVar(styleVar, valueX, valueY)

    /**
     * the condition of the main loop
     *
     * @return should end the main loop or not
     */
    @Contract(pure = true)
    fun windowShouldClose(): Boolean = wrapped.windowShouldClose()

    /** Should be called after drawing all widgets  */
    fun render() = wrapped.render()

    /** Should be called before drawing all widgets  */
    fun initNewFrame() = wrapped.initNewFrame()

    fun loadIniSettingsFromMemory(data: String) = wrapped.loadIniSettingsFromMemory(data)

    fun saveIniSettingsToMemory(): String = wrapped.saveIniSettingsToMemory()

    /**
     * @param label    label text
     * @param shortcut displayed for convenience but not processed by ImGui at the moment
     * @param selected like checkbox
     * @param enabled  if not, will be grey
     * @return true when activated.
     */
    @JvmOverloads  fun menuItem(label: String, shortcut: String?, selected: Boolean, enabled: Boolean = true): Boolean = wrapped.menuItem(label, shortcut, selected, enabled)

    fun beginTabItem(label: String, flags: Int): Boolean = wrapped.beginTabItem(label, flags)

    fun windowDrawListAddImage(id: JImTextureID, aX: Float, aY: Float, bX: Float, bY: Float, uvAX: Float, uvAY: Float, uvBX: Float, uvBY: Float, color: Int) = wrapped.windowDrawListAddImage(id, aX, aY, bX, bY, uvAX, uvAY, uvBX, uvBY, color)

    @JvmOverloads  fun windowDrawListAddLine(aX: Float, aY: Float, bX: Float, bY: Float, u32Color: Int, thickness: Float = 1f) = wrapped.windowDrawListAddLine(aX, aY, bX, bY, u32Color, thickness)

    fun image(id: JImTextureID) = wrapped.image(id)

    /**
     * @param label    label text
     * @param selected like checkbox
     * @return true when activated.
     */
    fun menuItem(label: String, selected: Boolean): Boolean = wrapped.menuItem(label, selected)

    /**
     * @param label    label text
     * @param selected like checkbox
     * @return true when activated.
     */
    fun menuItem(label: JImStr, selected: Boolean): Boolean = wrapped.menuItem(label, selected)

    @JvmOverloads  fun inputText(label: String, buffer: ByteArray, flags: Int = 0): Boolean = wrapped.inputText(label, buffer, flags)

    /**new, recommended style (default) */
    fun styleColorsDark(style: JImStyle) = wrapped.styleColorsDark(style)
    /**new, recommended style (default) */
    fun styleColorsDark() = wrapped.styleColorsDark()
    /**classic imgui style */
    fun styleColorsClassic(style: JImStyle) = wrapped.styleColorsClassic(style)
    /**classic imgui style */
    fun styleColorsClassic() = wrapped.styleColorsClassic()
    /**best used with borders and a custom, thicker font */
    fun styleColorsLight(style: JImStyle) = wrapped.styleColorsLight(style)
    /**best used with borders and a custom, thicker font */
    fun styleColorsLight() = wrapped.styleColorsLight()
    fun emptyButton(bounds: JImVec4): Boolean = wrapped.emptyButton(bounds)
    fun dragVec4(label: String, bounds: JImVec4, speed: Float, min: Float, max: Float) = wrapped.dragVec4(label, bounds, speed, min, max)
    fun dragVec4(label: JImStr, bounds: JImVec4, speed: Float, min: Float, max: Float) = wrapped.dragVec4(label, bounds, speed, min, max)
    fun dragVec4(label: String, bounds: JImVec4, speed: Float, min: Float) = wrapped.dragVec4(label, bounds, speed, min)
    fun dragVec4(label: String, bounds: JImVec4, speed: Float) = wrapped.dragVec4(label, bounds, speed)
    fun dragVec4(label: String, bounds: JImVec4) = wrapped.dragVec4(label, bounds)
    fun sliderVec4(label: String, bounds: JImVec4, min: Float, max: Float) = wrapped.sliderVec4(label, bounds, min, max)
    fun sliderVec4(label: JImStr, bounds: JImVec4, min: Float, max: Float) = wrapped.sliderVec4(label, bounds, min, max)
    fun sliderVec4(label: String, bounds: JImVec4, min: Float) = wrapped.sliderVec4(label, bounds, min)
    fun sliderVec4(label: String, bounds: JImVec4) = wrapped.sliderVec4(label, bounds)
    fun lineTo(deltaX: Float, deltaY: Float, color: JImVec4, thickness: Float) = wrapped.lineTo(deltaX, deltaY, color, thickness)
    fun lineTo(deltaX: Float, deltaY: Float, color: JImVec4) = wrapped.lineTo(deltaX, deltaY, color)
    /**@param thickness if < 0, circle will be filled
     */
    fun circle(radius: Float, color: JImVec4, numSegments: Int, thickness: Float) = wrapped.circle(radius, color, numSegments, thickness)
    /**@param thickness if < 0, circle will be filled
     */
    fun circle(radius: Float, color: JImVec4, numSegments: Int) = wrapped.circle(radius, color, numSegments)
    /**@param thickness if < 0, circle will be filled
     */
    fun circle(radius: Float, color: JImVec4) = wrapped.circle(radius, color)
    /**@param thickness if < 0, circle will be filled
     */
    fun rect(width: Float, height: Float, color: JImVec4, rounding: Float, thickness: Float, roundingCornersFlags: Int) = wrapped.rect(width, height, color, rounding, thickness, roundingCornersFlags)
    /**@param thickness if < 0, circle will be filled
     */
    fun rect(width: Float, height: Float, color: JImVec4, rounding: Float, thickness: Float) = wrapped.rect(width, height, color, rounding, thickness)
    /**@param thickness if < 0, circle will be filled
     */
    fun rect(width: Float, height: Float, color: JImVec4, rounding: Float) = wrapped.rect(width, height, color, rounding)
    /**@param thickness if < 0, circle will be filled
     */
    fun rect(width: Float, height: Float, color: JImVec4) = wrapped.rect(width, height, color)
    fun dialogBox(title: String, text: String, widthWindow: Float, heightWindow: Float, openPtr: NativeBool, percentageOnScreen: Float) = wrapped.dialogBox(title, text, widthWindow, heightWindow, openPtr, percentageOnScreen)
    fun dialogBox(title: JImStr, text: JImStr, widthWindow: Float, heightWindow: Float, openPtr: NativeBool, percentageOnScreen: Float) = wrapped.dialogBox(title, text, widthWindow, heightWindow, openPtr, percentageOnScreen)
    fun dialogBox(title: String, text: String, widthWindow: Float, heightWindow: Float, openPtr: NativeBool) = wrapped.dialogBox(title, text, widthWindow, heightWindow, openPtr)
    fun dialogBox(title: String, text: String, widthWindow: Float, heightWindow: Float) = wrapped.dialogBox(title, text, widthWindow, heightWindow)
    fun bufferingBar(value: Float, width: Float, height: Float, backgroundColor: JImVec4, foregroundColor: JImVec4) = wrapped.bufferingBar(value, width, height, backgroundColor, foregroundColor)
    fun spinner(radius: Float, thickness: Float, numSegments: Int, color: JImVec4) = wrapped.spinner(radius, thickness, numSegments, color)
    /**call between widgets or groups to layout them horizontally. X position given in window coordinates. */
    fun sameLine(posX: Float) = wrapped.sameLine(posX)
    /**call between widgets or groups to layout them horizontally. X position given in window coordinates. */
    fun sameLine() = wrapped.sameLine()
    /**move content position toward the right, by style.IndentSpacing or indent_w if != 0 */
    fun indent() = wrapped.indent()
    /**move content position back to the left, by style.IndentSpacing or indent_w if != 0 */
    fun unindent() = wrapped.unindent()
    /**create Demo window (previously called ShowTestWindow). demonstrate most ImGui features. call this to learn about the library! try to make it always available in your application! */
    fun showDemoWindow(openPtr: NativeBool) = wrapped.showDemoWindow(openPtr)
    /**create Demo window (previously called ShowTestWindow). demonstrate most ImGui features. call this to learn about the library! try to make it always available in your application! */
    fun showDemoWindow() = wrapped.showDemoWindow()
    /**create About window. display Dear ImGui version, credits and build|system information. */
    fun showAboutWindow(openPtr: NativeBool) = wrapped.showAboutWindow(openPtr)
    /**create About window. display Dear ImGui version, credits and build|system information. */
    fun showAboutWindow() = wrapped.showAboutWindow()
    /**create Metrics|Debug window. display Dear ImGui internals: draw commands (with individual draw calls and vertices), window list, basic internal state, etc. */
    fun showMetricsWindow(openPtr: NativeBool) = wrapped.showMetricsWindow(openPtr)
    /**create Metrics|Debug window. display Dear ImGui internals: draw commands (with individual draw calls and vertices), window list, basic internal state, etc. */
    fun showMetricsWindow() = wrapped.showMetricsWindow()
    /**add font selector block (not a window), essentially a combo listing the loaded fonts. */
    fun showFontSelector(label: String) = wrapped.showFontSelector(label)
    /**add font selector block (not a window), essentially a combo listing the loaded fonts. */
    fun showFontSelector(label: JImStr) = wrapped.showFontSelector(label)
    /**add style selector block (not a window), essentially a combo listing the default styles. */
    fun showStyleSelector(label: String) = wrapped.showStyleSelector(label)
    /**add style selector block (not a window), essentially a combo listing the default styles. */
    fun showStyleSelector(label: JImStr) = wrapped.showStyleSelector(label)
    /**add style editor block (not a window). you can pass in a reference ImGuiStyle structure to compare to, revert to and save to (else it uses the default style) */
    fun showStyleEditor(ref: JImStyle) = wrapped.showStyleEditor(ref)
    /**add style editor block (not a window). you can pass in a reference ImGuiStyle structure to compare to, revert to and save to (else it uses the default style) */
    fun showStyleEditor() = wrapped.showStyleEditor()
    /**set next window position. call before Begin(). use pivot=(0.5f,0.5f) to center on given point, etc. */
    fun setNextWindowPos(posX: Float, posY: Float, condition: Int) = wrapped.setNextWindowPos(posX, posY, condition)
    /**set next window position. call before Begin(). use pivot=(0.5f,0.5f) to center on given point, etc. */
    fun setNextWindowPos(posX: Float, posY: Float) = wrapped.setNextWindowPos(posX, posY)
    /**set next window size. set axis to 0.0f to force an auto-fit on this axis. call before Begin() */
    fun setNextWindowSize(width: Float, height: Float) = wrapped.setNextWindowSize(width, height)
    /**set next window collapsed state. call before Begin() */
    fun setNextWindowCollapsed(collapsed: Boolean) = wrapped.setNextWindowCollapsed(collapsed)
    /**(not recommended) set current window position - call within Begin()|End(). prefer using SetNextWindowPos(), as this may incur tearing and side-effects.
     * set named window position. */
    fun setWindowPos(name: String, windowPosX: Float, windowPosY: Float, condition: Int) = wrapped.setWindowPos(name, windowPosX, windowPosY, condition)
    /**(not recommended) set current window position - call within Begin()|End(). prefer using SetNextWindowPos(), as this may incur tearing and side-effects.
     * set named window position. */
    fun setWindowPos(name: JImStr, windowPosX: Float, windowPosY: Float, condition: Int) = wrapped.setWindowPos(name, windowPosX, windowPosY, condition)
    /**(not recommended) set current window position - call within Begin()|End(). prefer using SetNextWindowPos(), as this may incur tearing and side-effects.
     * set named window position. */
    fun setWindowPos(name: String, windowPosX: Float, windowPosY: Float) = wrapped.setWindowPos(name, windowPosX, windowPosY)
    /**(not recommended) set current window size - call within Begin()|End(). set to ImVec2(0,0) to force an auto-fit. prefer using SetNextWindowSize(), as this may incur tearing and minor side-effects.
     * set named window size. set axis to 0.0f to force an auto-fit on this axis. */
    fun setWindowSize(name: String, width: Float, height: Float, condition: Int) = wrapped.setWindowSize(name, width, height, condition)
    /**(not recommended) set current window size - call within Begin()|End(). set to ImVec2(0,0) to force an auto-fit. prefer using SetNextWindowSize(), as this may incur tearing and minor side-effects.
     * set named window size. set axis to 0.0f to force an auto-fit on this axis. */
    fun setWindowSize(name: JImStr, width: Float, height: Float, condition: Int) = wrapped.setWindowSize(name, width, height, condition)
    /**(not recommended) set current window size - call within Begin()|End(). set to ImVec2(0,0) to force an auto-fit. prefer using SetNextWindowSize(), as this may incur tearing and minor side-effects.
     * set named window size. set axis to 0.0f to force an auto-fit on this axis. */
    fun setWindowSize(name: String, width: Float, height: Float) = wrapped.setWindowSize(name, width, height)
    /**(not recommended) set current window collapsed state. prefer using SetNextWindowCollapsed().
     * set named window collapsed state */
    fun setWindowCollapsed(name: String, collapsed: Boolean, condition: Int) = wrapped.setWindowCollapsed(name, collapsed, condition)
    /**(not recommended) set current window collapsed state. prefer using SetNextWindowCollapsed().
     * set named window collapsed state */
    fun setWindowCollapsed(name: JImStr, collapsed: Boolean, condition: Int) = wrapped.setWindowCollapsed(name, collapsed, condition)
    /**(not recommended) set current window collapsed state. prefer using SetNextWindowCollapsed().
     * set named window collapsed state */
    fun setWindowCollapsed(name: String, collapsed: Boolean) = wrapped.setWindowCollapsed(name, collapsed)
    /**(not recommended) set current window to be focused | front-most. prefer using SetNextWindowFocus().
     * set named window to be focused | front-most. use NULL to remove focus. */
    fun setWindowFocus(name: String) = wrapped.setWindowFocus(name)
    /**(not recommended) set current window to be focused | front-most. prefer using SetNextWindowFocus().
     * set named window to be focused | front-most. use NULL to remove focus. */
    fun setWindowFocus(name: JImStr) = wrapped.setWindowFocus(name)
    /**was key pressed (went from !Down to Down). if repeat=true, uses io.KeyRepeatDelay | KeyRepeatRate */
    fun isKeyPressed(userKeyIndex: Int): Boolean = wrapped.isKeyPressed(userKeyIndex)
    /**did mouse button clicked (went from !Down to Down) (0=left, 1=right, 2=middle) */
    fun isMouseClicked(button: Int): Boolean = wrapped.isMouseClicked(button)
    /**is mouse dragging. if lock_threshold < -1.0f uses io.MouseDraggingThreshold */
    fun isMouseDragging(button: Int): Boolean = wrapped.isMouseDragging(button)
    /**is mouse hovering given bounding rect (in screen space). clipped by current clipping settings, but disregarding of other consideration of focus|window ordering|popup-block. */
    fun isMouseHoveringRect(widthRMin: Float, heightRMin: Float, widthRMax: Float, heightRMax: Float): Boolean = wrapped.isMouseHoveringRect(widthRMin, heightRMin, widthRMax, heightRMax)
    /**attention: misleading name! manually override io.WantCaptureKeyboard flag next frame (said flag is entirely left for your application to handle). e.g. force capture keyboard when your widget is being hovered. This is equivalent to setting "io.WantCaptureKeyboard = want_capture_keyboard_value"; after the next NewFrame() call. */
    fun captureKeyboardFromApp() = wrapped.captureKeyboardFromApp()
    /**attention: misleading name! manually override io.WantCaptureMouse flag next frame (said flag is entirely left for your application to handle). This is equivalent to setting "io.WantCaptureMouse = want_capture_mouse_value;" after the next NewFrame() call. */
    fun captureMouseFromApp() = wrapped.captureMouseFromApp()
    fun setClipboardText(text: JImStr) = wrapped.setClipboardText(text)
    /**push string into the ID stack (will hash string).
     * push string into the ID stack (will hash string).
     * push pointer into the ID stack (will hash pointer).
     * push integer into the ID stack (will hash integer). */
    fun pushID(stringID: String) = wrapped.pushID(stringID)
    /**calculate unique ID (hash of whole ID stack + given parameter). e.g. if you want to query into ImGuiStorage yourself */
    fun getID(stringID: String): Int = wrapped.getID(stringID)
    /**Use SetNextWindowSize(size, ImGuiCond_FirstUseEver) + SetNextWindowBgAlpha() instead.
     * Automatically called by constructor if you passed 'items_count' or by Step() in Step 1. */
    fun begin(name: String, openPtr: NativeBool, flags: Int): Boolean = wrapped.begin(name, openPtr, flags)
    /**Use SetNextWindowSize(size, ImGuiCond_FirstUseEver) + SetNextWindowBgAlpha() instead.
     * Automatically called by constructor if you passed 'items_count' or by Step() in Step 1. */
    fun begin(name: JImStr, openPtr: NativeBool, flags: Int): Boolean = wrapped.begin(name, openPtr, flags)
    /**Use SetNextWindowSize(size, ImGuiCond_FirstUseEver) + SetNextWindowBgAlpha() instead.
     * Automatically called by constructor if you passed 'items_count' or by Step() in Step 1. */
    fun begin(name: String, openPtr: NativeBool): Boolean = wrapped.begin(name, openPtr)
    /**Use SetNextWindowSize(size, ImGuiCond_FirstUseEver) + SetNextWindowBgAlpha() instead.
     * Automatically called by constructor if you passed 'items_count' or by Step() in Step 1. */
    fun begin(name: String): Boolean = wrapped.begin(name)
    fun beginChild(id: Int, width: Float, height: Float, border: Boolean): Boolean = wrapped.beginChild(id, width, height, border)
    fun beginChild(id: Int, width: Float, height: Float): Boolean = wrapped.beginChild(id, width, height)
    fun beginChild(id: Int): Boolean = wrapped.beginChild(id)
    fun beginChild0(stringID: String, width: Float, height: Float, border: Boolean, flags: Int): Boolean = wrapped.beginChild0(stringID, width, height, border, flags)
    fun beginChild0(stringID: JImStr, width: Float, height: Float, border: Boolean, flags: Int): Boolean = wrapped.beginChild0(stringID, width, height, border, flags)
    fun beginChild0(stringID: String, width: Float, height: Float, border: Boolean): Boolean = wrapped.beginChild0(stringID, width, height, border)
    fun beginChild0(stringID: String, width: Float, height: Float): Boolean = wrapped.beginChild0(stringID, width, height)
    fun beginChild0(stringID: String): Boolean = wrapped.beginChild0(stringID)
    /**shortcut for Bullet()+Text() */
    fun bulletText(text: String) = wrapped.bulletText(text)
    /**shortcut for Bullet()+Text() */
    fun bulletText(text: JImStr) = wrapped.bulletText(text)
    /**display text+label aligned the same way as value+label widgets */
    fun labelText(label: String, text: String) = wrapped.labelText(label, text)
    /**display text+label aligned the same way as value+label widgets */
    fun labelText(label: JImStr, text: JImStr) = wrapped.labelText(label, text)
    /**shortcut for PushTextWrapPos(0.0f); Text(fmt, ...); PopTextWrapPos();. Note that this won't work on an auto-resizing window if there's no other widgets to extend the window width, yoy may need to set a size using SetNextWindowSize(). */
    fun textWrapped(text: String) = wrapped.textWrapped(text)
    /**shortcut for PushTextWrapPos(0.0f); Text(fmt, ...); PopTextWrapPos();. Note that this won't work on an auto-resizing window if there's no other widgets to extend the window width, yoy may need to set a size using SetNextWindowSize(). */
    fun textWrapped(text: JImStr) = wrapped.textWrapped(text)
    /**raw text without formatting. Roughly equivalent to Text("%s", text) but: A) doesn't require null terminated string if 'text_end' is specified, B) it's faster, no memory copy is done, no buffer size limits, recommended for long chunks of text. */
    fun textUnformatted(text: String) = wrapped.textUnformatted(text)
    /**button */
    fun button(text: String, width: Float, height: Float): Boolean = wrapped.button(text, width, height)
    /**button */
    fun button(text: JImStr, width: Float, height: Float): Boolean = wrapped.button(text, width, height)
    /**button */
    fun button(text: String): Boolean = wrapped.button(text)
    /**button with FramePadding=(0,0) to easily embed within text */
    fun smallButton(text: String): Boolean = wrapped.smallButton(text)
    /**button with FramePadding=(0,0) to easily embed within text */
    fun smallButton(text: JImStr): Boolean = wrapped.smallButton(text)
    /**button behavior without the visuals, frequently useful to build custom behaviors using the public api (along with IsItemActive, IsItemHovered, etc.) */
    fun invisibleButton(text: String, width: Float, height: Float): Boolean = wrapped.invisibleButton(text, width, height)
    /**button behavior without the visuals, frequently useful to build custom behaviors using the public api (along with IsItemActive, IsItemHovered, etc.) */
    fun invisibleButton(text: JImStr, width: Float, height: Float): Boolean = wrapped.invisibleButton(text, width, height)
    /**button behavior without the visuals, frequently useful to build custom behaviors using the public api (along with IsItemActive, IsItemHovered, etc.) */
    fun invisibleButton(text: String): Boolean = wrapped.invisibleButton(text)
    /**square button with an arrow shape */
    fun arrowButton(text: String, direction: Int): Boolean = wrapped.arrowButton(text, direction)
    /**square button with an arrow shape */
    fun arrowButton(text: JImStr, direction: Int): Boolean = wrapped.arrowButton(text, direction)
    fun image(userTextureID: JImTextureID, width: Float, height: Float, uv0X: Float, uv0Y: Float, uv1X: Float, uv1Y: Float) = wrapped.image(userTextureID, width, height, uv0X, uv0Y, uv1X, uv1Y)
    fun image(userTextureID: JImTextureID, width: Float, height: Float, uv0X: Float, uv0Y: Float) = wrapped.image(userTextureID, width, height, uv0X, uv0Y)
    fun image(userTextureID: JImTextureID, width: Float, height: Float) = wrapped.image(userTextureID, width, height)
    /**<0 frame_padding uses default frame padding settings. 0 for no padding */
    fun imageButton(userTextureID: JImTextureID, width: Float, height: Float, uv0X: Float, uv0Y: Float, uv1X: Float, uv1Y: Float, framePadding: Int): Boolean = wrapped.imageButton(userTextureID, width, height, uv0X, uv0Y, uv1X, uv1Y, framePadding)
    /**<0 frame_padding uses default frame padding settings. 0 for no padding */
    fun imageButton(userTextureID: JImTextureID, width: Float, height: Float, uv0X: Float, uv0Y: Float, uv1X: Float, uv1Y: Float): Boolean = wrapped.imageButton(userTextureID, width, height, uv0X, uv0Y, uv1X, uv1Y)
    /**<0 frame_padding uses default frame padding settings. 0 for no padding */
    fun imageButton(userTextureID: JImTextureID, width: Float, height: Float, uv0X: Float, uv0Y: Float): Boolean = wrapped.imageButton(userTextureID, width, height, uv0X, uv0Y)
    /**<0 frame_padding uses default frame padding settings. 0 for no padding */
    fun imageButton(userTextureID: JImTextureID, width: Float, height: Float): Boolean = wrapped.imageButton(userTextureID, width, height)
    fun checkbox(label: String, v: NativeBool): Boolean = wrapped.checkbox(label, v)
    fun checkbox(label: JImStr, v: NativeBool): Boolean = wrapped.checkbox(label, v)
    fun checkbox(label: String): Boolean = wrapped.checkbox(label)
    /**use with e.g. if (RadioButton("one", my_value==1)) { my_value = 1; }
     * shortcut to handle the above pattern when value is an integer */
    fun radioButton(text: String, v: NativeInt, v_button: Int): Boolean = wrapped.radioButton(text, v, v_button)
    /**use with e.g. if (RadioButton("one", my_value==1)) { my_value = 1; }
     * shortcut to handle the above pattern when value is an integer */
    fun radioButton(text: JImStr, v: NativeInt, v_button: Int): Boolean = wrapped.radioButton(text, v, v_button)
    fun radioButton0(text: String, active: Boolean): Boolean = wrapped.radioButton0(text, active)
    fun radioButton0(text: JImStr, active: Boolean): Boolean = wrapped.radioButton0(text, active)
    fun progressBar(fraction: Float, width: Float, height: Float, overlay: String?) = wrapped.progressBar(fraction, width, height, overlay)
    fun progressBar(fraction: Float, width: Float, height: Float) = wrapped.progressBar(fraction, width, height)
    fun progressBar(fraction: Float) = wrapped.progressBar(fraction)
    fun beginCombo(label: String, previewValue: String, flags: Int): Boolean = wrapped.beginCombo(label, previewValue, flags)
    fun beginCombo(label: JImStr, previewValue: JImStr, flags: Int): Boolean = wrapped.beginCombo(label, previewValue, flags)
    fun beginCombo(label: String, previewValue: String): Boolean = wrapped.beginCombo(label, previewValue)
    /**Separate items with \0 within a string, end item-list with \0\0. e.g. "One\0Two\0Three\0" */
    fun combo(label: String, currentItem: NativeInt, itemsSeparatedByZeros: String, popupMaxHeightInItems: Int) = wrapped.combo(label, currentItem, itemsSeparatedByZeros, popupMaxHeightInItems)
    /**Separate items with \0 within a string, end item-list with \0\0. e.g. "One\0Two\0Three\0" */
    fun combo(label: JImStr, currentItem: NativeInt, itemsSeparatedByZeros: JImStr, popupMaxHeightInItems: Int) = wrapped.combo(label, currentItem, itemsSeparatedByZeros, popupMaxHeightInItems)
    /**Separate items with \0 within a string, end item-list with \0\0. e.g. "One\0Two\0Three\0" */
    fun combo(label: String, currentItem: NativeInt, itemsSeparatedByZeros: String) = wrapped.combo(label, currentItem, itemsSeparatedByZeros)
    /**Use if you want to reimplement listBox() will custom data or interactions. if the function return true, you can output elements then call listBoxFooter() afterwards. */
    fun listBoxHeader0(label: String, width: Float, height: Float): Boolean = wrapped.listBoxHeader0(label, width, height)
    /**Use if you want to reimplement listBox() will custom data or interactions. if the function return true, you can output elements then call listBoxFooter() afterwards. */
    fun listBoxHeader0(label: JImStr, width: Float, height: Float): Boolean = wrapped.listBoxHeader0(label, width, height)
    /**Use if you want to reimplement listBox() will custom data or interactions. if the function return true, you can output elements then call listBoxFooter() afterwards. */
    fun listBoxHeader(label: String, itemsCount: Int, heightInItems: Int): Boolean = wrapped.listBoxHeader(label, itemsCount, heightInItems)
    /**Use if you want to reimplement listBox() will custom data or interactions. if the function return true, you can output elements then call listBoxFooter() afterwards. */
    fun listBoxHeader(label: JImStr, itemsCount: Int, heightInItems: Int): Boolean = wrapped.listBoxHeader(label, itemsCount, heightInItems)
    /**Use if you want to reimplement listBox() will custom data or interactions. if the function return true, you can output elements then call listBoxFooter() afterwards. */
    fun listBoxHeader(label: String, itemsCount: Int): Boolean = wrapped.listBoxHeader(label, itemsCount)
    /**If v_min >= v_max we have no bound */
    fun dragFloat(label: String, value: NativeFloat, valueSpeed: Float, valueMin: Float, valueMax: Float, format: String, power: Float): Boolean = wrapped.dragFloat(label, value, valueSpeed, valueMin, valueMax, format, power)
    /**If v_min >= v_max we have no bound */
    fun dragFloat(label: JImStr, value: NativeFloat, valueSpeed: Float, valueMin: Float, valueMax: Float, format: JImStr, power: Float): Boolean = wrapped.dragFloat(label, value, valueSpeed, valueMin, valueMax, format, power)
    /**If v_min >= v_max we have no bound */
    fun dragFloat(label: String, value: NativeFloat, valueSpeed: Float, valueMin: Float, valueMax: Float, format: String): Boolean = wrapped.dragFloat(label, value, valueSpeed, valueMin, valueMax, format)
    /**If v_min >= v_max we have no bound */
    fun dragFloat(label: String, value: NativeFloat, valueSpeed: Float, valueMin: Float, valueMax: Float): Boolean = wrapped.dragFloat(label, value, valueSpeed, valueMin, valueMax)
    /**If v_min >= v_max we have no bound */
    fun dragFloat(label: String, value: NativeFloat, valueSpeed: Float, valueMin: Float): Boolean = wrapped.dragFloat(label, value, valueSpeed, valueMin)
    /**If v_min >= v_max we have no bound */
    fun dragFloat(label: String, value: NativeFloat, valueSpeed: Float): Boolean = wrapped.dragFloat(label, value, valueSpeed)
    /**If v_min >= v_max we have no bound */
    fun dragFloat(label: String, value: NativeFloat): Boolean = wrapped.dragFloat(label, value)
    fun dragFloatRange2(label: String, valueCurrentMin: NativeFloat, valueCurrentMax: NativeFloat, valueSpeed: Float, valueMin: Float, valueMax: Float, format: String, formatMax: String?, power: Float): Boolean = wrapped.dragFloatRange2(label, valueCurrentMin, valueCurrentMax, valueSpeed, valueMin, valueMax, format, formatMax, power)
    fun dragFloatRange2(label: JImStr, valueCurrentMin: NativeFloat, valueCurrentMax: NativeFloat, valueSpeed: Float, valueMin: Float, valueMax: Float, format: JImStr, formatMax: JImStr?, power: Float): Boolean = wrapped.dragFloatRange2(label, valueCurrentMin, valueCurrentMax, valueSpeed, valueMin, valueMax, format, formatMax, power)
    fun dragFloatRange2(label: String, valueCurrentMin: NativeFloat, valueCurrentMax: NativeFloat, valueSpeed: Float, valueMin: Float, valueMax: Float, format: String, formatMax: String?): Boolean = wrapped.dragFloatRange2(label, valueCurrentMin, valueCurrentMax, valueSpeed, valueMin, valueMax, format, formatMax)
    fun dragFloatRange2(label: String, valueCurrentMin: NativeFloat, valueCurrentMax: NativeFloat, valueSpeed: Float, valueMin: Float, valueMax: Float, format: String): Boolean = wrapped.dragFloatRange2(label, valueCurrentMin, valueCurrentMax, valueSpeed, valueMin, valueMax, format)
    fun dragFloatRange2(label: String, valueCurrentMin: NativeFloat, valueCurrentMax: NativeFloat, valueSpeed: Float, valueMin: Float, valueMax: Float): Boolean = wrapped.dragFloatRange2(label, valueCurrentMin, valueCurrentMax, valueSpeed, valueMin, valueMax)
    fun dragFloatRange2(label: String, valueCurrentMin: NativeFloat, valueCurrentMax: NativeFloat, valueSpeed: Float, valueMin: Float): Boolean = wrapped.dragFloatRange2(label, valueCurrentMin, valueCurrentMax, valueSpeed, valueMin)
    fun dragFloatRange2(label: String, valueCurrentMin: NativeFloat, valueCurrentMax: NativeFloat, valueSpeed: Float): Boolean = wrapped.dragFloatRange2(label, valueCurrentMin, valueCurrentMax, valueSpeed)
    fun dragFloatRange2(label: String, valueCurrentMin: NativeFloat, valueCurrentMax: NativeFloat): Boolean = wrapped.dragFloatRange2(label, valueCurrentMin, valueCurrentMax)
    /**If v_min >= v_max we have no bound */
    fun dragInt(label: String, value: NativeInt, valueSpeed: Float, valueMin: Int, valueMax: Int, format: String): Boolean = wrapped.dragInt(label, value, valueSpeed, valueMin, valueMax, format)
    /**If v_min >= v_max we have no bound */
    fun dragInt(label: JImStr, value: NativeInt, valueSpeed: Float, valueMin: Int, valueMax: Int, format: JImStr): Boolean = wrapped.dragInt(label, value, valueSpeed, valueMin, valueMax, format)
    /**If v_min >= v_max we have no bound */
    fun dragInt(label: String, value: NativeInt, valueSpeed: Float, valueMin: Int, valueMax: Int): Boolean = wrapped.dragInt(label, value, valueSpeed, valueMin, valueMax)
    /**If v_min >= v_max we have no bound */
    fun dragInt(label: String, value: NativeInt, valueSpeed: Float, valueMin: Int): Boolean = wrapped.dragInt(label, value, valueSpeed, valueMin)
    /**If v_min >= v_max we have no bound */
    fun dragInt(label: String, value: NativeInt, valueSpeed: Float): Boolean = wrapped.dragInt(label, value, valueSpeed)
    /**If v_min >= v_max we have no bound */
    fun dragInt(label: String, value: NativeInt): Boolean = wrapped.dragInt(label, value)
    fun dragIntRange2(label: String, valueCurrentMin: NativeInt, valueCurrentMax: NativeInt, valueSpeed: Float, valueMin: Int, valueMax: Int, format: String): Boolean = wrapped.dragIntRange2(label, valueCurrentMin, valueCurrentMax, valueSpeed, valueMin, valueMax, format)
    fun dragIntRange2(label: JImStr, valueCurrentMin: NativeInt, valueCurrentMax: NativeInt, valueSpeed: Float, valueMin: Int, valueMax: Int, format: JImStr): Boolean = wrapped.dragIntRange2(label, valueCurrentMin, valueCurrentMax, valueSpeed, valueMin, valueMax, format)
    fun dragIntRange2(label: String, valueCurrentMin: NativeInt, valueCurrentMax: NativeInt, valueSpeed: Float, valueMin: Int, valueMax: Int): Boolean = wrapped.dragIntRange2(label, valueCurrentMin, valueCurrentMax, valueSpeed, valueMin, valueMax)
    fun dragIntRange2(label: String, valueCurrentMin: NativeInt, valueCurrentMax: NativeInt, valueSpeed: Float, valueMin: Int): Boolean = wrapped.dragIntRange2(label, valueCurrentMin, valueCurrentMax, valueSpeed, valueMin)
    fun dragIntRange2(label: String, valueCurrentMin: NativeInt, valueCurrentMax: NativeInt, valueSpeed: Float): Boolean = wrapped.dragIntRange2(label, valueCurrentMin, valueCurrentMax, valueSpeed)
    fun dragIntRange2(label: String, valueCurrentMin: NativeInt, valueCurrentMax: NativeInt): Boolean = wrapped.dragIntRange2(label, valueCurrentMin, valueCurrentMax)
    /**Use the 'const char* format' version instead of 'decimal_precision'! */
    fun inputFloat(label: String, value: NativeFloat, step: Float, stepFast: Float, format: String, flags: Int): Boolean = wrapped.inputFloat(label, value, step, stepFast, format, flags)
    /**Use the 'const char* format' version instead of 'decimal_precision'! */
    fun inputFloat(label: JImStr, value: NativeFloat, step: Float, stepFast: Float, format: JImStr, flags: Int): Boolean = wrapped.inputFloat(label, value, step, stepFast, format, flags)
    /**Use the 'const char* format' version instead of 'decimal_precision'! */
    fun inputFloat(label: String, value: NativeFloat, step: Float, stepFast: Float, format: String): Boolean = wrapped.inputFloat(label, value, step, stepFast, format)
    /**Use the 'const char* format' version instead of 'decimal_precision'! */
    fun inputFloat(label: String, value: NativeFloat, step: Float, stepFast: Float): Boolean = wrapped.inputFloat(label, value, step, stepFast)
    /**Use the 'const char* format' version instead of 'decimal_precision'! */
    fun inputFloat(label: String, value: NativeFloat, step: Float): Boolean = wrapped.inputFloat(label, value, step)
    /**Use the 'const char* format' version instead of 'decimal_precision'! */
    fun inputFloat(label: String, value: NativeFloat): Boolean = wrapped.inputFloat(label, value)
    fun inputInt(label: String, value: NativeInt, step: Int, stepFast: Int, flags: Int): Boolean = wrapped.inputInt(label, value, step, stepFast, flags)
    fun inputInt(label: JImStr, value: NativeInt, step: Int, stepFast: Int, flags: Int): Boolean = wrapped.inputInt(label, value, step, stepFast, flags)
    fun inputInt(label: String, value: NativeInt, step: Int, stepFast: Int): Boolean = wrapped.inputInt(label, value, step, stepFast)
    fun inputInt(label: String, value: NativeInt, step: Int): Boolean = wrapped.inputInt(label, value, step)
    fun inputInt(label: String, value: NativeInt): Boolean = wrapped.inputInt(label, value)
    fun inputDouble(label: String, value: NativeDouble, step: Double, stepFast: Double, format: String, flags: Int): Boolean = wrapped.inputDouble(label, value, step, stepFast, format, flags)
    fun inputDouble(label: JImStr, value: NativeDouble, step: Double, stepFast: Double, format: JImStr, flags: Int): Boolean = wrapped.inputDouble(label, value, step, stepFast, format, flags)
    fun inputDouble(label: String, value: NativeDouble, step: Double, stepFast: Double, format: String): Boolean = wrapped.inputDouble(label, value, step, stepFast, format)
    fun inputDouble(label: String, value: NativeDouble, step: Double, stepFast: Double): Boolean = wrapped.inputDouble(label, value, step, stepFast)
    fun inputDouble(label: String, value: NativeDouble, step: Double): Boolean = wrapped.inputDouble(label, value, step)
    fun inputDouble(label: String, value: NativeDouble): Boolean = wrapped.inputDouble(label, value)
    /**adjust format to decorate the value with a prefix or a suffix for in-slider labels or unit display. Use power!=1.0 for power curve sliders */
    fun sliderFloat(label: String, value: NativeFloat, valueMin: Float, valueMax: Float, format: String, power: Float): Boolean = wrapped.sliderFloat(label, value, valueMin, valueMax, format, power)
    /**adjust format to decorate the value with a prefix or a suffix for in-slider labels or unit display. Use power!=1.0 for power curve sliders */
    fun sliderFloat(label: JImStr, value: NativeFloat, valueMin: Float, valueMax: Float, format: JImStr, power: Float): Boolean = wrapped.sliderFloat(label, value, valueMin, valueMax, format, power)
    /**adjust format to decorate the value with a prefix or a suffix for in-slider labels or unit display. Use power!=1.0 for power curve sliders */
    fun sliderFloat(label: String, value: NativeFloat, valueMin: Float, valueMax: Float, format: String): Boolean = wrapped.sliderFloat(label, value, valueMin, valueMax, format)
    /**adjust format to decorate the value with a prefix or a suffix for in-slider labels or unit display. Use power!=1.0 for power curve sliders */
    fun sliderFloat(label: String, value: NativeFloat, valueMin: Float, valueMax: Float): Boolean = wrapped.sliderFloat(label, value, valueMin, valueMax)
    /**adjust format to decorate the value with a prefix or a suffix for in-slider labels or unit display. Use power!=1.0 for power curve sliders */
    fun sliderFloat(label: String, value: NativeFloat, valueMin: Float): Boolean = wrapped.sliderFloat(label, value, valueMin)
    /**adjust format to decorate the value with a prefix or a suffix for in-slider labels or unit display. Use power!=1.0 for power curve sliders */
    fun sliderFloat(label: String, value: NativeFloat): Boolean = wrapped.sliderFloat(label, value)
    fun sliderAngle(label: String, valueRad: NativeFloat, valueDegreeMin: Float, valueDegreeMax: Float): Boolean = wrapped.sliderAngle(label, valueRad, valueDegreeMin, valueDegreeMax)
    fun sliderAngle(label: JImStr, valueRad: NativeFloat, valueDegreeMin: Float, valueDegreeMax: Float): Boolean = wrapped.sliderAngle(label, valueRad, valueDegreeMin, valueDegreeMax)
    fun sliderAngle(label: String, valueRad: NativeFloat, valueDegreeMin: Float): Boolean = wrapped.sliderAngle(label, valueRad, valueDegreeMin)
    fun sliderAngle(label: String, valueRad: NativeFloat): Boolean = wrapped.sliderAngle(label, valueRad)
    fun sliderInt(label: String, value: NativeInt, valueMin: Int, valueMax: Int, format: String): Boolean = wrapped.sliderInt(label, value, valueMin, valueMax, format)
    fun sliderInt(label: JImStr, value: NativeInt, valueMin: Int, valueMax: Int, format: JImStr): Boolean = wrapped.sliderInt(label, value, valueMin, valueMax, format)
    fun sliderInt(label: String, value: NativeInt, valueMin: Int, valueMax: Int): Boolean = wrapped.sliderInt(label, value, valueMin, valueMax)
    fun sliderInt(label: String, value: NativeInt, valueMin: Int): Boolean = wrapped.sliderInt(label, value, valueMin)
    fun sliderInt(label: String, value: NativeInt): Boolean = wrapped.sliderInt(label, value)
    fun vSliderFloat(label: String, width: Float, height: Float, value: NativeFloat, valueMin: Float, valueMax: Float, format: String, power: Float): Boolean = wrapped.vSliderFloat(label, width, height, value, valueMin, valueMax, format, power)
    fun vSliderFloat(label: JImStr, width: Float, height: Float, value: NativeFloat, valueMin: Float, valueMax: Float, format: JImStr, power: Float): Boolean = wrapped.vSliderFloat(label, width, height, value, valueMin, valueMax, format, power)
    fun vSliderFloat(label: String, width: Float, height: Float, value: NativeFloat, valueMin: Float, valueMax: Float, format: String): Boolean = wrapped.vSliderFloat(label, width, height, value, valueMin, valueMax, format)
    fun vSliderFloat(label: String, width: Float, height: Float, value: NativeFloat, valueMin: Float, valueMax: Float): Boolean = wrapped.vSliderFloat(label, width, height, value, valueMin, valueMax)
    fun vSliderFloat(label: String, width: Float, height: Float, value: NativeFloat, valueMin: Float): Boolean = wrapped.vSliderFloat(label, width, height, value, valueMin)
    fun vSliderFloat(label: String, width: Float, height: Float, value: NativeFloat): Boolean = wrapped.vSliderFloat(label, width, height, value)
    fun vSliderInt(label: String, width: Float, height: Float, value: NativeInt, valueMin: Int, valueMax: Int, format: String): Boolean = wrapped.vSliderInt(label, width, height, value, valueMin, valueMax, format)
    fun vSliderInt(label: JImStr, width: Float, height: Float, value: NativeInt, valueMin: Int, valueMax: Int, format: JImStr): Boolean = wrapped.vSliderInt(label, width, height, value, valueMin, valueMax, format)
    fun vSliderInt(label: String, width: Float, height: Float, value: NativeInt, valueMin: Int, valueMax: Int): Boolean = wrapped.vSliderInt(label, width, height, value, valueMin, valueMax)
    fun vSliderInt(label: String, width: Float, height: Float, value: NativeInt, valueMin: Int): Boolean = wrapped.vSliderInt(label, width, height, value, valueMin)
    fun vSliderInt(label: String, width: Float, height: Float, value: NativeInt): Boolean = wrapped.vSliderInt(label, width, height, value)
    /**helper variation to easily decorelate the id from the displayed string. Read the FAQ about why and how to use ID. to align arbitrary text at the same level as a TreeNode() you can use Bullet().
     * " */
    fun treeNode(label: String): Boolean = wrapped.treeNode(label)
    /**helper variation to easily decorelate the id from the displayed string. Read the FAQ about why and how to use ID. to align arbitrary text at the same level as a TreeNode() you can use Bullet().
     * " */
    fun treeNode(label: JImStr): Boolean = wrapped.treeNode(label)
    fun treeNodeEx(label: String, flags: Int): Boolean = wrapped.treeNodeEx(label, flags)
    fun treeNodeEx(label: JImStr, flags: Int): Boolean = wrapped.treeNodeEx(label, flags)
    fun treeNodeEx(label: String): Boolean = wrapped.treeNodeEx(label)
    /**~ Indent()+PushId(). Already called by TreeNode() when returning true, but you can call TreePush|TreePop yourself if desired.
     * " */
    fun treePush(stringID: String) = wrapped.treePush(stringID)
    /**~ Indent()+PushId(). Already called by TreeNode() when returning true, but you can call TreePush|TreePop yourself if desired.
     * " */
    fun treePush(stringID: JImStr) = wrapped.treePush(stringID)
    /**set next TreeNode|CollapsingHeader open state. */
    fun setNextItemOpen(isOpen: Boolean) = wrapped.SetNextItemOpen(isOpen)
    /**if returning 'true' the header is open. doesn't indent nor push on ID stack. user doesn't have to call TreePop().
     * when 'p_open' isn't NULL, display an additional small close button on upper right of the header */
    fun collapsingHeader(label: String, openPtr: NativeBool, flags: Int): Boolean = wrapped.collapsingHeader(label, openPtr, flags)
    /**if returning 'true' the header is open. doesn't indent nor push on ID stack. user doesn't have to call TreePop().
     * when 'p_open' isn't NULL, display an additional small close button on upper right of the header */
    fun collapsingHeader(label: JImStr, openPtr: NativeBool, flags: Int): Boolean = wrapped.collapsingHeader(label, openPtr, flags)
    /**if returning 'true' the header is open. doesn't indent nor push on ID stack. user doesn't have to call TreePop().
     * when 'p_open' isn't NULL, display an additional small close button on upper right of the header */
    fun collapsingHeader(label: String, openPtr: NativeBool): Boolean = wrapped.collapsingHeader(label, openPtr)
    /**if returning 'true' the header is open. doesn't indent nor push on ID stack. user doesn't have to call TreePop().
     * when 'p_open' isn't NULL, display an additional small close button on upper right of the header */
    fun collapsingHeader(label: String): Boolean = wrapped.collapsingHeader(label)
    fun colorEdit3(label: String, color: JImVec4, flags: Int): Boolean = wrapped.colorEdit3(label, color, flags)
    fun colorEdit3(label: JImStr, color: JImVec4, flags: Int): Boolean = wrapped.colorEdit3(label, color, flags)
    fun colorEdit3(label: String, color: JImVec4): Boolean = wrapped.colorEdit3(label, color)
    fun colorEdit4(label: String, color: JImVec4, flags: Int): Boolean = wrapped.colorEdit4(label, color, flags)
    fun colorEdit4(label: JImStr, color: JImVec4, flags: Int): Boolean = wrapped.colorEdit4(label, color, flags)
    fun colorEdit4(label: String, color: JImVec4): Boolean = wrapped.colorEdit4(label, color)
    /**display a colored square|button, hover for details, return true when pressed. */
    fun colorButton(descriptionID: String, color: JImVec4, flags: Int, width: Float, height: Float): Boolean = wrapped.colorButton(descriptionID, color, flags, width, height)
    /**display a colored square|button, hover for details, return true when pressed. */
    fun colorButton(descriptionID: JImStr, color: JImVec4, flags: Int, width: Float, height: Float): Boolean = wrapped.colorButton(descriptionID, color, flags, width, height)
    /**display a colored square|button, hover for details, return true when pressed. */
    fun colorButton(descriptionID: String, color: JImVec4, flags: Int): Boolean = wrapped.colorButton(descriptionID, color, flags)
    /**display a colored square|button, hover for details, return true when pressed. */
    fun colorButton(descriptionID: String, color: JImVec4): Boolean = wrapped.colorButton(descriptionID, color)
    fun colorPicker3(label: String, color: JImVec4, flags: Int): Boolean = wrapped.colorPicker3(label, color, flags)
    fun colorPicker3(label: JImStr, color: JImVec4, flags: Int): Boolean = wrapped.colorPicker3(label, color, flags)
    fun colorPicker3(label: String, color: JImVec4): Boolean = wrapped.colorPicker3(label, color)
    fun colorPicker4(label: String, color: JImVec4, flags: Int): Boolean = wrapped.colorPicker4(label, color, flags)
    fun colorPicker4(label: JImStr, color: JImVec4, flags: Int): Boolean = wrapped.colorPicker4(label, color, flags)
    fun colorPicker4(label: String, color: JImVec4): Boolean = wrapped.colorPicker4(label, color)
    /**initialize current options (generally on application startup) if you want to select a default format, picker type, etc. User will be able to change many settings, unless you pass the _NoOptions flag to your calls. */
    fun setColorEditOptions() = wrapped.setColorEditOptions()
    fun selectable0(label: String, selected: Boolean, flags: Int, width: Float, height: Float): Boolean = wrapped.selectable0(label, selected, flags, width, height)
    fun selectable0(label: JImStr, selected: Boolean, flags: Int, width: Float, height: Float): Boolean = wrapped.selectable0(label, selected, flags, width, height)
    fun selectable0(label: String, selected: Boolean, flags: Int): Boolean = wrapped.selectable0(label, selected, flags)
    fun selectable0(label: String, selected: Boolean): Boolean = wrapped.selectable0(label, selected)
    fun selectable0(label: String): Boolean = wrapped.selectable0(label)
    /**"bool selected" carry the selection state (read-only). Selectable() is clicked is returns true so you can modify your selection state. size.x==0.0: use remaining width, size.x>0.0: specify width. size.y==0.0: use label height, size.y>0.0: specify height
     * "bool* p_selected" point to the selection state (read-write), as a convenient helper. */
    fun selectable(label: String, selected: NativeBool, flags: Int, width: Float, height: Float): Boolean = wrapped.selectable(label, selected, flags, width, height)
    /**"bool selected" carry the selection state (read-only). Selectable() is clicked is returns true so you can modify your selection state. size.x==0.0: use remaining width, size.x>0.0: specify width. size.y==0.0: use label height, size.y>0.0: specify height
     * "bool* p_selected" point to the selection state (read-write), as a convenient helper. */
    fun selectable(label: JImStr, selected: NativeBool, flags: Int, width: Float, height: Float): Boolean = wrapped.selectable(label, selected, flags, width, height)
    /**"bool selected" carry the selection state (read-only). Selectable() is clicked is returns true so you can modify your selection state. size.x==0.0: use remaining width, size.x>0.0: specify width. size.y==0.0: use label height, size.y>0.0: specify height
     * "bool* p_selected" point to the selection state (read-write), as a convenient helper. */
    fun selectable(label: String, selected: NativeBool, flags: Int): Boolean = wrapped.selectable(label, selected, flags)
    /**"bool selected" carry the selection state (read-only). Selectable() is clicked is returns true so you can modify your selection state. size.x==0.0: use remaining width, size.x>0.0: specify width. size.y==0.0: use label height, size.y>0.0: specify height
     * "bool* p_selected" point to the selection state (read-write), as a convenient helper. */
    fun selectable(label: String, selected: NativeBool): Boolean = wrapped.selectable(label, selected)
    /**"bool selected" carry the selection state (read-only). Selectable() is clicked is returns true so you can modify your selection state. size.x==0.0: use remaining width, size.x>0.0: specify width. size.y==0.0: use label height, size.y>0.0: specify height
     * "bool* p_selected" point to the selection state (read-write), as a convenient helper. */
    fun selectable(label: String): Boolean = wrapped.selectable(label)
    /**set a text-only tooltip, typically use with ImGui::IsItemHovered(). override any previous call to SetTooltip(). */
    fun setTooltip(text: String) = wrapped.setTooltip(text)
    /**set a text-only tooltip, typically use with ImGui::IsItemHovered(). override any previous call to SetTooltip(). */
    fun setTooltip(text: JImStr) = wrapped.setTooltip(text)
    /**create a sub-menu entry. only call EndMenu() if this returns true! */
    fun beginMenu(label: String, enabled: Boolean): Boolean = wrapped.beginMenu(label, enabled)
    /**create a sub-menu entry. only call EndMenu() if this returns true! */
    fun beginMenu(label: JImStr, enabled: Boolean): Boolean = wrapped.beginMenu(label, enabled)
    /**create a sub-menu entry. only call EndMenu() if this returns true! */
    fun beginMenu(label: String): Boolean = wrapped.beginMenu(label)
    /**return true when activated. shortcuts are displayed for convenience but not processed by ImGui at the moment
     * return true when activated + toggle (*p_selected) if p_selected != NULL */
    fun menuItem(label: String, shortcut: String?, selected: NativeBool, enabled: Boolean): Boolean = wrapped.menuItem(label, shortcut, selected, enabled)
    /**return true when activated. shortcuts are displayed for convenience but not processed by ImGui at the moment
     * return true when activated + toggle (*p_selected) if p_selected != NULL */
    fun menuItem(label: JImStr, shortcut: JImStr?, selected: NativeBool, enabled: Boolean): Boolean = wrapped.menuItem(label, shortcut, selected, enabled)
    /**return true when activated. shortcuts are displayed for convenience but not processed by ImGui at the moment
     * return true when activated + toggle (*p_selected) if p_selected != NULL */
    fun menuItem(label: String, shortcut: String?, selected: NativeBool): Boolean = wrapped.menuItem(label, shortcut, selected)
    /**return true when activated. shortcuts are displayed for convenience but not processed by ImGui at the moment
     * return true when activated + toggle (*p_selected) if p_selected != NULL */
    fun menuItem(label: String, shortcut: String?): Boolean = wrapped.menuItem(label, shortcut)
    /**return true when activated. shortcuts are displayed for convenience but not processed by ImGui at the moment
     * return true when activated + toggle (*p_selected) if p_selected != NULL */
    fun menuItem(label: String): Boolean = wrapped.menuItem(label)
    /**call to mark popup as open (don't call every frame!). popups are closed when user click outside, or if CloseCurrentPopup() is called within a BeginPopup()|EndPopup() block. By default, Selectable()|MenuItem() are calling CloseCurrentPopup(). Popup identifiers are relative to the current ID-stack (so OpenPopup and BeginPopup needs to be at the same level). */
    fun openPopup(stringID: String) = wrapped.openPopup(stringID)
    /**call to mark popup as open (don't call every frame!). popups are closed when user click outside, or if CloseCurrentPopup() is called within a BeginPopup()|EndPopup() block. By default, Selectable()|MenuItem() are calling CloseCurrentPopup(). Popup identifiers are relative to the current ID-stack (so OpenPopup and BeginPopup needs to be at the same level). */
    fun openPopup(stringID: JImStr) = wrapped.openPopup(stringID)
    /**return true if the popup is open, and you can start outputting to it. only call EndPopup() if BeginPopup() returns true! */
    fun beginPopup(stringID: String, flags: Int): Boolean = wrapped.beginPopup(stringID, flags)
    /**return true if the popup is open, and you can start outputting to it. only call EndPopup() if BeginPopup() returns true! */
    fun beginPopup(stringID: JImStr, flags: Int): Boolean = wrapped.beginPopup(stringID, flags)
    /**return true if the popup is open, and you can start outputting to it. only call EndPopup() if BeginPopup() returns true! */
    fun beginPopup(stringID: String): Boolean = wrapped.beginPopup(stringID)
    /**helper to open and begin popup when clicked on last item. if you can pass a NULL str_id only if the previous item had an id. If you want to use that on a non-interactive item such as Text() you need to pass in an explicit ID here. read comments in .cpp! */
    fun beginPopupContextItem(stringID: String?, mouseButton: Int): Boolean = wrapped.beginPopupContextItem(stringID, mouseButton)
    /**helper to open and begin popup when clicked on last item. if you can pass a NULL str_id only if the previous item had an id. If you want to use that on a non-interactive item such as Text() you need to pass in an explicit ID here. read comments in .cpp! */
    fun beginPopupContextItem(stringID: String?): Boolean = wrapped.beginPopupContextItem(stringID)
    /**helper to open and begin popup when clicked on last item. if you can pass a NULL str_id only if the previous item had an id. If you want to use that on a non-interactive item such as Text() you need to pass in an explicit ID here. read comments in .cpp! */
    fun beginPopupContextItem(): Boolean = wrapped.beginPopupContextItem()
    /**helper to open and begin popup when clicked on current window. */
    fun beginPopupContextWindow(stringID: String?, mouseButton: Int): Boolean = wrapped.beginPopupContextWindow(stringID, mouseButton)
    /**helper to open and begin popup when clicked on current window. */
    fun beginPopupContextWindow(stringID: String?): Boolean = wrapped.beginPopupContextWindow(stringID)
    /**helper to open and begin popup when clicked on current window. */
    fun beginPopupContextWindow(): Boolean = wrapped.beginPopupContextWindow()
    /**helper to open and begin popup when clicked in void (where there are no imgui windows). */
    fun beginPopupContextVoid(stringID: String?, mouseButton: Int): Boolean = wrapped.beginPopupContextVoid(stringID, mouseButton)
    /**helper to open and begin popup when clicked in void (where there are no imgui windows). */
    fun beginPopupContextVoid(stringID: String?): Boolean = wrapped.beginPopupContextVoid(stringID)
    /**helper to open and begin popup when clicked in void (where there are no imgui windows). */
    fun beginPopupContextVoid(): Boolean = wrapped.beginPopupContextVoid()
    /**modal dialog (regular window with title bar, block interactions behind the modal window, can't close the modal window by clicking outside) */
    fun beginPopupModal(name: String, openPtr: NativeBool, flags: Int): Boolean = wrapped.beginPopupModal(name, openPtr, flags)
    /**modal dialog (regular window with title bar, block interactions behind the modal window, can't close the modal window by clicking outside) */
    fun beginPopupModal(name: JImStr, openPtr: NativeBool, flags: Int): Boolean = wrapped.beginPopupModal(name, openPtr, flags)
    /**modal dialog (regular window with title bar, block interactions behind the modal window, can't close the modal window by clicking outside) */
    fun beginPopupModal(name: String, openPtr: NativeBool): Boolean = wrapped.beginPopupModal(name, openPtr)
    /**modal dialog (regular window with title bar, block interactions behind the modal window, can't close the modal window by clicking outside) */
    fun beginPopupModal(name: String): Boolean = wrapped.beginPopupModal(name)
    /**helper to open popup when clicked on last item (note: actually triggers on the mouse _released_ event to be consistent with popup behaviors). return true when just opened. */
    fun openPopupOnItemClick(stringID: String?, mouseButton: Int): Boolean = wrapped.openPopupOnItemClick(stringID, mouseButton)
    /**helper to open popup when clicked on last item (note: actually triggers on the mouse _released_ event to be consistent with popup behaviors). return true when just opened. */
    fun openPopupOnItemClick(stringID: String?): Boolean = wrapped.openPopupOnItemClick(stringID)
    /**helper to open popup when clicked on last item (note: actually triggers on the mouse _released_ event to be consistent with popup behaviors). return true when just opened. */
    fun openPopupOnItemClick(): Boolean = wrapped.openPopupOnItemClick()
    /**return true if the popup is open at the current begin-ed level of the popup stack. */
    fun isPopupOpen(stringID: String): Boolean = wrapped.isPopupOpen(stringID)
    /**return true if the popup is open at the current begin-ed level of the popup stack. */
    fun isPopupOpen(stringID: JImStr): Boolean = wrapped.isPopupOpen(stringID)
    fun columns(count: Int, stringID: String?, border: Boolean) = wrapped.columns(count, stringID, border)
    fun columns(count: Int, stringID: String?) = wrapped.columns(count, stringID)
    fun columns(count: Int) = wrapped.columns(count)
    fun columns() = wrapped.columns()
    /**create and append into a TabBar */
    fun beginTabBar(stringID: String, flags: Int): Boolean = wrapped.beginTabBar(stringID, flags)
    /**create and append into a TabBar */
    fun beginTabBar(stringID: JImStr, flags: Int): Boolean = wrapped.beginTabBar(stringID, flags)
    /**create and append into a TabBar */
    fun beginTabBar(stringID: String): Boolean = wrapped.beginTabBar(stringID)
    /**create a Tab. Returns true if the Tab is selected. */
    fun beginTabItem(label: String, openPtr: NativeBool, flags: Int): Boolean = wrapped.beginTabItem(label, openPtr, flags)
    /**create a Tab. Returns true if the Tab is selected. */
    fun beginTabItem(label: JImStr, openPtr: NativeBool, flags: Int): Boolean = wrapped.beginTabItem(label, openPtr, flags)
    /**create a Tab. Returns true if the Tab is selected. */
    fun beginTabItem(label: String, openPtr: NativeBool): Boolean = wrapped.beginTabItem(label, openPtr)
    /**create a Tab. Returns true if the Tab is selected. */
    fun beginTabItem(label: String): Boolean = wrapped.beginTabItem(label)
    /**notify TabBar or Docking system of a closed tab|window ahead (useful to reduce visual flicker on reorderable tab bars). For tab-bar: call after BeginTabBar() and before Tab submissions. Otherwise call with a window name. */
    fun setTabItemClosed(tabOrDockedWindowLabel: String) = wrapped.setTabItemClosed(tabOrDockedWindowLabel)
    /**notify TabBar or Docking system of a closed tab|window ahead (useful to reduce visual flicker on reorderable tab bars). For tab-bar: call after BeginTabBar() and before Tab submissions. Otherwise call with a window name. */
    fun setTabItemClosed(tabOrDockedWindowLabel: JImStr) = wrapped.setTabItemClosed(tabOrDockedWindowLabel)
    /**start logging to tty (stdout) */
    fun logToTTY() = wrapped.logToTTY()
    /**start logging to file */
    fun logToFile(maxDepth: Int, fileName: String?) = wrapped.logToFile(maxDepth, fileName)
    /**start logging to file */
    fun logToFile(maxDepth: Int) = wrapped.logToFile(maxDepth)
    /**start logging to file */
    fun logToFile() = wrapped.logToFile()
    /**start logging to OS clipboard */
    fun logToClipboard() = wrapped.logToClipboard()
    /**pass text data straight to log (without being displayed) */
    fun logText(text: String) = wrapped.logText(text)
    /**pass text data straight to log (without being displayed) */
    fun logText(text: JImStr) = wrapped.logText(text)
    /**word-wrapping for Text*() commands. < 0.0f: no wrapping; 0.0f: wrap to end of window (or column); > 0.0f: wrap at 'wrap_pos_x' position in window local space */
    fun pushTextWrapPos() = wrapped.pushTextWrapPos()
    /**in 'repeat' mode, Button*() functions return repeated true in a typematic manner (using io.KeyRepeatDelay|io.KeyRepeatRate setting). Note that you can call IsItemActive() after any Button() to tell if the button is held in the current frame. */
    fun pushButtonRepeat() = wrapped.pushButtonRepeat()
    fun pushStyleColor(index: Int, color: JImVec4) = wrapped.pushStyleColor(index, color)
    fun popStyleColor() = wrapped.popStyleColor()
    fun popStyleVar() = wrapped.popStyleVar()
    /**call after CreateContext() and before the first call to NewFrame(). NewFrame() automatically calls LoadIniSettingsFromDisk(io.IniFilename). */
    fun loadIniSettingsFromDisk(iniFileName: String) = wrapped.loadIniSettingsFromDisk(iniFileName)
    /**call after CreateContext() and before the first call to NewFrame(). NewFrame() automatically calls LoadIniSettingsFromDisk(io.IniFilename). */
    fun loadIniSettingsFromDisk(iniFileName: JImStr) = wrapped.loadIniSettingsFromDisk(iniFileName)
    /**this is automatically called (if io.IniFilename is not empty) a few seconds after any modification that should be reflected in the .ini file (and also by DestroyContext). */
    fun saveIniSettingsToDisk(iniFileName: String) = wrapped.saveIniSettingsToDisk(iniFileName)
    /**this is automatically called (if io.IniFilename is not empty) a few seconds after any modification that should be reflected in the .ini file (and also by DestroyContext). */
    fun saveIniSettingsToDisk(iniFileName: JImStr) = wrapped.saveIniSettingsToDisk(iniFileName)

    companion object {
        lateinit var current: ImGui
        val IO = ImGuiIO.Companion
        
        val DEFAULT_TITLE = "ImGui window created by JImGui"

        fun pushID(intID: Int) = JImGui.pushID(intID)

        val windowPosX: Float
            get() = JImGui.getWindowPosX()
        val windowPosY: Float
            get() = JImGui.getWindowPosY()
        val contentRegionMaxX: Float
            get() = JImGui.getContentRegionMaxX()
        val contentRegionMaxY: Float
            get() = JImGui.getContentRegionMaxY()
        val windowContentRegionMinX: Float
            get() = JImGui.getWindowContentRegionMinX()
        val windowContentRegionMinY: Float
            get() = JImGui.getWindowContentRegionMinY()
        val windowContentRegionMaxX: Float
            get() = JImGui.getWindowContentRegionMaxX()
        val windowContentRegionMaxY: Float
            get() = JImGui.getWindowContentRegionMaxY()
        val fontTexUvWhitePixelX: Float
            get() = JImGui.getFontTexUvWhitePixelX()
        val fontTexUvWhitePixelY: Float
            get() = JImGui.getFontTexUvWhitePixelY()
        val itemRectMinX: Float
            get() = JImGui.getItemRectMinX()
        val itemRectMinY: Float
            get() = JImGui.getItemRectMinY()
        val itemRectMaxX: Float
            get() = JImGui.getItemRectMaxX()
        val itemRectMaxY: Float
            get() = JImGui.getItemRectMaxY()
        val itemRectSizeX: Float
            get() = JImGui.getItemRectSizeX()
        val itemRectSizeY: Float
            get() = JImGui.getItemRectSizeY()
        val mousePosOnOpeningCurrentPopupX: Float
            get() = JImGui.getMousePosOnOpeningCurrentPopupX()
        val mousePosOnOpeningCurrentPopupY: Float
            get() = JImGui.getMousePosOnOpeningCurrentPopupY()

        /**separator, generally horizontal. inside a menu bar or in horizontal layout mode, this becomes a vertical separator. */
        fun separator() = JImGui.separator()
        /**call between widgets or groups to layout them horizontally. X position given in window coordinates. */
        fun sameLine(posX: Float, spacingW: Float) = JImGui.sameLine(posX, spacingW)
        /**undo a SameLine() or force a new line when in an horizontal-layout context. */
        fun newLine() = JImGui.newLine()
        /**add vertical spacing. */
        fun spacing() = JImGui.spacing()
        /**add a dummy item of given size. unlike InvisibleButton(), Dummy() won't take the mouse click or be navigable into. */
        fun dummy(width: Float, height: Float) = JImGui.dummy(width, height)
        /**move content position toward the right, by style.IndentSpacing or indent_w if != 0 */
        fun indent(indentW: Float) = JImGui.indent(indentW)
        /**move content position back to the left, by style.IndentSpacing or indent_w if != 0 */
        fun unindent(indentW: Float) = JImGui.unindent(indentW)
        /**lock horizontal starting position */
        fun beginGroup() = JImGui.beginGroup()
        /**unlock horizontal starting position + capture the whole group bounding box into one "item" (so you can use IsItemHovered() or layout primitives such as SameLine() on whole group, etc.) */
        fun endGroup() = JImGui.endGroup()
        /**(some functions are using window-relative coordinates, such as: GetCursorPos, GetCursorStartPos, GetContentRegionMax, GetWindowContentRegion* etc. */
        /**GetWindowPos() + GetCursorPos() == GetCursorScreenPos() etc.) */
        var cursorPosX: Float
            get() = JImGui.getCursorPosX()
            set(value) = JImGui.setCursorPosX(value)
        /**other functions such as GetCursorScreenPos or everything in ImDrawList:: */
        /** */
        var cursorPosY: Float
            get() = JImGui.getCursorPosY()
            set(value) = JImGui.setCursorPosY(value)
        /**are using the main, absolute coordinate system. */
        fun setCursorPos(posX: Float, spacingW: Float) = JImGui.setCursorPos(posX, spacingW)
        /**cursor position in absolute screen coordinates [0..io.DisplaySize] */
        fun setCursorScreenPos(screenPosX: Float, screenPosY: Float) = JImGui.setCursorScreenPos(screenPosX, screenPosY)
        /**vertically align upcoming text baseline to FramePadding.y so that it will align properly to regularly framed items (call if you have text on a line before a framed item) */
        fun alignTextToFramePadding() = JImGui.alignTextToFramePadding()
        /**~ FontSize */
        val textLineHeight: Float
            get() = JImGui.getTextLineHeight()
        /**~ FontSize + style.ItemSpacing.y (distance in pixels between 2 consecutive lines of text) */
        val textLineHeightWithSpacing: Float
            get() = JImGui.getTextLineHeightWithSpacing()
        /**~ FontSize + style.FramePadding.y * 2 */
        val frameHeight: Float
            get() = JImGui.getFrameHeight()
        /**~ FontSize + style.FramePadding.y * 2 + style.ItemSpacing.y (distance in pixels between 2 consecutive lines of framed widgets) */
        val frameHeightWithSpacing: Float
            get() = JImGui.getFrameHeightWithSpacing()
        /**add basic help|info block (not a window): how to manipulate ImGui as a end-user (mouse|keyboard controls). */
        fun showUserGuide() = JImGui.showUserGuide()
        val isWindowAppearing: Boolean
            get() = JImGui.isWindowAppearing()
        val isWindowCollapsed: Boolean
            get() = JImGui.isWindowCollapsed()
        /**is current window focused? or its root|child, depending on flags. see flags for options. */
        fun isWindowFocused(flags: Int): Boolean = JImGui.isWindowFocused(flags)
        /**is current window hovered (and typically: not blocked by a popup|modal)? see flags for options. NB: If you are trying to check whether your mouse should be dispatched to imgui or to your app, you should use the 'io.WantCaptureMouse' boolean for that! Please read the FAQ! */
        fun isWindowHovered(flags: Int): Boolean = JImGui.isWindowHovered(flags)
        /**get current window width (shortcut for GetWindowSize().x) */
        val windowWidth: Float
            get() = JImGui.getWindowWidth()
        /**get current window height (shortcut for GetWindowSize().y) */
        val windowHeight: Float
            get() = JImGui.getWindowHeight()
        /** */
        val windowContentRegionWidth: Float
            get() = JImGui.getWindowContentRegionWidth()
        /**set next window position. call before Begin(). use pivot=(0.5f,0.5f) to center on given point, etc. */
        fun setNextWindowPos(posX: Float, posY: Float, condition: Int, windowPosPivotX: Float, windowPosPivotY: Float) = JImGui.setNextWindowPos(posX, posY, condition, windowPosPivotX, windowPosPivotY)
        /**set next window size. set axis to 0.0f to force an auto-fit on this axis. call before Begin() */
        fun setNextWindowSize(width: Float, height: Float, condition: Int) = JImGui.setNextWindowSize(width, height, condition)
        /**set next window size limits. use -1,-1 on either X|Y axis to preserve the current size. Sizes will be rounded down. Use callback to apply non-trivial programmatic constraints. */
        fun setNextWindowSizeConstraints(widthMin: Float, heightMin: Float, widthMax: Float, heightMax: Float) = JImGui.setNextWindowSizeConstraints(widthMin, heightMin, widthMax, heightMax)
        /**set next window content size (~ scrollable client area, which enforce the range of scrollbars). Not including window decorations (title bar, menu bar, etc.) nor WindowPadding. set an axis to 0.0f to leave it automatic. call before Begin() */
        fun setNextWindowContentSize(width: Float, height: Float) = JImGui.setNextWindowContentSize(width, height)
        /**set next window collapsed state. call before Begin() */
        fun setNextWindowCollapsed(collapsed: Boolean, condition: Int) = JImGui.setNextWindowCollapsed(collapsed, condition)
        /**set next window to be focused | front-most. call before Begin() */
        fun setNextWindowFocus() = JImGui.setNextWindowFocus()
        /**set next window background color alpha. helper to easily modify ImGuiCol_WindowBg|ChildBg|PopupBg. you may also use ImGuiWindowFlags_NoBackground. */
        fun setNextWindowBgAlpha(alpha: Float) = JImGui.setNextWindowBgAlpha(alpha)
        /**set font scale. Adjust IO.FontGlobalScale if you want to scale all windows */
        fun setWindowFontScale(scale: Float) = JImGui.setWindowFontScale(scale)
        /**map ImGuiKey_* values into user's key index. == io.KeyMap[key] */
        fun getKeyIndex(imguiKey: Int): Int = JImGui.getKeyIndex(imguiKey)
        /**is key being held. == io.KeysDown[user_key_index]. note that imgui doesn't know the semantic of each entry of io.KeysDown[]. Use your own indices|enums according to how your backend|engine stored them into io.KeysDown[]! */
        fun isKeyDown(userKeyIndex: Int): Boolean = JImGui.isKeyDown(userKeyIndex)
        /**was key pressed (went from !Down to Down). if repeat=true, uses io.KeyRepeatDelay | KeyRepeatRate */
        fun isKeyPressed(userKeyIndex: Int, repeat: Boolean): Boolean = JImGui.isKeyPressed(userKeyIndex, repeat)
        /**was key released (went from Down to !Down).. */
        fun isKeyReleased(userKeyIndex: Int): Boolean = JImGui.isKeyReleased(userKeyIndex)
        /**uses provided repeat rate|delay. return a count, most often 0 or 1 but might be >1 if RepeatRate is small enough that DeltaTime > RepeatRate */
        fun getKeyPressedAmount(keyIndex: Int, repeatDelay: Float, rate: Float): Int = JImGui.getKeyPressedAmount(keyIndex, repeatDelay, rate)
        /**is mouse button held (0=left, 1=right, 2=middle) */
        fun isMouseDown(button: Int): Boolean = JImGui.isMouseDown(button)
        /**is any mouse button held */
        val isAnyMouseDown: Boolean
            get() = JImGui.isAnyMouseDown()
        /**did mouse button clicked (went from !Down to Down) (0=left, 1=right, 2=middle) */
        fun isMouseClicked(button: Int, repeat: Boolean): Boolean = JImGui.isMouseClicked(button, repeat)
        /**did mouse button double-clicked. a double-click returns false in IsMouseClicked(). uses io.MouseDoubleClickTime. */
        fun isMouseDoubleClicked(button: Int): Boolean = JImGui.isMouseDoubleClicked(button)
        /**did mouse button released (went from Down to !Down) */
        fun isMouseReleased(button: Int): Boolean = JImGui.isMouseReleased(button)
        /**is mouse dragging. if lock_threshold < -1.0f uses io.MouseDraggingThreshold */
        fun isMouseDragging(button: Int, lockThreshold: Float): Boolean = JImGui.isMouseDragging(button, lockThreshold)
        /**is mouse hovering given bounding rect (in screen space). clipped by current clipping settings, but disregarding of other consideration of focus|window ordering|popup-block. */
        fun isMouseHoveringRect(widthRMin: Float, heightRMin: Float, widthRMax: Float, heightRMax: Float, clip: Boolean): Boolean = JImGui.isMouseHoveringRect(widthRMin, heightRMin, widthRMax, heightRMax, clip)
        /**by convention we use (-FLT_MAX,-FLT_MAX) to denote that there is no mouse */
        val isMousePosValid: Boolean
            get() = JImGui.isMousePosValid()
        /**attention: misleading name! manually override io.WantCaptureKeyboard flag next frame (said flag is entirely left for your application to handle). e.g. force capture keyboard when your widget is being hovered. This is equivalent to setting "io.WantCaptureKeyboard = want_capture_keyboard_value"; after the next NewFrame() call. */
        fun captureKeyboardFromApp(capture: Boolean) = JImGui.captureKeyboardFromApp(capture)
        /**attention: misleading name! manually override io.WantCaptureMouse flag next frame (said flag is entirely left for your application to handle). This is equivalent to setting "io.WantCaptureMouse = want_capture_mouse_value;" after the next NewFrame() call. */
        fun captureMouseFromApp(capture: Boolean) = JImGui.captureMouseFromApp(capture)
        /**pop from the ID stack. */
        fun popID() = JImGui.popID()
        /**Automatically called on the last call of Step() that returns false. */
        fun end() = JImGui.end()
        fun beginChild(id: Int, width: Float, height: Float, border: Boolean, flags: Int): Boolean = JImGui.beginChild(id, width, height, border, flags)
        fun endChild() = JImGui.endChild()
        /**draw a small circle and keep the cursor on the same line. advance cursor x position by GetTreeNodeToLabelSpacing(), same distance that TreeNode() uses */
        fun bullet() = JImGui.bullet()
        /**only call EndCombo() if BeginCombo() returns true! */
        fun endCombo() = JImGui.endCombo()
        /**terminate the scrolling region. only call ListBoxFooter() if ListBoxHeader() returned true! */
        fun listBoxFooter() = JImGui.listBoxFooter()
        /**~ Unindent()+PopId() */
        fun treePop() = JImGui.treePop()
        /**advance cursor x position by GetTreeNodeToLabelSpacing() */
        fun treeAdvanceToLabelPos() = JImGui.treeAdvanceToLabelPos()
        /**horizontal distance preceding label when using TreeNode*() or Bullet() == (g.FontSize + style.FramePadding.x*2) for a regular unframed TreeNode */
        val treeNodeToLabelSpacing: Float
            get() = JImGui.getTreeNodeToLabelSpacing()
        /**set next TreeNode|CollapsingHeader open state. */
        fun setNextItemOpen(isOpen: Boolean, condition: Int) = JImGui.SetNextItemOpen(isOpen, condition)
        /**initialize current options (generally on application startup) if you want to select a default format, picker type, etc. User will be able to change many settings, unless you pass the _NoOptions flag to your calls. */
        fun setColorEditOptions(flags: Int) = JImGui.setColorEditOptions(flags)
        /**begin|append a tooltip window. to create full-featured tooltip (with any kind of items). */
        fun beginTooltip() = JImGui.beginTooltip()
        fun endTooltip() = JImGui.endTooltip()
        /**create and append to a full screen menu-bar. */
        fun beginMainMenuBar(): Boolean = JImGui.beginMainMenuBar()
        /**only call EndMainMenuBar() if BeginMainMenuBar() returns true! */
        fun endMainMenuBar() = JImGui.endMainMenuBar()
        /**append to menu-bar of current window (requires ImGuiWindowFlags_MenuBar flag set on parent window). */
        fun beginMenuBar(): Boolean = JImGui.beginMenuBar()
        /**only call EndMenuBar() if BeginMenuBar() returns true! */
        fun endMenuBar() = JImGui.endMenuBar()
        /**only call EndMenu() if BeginMenu() returns true! */
        fun endMenu() = JImGui.endMenu()
        /**only call EndPopup() if BeginPopupXXX() returns true! */
        fun endPopup() = JImGui.endPopup()
        /**close the popup we have begin-ed into. clicking on a MenuItem or Selectable automatically close the current popup. */
        fun closeCurrentPopup() = JImGui.closeCurrentPopup()
        /**next column, defaults to current row or next row if the current row is finished */
        fun nextColumn() = JImGui.nextColumn()
        /**get current column index */
        val columnIndex: Int
            get() = JImGui.getColumnIndex()
        /**get column width (in pixels). pass -1 to use current column */
        fun getColumnWidth(columnIndex: Int): Float = JImGui.getColumnWidth(columnIndex)
        /**get position of column line (in pixels, from the left side of the contents region). pass -1 to use current column, otherwise 0..GetColumnsCount() inclusive. column 0 is typically 0.0f */
        fun getColumnOffset(columnIndex: Int): Float = JImGui.getColumnOffset(columnIndex)
        /**set column width (in pixels). pass -1 to use current column */
        fun setColumnWidth(columnIndex: Int, width: Float) = JImGui.setColumnWidth(columnIndex, width)
        /**set position of column line (in pixels, from the left side of the contents region). pass -1 to use current column */
        fun setColumnOffset(columnIndex: Int, offsetX: Float) = JImGui.setColumnOffset(columnIndex, offsetX)
        val columnsCount: Int
            get() = JImGui.getColumnsCount()
        /**only call EndTabBar() if BeginTabBar() returns true! */
        fun endTabBar() = JImGui.endTabBar()
        /**only call EndTabItem() if BeginTabItem() returns true! */
        fun endTabItem() = JImGui.endTabItem()
        /**start logging to tty (stdout) */
        fun logToTTY(maxDepth: Int) = JImGui.logToTTY(maxDepth)
        /**start logging to OS clipboard */
        fun logToClipboard(maxDepth: Int) = JImGui.logToClipboard(maxDepth)
        /**stop logging (close file, etc.) */
        fun logFinish() = JImGui.logFinish()
        /**helper to display buttons for logging to tty|file|clipboard */
        fun logButtons() = JImGui.logButtons()
        /**Render-level scissoring. This is passed down to your render function but not used for CPU-side coarse clipping. Prefer using higher-level ImGui::PushClipRect() to affect logic (hit-testing and widget culling) */
        fun pushClipRect(widthClipRectMin: Float, heightClipRectMin: Float, widthClipRectMax: Float, heightClipRectMax: Float, intersectWithCurrentClipRect: Boolean) = JImGui.pushClipRect(widthClipRectMin, heightClipRectMin, widthClipRectMax, heightClipRectMax, intersectWithCurrentClipRect)
        fun popClipRect() = JImGui.popClipRect()
        /**set width of items for common large "item+label" widgets. >0.0f: width in pixels, <0.0f align xx pixels to the right of window (so -1.0f always align width to the right side). 0.0f = default to ~2|3 of windows width, */
        fun pushItemWidth(itemWidth: Float) = JImGui.pushItemWidth(itemWidth)
        fun popItemWidth() = JImGui.popItemWidth()
        /**set width of the _next_ common large "item+label" widget. >0.0f: width in pixels, <0.0f align xx pixels to the right of window (so -1.0f always align width to the right side) */
        fun setNextItemWidth(itemWidth: Float) = JImGui.setNextItemWidth(itemWidth)
        /**width of item given pushed settings and current cursor position */
        fun calcItemWidth(): Float = JImGui.calcItemWidth()
        /**word-wrapping for Text*() commands. < 0.0f: no wrapping; 0.0f: wrap to end of window (or column); > 0.0f: wrap at 'wrap_pos_x' position in window local space */
        fun pushTextWrapPos(wrapPosX: Float) = JImGui.pushTextWrapPos(wrapPosX)
        fun popTextWrapPos() = JImGui.popTextWrapPos()
        /**allow focusing using TAB|Shift-TAB, enabled by default but you can disable it for certain widgets */
        fun pushAllowKeyboardFocus(allowKeyboardFocus: Boolean) = JImGui.pushAllowKeyboardFocus(allowKeyboardFocus)
        fun popAllowKeyboardFocus() = JImGui.popAllowKeyboardFocus()
        /**in 'repeat' mode, Button*() functions return repeated true in a typematic manner (using io.KeyRepeatDelay|io.KeyRepeatRate setting). Note that you can call IsItemActive() after any Button() to tell if the button is held in the current frame. */
        fun pushButtonRepeat(repeat: Boolean) = JImGui.pushButtonRepeat(repeat)
        fun popButtonRepeat() = JImGui.popButtonRepeat()
        /**get current font size (= height in pixels) of current font with current scale applied */
        val fontSize: Float
            get() = JImGui.getFontSize()
        fun popFont() = JImGui.popFont()
        fun popStyleColor(count: Int) = JImGui.popStyleColor(count)
        fun popStyleVar(count: Int) = JImGui.popStyleVar(count)
        /**make last item the default focused item of a window. */
        fun setItemDefaultFocus() = JImGui.setItemDefaultFocus()
        /**focus keyboard on the next widget. Use positive 'offset' to access sub components of a multiple component widget. Use -1 to access previous widget. */
        fun setKeyboardFocusHere(offset: Int) = JImGui.setKeyboardFocusHere(offset)
        /**is the last item hovered? (and usable, aka not blocked by a popup, etc.). See ImGuiHoveredFlags for more options. */
        fun isItemHovered(flags: Int): Boolean = JImGui.isItemHovered(flags)
        /**is the last item active? (e.g. button being held, text field being edited. This will continuously return true while holding mouse button on an item. Items that don't interact will always return false) */
        val isItemActive: Boolean
            get() = JImGui.isItemActive()
        /**is the last item focused for keyboard|gamepad navigation? */
        val isItemFocused: Boolean
            get() = JImGui.isItemFocused()
        /**is the last item clicked? (e.g. button|node just clicked on) == IsMouseClicked(mouse_button) && IsItemHovered() */
        fun isItemClicked(mouseButton: Int): Boolean = JImGui.isItemClicked(mouseButton)
        /**is the last item visible? (items may be out of sight because of clipping|scrolling) */
        val isItemVisible: Boolean
            get() = JImGui.isItemVisible()
        /**was the last item just made inactive (item was previously active). Useful for Undo|Redo patterns with widgets that requires continuous editing. */
        val isItemDeactivated: Boolean
            get() = JImGui.isItemDeactivated()
        /**was the last item just made inactive and made a value change when it was active? (e.g. Slider|Drag moved). Useful for Undo|Redo patterns with widgets that requires continuous editing. Note that you may get false positives (some widgets such as Combo()|ListBox()|Selectable() will return true even when clicking an already selected item). */
        val isItemDeactivatedAfterEdit: Boolean
            get() = JImGui.isItemDeactivatedAfterEdit()
        /**is any item hovered? */
        val isAnyItemHovered: Boolean
            get() = JImGui.isAnyItemHovered()
        /**is any item active? */
        val isAnyItemActive: Boolean
            get() = JImGui.isAnyItemActive()
        /**is any item focused? */
        val isAnyItemFocused: Boolean
            get() = JImGui.isAnyItemFocused()
        /**allow last item to be overlapped by a subsequent item. sometimes useful with invisible buttons, selectables, etc. to catch unused area. */
        fun setItemAllowOverlap() = JImGui.setItemAllowOverlap()
        /**test if rectangle (of given size, starting from cursor position) is visible | not clipped.
         * test if rectangle (in screen space) is visible | not clipped. to perform coarse clipping on user's side. */
        fun isRectVisible(width: Float, height: Float): Boolean = JImGui.isRectVisible(width, height)
        /**get global imgui time. incremented by io.DeltaTime every frame. */
        val time: Float
            get() = JImGui.getTime()
        /**get global imgui frame count. incremented by 1 every frame. */
        val frameCount: Int
            get() = JImGui.getFrameCount()
        /**adjust scrolling amount to make current cursor position visible. center_y_ratio=0.0: top, 0.5: center, 1.0: bottom. When using to make a "default|current item" visible, consider using SetItemDefaultFocus() instead. */
        fun setScrollHereY(centerYRatio: Float) = JImGui.setScrollHereY(centerYRatio)
        /**adjust scrolling amount to make given position visible. Generally GetCursorStartPos() + offset to compute a valid position. */
        fun setScrollFromPosY(posY: Float, centerYRatio: Float) = JImGui.setScrollFromPosY(posY, centerYRatio)
        /**get scrolling amount [0..GetScrollMaxX()] */
        /**set scrolling amount [0..GetScrollMaxX()] */
        var scrollX: Float
            get() = JImGui.getScrollX()
            set(value) = JImGui.setScrollX(value)
        /**get scrolling amount [0..GetScrollMaxY()] */
        /**set scrolling amount [0..GetScrollMaxY()] */
        var scrollY: Float
            get() = JImGui.getScrollY()
            set(value) = JImGui.setScrollY(value)
        /**get maximum scrolling amount ~~ ContentSize.X - WindowSize.X */
        val scrollMaxX: Float
            get() = JImGui.getScrollMaxX()
        /**get maximum scrolling amount ~~ ContentSize.Y - WindowSize.Y */
        val scrollMaxY: Float
            get() = JImGui.getScrollMaxY()
    }

    val DEFAULT_TITLE = "ImGui window created by JImGui"

    fun pushID(intID: Int) = JImGui.pushID(intID)

    val windowPosX: Float
        get() = JImGui.getWindowPosX()
    val windowPosY: Float
        get() = JImGui.getWindowPosY()
    val contentRegionMaxX: Float
        get() = JImGui.getContentRegionMaxX()
    val contentRegionMaxY: Float
        get() = JImGui.getContentRegionMaxY()
    val windowContentRegionMinX: Float
        get() = JImGui.getWindowContentRegionMinX()
    val windowContentRegionMinY: Float
        get() = JImGui.getWindowContentRegionMinY()
    val windowContentRegionMaxX: Float
        get() = JImGui.getWindowContentRegionMaxX()
    val windowContentRegionMaxY: Float
        get() = JImGui.getWindowContentRegionMaxY()
    val fontTexUvWhitePixelX: Float
        get() = JImGui.getFontTexUvWhitePixelX()
    val fontTexUvWhitePixelY: Float
        get() = JImGui.getFontTexUvWhitePixelY()
    val itemRectMinX: Float
        get() = JImGui.getItemRectMinX()
    val itemRectMinY: Float
        get() = JImGui.getItemRectMinY()
    val itemRectMaxX: Float
        get() = JImGui.getItemRectMaxX()
    val itemRectMaxY: Float
        get() = JImGui.getItemRectMaxY()
    val itemRectSizeX: Float
        get() = JImGui.getItemRectSizeX()
    val itemRectSizeY: Float
        get() = JImGui.getItemRectSizeY()
    val mousePosOnOpeningCurrentPopupX: Float
        get() = JImGui.getMousePosOnOpeningCurrentPopupX()
    val mousePosOnOpeningCurrentPopupY: Float
        get() = JImGui.getMousePosOnOpeningCurrentPopupY()

    /**separator, generally horizontal. inside a menu bar or in horizontal layout mode, this becomes a vertical separator. */
    fun separator() = JImGui.separator()
    /**call between widgets or groups to layout them horizontally. X position given in window coordinates. */
    fun sameLine(posX: Float, spacingW: Float) = JImGui.sameLine(posX, spacingW)
    /**undo a SameLine() or force a new line when in an horizontal-layout context. */
    fun newLine() = JImGui.newLine()
    /**add vertical spacing. */
    fun spacing() = JImGui.spacing()
    /**add a dummy item of given size. unlike InvisibleButton(), Dummy() won't take the mouse click or be navigable into. */
    fun dummy(width: Float, height: Float) = JImGui.dummy(width, height)
    /**move content position toward the right, by style.IndentSpacing or indent_w if != 0 */
    fun indent(indentW: Float) = JImGui.indent(indentW)
    /**move content position back to the left, by style.IndentSpacing or indent_w if != 0 */
    fun unindent(indentW: Float) = JImGui.unindent(indentW)
    /**lock horizontal starting position */
    fun beginGroup() = JImGui.beginGroup()
    /**unlock horizontal starting position + capture the whole group bounding box into one "item" (so you can use IsItemHovered() or layout primitives such as SameLine() on whole group, etc.) */
    fun endGroup() = JImGui.endGroup()
    /**(some functions are using window-relative coordinates, such as: GetCursorPos, GetCursorStartPos, GetContentRegionMax, GetWindowContentRegion* etc. */
    /**GetWindowPos() + GetCursorPos() == GetCursorScreenPos() etc.) */
    var cursorPosX: Float
        get() = JImGui.getCursorPosX()
        set(value) = JImGui.setCursorPosX(value)
    /**other functions such as GetCursorScreenPos or everything in ImDrawList:: */
    /** */
    var cursorPosY: Float
        get() = JImGui.getCursorPosY()
        set(value) = JImGui.setCursorPosY(value)
    /**are using the main, absolute coordinate system. */
    fun setCursorPos(posX: Float, spacingW: Float) = JImGui.setCursorPos(posX, spacingW)
    /**cursor position in absolute screen coordinates [0..io.DisplaySize] */
    fun setCursorScreenPos(screenPosX: Float, screenPosY: Float) = JImGui.setCursorScreenPos(screenPosX, screenPosY)
    /**vertically align upcoming text baseline to FramePadding.y so that it will align properly to regularly framed items (call if you have text on a line before a framed item) */
    fun alignTextToFramePadding() = JImGui.alignTextToFramePadding()
    /**~ FontSize */
    val textLineHeight: Float
        get() = JImGui.getTextLineHeight()
    /**~ FontSize + style.ItemSpacing.y (distance in pixels between 2 consecutive lines of text) */
    val textLineHeightWithSpacing: Float
        get() = JImGui.getTextLineHeightWithSpacing()
    /**~ FontSize + style.FramePadding.y * 2 */
    val frameHeight: Float
        get() = JImGui.getFrameHeight()
    /**~ FontSize + style.FramePadding.y * 2 + style.ItemSpacing.y (distance in pixels between 2 consecutive lines of framed widgets) */
    val frameHeightWithSpacing: Float
        get() = JImGui.getFrameHeightWithSpacing()
    /**add basic help|info block (not a window): how to manipulate ImGui as a end-user (mouse|keyboard controls). */
    fun showUserGuide() = JImGui.showUserGuide()
    val isWindowAppearing: Boolean
        get() = JImGui.isWindowAppearing()
    val isWindowCollapsed: Boolean
        get() = JImGui.isWindowCollapsed()
    /**is current window focused? or its root|child, depending on flags. see flags for options. */
    fun isWindowFocused(flags: Int): Boolean = JImGui.isWindowFocused(flags)
    /**is current window hovered (and typically: not blocked by a popup|modal)? see flags for options. NB: If you are trying to check whether your mouse should be dispatched to imgui or to your app, you should use the 'io.WantCaptureMouse' boolean for that! Please read the FAQ! */
    fun isWindowHovered(flags: Int): Boolean = JImGui.isWindowHovered(flags)
    /**get current window width (shortcut for GetWindowSize().x) */
    val windowWidth: Float
        get() = JImGui.getWindowWidth()
    /**get current window height (shortcut for GetWindowSize().y) */
    val windowHeight: Float
        get() = JImGui.getWindowHeight()
    /** */
    val windowContentRegionWidth: Float
        get() = JImGui.getWindowContentRegionWidth()
    /**set next window position. call before Begin(). use pivot=(0.5f,0.5f) to center on given point, etc. */
    fun setNextWindowPos(posX: Float, posY: Float, condition: Int, windowPosPivotX: Float, windowPosPivotY: Float) = JImGui.setNextWindowPos(posX, posY, condition, windowPosPivotX, windowPosPivotY)
    /**set next window size. set axis to 0.0f to force an auto-fit on this axis. call before Begin() */
    fun setNextWindowSize(width: Float, height: Float, condition: Int) = JImGui.setNextWindowSize(width, height, condition)
    /**set next window size limits. use -1,-1 on either X|Y axis to preserve the current size. Sizes will be rounded down. Use callback to apply non-trivial programmatic constraints. */
    fun setNextWindowSizeConstraints(widthMin: Float, heightMin: Float, widthMax: Float, heightMax: Float) = JImGui.setNextWindowSizeConstraints(widthMin, heightMin, widthMax, heightMax)
    /**set next window content size (~ scrollable client area, which enforce the range of scrollbars). Not including window decorations (title bar, menu bar, etc.) nor WindowPadding. set an axis to 0.0f to leave it automatic. call before Begin() */
    fun setNextWindowContentSize(width: Float, height: Float) = JImGui.setNextWindowContentSize(width, height)
    /**set next window collapsed state. call before Begin() */
    fun setNextWindowCollapsed(collapsed: Boolean, condition: Int) = JImGui.setNextWindowCollapsed(collapsed, condition)
    /**set next window to be focused | front-most. call before Begin() */
    fun setNextWindowFocus() = JImGui.setNextWindowFocus()
    /**set next window background color alpha. helper to easily modify ImGuiCol_WindowBg|ChildBg|PopupBg. you may also use ImGuiWindowFlags_NoBackground. */
    fun setNextWindowBgAlpha(alpha: Float) = JImGui.setNextWindowBgAlpha(alpha)
    /**set font scale. Adjust IO.FontGlobalScale if you want to scale all windows */
    fun setWindowFontScale(scale: Float) = JImGui.setWindowFontScale(scale)
    /**map ImGuiKey_* values into user's key index. == io.KeyMap[key] */
    fun getKeyIndex(imguiKey: Int): Int = JImGui.getKeyIndex(imguiKey)
    /**is key being held. == io.KeysDown[user_key_index]. note that imgui doesn't know the semantic of each entry of io.KeysDown[]. Use your own indices|enums according to how your backend|engine stored them into io.KeysDown[]! */
    fun isKeyDown(userKeyIndex: Int): Boolean = JImGui.isKeyDown(userKeyIndex)
    /**was key pressed (went from !Down to Down). if repeat=true, uses io.KeyRepeatDelay | KeyRepeatRate */
    fun isKeyPressed(userKeyIndex: Int, repeat: Boolean): Boolean = JImGui.isKeyPressed(userKeyIndex, repeat)
    /**was key released (went from Down to !Down).. */
    fun isKeyReleased(userKeyIndex: Int): Boolean = JImGui.isKeyReleased(userKeyIndex)
    /**uses provided repeat rate|delay. return a count, most often 0 or 1 but might be >1 if RepeatRate is small enough that DeltaTime > RepeatRate */
    fun getKeyPressedAmount(keyIndex: Int, repeatDelay: Float, rate: Float): Int = JImGui.getKeyPressedAmount(keyIndex, repeatDelay, rate)
    /**is mouse button held (0=left, 1=right, 2=middle) */
    fun isMouseDown(button: Int): Boolean = JImGui.isMouseDown(button)
    /**is any mouse button held */
    val isAnyMouseDown: Boolean
        get() = JImGui.isAnyMouseDown()
    /**did mouse button clicked (went from !Down to Down) (0=left, 1=right, 2=middle) */
    fun isMouseClicked(button: Int, repeat: Boolean): Boolean = JImGui.isMouseClicked(button, repeat)
    /**did mouse button double-clicked. a double-click returns false in IsMouseClicked(). uses io.MouseDoubleClickTime. */
    fun isMouseDoubleClicked(button: Int): Boolean = JImGui.isMouseDoubleClicked(button)
    /**did mouse button released (went from Down to !Down) */
    fun isMouseReleased(button: Int): Boolean = JImGui.isMouseReleased(button)
    /**is mouse dragging. if lock_threshold < -1.0f uses io.MouseDraggingThreshold */
    fun isMouseDragging(button: Int, lockThreshold: Float): Boolean = JImGui.isMouseDragging(button, lockThreshold)
    /**is mouse hovering given bounding rect (in screen space). clipped by current clipping settings, but disregarding of other consideration of focus|window ordering|popup-block. */
    fun isMouseHoveringRect(widthRMin: Float, heightRMin: Float, widthRMax: Float, heightRMax: Float, clip: Boolean): Boolean = JImGui.isMouseHoveringRect(widthRMin, heightRMin, widthRMax, heightRMax, clip)
    /**by convention we use (-FLT_MAX,-FLT_MAX) to denote that there is no mouse */
    val isMousePosValid: Boolean
        get() = JImGui.isMousePosValid()
    /**attention: misleading name! manually override io.WantCaptureKeyboard flag next frame (said flag is entirely left for your application to handle). e.g. force capture keyboard when your widget is being hovered. This is equivalent to setting "io.WantCaptureKeyboard = want_capture_keyboard_value"; after the next NewFrame() call. */
    fun captureKeyboardFromApp(capture: Boolean) = JImGui.captureKeyboardFromApp(capture)
    /**attention: misleading name! manually override io.WantCaptureMouse flag next frame (said flag is entirely left for your application to handle). This is equivalent to setting "io.WantCaptureMouse = want_capture_mouse_value;" after the next NewFrame() call. */
    fun captureMouseFromApp(capture: Boolean) = JImGui.captureMouseFromApp(capture)
    /**pop from the ID stack. */
    fun popID() = JImGui.popID()
    /**Automatically called on the last call of Step() that returns false. */
    fun end() = JImGui.end()
    fun beginChild(id: Int, width: Float, height: Float, border: Boolean, flags: Int): Boolean = JImGui.beginChild(id, width, height, border, flags)
    fun endChild() = JImGui.endChild()
    /**draw a small circle and keep the cursor on the same line. advance cursor x position by GetTreeNodeToLabelSpacing(), same distance that TreeNode() uses */
    fun bullet() = JImGui.bullet()
    /**only call EndCombo() if BeginCombo() returns true! */
    fun endCombo() = JImGui.endCombo()
    /**terminate the scrolling region. only call ListBoxFooter() if ListBoxHeader() returned true! */
    fun listBoxFooter() = JImGui.listBoxFooter()
    /**~ Unindent()+PopId() */
    fun treePop() = JImGui.treePop()
    /**advance cursor x position by GetTreeNodeToLabelSpacing() */
    fun treeAdvanceToLabelPos() = JImGui.treeAdvanceToLabelPos()
    /**horizontal distance preceding label when using TreeNode*() or Bullet() == (g.FontSize + style.FramePadding.x*2) for a regular unframed TreeNode */
    val treeNodeToLabelSpacing: Float
        get() = JImGui.getTreeNodeToLabelSpacing()
    /**set next TreeNode|CollapsingHeader open state. */
    fun setNextItemOpen(isOpen: Boolean, condition: Int) = JImGui.SetNextItemOpen(isOpen, condition)
    /**initialize current options (generally on application startup) if you want to select a default format, picker type, etc. User will be able to change many settings, unless you pass the _NoOptions flag to your calls. */
    fun setColorEditOptions(flags: Int) = JImGui.setColorEditOptions(flags)
    /**begin|append a tooltip window. to create full-featured tooltip (with any kind of items). */
    fun beginTooltip() = JImGui.beginTooltip()
    fun endTooltip() = JImGui.endTooltip()
    /**create and append to a full screen menu-bar. */
    fun beginMainMenuBar(): Boolean = JImGui.beginMainMenuBar()
    /**only call EndMainMenuBar() if BeginMainMenuBar() returns true! */
    fun endMainMenuBar() = JImGui.endMainMenuBar()
    /**append to menu-bar of current window (requires ImGuiWindowFlags_MenuBar flag set on parent window). */
    fun beginMenuBar(): Boolean = JImGui.beginMenuBar()
    /**only call EndMenuBar() if BeginMenuBar() returns true! */
    fun endMenuBar() = JImGui.endMenuBar()
    /**only call EndMenu() if BeginMenu() returns true! */
    fun endMenu() = JImGui.endMenu()
    /**only call EndPopup() if BeginPopupXXX() returns true! */
    fun endPopup() = JImGui.endPopup()
    /**close the popup we have begin-ed into. clicking on a MenuItem or Selectable automatically close the current popup. */
    fun closeCurrentPopup() = JImGui.closeCurrentPopup()
    /**next column, defaults to current row or next row if the current row is finished */
    fun nextColumn() = JImGui.nextColumn()
    /**get current column index */
    val columnIndex: Int
        get() = JImGui.getColumnIndex()
    /**get column width (in pixels). pass -1 to use current column */
    fun getColumnWidth(columnIndex: Int): Float = JImGui.getColumnWidth(columnIndex)
    /**get position of column line (in pixels, from the left side of the contents region). pass -1 to use current column, otherwise 0..GetColumnsCount() inclusive. column 0 is typically 0.0f */
    fun getColumnOffset(columnIndex: Int): Float = JImGui.getColumnOffset(columnIndex)
    /**set column width (in pixels). pass -1 to use current column */
    fun setColumnWidth(columnIndex: Int, width: Float) = JImGui.setColumnWidth(columnIndex, width)
    /**set position of column line (in pixels, from the left side of the contents region). pass -1 to use current column */
    fun setColumnOffset(columnIndex: Int, offsetX: Float) = JImGui.setColumnOffset(columnIndex, offsetX)
    val columnsCount: Int
        get() = JImGui.getColumnsCount()
    /**only call EndTabBar() if BeginTabBar() returns true! */
    fun endTabBar() = JImGui.endTabBar()
    /**only call EndTabItem() if BeginTabItem() returns true! */
    fun endTabItem() = JImGui.endTabItem()
    /**start logging to tty (stdout) */
    fun logToTTY(maxDepth: Int) = JImGui.logToTTY(maxDepth)
    /**start logging to OS clipboard */
    fun logToClipboard(maxDepth: Int) = JImGui.logToClipboard(maxDepth)
    /**stop logging (close file, etc.) */
    fun logFinish() = JImGui.logFinish()
    /**helper to display buttons for logging to tty|file|clipboard */
    fun logButtons() = JImGui.logButtons()
    /**Render-level scissoring. This is passed down to your render function but not used for CPU-side coarse clipping. Prefer using higher-level ImGui::PushClipRect() to affect logic (hit-testing and widget culling) */
    fun pushClipRect(widthClipRectMin: Float, heightClipRectMin: Float, widthClipRectMax: Float, heightClipRectMax: Float, intersectWithCurrentClipRect: Boolean) = JImGui.pushClipRect(widthClipRectMin, heightClipRectMin, widthClipRectMax, heightClipRectMax, intersectWithCurrentClipRect)
    fun popClipRect() = JImGui.popClipRect()
    /**set width of items for common large "item+label" widgets. >0.0f: width in pixels, <0.0f align xx pixels to the right of window (so -1.0f always align width to the right side). 0.0f = default to ~2|3 of windows width, */
    fun pushItemWidth(itemWidth: Float) = JImGui.pushItemWidth(itemWidth)
    fun popItemWidth() = JImGui.popItemWidth()
    /**set width of the _next_ common large "item+label" widget. >0.0f: width in pixels, <0.0f align xx pixels to the right of window (so -1.0f always align width to the right side) */
    fun setNextItemWidth(itemWidth: Float) = JImGui.setNextItemWidth(itemWidth)
    /**width of item given pushed settings and current cursor position */
    fun calcItemWidth(): Float = JImGui.calcItemWidth()
    /**word-wrapping for Text*() commands. < 0.0f: no wrapping; 0.0f: wrap to end of window (or column); > 0.0f: wrap at 'wrap_pos_x' position in window local space */
    fun pushTextWrapPos(wrapPosX: Float) = JImGui.pushTextWrapPos(wrapPosX)
    fun popTextWrapPos() = JImGui.popTextWrapPos()
    /**allow focusing using TAB|Shift-TAB, enabled by default but you can disable it for certain widgets */
    fun pushAllowKeyboardFocus(allowKeyboardFocus: Boolean) = JImGui.pushAllowKeyboardFocus(allowKeyboardFocus)
    fun popAllowKeyboardFocus() = JImGui.popAllowKeyboardFocus()
    /**in 'repeat' mode, Button*() functions return repeated true in a typematic manner (using io.KeyRepeatDelay|io.KeyRepeatRate setting). Note that you can call IsItemActive() after any Button() to tell if the button is held in the current frame. */
    fun pushButtonRepeat(repeat: Boolean) = JImGui.pushButtonRepeat(repeat)
    fun popButtonRepeat() = JImGui.popButtonRepeat()
    /**get current font size (= height in pixels) of current font with current scale applied */
    val fontSize: Float
        get() = JImGui.getFontSize()
    fun popFont() = JImGui.popFont()
    fun popStyleColor(count: Int) = JImGui.popStyleColor(count)
    fun popStyleVar(count: Int) = JImGui.popStyleVar(count)
    /**make last item the default focused item of a window. */
    fun setItemDefaultFocus() = JImGui.setItemDefaultFocus()
    /**focus keyboard on the next widget. Use positive 'offset' to access sub components of a multiple component widget. Use -1 to access previous widget. */
    fun setKeyboardFocusHere(offset: Int) = JImGui.setKeyboardFocusHere(offset)
    /**is the last item hovered? (and usable, aka not blocked by a popup, etc.). See ImGuiHoveredFlags for more options. */
    fun isItemHovered(flags: Int): Boolean = JImGui.isItemHovered(flags)
    /**is the last item active? (e.g. button being held, text field being edited. This will continuously return true while holding mouse button on an item. Items that don't interact will always return false) */
    val isItemActive: Boolean
        get() = JImGui.isItemActive()
    /**is the last item focused for keyboard|gamepad navigation? */
    val isItemFocused: Boolean
        get() = JImGui.isItemFocused()
    /**is the last item clicked? (e.g. button|node just clicked on) == IsMouseClicked(mouse_button) && IsItemHovered() */
    fun isItemClicked(mouseButton: Int): Boolean = JImGui.isItemClicked(mouseButton)
    /**is the last item visible? (items may be out of sight because of clipping|scrolling) */
    val isItemVisible: Boolean
        get() = JImGui.isItemVisible()
    /**was the last item just made inactive (item was previously active). Useful for Undo|Redo patterns with widgets that requires continuous editing. */
    val isItemDeactivated: Boolean
        get() = JImGui.isItemDeactivated()
    /**was the last item just made inactive and made a value change when it was active? (e.g. Slider|Drag moved). Useful for Undo|Redo patterns with widgets that requires continuous editing. Note that you may get false positives (some widgets such as Combo()|ListBox()|Selectable() will return true even when clicking an already selected item). */
    val isItemDeactivatedAfterEdit: Boolean
        get() = JImGui.isItemDeactivatedAfterEdit()
    /**is any item hovered? */
    val isAnyItemHovered: Boolean
        get() = JImGui.isAnyItemHovered()
    /**is any item active? */
    val isAnyItemActive: Boolean
        get() = JImGui.isAnyItemActive()
    /**is any item focused? */
    val isAnyItemFocused: Boolean
        get() = JImGui.isAnyItemFocused()
    /**allow last item to be overlapped by a subsequent item. sometimes useful with invisible buttons, selectables, etc. to catch unused area. */
    fun setItemAllowOverlap() = JImGui.setItemAllowOverlap()
    /**test if rectangle (of given size, starting from cursor position) is visible | not clipped.
     * test if rectangle (in screen space) is visible | not clipped. to perform coarse clipping on user's side. */
    fun isRectVisible(width: Float, height: Float): Boolean = JImGui.isRectVisible(width, height)
    /**get global imgui time. incremented by io.DeltaTime every frame. */
    val time: Float
        get() = JImGui.getTime()
    /**get global imgui frame count. incremented by 1 every frame. */
    val frameCount: Int
        get() = JImGui.getFrameCount()
    /**adjust scrolling amount to make current cursor position visible. center_y_ratio=0.0: top, 0.5: center, 1.0: bottom. When using to make a "default|current item" visible, consider using SetItemDefaultFocus() instead. */
    fun setScrollHereY(centerYRatio: Float) = JImGui.setScrollHereY(centerYRatio)
    /**adjust scrolling amount to make given position visible. Generally GetCursorStartPos() + offset to compute a valid position. */
    fun setScrollFromPosY(posY: Float, centerYRatio: Float) = JImGui.setScrollFromPosY(posY, centerYRatio)
    /**get scrolling amount [0..GetScrollMaxX()] */
    /**set scrolling amount [0..GetScrollMaxX()] */
    var scrollX: Float
        get() = JImGui.getScrollX()
        set(value) = JImGui.setScrollX(value)
    /**get scrolling amount [0..GetScrollMaxY()] */
    /**set scrolling amount [0..GetScrollMaxY()] */
    var scrollY: Float
        get() = JImGui.getScrollY()
        set(value) = JImGui.setScrollY(value)
    /**get maximum scrolling amount ~~ ContentSize.X - WindowSize.X */
    val scrollMaxX: Float
        get() = JImGui.getScrollMaxX()
    /**get maximum scrolling amount ~~ ContentSize.Y - WindowSize.Y */
    val scrollMaxY: Float
        get() = JImGui.getScrollMaxY()


    // extensions =====================================================================================================

    fun listBox(label: String, selectedIndex: Int, items: List<String>, height: Int): Int {
        listBoxHeader(label, height)
        var newSelectedIndex = selectedIndex
        withNative(false) { selected ->
            items.forEachIndexed { i, item ->
                selected.modifyValue(selectedIndex == i)
                selectable(item, selected)
                if(selected.accessValue())
                    newSelectedIndex = i
            }
        }
        listBoxFooter()
        return newSelectedIndex
    }

    inline fun menu(label: String, callback: () -> Unit) {
        this.beginMenu(label)
        callback()
        this.endMenu()
    }

    var cursorPos: Vec2
        get() = vec(cursorPosX, cursorPosY)
        set(value) {
            cursorPosX = value.xf
            cursorPosY = value.yf
        }
    var scroll: Vec2
        get() = vec(scrollX, scrollY)
        set(value) {
            scrollX = value.xf
            scrollY = value.yf
        }
    val scrollMax: Vec2
        get() = vec(scrollMaxX, scrollMaxY)

    val contentRegionRect: Rect
        get() = rect(0, 0, contentRegionMaxX, contentRegionMaxY)
    val windowContentRegionRect: Rect
        get() = rect(windowContentRegionMinX, windowContentRegionMinY, windowContentRegionMaxX, windowContentRegionMaxY)

    val itemRect: Rect
        get() = rect(itemRectMinX, itemRectMinY, itemRectMaxX, itemRectMaxY)
    val itemRectSize: Vec2
        get() = vec(itemRectSizeX, itemRectSizeY)

    var platformWindowPos: Vec2
        get() = vec(platformWindowPosX, platformWindowPosY)
        set(value) {
            platformWindowPosX = value.xf
            platformWindowPosY = value.yf
        }
    var platformWindowSize: Vec2
        get() = vec(platformWindowSizeX, platformWindowSizeY)
        set(value) {
            platformWindowSizeX = value.xf
            platformWindowSizeY = value.yf
        }
    var platformWindowRect: Rect
        get() = rect(
            platformWindowPosX, platformWindowPosY,
            platformWindowPosX + platformWindowSizeX, platformWindowPosY + platformWindowSizeY
        )
        set(value) {
            platformWindowPosX = value.min.xf
            platformWindowPosY = value.min.yf
            platformWindowSizeX = value.max.xf - value.min.xf
            platformWindowSizeY = value.max.yf - value.min.yf
        }

    fun pushClipRect(clipRect: Rect, intersectWithCurrentClipRect: Boolean)
        = JImGui.pushClipRect(clipRect.min.xf, clipRect.min.yf, clipRect.max.xf, clipRect.max.yf, intersectWithCurrentClipRect)
}
