package dev.thecodewarrior.bitfont.editor.imgui

import dev.thecodewarrior.bitfont.editor.utils.math.Rect
import dev.thecodewarrior.bitfont.editor.utils.math.Vec2
import org.ice1000.jimgui.JImDrawList
import org.ice1000.jimgui.JImFont
import org.ice1000.jimgui.JImTextureID
import org.ice1000.jimgui.JImVec4
import org.ice1000.jimgui.flag.JImDrawListFlags
import org.intellij.lang.annotations.MagicConstant

import java.nio.charset.StandardCharsets
import java.util.WeakHashMap

class ImDrawList(val wrapped: JImDrawList) {

    var flags: Int
        get() = wrapped.flags
        set(value) { wrapped.flags = value }

    fun addText(posX: Float, posY: Float, u32Color: Int, text: String)
        = wrapped.addText(posX, posY, u32Color, text)

    fun addText(fontSize: Float, posX: Float, posY: Float, u32Color: Int, text: String)
        = wrapped.addText(fontSize, posX, posY, u32Color, text)

    /**Render-level scissoring. This is passed down to your render function but not used for CPU-side coarse clipping. Prefer using higher-level ImGui::PushClipRect() to affect logic (hit-testing and widget culling) */
    fun pushClipRect(widthclipRectMin: Float, heightclipRectMin: Float, widthclipRectMax: Float, heightclipRectMax: Float, intersectWithCurrentClipRect: Boolean)
        = wrapped.pushClipRect(widthclipRectMin, heightclipRectMin, widthclipRectMax, heightclipRectMax, intersectWithCurrentClipRect)

    /**Render-level scissoring. This is passed down to your render function but not used for CPU-side coarse clipping. Prefer using higher-level ImGui::PushClipRect() to affect logic (hit-testing and widget culling) */
    fun pushClipRect(widthclipRectMin: Float, heightclipRectMin: Float, widthclipRectMax: Float, heightclipRectMax: Float)
        = wrapped.pushClipRect(widthclipRectMin, heightclipRectMin, widthclipRectMax, heightclipRectMax)

    fun pushClipRectFullScreen()
        = wrapped.pushClipRectFullScreen()

    fun popClipRect()
        = wrapped.popClipRect()

    fun pushTextureID(textureID: JImTextureID)
        = wrapped.pushTextureID(textureID)

    fun popTextureID()
        = wrapped.popTextureID()

    fun addLine(aX: Float, aY: Float, bX: Float, bY: Float, u32Color: Int, thickness: Float)
        = wrapped.addLine(aX, aY, bX, bY, u32Color, thickness)

    fun addLine(aX: Float, aY: Float, bX: Float, bY: Float, u32Color: Int)
        = wrapped.addLine(aX, aY, bX, bY, u32Color)

    /**a: upper-left, b: lower-right (== upper-left + size), rounding_corners_flags: 4-bits corresponding to which corner to round */
    fun addRect(aX: Float, aY: Float, bX: Float, bY: Float, u32Color: Int, rounding: Float, roundingCornersFlags: Int, thickness: Float)
        = wrapped.addRect(aX, aY, bX, bY, u32Color, rounding, roundingCornersFlags, thickness)

    /**a: upper-left, b: lower-right (== upper-left + size), rounding_corners_flags: 4-bits corresponding to which corner to round */
    fun addRect(aX: Float, aY: Float, bX: Float, bY: Float, u32Color: Int, rounding: Float, roundingCornersFlags: Int)
        = wrapped.addRect(aX, aY, bX, bY, u32Color, rounding, roundingCornersFlags)

    /**a: upper-left, b: lower-right (== upper-left + size), rounding_corners_flags: 4-bits corresponding to which corner to round */
    fun addRect(aX: Float, aY: Float, bX: Float, bY: Float, u32Color: Int, rounding: Float)
        = wrapped.addRect(aX, aY, bX, bY, u32Color, rounding)

    /**a: upper-left, b: lower-right (== upper-left + size), rounding_corners_flags: 4-bits corresponding to which corner to round */
    fun addRect(aX: Float, aY: Float, bX: Float, bY: Float, u32Color: Int)
        = wrapped.addRect(aX, aY, bX, bY, u32Color)

    /**a: upper-left, b: lower-right (== upper-left + size) */
    fun addRectFilled(aX: Float, aY: Float, bX: Float, bY: Float, u32Color: Int, rounding: Float, roundingCornersFlags: Int)
        = wrapped.addRectFilled(aX, aY, bX, bY, u32Color, rounding, roundingCornersFlags)

