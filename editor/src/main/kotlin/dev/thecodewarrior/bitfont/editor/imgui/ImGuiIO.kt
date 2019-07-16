@file:Suppress("unused")

package dev.thecodewarrior.bitfont.editor.imgui

import dev.thecodewarrior.bitfont.editor.utils.delegate
import dev.thecodewarrior.bitfont.editor.utils.math.Vec2
import dev.thecodewarrior.bitfont.editor.utils.math.vec
import org.ice1000.jimgui.*
import org.ice1000.jimgui.flag.JImBackendFlags
import org.ice1000.jimgui.flag.JImConfigFlags
import org.ice1000.jimgui.flag.JImMouseIndexes
import org.intellij.lang.annotations.MagicConstant
import org.jetbrains.annotations.Contract
import java.lang.ref.WeakReference

class ImGuiIO(val wrapped: JImGuiIO) {

    val inputString: String get() = wrapped.inputString

    val fonts: JImFontAtlas get() = wrapped.fonts

    val fontDefault: JImFont get() = wrapped.fontDefault

    val inputChars: CharArray get() = wrapped.inputChars

    fun getMouseClickedPosX(index: Int): Float = wrapped.getMouseClickedPosX(index)
    fun getMouseClickedPosY(index: Int): Float = wrapped.getMouseClickedPosY(index)
    fun getMouseDragMaxDistanceAbsX(index: Int): Float = wrapped.getMouseDragMaxDistanceAbsX(index)
    fun getMouseDragMaxDistanceAbsY(index: Int): Float = wrapped.getMouseDragMaxDistanceAbsY(index)

    fun addInputCharacter(character: Char) = wrapped.addInputCharacter(character)

    /**Queue new characters input from an UTF-8 string */
    fun addInputCharactersUTF8(characters: String) = wrapped.addInputCharactersUTF8(characters)

    /**Queue new characters input from an UTF-8 string */
    fun addInputCharactersUTF8(characters: JImStr) = wrapped.addInputCharactersUTF8(characters)

    /**= "imgui.ini"    || Path to .ini file. Set NULL to disable automatic .ini loading|saving, if e.g. you want to manually load|save from memory. */
    fun setIniFilename(newValue: String) = wrapped.setIniFilename(newValue)

    /**= "imgui_log.txt"|| Path to .log file (default parameter to ImGui::LogToFile when no file is specified). */
    fun setLogFilename(newValue: String) = wrapped.setLogFilename(newValue)

