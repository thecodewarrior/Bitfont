package games.thecodewarrior.bitfont.utils.extensions

import glm_.f
import glm_.func.common.clamp
import glm_.glm
import glm_.i
import glm_.max
import glm_.min
import glm_.vec1.operators.minus
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3i
import glm_.vec4.Vec4i
import imgui.Col
import imgui.DataType
import imgui.Dir
import imgui.ID
import imgui.ImGui
import imgui.Ref
import imgui.format
import imgui.g
import imgui.getValue
import imgui.internal.InputReadMode
import imgui.internal.InputSource
import imgui.internal.NavDirSourceFlag
import imgui.internal.Rect
import imgui.internal.focus
import imgui.internal.or
import imgui.setValue
import kotlin.reflect.KMutableProperty0

object ImGuiDrags {
    @Suppress("UNCHECKED_CAST")
    fun <T: Number> dragScalar(
        label: String, value: KMutableProperty0<T>,
        speed: Double = 1.0, power: Double = 1.0, minValue: T, maxValue: T,
        valueToDisplay: (T) -> String, correct: (Double) -> T, valueToString: (T) -> String, stringToValue: (T, String) -> Pair<T, String>
    ): Boolean {

        val window = ImGui.currentWindow
        if (window.skipItems) return false

        val id = window.getId(label)
        val w = ImGui.calcItemWidth()

        val labelSize = ImGui.calcTextSize(label, 0, true)
        val frameBb = Rect(window.dc.cursorPos, window.dc.cursorPos + Vec2(w, labelSize.y + ImGui.style.framePadding.y * 2f))
        val innerBb = Rect(frameBb.min + ImGui.style.framePadding, frameBb.max - ImGui.style.framePadding)
        val totalBb = Rect(frameBb.min, frameBb.max + Vec2(if (labelSize.x > 0f) ImGui.style.itemInnerSpacing.x + labelSize.x else 0f, 0f))

        // NB- we don't call ItemSize() yet because we may turn into a text edit box below
        if (!ImGui.itemAdd(totalBb, id, frameBb)) {
            ImGui.itemSize(totalBb, ImGui.style.framePadding.y)
            return false
        }

        val hovered = ImGui.itemHoverable(frameBb, id)

        // Tabbing or CTRL-clicking on Drag turns it into an input box
        var startTextInput = false
        val tabFocusRequested = ImGui.focusableItemRegister(window, id)
        if (tabFocusRequested || (hovered && (ImGui.io.mouseClicked[0] || ImGui.io.mouseDoubleClicked[0]) || g.navActivateId == id || (g.navInputId == id && g.scalarAsInputTextId != id))) {
            ImGui.setActiveId(id, window)
            ImGui.setFocusId(id, window)
            window.focus()
            g.activeIdAllowNavDirFlags = (1 shl Dir.Up.i) or (1 shl Dir.Down.i)
            if (tabFocusRequested || ImGui.io.keyCtrl || ImGui.io.mouseDoubleClicked[0] || g.navInputId == id) {
                startTextInput = true
                g.scalarAsInputTextId = 0
            }
        }
        if (startTextInput || (g.activeId == id && g.scalarAsInputTextId == id))
            return inputScalarAsWidgetReplacement(frameBb, id, label, value, valueToString, stringToValue)

        // Actual drag behavior
        ImGui.itemSize(totalBb, ImGui.style.framePadding.y)
        val newValue = dragBehavior(id, value().toDouble(), speed, minValue.toDouble(), maxValue.toDouble(), power)
        val valueChanged = value() != newValue
        if(valueChanged) value.set(correct(newValue))

        // Draw frame
        val frameCol = when (g.activeId) {
            id -> Col.FrameBgActive
            else -> when (g.hoveredId) {
                id -> Col.FrameBgHovered
                else -> Col.FrameBg
            }
        }
        ImGui.renderNavHighlight(frameBb, id)
        ImGui.renderFrame(frameBb.min, frameBb.max, frameCol.u32, true, ImGui.style.frameRounding)

        // Display value using user-provided display format so user can add prefix/suffix/decorations to the value.
        val valueString = valueToDisplay(value())
        ImGui.renderTextClipped(frameBb.min, frameBb.max, valueString, valueString.length, null, Vec2(0.5f))

        if (labelSize.x > 0f)
            ImGui.renderText(Vec2(frameBb.max.x + ImGui.style.itemInnerSpacing.x, innerBb.min.y), label)

        return valueChanged
    }