    /**a: upper-left, b: lower-right (== upper-left + size) */
    fun addRectFilled(aX: Float, aY: Float, bX: Float, bY: Float, u32Color: Int, rounding: Float)
        = wrapped.addRectFilled(aX, aY, bX, bY, u32Color, rounding)

    /**a: upper-left, b: lower-right (== upper-left + size) */
    fun addRectFilled(aX: Float, aY: Float, bX: Float, bY: Float, u32Color: Int)
        = wrapped.addRectFilled(aX, aY, bX, bY, u32Color)

    fun addRectFilledMultiColor(aX: Float, aY: Float, bX: Float, bY: Float, colorUpperLeft: Int, colorUpperRight: Int, colorBottomRight: Int, colorBottomLeft: Int)
        = wrapped.addRectFilledMultiColor(aX, aY, bX, bY, colorUpperLeft, colorUpperRight, colorBottomRight, colorBottomLeft)

    fun addQuad(aX: Float, aY: Float, bX: Float, bY: Float, cX: Float, cY: Float, dX: Float, dY: Float, u32Color: Int, thickness: Float)
        = wrapped.addQuad(aX, aY, bX, bY, cX, cY, dX, dY, u32Color, thickness)

    fun addQuad(aX: Float, aY: Float, bX: Float, bY: Float, cX: Float, cY: Float, dX: Float, dY: Float, u32Color: Int)
        = wrapped.addQuad(aX, aY, bX, bY, cX, cY, dX, dY, u32Color)

    fun addQuadFilled(aX: Float, aY: Float, bX: Float, bY: Float, cX: Float, cY: Float, dX: Float, dY: Float, u32Color: Int)
        = wrapped.addQuadFilled(aX, aY, bX, bY, cX, cY, dX, dY, u32Color)

    fun addTriangle(aX: Float, aY: Float, bX: Float, bY: Float, cX: Float, cY: Float, u32Color: Int, thickness: Float)
        = wrapped.addTriangle(aX, aY, bX, bY, cX, cY, u32Color, thickness)

    fun addTriangle(aX: Float, aY: Float, bX: Float, bY: Float, cX: Float, cY: Float, u32Color: Int)
        = wrapped.addTriangle(aX, aY, bX, bY, cX, cY, u32Color)

    fun addTriangleFilled(aX: Float, aY: Float, bX: Float, bY: Float, cX: Float, cY: Float, u32Color: Int)
        = wrapped.addTriangleFilled(aX, aY, bX, bY, cX, cY, u32Color)

    fun addCircle(centreX: Float, centreY: Float, radius: Float, u32Color: Int, numSegments: Int, thickness: Float)
        = wrapped.addCircle(centreX, centreY, radius, u32Color, numSegments, thickness)

    fun addCircle(centreX: Float, centreY: Float, radius: Float, u32Color: Int, numSegments: Int)
        = wrapped.addCircle(centreX, centreY, radius, u32Color, numSegments)

    fun addCircle(centreX: Float, centreY: Float, radius: Float, u32Color: Int)
        = wrapped.addCircle(centreX, centreY, radius, u32Color)

    fun addCircleFilled(centreX: Float, centreY: Float, radius: Float, u32Color: Int, numSegments: Int)
        = wrapped.addCircleFilled(centreX, centreY, radius, u32Color, numSegments)

    fun addCircleFilled(centreX: Float, centreY: Float, radius: Float, u32Color: Int)
        = wrapped.addCircleFilled(centreX, centreY, radius, u32Color)

    /**Add string (each character of the UTF-8 string are added) */
    fun addText(font: JImFont, fontSize: Float, posX: Float, posY: Float, u32Color: Int, text: String, wrapWidth: Float, cpuFineClipRect: JImVec4)
        = wrapped.addText(font, fontSize, posX, posY, u32Color, text, wrapWidth, cpuFineClipRect)

    /**Add string (each character of the UTF-8 string are added) */
    fun addText(font: JImFont, fontSize: Float, posX: Float, posY: Float, u32Color: Int, text: String, wrapWidth: Float)
        = wrapped.addText(font, fontSize, posX, posY, u32Color, text, wrapWidth)

    /**Add string (each character of the UTF-8 string are added) */
    fun addText(font: JImFont, fontSize: Float, posX: Float, posY: Float, u32Color: Int, text: String)
        = wrapped.addText(font, fontSize, posX, posY, u32Color, text)

    fun addImage(userTextureID: JImTextureID, aX: Float, aY: Float, bX: Float, bY: Float, uvAX: Float, uvAY: Float, uvBX: Float, uvBY: Float, color: Int)
        = wrapped.addImage(userTextureID, aX, aY, bX, bY, uvAX, uvAY, uvBX, uvBY, color)