    companion object {

        /**= (1, 1)         || For retina display or other situations where window coordinates are different from framebuffer coordinates. This generally ends up in ImDrawData::FramebufferScale. */
        /**= (1, 1)         || For retina display or other situations where window coordinates are different from framebuffer coordinates. This generally ends up in ImDrawData::FramebufferScale. */
        var displayFramebufferScaleX: Float
            get() = JImGuiIO.getDisplayFramebufferScaleX()
            set(value) = JImGuiIO.setDisplayFramebufferScaleX(value)
        /**= (1, 1)         || For retina display or other situations where window coordinates are different from framebuffer coordinates. This generally ends up in ImDrawData::FramebufferScale. */
        /**= (1, 1)         || For retina display or other situations where window coordinates are different from framebuffer coordinates. This generally ends up in ImDrawData::FramebufferScale. */
        var displayFramebufferScaleY: Float
            get() = JImGuiIO.getDisplayFramebufferScaleY()
            set(value) = JImGuiIO.setDisplayFramebufferScaleY(value)
        /**<unset>          || Main display size, in pixels.
         * Size of the viewport to render (== io.DisplaySize for the main viewport) (DisplayPos + DisplaySize == lower-right of the orthogonal projection matrix to use)</unset> */
        /**<unset>          || Main display size, in pixels.
         * Size of the viewport to render (== io.DisplaySize for the main viewport) (DisplayPos + DisplaySize == lower-right of the orthogonal projection matrix to use)</unset> */
        var displaySizeX: Float
            get() = JImGuiIO.getDisplaySizeX()
            set(value) = JImGuiIO.setDisplaySizeX(value)
        /**<unset>          || Main display size, in pixels.
         * Size of the viewport to render (== io.DisplaySize for the main viewport) (DisplayPos + DisplaySize == lower-right of the orthogonal projection matrix to use)</unset> */
        /**<unset>          || Main display size, in pixels.
         * Size of the viewport to render (== io.DisplaySize for the main viewport) (DisplayPos + DisplaySize == lower-right of the orthogonal projection matrix to use)</unset> */
        var displaySizeY: Float
            get() = JImGuiIO.getDisplaySizeY()
            set(value) = JImGuiIO.setDisplaySizeY(value)
        /**Mouse position, in pixels. Set to ImVec2(-FLT_MAX,-FLT_MAX) if mouse is unavailable (on another screen, etc.) */
        /**Mouse position, in pixels. Set to ImVec2(-FLT_MAX,-FLT_MAX) if mouse is unavailable (on another screen, etc.) */
        var mousePosX: Float
            get() = JImGuiIO.getMousePosX()
            set(value) = JImGuiIO.setMousePosX(value)
        /**Mouse position, in pixels. Set to ImVec2(-FLT_MAX,-FLT_MAX) if mouse is unavailable (on another screen, etc.) */
        /**Mouse position, in pixels. Set to ImVec2(-FLT_MAX,-FLT_MAX) if mouse is unavailable (on another screen, etc.) */
        var mousePosY: Float
            get() = JImGuiIO.getMousePosY()
            set(value) = JImGuiIO.setMousePosY(value)
        /**Mouse delta. Note that this is zero if either current or previous position are invalid (-FLT_MAX,-FLT_MAX), so a disappearing|reappearing mouse won't have a huge delta. */
        /**Mouse delta. Note that this is zero if either current or previous position are invalid (-FLT_MAX,-FLT_MAX), so a disappearing|reappearing mouse won't have a huge delta. */
        var mouseDeltaX: Float
            get() = JImGuiIO.getMouseDeltaX()
            set(value) = JImGuiIO.setMouseDeltaX(value)
        /**Mouse delta. Note that this is zero if either current or previous position are invalid (-FLT_MAX,-FLT_MAX), so a disappearing|reappearing mouse won't have a huge delta. */
        /**Mouse delta. Note that this is zero if either current or previous position are invalid (-FLT_MAX,-FLT_MAX), so a disappearing|reappearing mouse won't have a huge delta. */
        var mouseDeltaY: Float
            get() = JImGuiIO.getMouseDeltaY()
            set(value) = JImGuiIO.setMouseDeltaY(value)
        /**Previous mouse position (note that MouseDelta is not necessary == MousePos-MousePosPrev, in case either position is invalid) */
        /**Previous mouse position (note that MouseDelta is not necessary == MousePos-MousePosPrev, in case either position is invalid) */
        var mousePosPrevX: Float
            get() = JImGuiIO.getMousePosPrevX()
            set(value) = JImGuiIO.setMousePosPrevX(value)
        /**Previous mouse position (note that MouseDelta is not necessary == MousePos-MousePosPrev, in case either position is invalid) */
        /**Previous mouse position (note that MouseDelta is not necessary == MousePos-MousePosPrev, in case either position is invalid) */
        var mousePosPrevY: Float
            get() = JImGuiIO.getMousePosPrevY()
            set(value) = JImGuiIO.setMousePosPrevY(value)

        /**Clear the text input buffer manually */
        fun clearInputCharacters() = JImGuiIO.clearInputCharacters()

        /**Queue new character input */
        fun addInputCharacter(character: Int) = JImGuiIO.addInputCharacter(character)

        /**Queue of _characters_ input (obtained by platform back-end). Fill using AddInputCharacter() helper. */
        fun inputQueueCharacterAt(index: Int): Int = JImGuiIO.inputQueueCharacterAt(index)

        /**Queue of _characters_ input (obtained by platform back-end). Fill using AddInputCharacter() helper. */
        fun inputQueueCharacter(index: Int, newValue: Int) = JImGuiIO.inputQueueCharacter(index, newValue)

        /**Gamepad inputs. Cleared back to zero by EndFrame(). Keyboard keys will be auto-mapped and be written here by NewFrame(). */
        fun navInputAt(index: Int): Float = JImGuiIO.navInputAt(index)

        /**Gamepad inputs. Cleared back to zero by EndFrame(). Keyboard keys will be auto-mapped and be written here by NewFrame(). */
        fun navInput(index: Int, newValue: Float) = JImGuiIO.navInput(index, newValue)

        /**Time of last click (used to figure out double-click) */
        @MagicConstant(valuesFromClass = JImMouseIndexes::class)
        fun mouseClickedTimeAt(index: Int): Float = JImGuiIO.mouseClickedTimeAt(index)

        /**Time of last click (used to figure out double-click) */
        fun mouseClickedTime(index: Int, newValue: Float) = JImGuiIO.mouseClickedTime(index, newValue)

        /**Duration the mouse button has been down (0.0f == just clicked) */
        @MagicConstant(valuesFromClass = JImMouseIndexes::class)
        fun mouseDownDurationAt(index: Int): Float = JImGuiIO.mouseDownDurationAt(index)

        /**Duration the mouse button has been down (0.0f == just clicked) */
        fun mouseDownDuration(index: Int, newValue: Float) = JImGuiIO.mouseDownDuration(index, newValue)

        /**Previous time the mouse button has been down */
        @MagicConstant(valuesFromClass = JImMouseIndexes::class)
        fun mouseDownDurationPrevAt(index: Int): Float = JImGuiIO.mouseDownDurationPrevAt(index)

        /**Previous time the mouse button has been down */
        fun mouseDownDurationPrev(index: Int, newValue: Float) = JImGuiIO.mouseDownDurationPrev(index, newValue)

        /**Squared maximum distance of how much mouse has traveled from the clicking point */
        @MagicConstant(valuesFromClass = JImMouseIndexes::class)
        fun mouseDragMaxDistanceSqrAt(index: Int): Float = JImGuiIO.mouseDragMaxDistanceSqrAt(index)

        /**Squared maximum distance of how much mouse has traveled from the clicking point */
        fun mouseDragMaxDistanceSqr(index: Int, newValue: Float) = JImGuiIO.mouseDragMaxDistanceSqr(index, newValue)

        /**Duration the keyboard key has been down (0.0f == just pressed) */
        fun keysDownDurationAt(index: Int): Float = JImGuiIO.keysDownDurationAt(index)

        /**Duration the keyboard key has been down (0.0f == just pressed) */

        /**Previous duration the key has been down */
        fun keysDownDurationPrevAt(index: Int): Float = JImGuiIO.keysDownDurationPrevAt(index)

        /**Previous duration the key has been down */
        fun keysDownDurationPrev(index: Int, newValue: Float) = JImGuiIO.keysDownDurationPrev(index, newValue)

        fun navInputsDownDurationAt(index: Int): Float = JImGuiIO.navInputsDownDurationAt(index)
        fun navInputsDownDuration(index: Int, newValue: Float) = JImGuiIO.navInputsDownDuration(index, newValue)
        fun navInputsDownDurationPrevAt(index: Int): Float = JImGuiIO.navInputsDownDurationPrevAt(index)
        fun navInputsDownDurationPrev(index: Int, newValue: Float) = JImGuiIO.navInputsDownDurationPrev(index, newValue)
        /**Vertices output during last call to Render() */
        /**Vertices output during last call to Render() */
        var metricsRenderVertices: Int
            get() = JImGuiIO.getMetricsRenderVertices()
            set(value) = JImGuiIO.setMetricsRenderVertices(value)
        /**Indices output during last call to Render() = number of triangles * 3 */
        /**Indices output during last call to Render() = number of triangles * 3 */
        var metricsRenderIndices: Int
            get() = JImGuiIO.getMetricsRenderIndices()
            set(value) = JImGuiIO.setMetricsRenderIndices(value)
        /**Number of active windows */
        /**Number of active windows */
        var metricsActiveWindows: Int
            get() = JImGuiIO.getMetricsActiveWindows()
            set(value) = JImGuiIO.setMetricsActiveWindows(value)

        /**<unset>          || Map of indices into the KeysDown[512] entries array which represent your "native" keyboard state.</unset> */
        fun keyMapAt(index: Int): Int = JImGuiIO.keyMapAt(index)

        /**<unset>          || Map of indices into the KeysDown[512] entries array which represent your "native" keyboard state.</unset> */
        fun keyMap(index: Int, newValue: Int) = JImGuiIO.keyMap(index, newValue)
        /**= 0              || See ImGuiConfigFlags_ enum. Set by user|application. Gamepad|keyboard navigation options, etc. */
        /**= 0              || See ImGuiConfigFlags_ enum. Set by user|application. Gamepad|keyboard navigation options, etc. */
        var configFlags: Int
            get() = JImGuiIO.getConfigFlags()
            set(value) = JImGuiIO.setConfigFlags(value)
        /**= 0              || See ImGuiBackendFlags_ enum. Set by back-end (imgui_impl_xxx files or custom back-end) to communicate features supported by the back-end. */
        /**= 0              || See ImGuiBackendFlags_ enum. Set by back-end (imgui_impl_xxx files or custom back-end) to communicate features supported by the back-end. */
        var backendFlags: Int
            get() = JImGuiIO.getBackendFlags()
            set(value) = JImGuiIO.setBackendFlags(value)
        /**= 0.30f          || Time for a double-click, in seconds. */
        /**= 0.30f          || Time for a double-click, in seconds. */
        var mouseDoubleClickTime: Float
            get() = JImGuiIO.getMouseDoubleClickTime()
            set(value) = JImGuiIO.setMouseDoubleClickTime(value)
        /**= 6.0f           || Distance threshold to stay in to validate a double-click, in pixels. */
        /**= 6.0f           || Distance threshold to stay in to validate a double-click, in pixels. */
        var mouseDoubleClickMaxDist: Float
            get() = JImGuiIO.getMouseDoubleClickMaxDist()
            set(value) = JImGuiIO.setMouseDoubleClickMaxDist(value)
        /**= 0.250f         || When holding a key|button, time before it starts repeating, in seconds (for buttons in Repeat mode, etc.). */
        /**= 0.250f         || When holding a key|button, time before it starts repeating, in seconds (for buttons in Repeat mode, etc.). */
        var keyRepeatDelay: Float
            get() = JImGuiIO.getKeyRepeatDelay()
            set(value) = JImGuiIO.setKeyRepeatDelay(value)
        /**= 0.050f         || When holding a key|button, rate at which it repeats, in seconds. */
        /**= 0.050f         || When holding a key|button, rate at which it repeats, in seconds. */
        var keyRepeatRate: Float
            get() = JImGuiIO.getKeyRepeatRate()
            set(value) = JImGuiIO.setKeyRepeatRate(value)
        /**= 1.0f           || Global scale all fonts */
        /**= 1.0f           || Global scale all fonts */
        var fontGlobalScale: Float
            get() = JImGuiIO.getFontGlobalScale()
            set(value) = JImGuiIO.setFontGlobalScale(value)
        /**Mouse wheel Vertical: 1 unit scrolls about 5 lines text. */
        /**Mouse wheel Vertical: 1 unit scrolls about 5 lines text. */
        var mouseWheel: Float
            get() = JImGuiIO.getMouseWheel()
            set(value) = JImGuiIO.setMouseWheel(value)
        /**Mouse wheel Horizontal. Most users don't have a mouse with an horizontal wheel, may not be filled by all back-ends. */
        /**Mouse wheel Horizontal. Most users don't have a mouse with an horizontal wheel, may not be filled by all back-ends. */
        var mouseWheelH: Float
            get() = JImGuiIO.getMouseWheelH()
            set(value) = JImGuiIO.setMouseWheelH(value)
        /**Application framerate estimation, in frame per second. Solely for convenience. Rolling average estimation based on IO.DeltaTime over 120 frames */
        /**Application framerate estimation, in frame per second. Solely for convenience. Rolling average estimation based on IO.DeltaTime over 120 frames */
        var framerate: Float
            get() = JImGuiIO.getFramerate()
            set(value) = JImGuiIO.setFramerate(value)
        /**= 1.0f|60.0f     || Time elapsed since last frame, in seconds. */
        /**= 1.0f|60.0f     || Time elapsed since last frame, in seconds. */
        var deltaTime: Float
            get() = JImGuiIO.getDeltaTime()
            set(value) = JImGuiIO.setDeltaTime(value)
        /**= 5.0f           || Minimum time between saving positions|sizes to .ini file, in seconds. */
        /**= 5.0f           || Minimum time between saving positions|sizes to .ini file, in seconds. */
        var iniSavingRate: Float
            get() = JImGuiIO.getIniSavingRate()
            set(value) = JImGuiIO.setIniSavingRate(value)

        /**Keyboard keys that are pressed (ideally left in the "native" order your engine has access to keyboard keys, so you can use your own defines|enums for keys). */
        fun keyDownAt(index: Int): Boolean = JImGuiIO.keyDownAt(index)

        /**Keyboard keys that are pressed (ideally left in the "native" order your engine has access to keyboard keys, so you can use your own defines|enums for keys). */
        fun keyDown(index: Int, newValue: Boolean) = JImGuiIO.keyDown(index, newValue)

        /**Mouse button went from !Down to Down */
        fun mouseClickedAt(index: Int): Boolean = JImGuiIO.mouseClickedAt(index)

        /**Mouse button went from !Down to Down */
        fun mouseClicked(index: Int, newValue: Boolean) = JImGuiIO.mouseClicked(index, newValue)

        /**Has mouse button been double-clicked? */
        fun mouseDoubleClickedAt(index: Int): Boolean = JImGuiIO.mouseDoubleClickedAt(index)

        /**Has mouse button been double-clicked? */
        fun mouseDoubleClicked(index: Int, newValue: Boolean) = JImGuiIO.mouseDoubleClicked(index, newValue)

        /**Mouse button went from Down to !Down */
        fun mouseReleasedAt(index: Int): Boolean = JImGuiIO.mouseReleasedAt(index)

        /**Mouse button went from Down to !Down */
        fun mouseReleased(index: Int, newValue: Boolean) = JImGuiIO.mouseReleased(index, newValue)

        /**Track if button was clicked inside an imgui window. We don't request mouse capture from the application if click started outside ImGui bounds. */
        fun mouseDownOwnedAt(index: Int): Boolean = JImGuiIO.mouseDownOwnedAt(index)

        /**Track if button was clicked inside an imgui window. We don't request mouse capture from the application if click started outside ImGui bounds. */
        fun mouseDownOwned(index: Int, newValue: Boolean) = JImGuiIO.mouseDownOwned(index, newValue)
        /**= false          || Allow user scaling text of individual window with CTRL+Wheel. */
        /**= false          || Allow user scaling text of individual window with CTRL+Wheel. */
        var fontAllowUserScaling: Boolean
            get() = JImGuiIO.isFontAllowUserScaling()
            set(value) = JImGuiIO.setFontAllowUserScaling(value)
        /**= defined(__APPLE__) || OS X style: Text editing cursor movement using Alt instead of Ctrl, Shortcuts using Cmd|Super instead of Ctrl, Line|Text Start and End using Cmd+Arrows instead of Home|End, Double click selects by word instead of selecting whole text, Multi-selection in lists uses Cmd|Super instead of Ctrl (was called io.OptMacOSXBehaviors prior to 1.63) */
        /**= defined(__APPLE__) || OS X style: Text editing cursor movement using Alt instead of Ctrl, Shortcuts using Cmd|Super instead of Ctrl, Line|Text Start and End using Cmd+Arrows instead of Home|End, Double click selects by word instead of selecting whole text, Multi-selection in lists uses Cmd|Super instead of Ctrl (was called io.OptMacOSXBehaviors prior to 1.63) */
        var configMacOSXBehaviors: Boolean
            get() = JImGuiIO.isConfigMacOSXBehaviors()
            set(value) = JImGuiIO.setConfigMacOSXBehaviors(value)
        /**= true           || Set to false to disable blinking cursor, for users who consider it distracting. (was called: io.OptCursorBlink prior to 1.63) */
        /**= true           || Set to false to disable blinking cursor, for users who consider it distracting. (was called: io.OptCursorBlink prior to 1.63) */
        var configInputTextCursorBlink: Boolean
            get() = JImGuiIO.isConfigInputTextCursorBlink()
            set(value) = JImGuiIO.setConfigInputTextCursorBlink(value)
        /**= true           || Enable resizing of windows from their edges and from the lower-left corner. This requires (io.BackendFlags & ImGuiBackendFlags_HasMouseCursors) because it needs mouse cursor feedback. (This used to be a per-window ImGuiWindowFlags_ResizeFromAnySide flag) */
        /**= true           || Enable resizing of windows from their edges and from the lower-left corner. This requires (io.BackendFlags & ImGuiBackendFlags_HasMouseCursors) because it needs mouse cursor feedback. (This used to be a per-window ImGuiWindowFlags_ResizeFromAnySide flag) */
        var configWindowsResizeFromEdges: Boolean
            get() = JImGuiIO.isConfigWindowsResizeFromEdges()
            set(value) = JImGuiIO.setConfigWindowsResizeFromEdges(value)
        /**= false       || [BETA] Set to true to only allow moving windows when clicked+dragged from the title bar. Windows without a title bar are not affected. */
        /**= false       || [BETA] Set to true to only allow moving windows when clicked+dragged from the title bar. Windows without a title bar are not affected. */
        var configWindowsMoveFromTitleBarOnly: Boolean
            get() = JImGuiIO.isConfigWindowsMoveFromTitleBarOnly()
            set(value) = JImGuiIO.setConfigWindowsMoveFromTitleBarOnly(value)
        /**= false          || Request ImGui to draw a mouse cursor for you (if you are on a platform without a mouse cursor). Cannot be easily renamed to 'io.ConfigXXX' because this is frequently used by back-end implementations. */
        /**= false          || Request ImGui to draw a mouse cursor for you (if you are on a platform without a mouse cursor). Cannot be easily renamed to 'io.ConfigXXX' because this is frequently used by back-end implementations. */
        var mouseDrawCursor: Boolean
            get() = JImGuiIO.isMouseDrawCursor()
            set(value) = JImGuiIO.setMouseDrawCursor(value)
        /**Keyboard modifier pressed: Control */
        /**Keyboard modifier pressed: Control */
        var keyCtrl: Boolean
            get() = JImGuiIO.isKeyCtrl()
            set(value) = JImGuiIO.setKeyCtrl(value)
        /**Keyboard modifier pressed: Shift */
        /**Keyboard modifier pressed: Shift */
        var keyShift: Boolean
            get() = JImGuiIO.isKeyShift()
            set(value) = JImGuiIO.setKeyShift(value)
        /**Keyboard modifier pressed: Alt */
        /**Keyboard modifier pressed: Alt */
        var keyAlt: Boolean
            get() = JImGuiIO.isKeyAlt()
            set(value) = JImGuiIO.setKeyAlt(value)
        /**Keyboard modifier pressed: Cmd|Super|Windows */
        /**Keyboard modifier pressed: Cmd|Super|Windows */
        var keySuper: Boolean
            get() = JImGuiIO.isKeySuper()
            set(value) = JImGuiIO.setKeySuper(value)
        /**When io.WantCaptureMouse is true, imgui will use the mouse inputs, do not dispatch them to your main game|application (in both cases, always pass on mouse inputs to imgui). (e.g. unclicked mouse is hovering over an imgui window, widget is active, mouse was clicked over an imgui window, etc.). */
        /**When io.WantCaptureMouse is true, imgui will use the mouse inputs, do not dispatch them to your main game|application (in both cases, always pass on mouse inputs to imgui). (e.g. unclicked mouse is hovering over an imgui window, widget is active, mouse was clicked over an imgui window, etc.). */
        var wantCaptureMouse: Boolean
            get() = JImGuiIO.isWantCaptureMouse()
            set(value) = JImGuiIO.setWantCaptureMouse(value)
        /**When io.WantCaptureKeyboard is true, imgui will use the keyboard inputs, do not dispatch them to your main game|application (in both cases, always pass keyboard inputs to imgui). (e.g. InputText active, or an imgui window is focused and navigation is enabled, etc.). */
        /**When io.WantCaptureKeyboard is true, imgui will use the keyboard inputs, do not dispatch them to your main game|application (in both cases, always pass keyboard inputs to imgui). (e.g. InputText active, or an imgui window is focused and navigation is enabled, etc.). */
        var wantCaptureKeyboard: Boolean
            get() = JImGuiIO.isWantCaptureKeyboard()
            set(value) = JImGuiIO.setWantCaptureKeyboard(value)
        /**Mobile|console: when io.WantTextInput is true, you may display an on-screen keyboard. This is set by ImGui when it wants textual keyboard input to happen (e.g. when a InputText widget is active). */
        /**Mobile|console: when io.WantTextInput is true, you may display an on-screen keyboard. This is set by ImGui when it wants textual keyboard input to happen (e.g. when a InputText widget is active). */
        var wantTextInput: Boolean
            get() = JImGuiIO.isWantTextInput()
            set(value) = JImGuiIO.setWantTextInput(value)
        /**MousePos has been altered, back-end should reposition mouse on next frame. Set only when ImGuiConfigFlags_NavEnableSetMousePos flag is enabled. */
        /**MousePos has been altered, back-end should reposition mouse on next frame. Set only when ImGuiConfigFlags_NavEnableSetMousePos flag is enabled. */
        var wantSetMousePos: Boolean
            get() = JImGuiIO.isWantSetMousePos()
            set(value) = JImGuiIO.setWantSetMousePos(value)
        /**When manual .ini load|save is active (io.IniFilename == NULL), this will be set to notify your application that you can call SaveIniSettingsToMemory() and save yourself. IMPORTANT: You need to clear io.WantSaveIniSettings yourself. */
        /**When manual .ini load|save is active (io.IniFilename == NULL), this will be set to notify your application that you can call SaveIniSettingsToMemory() and save yourself. IMPORTANT: You need to clear io.WantSaveIniSettings yourself. */
        var wantSaveIniSettings: Boolean
            get() = JImGuiIO.isWantSaveIniSettings()
            set(value) = JImGuiIO.setWantSaveIniSettings(value)
        /**Directional navigation is currently allowed (will handle ImGuiKey_NavXXX events) = a window is focused and it doesn't use the ImGuiWindowFlags_NoNavInputs flag. */
        /**Directional navigation is currently allowed (will handle ImGuiKey_NavXXX events) = a window is focused and it doesn't use the ImGuiWindowFlags_NoNavInputs flag. */
        var navActive: Boolean
            get() = JImGuiIO.isNavActive()
            set(value) = JImGuiIO.setNavActive(value)
        /**Directional navigation is visible and allowed (will handle ImGuiKey_NavXXX events). */
        /**Directional navigation is visible and allowed (will handle ImGuiKey_NavXXX events). */
        var navVisible: Boolean
            get() = JImGuiIO.isNavVisible()
            set(value) = JImGuiIO.setNavVisible(value)
    }