    fun dragBehavior(id: ID, value: Double, vSpeed: Double, vMin: Double, vMax: Double, power: Double): Double {

        if (g.activeId == id)
            if (g.activeIdSource == InputSource.Mouse && !ImGui.io.mouseDown[0])
                ImGui.clearActiveId()
            else if (g.activeIdSource == InputSource.Nav && g.navActivatePressedId == id && !g.activeIdIsJustActivated)
                ImGui.clearActiveId()

        return when (g.activeId) {
            id -> dragBehaviorT(value, vSpeed, vMin, vMax, power)
            else -> value
        }
    }

    fun dragBehaviorT(currentValue: Double, vSpeed_: Double, vMin: Double, vMax: Double, power: Double): Double {

        var value = currentValue

        // Default tweak speed
        val hasMinMax = vMin != vMax
        var vSpeed = vSpeed_
        if (vSpeed == 0.0 && hasMinMax)
            vSpeed = (vMax - vMin) * g.dragSpeedDefaultRatio

        // Inputs accumulates into g.DragCurrentAccum, which is flushed into the current value as soon as it makes a difference with our precision settings
        var adjustDelta = 0.0
        if (g.activeIdSource == InputSource.Mouse && ImGui.isMousePosValid() && ImGui.io.mouseDragMaxDistanceSqr[0] > 1.0 * 1.0) {
            adjustDelta = ImGui.io.mouseDelta.x.toDouble()
            if (ImGui.io.keyAlt)
                adjustDelta *= 1.0 / 100
            if (ImGui.io.keyShift)
                adjustDelta *= 10.0
        } else if (g.activeIdSource == InputSource.Nav) {
            adjustDelta = ImGui.getNavInputAmount2d(NavDirSourceFlag.Keyboard or NavDirSourceFlag.PadDPad, InputReadMode.RepeatFast, 1f / 10f, 10f).x.toDouble()
        }
        adjustDelta *= vSpeed

        /*  Clear current value on activation
            Avoid altering values and clamping when we are _already_ past the limits and heading in the same direction,
            so e.g. if range is 0..255, current value is 300 and we are pushing to the right side, keep the 300.             */
        val isJustActivated = g.activeIdIsJustActivated
        val isAlreadyPastLimitsAndPushingOutward = hasMinMax && ((value >= vMax && adjustDelta > 0f) || (value <= vMin && adjustDelta < 0f))
        if (isJustActivated || isAlreadyPastLimitsAndPushingOutward) {
            g.dragCurrentAccum = 0f
            g.dragCurrentAccumDirty = false
        } else if (adjustDelta != 0.0) {
            g.dragCurrentAccum += adjustDelta.toFloat()
            g.dragCurrentAccumDirty = true
        }

        if (!g.dragCurrentAccumDirty)
            return currentValue

        var vOldRefForAccumRemainder = 0.0

        val isPower = power != 1.0 && hasMinMax
        if (isPower) {
            // Offset + round to user desired precision, with a curve on the v_min..v_max range to get more precision on one side of the range
            val vOldNormCurved = glm.pow((value - vMin) / (vMax - vMin), 1.0 / power)
            val vNewNormCurved = vOldNormCurved + g.dragCurrentAccum / (vMax - vMin)
            value = vMin + glm.pow(vNewNormCurved.clamp(0.0, 1.0), power) * (vMax - vMin)
            vOldRefForAccumRemainder = vOldNormCurved
        } else value += g.dragCurrentAccum

        // Preserve remainder after rounding has been applied. This also allow slow tweaking of values.
        g.dragCurrentAccumDirty = false
        g.dragCurrentAccum -= when {
            isPower -> {
                val vCurNormCurved = glm.pow((value - vMin) / (vMax - vMin), 1.0 / power)
                vCurNormCurved - vOldRefForAccumRemainder
            }
            else -> (value - currentValue)
        }.toFloat()

        // Lose zero sign for float/double
        if (value == -0.0)
            value = 0.0

        // Clamp values (handle overflow/wrap-around)
        if (value != currentValue && hasMinMax) {
            if (value < vMin || (value > currentValue && adjustDelta < 0f))
                value = vMin
            if (value > vMax || (value < currentValue && adjustDelta > 0f))
                value = vMax
        }

        return value
    }
}