    fun addImage(userTextureID: JImTextureID, aX: Float, aY: Float, bX: Float, bY: Float, uvAX: Float, uvAY: Float, uvBX: Float, uvBY: Float)
        = wrapped.addImage(userTextureID, aX, aY, bX, bY, uvAX, uvAY, uvBX, uvBY)

    fun addImage(userTextureID: JImTextureID, aX: Float, aY: Float, bX: Float, bY: Float, uvAX: Float, uvAY: Float)
        = wrapped.addImage(userTextureID, aX, aY, bX, bY, uvAX, uvAY)

    fun addImage(userTextureID: JImTextureID, aX: Float, aY: Float, bX: Float, bY: Float)
        = wrapped.addImage(userTextureID, aX, aY, bX, bY)

    fun addImageQuad(userTextureID: JImTextureID, aX: Float, aY: Float, bX: Float, bY: Float, cX: Float, cY: Float, dX: Float, dY: Float, uvAX: Float, uvAY: Float, uvBX: Float, uvBY: Float, uvCX: Float, uvCY: Float, uvDX: Float, uvDY: Float, color: Int)
        = wrapped.addImageQuad(userTextureID, aX, aY, bX, bY, cX, cY, dX, dY, uvAX, uvAY, uvBX, uvBY, uvCX, uvCY, uvDX, uvDY, color)

    fun addImageQuad(userTextureID: JImTextureID, aX: Float, aY: Float, bX: Float, bY: Float, cX: Float, cY: Float, dX: Float, dY: Float, uvAX: Float, uvAY: Float, uvBX: Float, uvBY: Float, uvCX: Float, uvCY: Float, uvDX: Float, uvDY: Float)
        = wrapped.addImageQuad(userTextureID, aX, aY, bX, bY, cX, cY, dX, dY, uvAX, uvAY, uvBX, uvBY, uvCX, uvCY, uvDX, uvDY)

    fun addImageQuad(userTextureID: JImTextureID, aX: Float, aY: Float, bX: Float, bY: Float, cX: Float, cY: Float, dX: Float, dY: Float, uvAX: Float, uvAY: Float, uvBX: Float, uvBY: Float, uvCX: Float, uvCY: Float)
        = wrapped.addImageQuad(userTextureID, aX, aY, bX, bY, cX, cY, dX, dY, uvAX, uvAY, uvBX, uvBY, uvCX, uvCY)

    fun addImageQuad(userTextureID: JImTextureID, aX: Float, aY: Float, bX: Float, bY: Float, cX: Float, cY: Float, dX: Float, dY: Float, uvAX: Float, uvAY: Float, uvBX: Float, uvBY: Float)
        = wrapped.addImageQuad(userTextureID, aX, aY, bX, bY, cX, cY, dX, dY, uvAX, uvAY, uvBX, uvBY)

    fun addImageQuad(userTextureID: JImTextureID, aX: Float, aY: Float, bX: Float, bY: Float, cX: Float, cY: Float, dX: Float, dY: Float, uvAX: Float, uvAY: Float)
        = wrapped.addImageQuad(userTextureID, aX, aY, bX, bY, cX, cY, dX, dY, uvAX, uvAY)

    fun addImageQuad(userTextureID: JImTextureID, aX: Float, aY: Float, bX: Float, bY: Float, cX: Float, cY: Float, dX: Float, dY: Float)
        = wrapped.addImageQuad(userTextureID, aX, aY, bX, bY, cX, cY, dX, dY)

    fun addImageRounded(userTextureID: JImTextureID, aX: Float, aY: Float, bX: Float, bY: Float, uvAX: Float, uvAY: Float, uvBX: Float, uvBY: Float, color: Int, rounding: Float, roundingCornersFlags: Int)
        = wrapped.addImageRounded(userTextureID, aX, aY, bX, bY, uvAX, uvAY, uvBX, uvBY, color, rounding, roundingCornersFlags)

    fun addImageRounded(userTextureID: JImTextureID, aX: Float, aY: Float, bX: Float, bY: Float, uvAX: Float, uvAY: Float, uvBX: Float, uvBY: Float, color: Int, rounding: Float)
        = wrapped.addImageRounded(userTextureID, aX, aY, bX, bY, uvAX, uvAY, uvBX, uvBY, color, rounding)

    fun addBezierCurve(pos0X: Float, pos0Y: Float, cp0X: Float, cp0Y: Float, cp1X: Float, cp1Y: Float, pos1X: Float, pos1Y: Float, u32Color: Int, thickness: Float, numSegments: Int)
        = wrapped.addBezierCurve(pos0X, pos0Y, cp0X, cp0Y, cp1X, cp1Y, pos1X, pos1Y, u32Color, thickness, numSegments)