    /**= (1, 1)         || For retina display or other situations where window coordinates are different from framebuffer coordinates. This generally ends up in ImDrawData::FramebufferScale. */
    /**= (1, 1)         || For retina display or other situations where window coordinates are different from framebuffer coordinates. This generally ends up in ImDrawData::FramebufferScale. */
    var displayFramebufferScaleX: Float
        get() = JImGuiIO.getDisplayFramebufferScaleX()
        set(value) = JImGuiIO.setDisplayFramebufferScaleX(value)
    /**= (1, 1)         || For retina display or other situations where window coordinates are different from framebuffer coordinates. This generally ends up in ImDrawData::FramebufferScale. */
    /**= (1, 1)         || For retina display or other situations where window coordinates are different from framebuffer coordinates. This generally ends up in ImDrawData::FramebufferScale. */
    var displayFramebufferScaleY: Float
        get() = JImGuiIO.getDisplayFramebufferScaleY()
        set(value) = JImGuiIO.setDisplayFramebufferScaleY(value)
    /**<unset>          || Main display size, in pixels.
     * Size of the viewport to render (== io.DisplaySize for the main viewport) (DisplayPos + DisplaySize == lower-right of the orthogonal projection matrix to use)</unset> */
    /**<unset>          || Main display size, in pixels.
     * Size of the viewport to render (== io.DisplaySize for the main viewport) (DisplayPos + DisplaySize == lower-right of the orthogonal projection matrix to use)</unset> */
    var displaySizeX: Float
        get() = JImGuiIO.getDisplaySizeX()
        set(value) = JImGuiIO.setDisplaySizeX(value)
    /**<unset>          || Main display size, in pixels.
     * Size of the viewport to render (== io.DisplaySize for the main viewport) (DisplayPos + DisplaySize == lower-right of the orthogonal projection matrix to use)</unset> */
    /**<unset>          || Main display size, in pixels.
     * Size of the viewport to render (== io.DisplaySize for the main viewport) (DisplayPos + DisplaySize == lower-right of the orthogonal projection matrix to use)</unset> */
    var displaySizeY: Float
        get() = JImGuiIO.getDisplaySizeY()
        set(value) = JImGuiIO.setDisplaySizeY(value)
    /**Mouse position, in pixels. Set to ImVec2(-FLT_MAX,-FLT_MAX) if mouse is unavailable (on another screen, etc.) */
    /**Mouse position, in pixels. Set to ImVec2(-FLT_MAX,-FLT_MAX) if mouse is unavailable (on another screen, etc.) */
    var mousePosX: Float
        get() = JImGuiIO.getMousePosX()
        set(value) = JImGuiIO.setMousePosX(value)
    /**Mouse position, in pixels. Set to ImVec2(-FLT_MAX,-FLT_MAX) if mouse is unavailable (on another screen, etc.) */
    /**Mouse position, in pixels. Set to ImVec2(-FLT_MAX,-FLT_MAX) if mouse is unavailable (on another screen, etc.) */
    var mousePosY: Float
        get() = JImGuiIO.getMousePosY()
        set(value) = JImGuiIO.setMousePosY(value)
    /**Mouse delta. Note that this is zero if either current or previous position are invalid (-FLT_MAX,-FLT_MAX), so a disappearing|reappearing mouse won't have a huge delta. */
    /**Mouse delta. Note that this is zero if either current or previous position are invalid (-FLT_MAX,-FLT_MAX), so a disappearing|reappearing mouse won't have a huge delta. */
    var mouseDeltaX: Float
        get() = JImGuiIO.getMouseDeltaX()
        set(value) = JImGuiIO.setMouseDeltaX(value)
    /**Mouse delta. Note that this is zero if either current or previous position are invalid (-FLT_MAX,-FLT_MAX), so a disappearing|reappearing mouse won't have a huge delta. */
    /**Mouse delta. Note that this is zero if either current or previous position are invalid (-FLT_MAX,-FLT_MAX), so a disappearing|reappearing mouse won't have a huge delta. */
    var mouseDeltaY: Float
        get() = JImGuiIO.getMouseDeltaY()
        set(value) = JImGuiIO.setMouseDeltaY(value)
    /**Previous mouse position (note that MouseDelta is not necessary == MousePos-MousePosPrev, in case either position is invalid) */
    /**Previous mouse position (note that MouseDelta is not necessary == MousePos-MousePosPrev, in case either position is invalid) */
    var mousePosPrevX: Float
        get() = JImGuiIO.getMousePosPrevX()
        set(value) = JImGuiIO.setMousePosPrevX(value)
    /**Previous mouse position (note that MouseDelta is not necessary == MousePos-MousePosPrev, in case either position is invalid) */
    /**Previous mouse position (note that MouseDelta is not necessary == MousePos-MousePosPrev, in case either position is invalid) */
    var mousePosPrevY: Float
        get() = JImGuiIO.getMousePosPrevY()
        set(value) = JImGuiIO.setMousePosPrevY(value)

    /**Clear the text input buffer manually */
    fun clearInputCharacters() = JImGuiIO.clearInputCharacters()

    /**Queue new character input */
    fun addInputCharacter(character: Int) = JImGuiIO.addInputCharacter(character)

    /**Queue of _characters_ input (obtained by platform back-end). Fill using AddInputCharacter() helper. */
    fun inputQueueCharacterAt(index: Int): Int = JImGuiIO.inputQueueCharacterAt(index)

    /**Queue of _characters_ input (obtained by platform back-end). Fill using AddInputCharacter() helper. */
    fun inputQueueCharacter(index: Int, newValue: Int) = JImGuiIO.inputQueueCharacter(index, newValue)

    /**Gamepad inputs. Cleared back to zero by EndFrame(). Keyboard keys will be auto-mapped and be written here by NewFrame(). */
    fun navInputAt(index: Int): Float = JImGuiIO.navInputAt(index)

    /**Gamepad inputs. Cleared back to zero by EndFrame(). Keyboard keys will be auto-mapped and be written here by NewFrame(). */
    fun navInput(index: Int, newValue: Float) = JImGuiIO.navInput(index, newValue)

    /**Time of last click (used to figure out double-click) */
    @MagicConstant(valuesFromClass = JImMouseIndexes::class)
    fun mouseClickedTimeAt(index: Int): Float = JImGuiIO.mouseClickedTimeAt(index)