    fun addBezierCurve(pos0X: Float, pos0Y: Float, cp0X: Float, cp0Y: Float, cp1X: Float, cp1Y: Float, pos1X: Float, pos1Y: Float, u32Color: Int, thickness: Float)
        = wrapped.addBezierCurve(pos0X, pos0Y, cp0X, cp0Y, cp1X, cp1Y, pos1X, pos1Y, u32Color, thickness)

    fun addBezierCurve(pos0X: Float, pos0Y: Float, cp0X: Float, cp0Y: Float, cp1X: Float, cp1Y: Float, pos1X: Float, pos1Y: Float, u32Color: Int)
        = wrapped.addBezierCurve(pos0X, pos0Y, cp0X, cp0Y, cp1X, cp1Y, pos1X, pos1Y, u32Color)

    fun pathClear()
        = wrapped.pathClear()

    fun pathLineTo(posX: Float, posY: Float)
        = wrapped.pathLineTo(posX, posY)

    fun pathLineToMergeDuplicate(posX: Float, posY: Float)
        = wrapped.pathLineToMergeDuplicate(posX, posY)

    /**Note: Anti-aliased filling requires points to be in clockwise order. */
    fun pathFillConvex(u32Color: Int)
        = wrapped.pathFillConvex(u32Color)

    fun pathStroke(u32Color: Int, closed: Boolean, thickness: Float)
        = wrapped.pathStroke(u32Color, closed, thickness)

    fun pathStroke(u32Color: Int, closed: Boolean)
        = wrapped.pathStroke(u32Color, closed)

    fun pathArcTo(centreX: Float, centreY: Float, radius: Float, aMin: Float, aMax: Float, numSegments: Int)
        = wrapped.pathArcTo(centreX, centreY, radius, aMin, aMax, numSegments)

    fun pathArcTo(centreX: Float, centreY: Float, radius: Float, aMin: Float, aMax: Float)
        = wrapped.pathArcTo(centreX, centreY, radius, aMin, aMax)

    /**Use precomputed angles for a 12 steps circle */
    fun pathArcToFast(centreX: Float, centreY: Float, radius: Float, aMinOf12: Float, aMaxOf12: Float)
        = wrapped.pathArcToFast(centreX, centreY, radius, aMinOf12, aMaxOf12)

    fun pathBezierCurveTo(p1X: Float, p1Y: Float, p2X: Float, p2Y: Float, p3X: Float, p3Y: Float, numSegments: Int)
        = wrapped.pathBezierCurveTo(p1X, p1Y, p2X, p2Y, p3X, p3Y, numSegments)

    fun pathBezierCurveTo(p1X: Float, p1Y: Float, p2X: Float, p2Y: Float, p3X: Float, p3Y: Float)
        = wrapped.pathBezierCurveTo(p1X, p1Y, p2X, p2Y, p3X, p3Y)

    fun pathRect(rectMinX: Float, rectMinY: Float, rectMaxX: Float, rectMaxY: Float, rounding: Float, roundingCornersFlags: Int)
        = wrapped.pathRect(rectMinX, rectMinY, rectMaxX, rectMaxY, rounding, roundingCornersFlags)

    fun pathRect(rectMinX: Float, rectMinY: Float, rectMaxX: Float, rectMaxY: Float, rounding: Float)
        = wrapped.pathRect(rectMinX, rectMinY, rectMaxX, rectMaxY, rounding)

    fun pathRect(rectMinX: Float, rectMinY: Float, rectMaxX: Float, rectMaxY: Float)
        = wrapped.pathRect(rectMinX, rectMinY, rectMaxX, rectMaxY)

    /**This is useful if you need to forcefully create a new draw call (to allow for dependent rendering | blending). Otherwise primitives are merged into the same draw-call as much as possible */
    fun addDrawCmd()
        = wrapped.addDrawCmd()

    fun channelsSplit(channelsCount: Int)
        = wrapped.channelsSplit(channelsCount)

    fun channelsMerge()
        = wrapped.channelsMerge()

    fun channelsSetCurrent(channelsIndex: Int)
        = wrapped.channelsSetCurrent(channelsIndex)

    /**Do not clear Channels[] so our allocations are reused next frame
     * The ImDrawList are owned by ImGuiContext!
     * Clear all input and output. */
    fun clear()
        = wrapped.clear()

    fun clearFreeMemory()
        = wrapped.clearFreeMemory()

    fun primReserve(idxCount: Int, vtxCount: Int)
        = wrapped.primReserve(idxCount, vtxCount)