    /**Time of last click (used to figure out double-click) */
    fun mouseClickedTime(index: Int, newValue: Float) = JImGuiIO.mouseClickedTime(index, newValue)

    /**Duration the mouse button has been down (0.0f == just clicked) */
    @MagicConstant(valuesFromClass = JImMouseIndexes::class)
    fun mouseDownDurationAt(index: Int): Float = JImGuiIO.mouseDownDurationAt(index)

    /**Duration the mouse button has been down (0.0f == just clicked) */
    fun mouseDownDuration(index: Int, newValue: Float) = JImGuiIO.mouseDownDuration(index, newValue)

    /**Previous time the mouse button has been down */
    @MagicConstant(valuesFromClass = JImMouseIndexes::class)
    fun mouseDownDurationPrevAt(index: Int): Float = JImGuiIO.mouseDownDurationPrevAt(index)

    /**Previous time the mouse button has been down */
    fun mouseDownDurationPrev(index: Int, newValue: Float) = JImGuiIO.mouseDownDurationPrev(index, newValue)

    /**Squared maximum distance of how much mouse has traveled from the clicking point */
    @MagicConstant(valuesFromClass = JImMouseIndexes::class)
    fun mouseDragMaxDistanceSqrAt(index: Int): Float = JImGuiIO.mouseDragMaxDistanceSqrAt(index)