    /**Axis aligned rectangle (composed of two triangles) */
    fun primRect(aX: Float, aY: Float, bX: Float, bY: Float, u32Color: Int)
        = wrapped.primRect(aX, aY, bX, bY, u32Color)

    fun primRectUV(aX: Float, aY: Float, bX: Float, bY: Float, uvAX: Float, uvAY: Float, uvBX: Float, uvBY: Float, u32Color: Int)
        = wrapped.primRectUV(aX, aY, bX, bY, uvAX, uvAY, uvBX, uvBY, u32Color)

    fun primQuadUV(aX: Float, aY: Float, bX: Float, bY: Float, cX: Float, cY: Float, dX: Float, dY: Float, uvAX: Float, uvAY: Float, uvBX: Float, uvBY: Float, uvCX: Float, uvCY: Float, uvDX: Float, uvDY: Float, u32Color: Int)
        = wrapped.primQuadUV(aX, aY, bX, bY, cX, cY, dX, dY, uvAX, uvAY, uvBX, uvBY, uvCX, uvCY, uvDX, uvDY, u32Color)

    fun primWriteVtx(posX: Float, posY: Float, uvX: Float, uvY: Float, u32Color: Int)
        = wrapped.primWriteVtx(posX, posY, uvX, uvY, u32Color)

    fun primWriteIdx(idx: Int)
        = wrapped.primWriteIdx(idx)

    fun primVtx(posX: Float, posY: Float, uvX: Float, uvY: Float, u32Color: Int)
        = wrapped.primVtx(posX, posY, uvX, uvY, u32Color)

    fun updateClipRect()
        = wrapped.updateClipRect()

    fun updateTextureID()
        = wrapped.updateTextureID()


    companion object {
        private val cache = WeakHashMap<JImDrawList, ImDrawList>()

        fun wrap(list: JImDrawList): ImDrawList = cache.getOrPut(list) { ImDrawList(list) }
    }

    // extensions ======================================================================================================

    fun addText(pos: Vec2, u32Color: Int, text: String)
        = wrapped.addText(pos.xf, pos.yf, u32Color, text)

    fun addText(fontSize: Float, pos: Vec2, u32Color: Int, text: String)
        = wrapped.addText(fontSize, pos.xf, pos.yf, u32Color, text)

    /**Render-level scissoring. This is passed down to your render function but not used for CPU-side coarse clipping.
     * Prefer using higher-level ImGui::PushClipRect() to affect logic (hit-testing and widget culling) */
    fun pushClipRect(clipRect: Rect, intersectWithCurrentClipRect: Boolean)
        = wrapped.pushClipRect(clipRect.min.xf, clipRect.min.yf, clipRect.max.xf, clipRect.max.yf, intersectWithCurrentClipRect)

    /**Render-level scissoring. This is passed down to your render function but not used for CPU-side coarse clipping.
     * Prefer using higher-level ImGui::PushClipRect() to affect logic (hit-testing and widget culling) */
    fun pushClipRect(clipRect: Rect)
        = wrapped.pushClipRect(clipRect.min.xf, clipRect.min.yf, clipRect.max.xf, clipRect.max.yf)

    fun addLine(a: Vec2, b: Vec2, u32Color: Int, thickness: Float)
        = wrapped.addLine(a.xf, a.yf, b.xf, b.yf, u32Color, thickness)

    fun addLine(a: Vec2, b: Vec2, u32Color: Int)
        = wrapped.addLine(a.xf, a.yf, b.xf, b.yf, u32Color)

    /**a: upper-left, b: lower-right (== upper-left + size), rounding_corners_flags: 4-bits corresponding to which corner to round */
    fun addRect(a: Vec2, b: Vec2, u32Color: Int, rounding: Float, roundingCornersFlags: Int, thickness: Float)
        = wrapped.addRect(a.xf, a.yf, b.xf, b.yf, u32Color, rounding, roundingCornersFlags, thickness)

    /**a: upper-left, b: lower-right (== upper-left + size), rounding_corners_flags: 4-bits corresponding to which corner to round */
    fun addRect(a: Vec2, b: Vec2, u32Color: Int, rounding: Float, roundingCornersFlags: Int)
        = wrapped.addRect(a.xf, a.yf, b.xf, b.yf, u32Color, rounding, roundingCornersFlags)

    /**a: upper-left, b: lower-right (== upper-left + size), rounding_corners_flags: 4-bits corresponding to which corner to round */
    fun addRect(a: Vec2, b: Vec2, u32Color: Int, rounding: Float)
        = wrapped.addRect(a.xf, a.yf, b.xf, b.yf, u32Color, rounding)

    /**a: upper-left, b: lower-right (== upper-left + size), rounding_corners_flags: 4-bits corresponding to which corner to round */
    fun addRect(a: Vec2, b: Vec2, u32Color: Int)
        = wrapped.addRect(a.xf, a.yf, b.xf, b.yf, u32Color)

    /**a: upper-left, b: lower-right (== upper-left + size) */
    fun addRectFilled(a: Vec2, b: Vec2, u32Color: Int, rounding: Float, roundingCornersFlags: Int)
        = wrapped.addRectFilled(a.xf, a.yf, b.xf, b.yf, u32Color, rounding, roundingCornersFlags)

    /**a: upper-left, b: lower-right (== upper-left + size) */
    fun addRectFilled(a: Vec2, b: Vec2, u32Color: Int, rounding: Float)
        = wrapped.addRectFilled(a.xf, a.yf, b.xf, b.yf, u32Color, rounding)

    /**a: upper-left, b: lower-right (== upper-left + size) */
    fun addRectFilled(a: Vec2, b: Vec2, u32Color: Int)
        = wrapped.addRectFilled(a.xf, a.yf, b.xf, b.yf, u32Color)

    fun addRectFilledMultiColor(a: Vec2, b: Vec2, colorUpperLeft: Int, colorUpperRight: Int, colorBottomRight: Int, colorBottomLeft: Int)
        = wrapped.addRectFilledMultiColor(a.xf, a.yf, b.xf, b.yf, colorUpperLeft, colorUpperRight, colorBottomRight, colorBottomLeft)

    fun addQuad(a: Vec2, b: Vec2, c: Vec2, d: Vec2, u32Color: Int, thickness: Float)
        = wrapped.addQuad(a.xf, a.yf, b.xf, b.yf, c.xf, c.yf, d.xf, d.yf, u32Color, thickness)

    fun addQuad(a: Vec2, b: Vec2, c: Vec2, d: Vec2, u32Color: Int)
        = wrapped.addQuad(a.xf, a.yf, b.xf, b.yf, c.xf, c.yf, d.xf, d.yf, u32Color)

    fun addQuadFilled(a: Vec2, b: Vec2, c: Vec2, d: Vec2, u32Color: Int)
        = wrapped.addQuadFilled(a.xf, a.yf, b.xf, b.yf, c.xf, c.yf, d.xf, d.yf, u32Color)

    fun addTriangle(a: Vec2, b: Vec2, c: Vec2, u32Color: Int, thickness: Float)
        = wrapped.addTriangle(a.xf, a.yf, b.xf, b.yf, c.xf, c.yf, u32Color, thickness)

    fun addTriangle(a: Vec2, b: Vec2, c: Vec2, u32Color: Int)
        = wrapped.addTriangle(a.xf, a.yf, b.xf, b.yf, c.xf, c.yf, u32Color)

    fun addTriangleFilled(a: Vec2, b: Vec2, c: Vec2, u32Color: Int)
        = wrapped.addTriangleFilled(a.xf, a.yf, b.xf, b.yf, c.xf, c.yf, u32Color)

    fun addCircle(centre: Vec2, radius: Float, u32Color: Int, numSegments: Int, thickness: Float)
        = wrapped.addCircle(centre.xf, centre.yf, radius, u32Color, numSegments, thickness)

    fun addCircle(centre: Vec2, radius: Float, u32Color: Int, numSegments: Int)
        = wrapped.addCircle(centre.xf, centre.yf, radius, u32Color, numSegments)

    fun addCircle(centre: Vec2, radius: Float, u32Color: Int)
        = wrapped.addCircle(centre.xf, centre.yf, radius, u32Color)

    fun addCircleFilled(centre: Vec2, radius: Float, u32Color: Int, numSegments: Int)
        = wrapped.addCircleFilled(centre.xf, centre.yf, radius, u32Color, numSegments)

    fun addCircleFilled(centre: Vec2, radius: Float, u32Color: Int)
        = wrapped.addCircleFilled(centre.xf, centre.yf, radius, u32Color)

    /**Add string (each character of the UTF-8 string are added) */
    fun addText(font: JImFont, fontSize: Float, pos: Vec2, u32Color: Int, text: String, wrapWidth: Float, cpuFineClipRect: JImVec4)
        = wrapped.addText(font, fontSize, pos.xf, pos.yf, u32Color, text, wrapWidth, cpuFineClipRect)

    /**Add string (each character of the UTF-8 string are added) */
    fun addText(font: JImFont, fontSize: Float, pos: Vec2, u32Color: Int, text: String, wrapWidth: Float)
        = wrapped.addText(font, fontSize, pos.xf, pos.yf, u32Color, text, wrapWidth)