    /**Squared maximum distance of how much mouse has traveled from the clicking point */
    fun mouseDragMaxDistanceSqr(index: Int, newValue: Float) = JImGuiIO.mouseDragMaxDistanceSqr(index, newValue)

    /**Duration the keyboard key has been down (0.0f == just pressed) */
    fun keysDownDurationAt(index: Int): Float = JImGuiIO.keysDownDurationAt(index)

    /**Duration the keyboard key has been down (0.0f == just pressed) */

    /**Previous duration the key has been down */
    fun keysDownDurationPrevAt(index: Int): Float = JImGuiIO.keysDownDurationPrevAt(index)

    /**Previous duration the key has been down */
    fun keysDownDurationPrev(index: Int, newValue: Float) = JImGuiIO.keysDownDurationPrev(index, newValue)

    fun navInputsDownDurationAt(index: Int): Float = JImGuiIO.navInputsDownDurationAt(index)
    fun navInputsDownDuration(index: Int, newValue: Float) = JImGuiIO.navInputsDownDuration(index, newValue)
    fun navInputsDownDurationPrevAt(index: Int): Float = JImGuiIO.navInputsDownDurationPrevAt(index)
    fun navInputsDownDurationPrev(index: Int, newValue: Float) = JImGuiIO.navInputsDownDurationPrev(index, newValue)
    /**Vertices output during last call to Render() */
    /**Vertices output during last call to Render() */
    var metricsRenderVertices: Int
        get() = JImGuiIO.getMetricsRenderVertices()
        set(value) = JImGuiIO.setMetricsRenderVertices(value)
    /**Indices output during last call to Render() = number of triangles * 3 */
    /**Indices output during last call to Render() = number of triangles * 3 */
    var metricsRenderIndices: Int
        get() = JImGuiIO.getMetricsRenderIndices()
        set(value) = JImGuiIO.setMetricsRenderIndices(value)
    /**Number of active windows */
    /**Number of active windows */
    var metricsActiveWindows: Int
        get() = JImGuiIO.getMetricsActiveWindows()
        set(value) = JImGuiIO.setMetricsActiveWindows(value)

    /**<unset>          || Map of indices into the KeysDown[512] entries array which represent your "native" keyboard state.</unset> */
    fun keyMapAt(index: Int): Int = JImGuiIO.keyMapAt(index)

    /**<unset>          || Map of indices into the KeysDown[512] entries array which represent your "native" keyboard state.</unset> */
    fun keyMap(index: Int, newValue: Int) = JImGuiIO.keyMap(index, newValue)
    /**= 0              || See ImGuiConfigFlags_ enum. Set by user|application. Gamepad|keyboard navigation options, etc. */
    /**= 0              || See ImGuiConfigFlags_ enum. Set by user|application. Gamepad|keyboard navigation options, etc. */
    var configFlags: Int
        get() = JImGuiIO.getConfigFlags()
        set(value) = JImGuiIO.setConfigFlags(value)
    /**= 0              || See ImGuiBackendFlags_ enum. Set by back-end (imgui_impl_xxx files or custom back-end) to communicate features supported by the back-end. */
    /**= 0              || See ImGuiBackendFlags_ enum. Set by back-end (imgui_impl_xxx files or custom back-end) to communicate features supported by the back-end. */
    var backendFlags: Int
        get() = JImGuiIO.getBackendFlags()
        set(value) = JImGuiIO.setBackendFlags(value)
    /**= 0.30f          || Time for a double-click, in seconds. */
    /**= 0.30f          || Time for a double-click, in seconds. */
    var mouseDoubleClickTime: Float
        get() = JImGuiIO.getMouseDoubleClickTime()
        set(value) = JImGuiIO.setMouseDoubleClickTime(value)
    /**= 6.0f           || Distance threshold to stay in to validate a double-click, in pixels. */
    /**= 6.0f           || Distance threshold to stay in to validate a double-click, in pixels. */
    var mouseDoubleClickMaxDist: Float
        get() = JImGuiIO.getMouseDoubleClickMaxDist()
        set(value) = JImGuiIO.setMouseDoubleClickMaxDist(value)
    /**= 0.250f         || When holding a key|button, time before it starts repeating, in seconds (for buttons in Repeat mode, etc.). */
    /**= 0.250f         || When holding a key|button, time before it starts repeating, in seconds (for buttons in Repeat mode, etc.). */
    var keyRepeatDelay: Float
        get() = JImGuiIO.getKeyRepeatDelay()
        set(value) = JImGuiIO.setKeyRepeatDelay(value)
    /**= 0.050f         || When holding a key|button, rate at which it repeats, in seconds. */
    /**= 0.050f         || When holding a key|button, rate at which it repeats, in seconds. */
    var keyRepeatRate: Float
        get() = JImGuiIO.getKeyRepeatRate()
        set(value) = JImGuiIO.setKeyRepeatRate(value)
    /**= 1.0f           || Global scale all fonts */
    /**= 1.0f           || Global scale all fonts */
    var fontGlobalScale: Float
        get() = JImGuiIO.getFontGlobalScale()
        set(value) = JImGuiIO.setFontGlobalScale(value)
    /**Mouse wheel Vertical: 1 unit scrolls about 5 lines text. */
    /**Mouse wheel Vertical: 1 unit scrolls about 5 lines text. */
    var mouseWheel: Float
        get() = JImGuiIO.getMouseWheel()
        set(value) = JImGuiIO.setMouseWheel(value)
    /**Mouse wheel Horizontal. Most users don't have a mouse with an horizontal wheel, may not be filled by all back-ends. */
    /**Mouse wheel Horizontal. Most users don't have a mouse with an horizontal wheel, may not be filled by all back-ends. */
    var mouseWheelH: Float
        get() = JImGuiIO.getMouseWheelH()
        set(value) = JImGuiIO.setMouseWheelH(value)
    /**Application framerate estimation, in frame per second. Solely for convenience. Rolling average estimation based on IO.DeltaTime over 120 frames */
    /**Application framerate estimation, in frame per second. Solely for convenience. Rolling average estimation based on IO.DeltaTime over 120 frames */
    var framerate: Float
        get() = JImGuiIO.getFramerate()
        set(value) = JImGuiIO.setFramerate(value)
    /**= 1.0f|60.0f     || Time elapsed since last frame, in seconds. */
    /**= 1.0f|60.0f     || Time elapsed since last frame, in seconds. */
    var deltaTime: Float
        get() = JImGuiIO.getDeltaTime()
        set(value) = JImGuiIO.setDeltaTime(value)
    /**= 5.0f           || Minimum time between saving positions|sizes to .ini file, in seconds. */
    /**= 5.0f           || Minimum time between saving positions|sizes to .ini file, in seconds. */
    var iniSavingRate: Float
        get() = JImGuiIO.getIniSavingRate()
        set(value) = JImGuiIO.setIniSavingRate(value)