    /**Add string (each character of the UTF-8 string are added) */
    fun addText(font: JImFont, fontSize: Float, pos: Vec2, u32Color: Int, text: String)
        = wrapped.addText(font, fontSize, pos.xf, pos.yf, u32Color, text)

    fun addImage(userTextureID: JImTextureID, a: Vec2, b: Vec2, uvA: Vec2, uvB: Vec2, color: Int)
        = wrapped.addImage(userTextureID, a.xf, a.yf, b.xf, b.yf, uvA.xf, uvA.yf, uvB.xf, uvB.yf, color)

    fun addImage(userTextureID: JImTextureID, a: Vec2, b: Vec2, uvA: Vec2, uvB: Vec2)
        = wrapped.addImage(userTextureID, a.xf, a.yf, b.xf, b.yf, uvA.xf, uvA.yf, uvB.xf, uvB.yf)

    fun addImage(userTextureID: JImTextureID, a: Vec2, b: Vec2, uvA: Vec2)
        = wrapped.addImage(userTextureID, a.xf, a.yf, b.xf, b.yf, uvA.xf, uvA.yf)

    fun addImage(userTextureID: JImTextureID, a: Vec2, b: Vec2)
        = wrapped.addImage(userTextureID, a.xf, a.yf, b.xf, b.yf)

    fun addImageQuad(userTextureID: JImTextureID, a: Vec2, b: Vec2, c: Vec2, d: Vec2, uvA: Vec2, uvB: Vec2, uvC: Vec2, uvD: Vec2, color: Int)
        = wrapped.addImageQuad(userTextureID, a.xf, a.yf, b.xf, b.yf, c.xf, c.yf, d.xf, d.yf, uvA.xf, uvA.yf, uvB.xf, uvB.yf, uvC.xf, uvC.yf, uvD.xf, uvD.yf, color)

    fun addImageQuad(userTextureID: JImTextureID, a: Vec2, b: Vec2, c: Vec2, d: Vec2, uvA: Vec2, uvB: Vec2, uvC: Vec2, uvD: Vec2)
        = wrapped.addImageQuad(userTextureID, a.xf, a.yf, b.xf, b.yf, c.xf, c.yf, d.xf, d.yf, uvA.xf, uvA.yf, uvB.xf, uvB.yf, uvC.xf, uvC.yf, uvD.xf, uvD.yf)

    fun addImageQuad(userTextureID: JImTextureID, a: Vec2, b: Vec2, c: Vec2, d: Vec2, uvA: Vec2, uvB: Vec2, uvC: Vec2)
        = wrapped.addImageQuad(userTextureID, a.xf, a.yf, b.xf, b.yf, c.xf, c.yf, d.xf, d.yf, uvA.xf, uvA.yf, uvB.xf, uvB.yf, uvC.xf, uvC.yf)

    fun addImageQuad(userTextureID: JImTextureID, a: Vec2, b: Vec2, c: Vec2, d: Vec2, uvA: Vec2, uvB: Vec2)
        = wrapped.addImageQuad(userTextureID, a.xf, a.yf, b.xf, b.yf, c.xf, c.yf, d.xf, d.yf, uvA.xf, uvA.yf, uvB.xf, uvB.yf)

    fun addImageQuad(userTextureID: JImTextureID, a: Vec2, b: Vec2, c: Vec2, d: Vec2, uvA: Vec2)
        = wrapped.addImageQuad(userTextureID, a.xf, a.yf, b.xf, b.yf, c.xf, c.yf, d.xf, d.yf, uvA.xf, uvA.yf)

    fun addImageQuad(userTextureID: JImTextureID, a: Vec2, b: Vec2, c: Vec2, d: Vec2)
        = wrapped.addImageQuad(userTextureID, a.xf, a.yf, b.xf, b.yf, c.xf, c.yf, d.xf, d.yf)

    fun addImageRounded(userTextureID: JImTextureID, a: Vec2, b: Vec2, uvA: Vec2, uvB: Vec2, color: Int, rounding: Float, roundingCornersFlags: Int)
        = wrapped.addImageRounded(userTextureID, a.xf, a.yf, b.xf, b.yf, uvA.xf, uvA.yf, uvB.xf, uvB.yf, color, rounding, roundingCornersFlags)

    fun addImageRounded(userTextureID: JImTextureID, a: Vec2, b: Vec2, uvA: Vec2, uvB: Vec2, color: Int, rounding: Float)
        = wrapped.addImageRounded(userTextureID, a.xf, a.yf, b.xf, b.yf, uvA.xf, uvA.yf, uvB.xf, uvB.yf, color, rounding)