    /**Keyboard keys that are pressed (ideally left in the "native" order your engine has access to keyboard keys, so you can use your own defines|enums for keys). */
    fun keyDownAt(index: Int): Boolean = JImGuiIO.keyDownAt(index)

    /**Keyboard keys that are pressed (ideally left in the "native" order your engine has access to keyboard keys, so you can use your own defines|enums for keys). */
    fun keyDown(index: Int, newValue: Boolean) = JImGuiIO.keyDown(index, newValue)

    /**Mouse button went from !Down to Down */
    fun mouseClickedAt(index: Int): Boolean = JImGuiIO.mouseClickedAt(index)

    /**Mouse button went from !Down to Down */
    fun mouseClicked(index: Int, newValue: Boolean) = JImGuiIO.mouseClicked(index, newValue)

    /**Has mouse button been double-clicked? */
    fun mouseDoubleClickedAt(index: Int): Boolean = JImGuiIO.mouseDoubleClickedAt(index)

    /**Has mouse button been double-clicked? */
    fun mouseDoubleClicked(index: Int, newValue: Boolean) = JImGuiIO.mouseDoubleClicked(index, newValue)

    /**Mouse button went from Down to !Down */
    fun mouseReleasedAt(index: Int): Boolean = JImGuiIO.mouseReleasedAt(index)

    /**Mouse button went from Down to !Down */
    fun mouseReleased(index: Int, newValue: Boolean) = JImGuiIO.mouseReleased(index, newValue)

    /**Track if button was clicked inside an imgui window. We don't request mouse capture from the application if click started outside ImGui bounds. */
    fun mouseDownOwnedAt(index: Int): Boolean = JImGuiIO.mouseDownOwnedAt(index)

    /**Track if button was clicked inside an imgui window. We don't request mouse capture from the application if click started outside ImGui bounds. */
    fun mouseDownOwned(index: Int, newValue: Boolean) = JImGuiIO.mouseDownOwned(index, newValue)
    /**= false          || Allow user scaling text of individual window with CTRL+Wheel. */
    /**= false          || Allow user scaling text of individual window with CTRL+Wheel. */
    var fontAllowUserScaling: Boolean
        get() = JImGuiIO.isFontAllowUserScaling()
        set(value) = JImGuiIO.setFontAllowUserScaling(value)
    /**= defined(__APPLE__) || OS X style: Text editing cursor movement using Alt instead of Ctrl, Shortcuts using Cmd|Super instead of Ctrl, Line|Text Start and End using Cmd+Arrows instead of Home|End, Double click selects by word instead of selecting whole text, Multi-selection in lists uses Cmd|Super instead of Ctrl (was called io.OptMacOSXBehaviors prior to 1.63) */
    /**= defined(__APPLE__) || OS X style: Text editing cursor movement using Alt instead of Ctrl, Shortcuts using Cmd|Super instead of Ctrl, Line|Text Start and End using Cmd+Arrows instead of Home|End, Double click selects by word instead of selecting whole text, Multi-selection in lists uses Cmd|Super instead of Ctrl (was called io.OptMacOSXBehaviors prior to 1.63) */
    var configMacOSXBehaviors: Boolean
        get() = JImGuiIO.isConfigMacOSXBehaviors()
        set(value) = JImGuiIO.setConfigMacOSXBehaviors(value)
    /**= true           || Set to false to disable blinking cursor, for users who consider it distracting. (was called: io.OptCursorBlink prior to 1.63) */
    /**= true           || Set to false to disable blinking cursor, for users who consider it distracting. (was called: io.OptCursorBlink prior to 1.63) */
    var configInputTextCursorBlink: Boolean
        get() = JImGuiIO.isConfigInputTextCursorBlink()
        set(value) = JImGuiIO.setConfigInputTextCursorBlink(value)
    /**= true           || Enable resizing of windows from their edges and from the lower-left corner. This requires (io.BackendFlags & ImGuiBackendFlags_HasMouseCursors) because it needs mouse cursor feedback. (This used to be a per-window ImGuiWindowFlags_ResizeFromAnySide flag) */
    /**= true           || Enable resizing of windows from their edges and from the lower-left corner. This requires (io.BackendFlags & ImGuiBackendFlags_HasMouseCursors) because it needs mouse cursor feedback. (This used to be a per-window ImGuiWindowFlags_ResizeFromAnySide flag) */
    var configWindowsResizeFromEdges: Boolean
        get() = JImGuiIO.isConfigWindowsResizeFromEdges()
        set(value) = JImGuiIO.setConfigWindowsResizeFromEdges(value)
    /**= false       || [BETA] Set to true to only allow moving windows when clicked+dragged from the title bar. Windows without a title bar are not affected. */
    /**= false       || [BETA] Set to true to only allow moving windows when clicked+dragged from the title bar. Windows without a title bar are not affected. */
    var configWindowsMoveFromTitleBarOnly: Boolean
        get() = JImGuiIO.isConfigWindowsMoveFromTitleBarOnly()
        set(value) = JImGuiIO.setConfigWindowsMoveFromTitleBarOnly(value)
    /**= false          || Request ImGui to draw a mouse cursor for you (if you are on a platform without a mouse cursor). Cannot be easily renamed to 'io.ConfigXXX' because this is frequently used by back-end implementations. */
    /**= false          || Request ImGui to draw a mouse cursor for you (if you are on a platform without a mouse cursor). Cannot be easily renamed to 'io.ConfigXXX' because this is frequently used by back-end implementations. */
    var mouseDrawCursor: Boolean
        get() = JImGuiIO.isMouseDrawCursor()
        set(value) = JImGuiIO.setMouseDrawCursor(value)
    /**Keyboard modifier pressed: Control */
    /**Keyboard modifier pressed: Control */
    var keyCtrl: Boolean
        get() = JImGuiIO.isKeyCtrl()
        set(value) = JImGuiIO.setKeyCtrl(value)
    /**Keyboard modifier pressed: Shift */
    /**Keyboard modifier pressed: Shift */
    var keyShift: Boolean
        get() = JImGuiIO.isKeyShift()
        set(value) = JImGuiIO.setKeyShift(value)
    /**Keyboard modifier pressed: Alt */
    /**Keyboard modifier pressed: Alt */
    var keyAlt: Boolean
        get() = JImGuiIO.isKeyAlt()
        set(value) = JImGuiIO.setKeyAlt(value)
    /**Keyboard modifier pressed: Cmd|Super|Windows */
    /**Keyboard modifier pressed: Cmd|Super|Windows */
    var keySuper: Boolean
        get() = JImGuiIO.isKeySuper()
        set(value) = JImGuiIO.setKeySuper(value)
    /**When io.WantCaptureMouse is true, imgui will use the mouse inputs, do not dispatch them to your main game|application (in both cases, always pass on mouse inputs to imgui). (e.g. unclicked mouse is hovering over an imgui window, widget is active, mouse was clicked over an imgui window, etc.). */
    /**When io.WantCaptureMouse is true, imgui will use the mouse inputs, do not dispatch them to your main game|application (in both cases, always pass on mouse inputs to imgui). (e.g. unclicked mouse is hovering over an imgui window, widget is active, mouse was clicked over an imgui window, etc.). */
    var wantCaptureMouse: Boolean
        get() = JImGuiIO.isWantCaptureMouse()
        set(value) = JImGuiIO.setWantCaptureMouse(value)
    /**When io.WantCaptureKeyboard is true, imgui will use the keyboard inputs, do not dispatch them to your main game|application (in both cases, always pass keyboard inputs to imgui). (e.g. InputText active, or an imgui window is focused and navigation is enabled, etc.). */
    /**When io.WantCaptureKeyboard is true, imgui will use the keyboard inputs, do not dispatch them to your main game|application (in both cases, always pass keyboard inputs to imgui). (e.g. InputText active, or an imgui window is focused and navigation is enabled, etc.). */
    var wantCaptureKeyboard: Boolean
        get() = JImGuiIO.isWantCaptureKeyboard()
        set(value) = JImGuiIO.setWantCaptureKeyboard(value)
    /**Mobile|console: when io.WantTextInput is true, you may display an on-screen keyboard. This is set by ImGui when it wants textual keyboard input to happen (e.g. when a InputText widget is active). */
    /**Mobile|console: when io.WantTextInput is true, you may display an on-screen keyboard. This is set by ImGui when it wants textual keyboard input to happen (e.g. when a InputText widget is active). */
    var wantTextInput: Boolean
        get() = JImGuiIO.isWantTextInput()
        set(value) = JImGuiIO.setWantTextInput(value)
    /**MousePos has been altered, back-end should reposition mouse on next frame. Set only when ImGuiConfigFlags_NavEnableSetMousePos flag is enabled. */
    /**MousePos has been altered, back-end should reposition mouse on next frame. Set only when ImGuiConfigFlags_NavEnableSetMousePos flag is enabled. */
    var wantSetMousePos: Boolean
        get() = JImGuiIO.isWantSetMousePos()
        set(value) = JImGuiIO.setWantSetMousePos(value)
    /**When manual .ini load|save is active (io.IniFilename == NULL), this will be set to notify your application that you can call SaveIniSettingsToMemory() and save yourself. IMPORTANT: You need to clear io.WantSaveIniSettings yourself. */
    /**When manual .ini load|save is active (io.IniFilename == NULL), this will be set to notify your application that you can call SaveIniSettingsToMemory() and save yourself. IMPORTANT: You need to clear io.WantSaveIniSettings yourself. */
    var wantSaveIniSettings: Boolean
        get() = JImGuiIO.isWantSaveIniSettings()
        set(value) = JImGuiIO.setWantSaveIniSettings(value)
    /**Directional navigation is currently allowed (will handle ImGuiKey_NavXXX events) = a window is focused and it doesn't use the ImGuiWindowFlags_NoNavInputs flag. */
    /**Directional navigation is currently allowed (will handle ImGuiKey_NavXXX events) = a window is focused and it doesn't use the ImGuiWindowFlags_NoNavInputs flag. */
    var navActive: Boolean
        get() = JImGuiIO.isNavActive()
        set(value) = JImGuiIO.setNavActive(value)
    /**Directional navigation is visible and allowed (will handle ImGuiKey_NavXXX events). */
    /**Directional navigation is visible and allowed (will handle ImGuiKey_NavXXX events). */
    var navVisible: Boolean
        get() = JImGuiIO.isNavVisible()
        set(value) = JImGuiIO.setNavVisible(value)

    // extensions =====================================================================================================

    fun getMouseClickedPos(index: Int): Vec2 = vec(getMouseClickedPosX(index), getMouseClickedPosY(index))
    fun getMouseDragMaxDistanceAbs(index: Int): Vec2 = vec(getMouseDragMaxDistanceAbsX(index), getMouseDragMaxDistanceAbsY(index))

    var displayFramebufferScale: Vec2
        get() = vec(displayFramebufferScaleX, displayFramebufferScaleY)
        set(value) {
            displayFramebufferScaleX = value.xf
            displayFramebufferScaleY = value.yf
        }
    var displaySize: Vec2
        get() = vec(displaySizeX, displaySizeY)
        set(value) {
            displaySizeX = value.xf
            displaySizeY = value.yf
        }

    var mousePos: Vec2
        get() = vec(mousePosX, mousePosY)
        set(value) {
            mousePosX = value.xf
            mousePosY = value.yf
        }
    var mouseDelta: Vec2
        get() = vec(mouseDeltaX, mouseDeltaY)
        set(value) {
            mouseDeltaX = value.xf
            mouseDeltaY = value.yf
        }
    var mousePosPrev: Vec2
        get() = vec(mousePosPrevX, mousePosPrevY)
        set(value) {
            mousePosPrevX = value.xf
            mousePosPrevY = value.yf
        }
}