    fun addBezierCurve(pos0: Vec2, cp0: Vec2, cp1: Vec2, pos1: Vec2, u32Color: Int, thickness: Float, numSegments: Int)
        = wrapped.addBezierCurve(pos0.xf, pos0.yf, cp0.xf, cp0.yf, cp1.xf, cp1.yf, pos1.xf, pos1.yf, u32Color, thickness, numSegments)

    fun addBezierCurve(pos0: Vec2, cp0: Vec2, cp1: Vec2, pos1: Vec2, u32Color: Int, thickness: Float)
        = wrapped.addBezierCurve(pos0.xf, pos0.yf, cp0.xf, cp0.yf, cp1.xf, cp1.yf, pos1.xf, pos1.yf, u32Color, thickness)

    fun addBezierCurve(pos0: Vec2, cp0: Vec2, cp1: Vec2, pos1: Vec2, u32Color: Int)
        = wrapped.addBezierCurve(pos0.xf, pos0.yf, cp0.xf, cp0.yf, cp1.xf, cp1.yf, pos1.xf, pos1.yf, u32Color)

    fun pathLineTo(pos: Vec2)
        = wrapped.pathLineTo(pos.xf, pos.yf)

    fun pathLineToMergeDuplicate(pos: Vec2)
        = wrapped.pathLineToMergeDuplicate(pos.xf, pos.yf)

    fun pathArcTo(centre: Vec2, radius: Float, aMin: Float, aMax: Float, numSegments: Int)
        = wrapped.pathArcTo(centre.xf, centre.yf, radius, aMin, aMax, numSegments)

    fun pathArcTo(centre: Vec2, radius: Float, aMin: Float, aMax: Float)
        = wrapped.pathArcTo(centre.xf, centre.yf, radius, aMin, aMax)

    /**Use precomputed angles for a 12 steps circle */
    fun pathArcToFast(centre: Vec2, radius: Float, aMinOf12: Float, aMaxOf12: Float)
        = wrapped.pathArcToFast(centre.xf, centre.yf, radius, aMinOf12, aMaxOf12)

    fun pathBezierCurveTo(p1: Vec2, p2: Vec2, p3: Vec2, numSegments: Int)
        = wrapped.pathBezierCurveTo(p1.xf, p1.yf, p2.xf, p2.yf, p3.xf, p3.yf, numSegments)

    fun pathBezierCurveTo(p1: Vec2, p2: Vec2, p3: Vec2)
        = wrapped.pathBezierCurveTo(p1.xf, p1.yf, p2.xf, p2.yf, p3.xf, p3.yf)

    fun pathRect(rectMin: Vec2, rectMax: Vec2, rounding: Float, roundingCornersFlags: Int)
        = wrapped.pathRect(rectMin.xf, rectMin.yf, rectMax.xf, rectMax.yf, rounding, roundingCornersFlags)

    fun pathRect(rectMin: Vec2, rectMax: Vec2, rounding: Float)
        = wrapped.pathRect(rectMin.xf, rectMin.yf, rectMax.xf, rectMax.yf, rounding)

    fun pathRect(rectMin: Vec2, rectMax: Vec2)
        = wrapped.pathRect(rectMin.xf, rectMin.yf, rectMax.xf, rectMax.yf)

    fun primRect(a: Vec2, b: Vec2, u32Color: Int)
        = wrapped.primRect(a.xf, a.yf, b.xf, b.yf, u32Color)

    fun primRectUV(a: Vec2, b: Vec2, uvA: Vec2, uvB: Vec2, u32Color: Int)
        = wrapped.primRectUV(a.xf, a.yf, b.xf, b.yf, uvA.xf, uvA.yf, uvB.xf, uvB.yf, u32Color)

    fun primQuadUV(a: Vec2, b: Vec2, c: Vec2, d: Vec2, uvA: Vec2, uvB: Vec2, uvC: Vec2, uvD: Vec2, u32Color: Int)
        = wrapped.primQuadUV(a.xf, a.yf, b.xf, b.yf, c.xf, c.yf, d.xf, d.yf, uvA.xf, uvA.yf, uvB.xf, uvB.yf, uvC.xf, uvC.yf, uvD.xf, uvD.yf, u32Color)

    fun primWriteVtx(pos: Vec2, uv: Vec2, u32Color: Int)
        = wrapped.primWriteVtx(pos.xf, pos.yf, uv.xf, uv.yf, u32Color)

    fun primVtx(pos: Vec2, uv: Vec2, u32Color: Int)
        = wrapped.primVtx(pos.xf, pos.yf, uv.xf, uv.yf, u32Color)
}
