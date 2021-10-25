package dev.thecodewarrior.bitfont.fonteditor.utils

import org.lwjgl.nuklear.NkColor
import org.lwjgl.nuklear.NkCommandBuffer
import org.lwjgl.nuklear.NkContext
import org.lwjgl.nuklear.NkImage
import org.lwjgl.nuklear.NkRect
import org.lwjgl.nuklear.NkUserFont
import org.lwjgl.nuklear.Nuklear.*
import org.lwjgl.system.MemoryStack
import java.awt.Color
import kotlin.math.abs
import kotlin.math.sqrt

@Suppress("NOTHING_TO_INLINE")
class DrawList(val drawCommands: MutableList<DrawCommand> = mutableListOf()) {
    var transformX: Float = 0f
    var transformY: Float = 0f
    var transformScale: Float = 1f

    fun push(ctx: NkContext) {
        val canvas = nk_window_get_canvas(ctx)!!
        push(canvas)
    }

    fun push(canvas: NkCommandBuffer) {
        MemoryStack.stackGet().also { stack ->
            val oldClip = NkRect.mallocStack(stack).set(canvas.clip())
            drawCommands.forEach {
                stack.push()
                it.push(this, canvas, stack)
                stack.pop()
            }
            nk_push_scissor(canvas, oldClip)
        }
    }

    fun clear() {
        drawCommands.clear()
    }

    fun add(command: DrawCommand): DrawList {
        drawCommands.add(command)
        return this
    }

    inline fun draw(
        x: Number, y: Number,
        scale: Number, childList: DrawList
    ): DrawList {
        drawCommands.add(DrawCommand.Draw(
            x.toFloat(), y.toFloat(),
            scale.toFloat(), childList,
        ))
        return this
    }

    inline fun clip(
        x: Number, y: Number,
        w: Number, h: Number,
    ): DrawList {
        drawCommands.add(DrawCommand.Clip(
            x.toFloat(), y.toFloat(),
            w.toFloat(), h.toFloat(),
        ))
        return this
    }

    inline fun image(
        x: Number, y: Number,
        w: Number, h: Number,
        nkImage: NkImage,
        color: Color
    ): DrawList {
        drawCommands.add(DrawCommand.Image(
            x.toFloat(), y.toFloat(),
            w.toFloat(), h.toFloat(),
            nkImage, color
        ))
        return this
    }

    inline fun strokeLine(
        x0: Number, y0: Number,
        x1: Number, y1: Number,
        thickness: Number, color: Color
    ): DrawList {
        if(thickness.toFloat() == 1f) {
            drawCommands.add(DrawCommand.PixelStroke(
                x0.toFloat(), y0.toFloat(),
                x1.toFloat(), y1.toFloat(),
                color
            ))
        } else {
            drawCommands.add(DrawCommand.StrokeLine(
                x0.toFloat(), y0.toFloat(),
                x1.toFloat(), y1.toFloat(),
                thickness.toFloat(), color
            ))
        }
        return this
    }

    inline fun strokeCurve(
        ax: Number, ay: Number,
        ctrl0x: Number, ctrl0y: Number,
        ctrl1x: Number, ctrl1y: Number,
        bx: Number, by: Number,
        thickness: Number, color: Color
    ): DrawList {
        drawCommands.add(DrawCommand.StrokeCurve(
            ax.toFloat(), ay.toFloat(),
            ctrl0x.toFloat(), ctrl0y.toFloat(),
            ctrl1x.toFloat(), ctrl1y.toFloat(),
            bx.toFloat(), by.toFloat(),
            thickness.toFloat(), color
        ))
        return this
    }

    inline fun strokeRect(
        x: Number, y: Number,
        w: Number, h: Number,
        rounding: Number,
        thickness: Number, color: Color
    ): DrawList {
        if(rounding.toFloat() == 0f && thickness.toFloat() == 1f) {
            drawCommands.add(DrawCommand.PixelStrokeRect(
                x.toFloat(), y.toFloat(),
                w.toFloat(), h.toFloat(),
                color
            ))
        } else {
            drawCommands.add(DrawCommand.StrokeRect(
                x.toFloat(), y.toFloat(),
                w.toFloat(), h.toFloat(),
                rounding.toFloat(),
                thickness.toFloat(), color
            ))
        }
        return this
    }

    inline fun strokeCircle(
        x: Number, y: Number,
        w: Number, h: Number,
        thickness: Number, color: Color
    ): DrawList {
        drawCommands.add(DrawCommand.StrokeCircle(
            x.toFloat(), y.toFloat(),
            w.toFloat(), h.toFloat(),
            thickness.toFloat(), color
        ))
        return this
    }

    inline fun strokeArc(
        cx: Number, cy: Number,
        radius: Number,
        minAngle: Number, maxAngle: Number,
        thickness: Number, color: Color
    ): DrawList {
        drawCommands.add(DrawCommand.StrokeArc(
            cx.toFloat(), cy.toFloat(),
            radius.toFloat(),
            minAngle.toFloat(), maxAngle.toFloat(),
            thickness.toFloat(), color
        ))
        return this
    }

    inline fun strokeTriangle(
        x0: Number, y0: Number,
        x1: Number, y1: Number,
        x2: Number, y2: Number,
        thickness: Number, color: Color
    ): DrawList {
        drawCommands.add(DrawCommand.StrokeTriangle(
            x0.toFloat(), y0.toFloat(),
            x1.toFloat(), y1.toFloat(),
            x2.toFloat(), y2.toFloat(),
            thickness.toFloat(), color
        ))
        return this
    }

    inline fun strokePolyline(
        thickness: Number, color: Color
    ): PolyBuilder {
        val points = mutableListOf<Float>()
        drawCommands.add(DrawCommand.StrokePolyline(points, thickness.toFloat(), color))
        return PolyBuilder(points, this)
    }

    inline fun strokePolygon(
        thickness: Number, color: Color
    ): PolyBuilder {
        val points = mutableListOf<Float>()
        drawCommands.add(DrawCommand.StrokePolygon(points, thickness.toFloat(), color))
        return PolyBuilder(points, this)
    }

    inline fun fillRect(
        x: Number, y: Number,
        w: Number, h: Number,
        rounding: Number,
        color: Color
    ): DrawList {
        drawCommands.add(DrawCommand.FillRect(
            x.toFloat(), y.toFloat(),
            w.toFloat(), h.toFloat(),
            rounding.toFloat(), color
        ))
        return this
    }

    inline fun fillArc(
        cx: Number, cy: Number,
        radius: Number,
        minAngle: Number, maxAngle: Number,
        color: Color
    ): DrawList {
        drawCommands.add(DrawCommand.FillArc(
            cx.toFloat(), cy.toFloat(),
            radius.toFloat(),
            minAngle.toFloat(), maxAngle.toFloat(),
            color
        ))
        return this
    }

    inline fun fillTriangle(
        x0: Number, y0: Number,
        x1: Number, y1: Number,
        x2: Number, y2: Number,
        color: Color
    ): DrawList {
        drawCommands.add(DrawCommand.FillTriangle(
            x0.toFloat(), y0.toFloat(),
            x1.toFloat(), y1.toFloat(),
            x2.toFloat(), y2.toFloat(),
            color
        ))
        return this
    }

    inline fun fillPolygon(
        color: Color
    ): PolyBuilder {
        val points = mutableListOf<Float>()
        drawCommands.add(DrawCommand.FillPolygon(points, color))
        return PolyBuilder(points, this)
    }

    inline fun fillText(
        x: Number, y: Number, height: Number,
        text: String, font: NkUserFont,
        color: Color
    ): DrawList {
        drawCommands.add(DrawCommand.FillText(
            x.toFloat(), y.toFloat(), height.toFloat(),
            font, text,
            color
        ))
        return this
    }

    /**
     * Apply the transform to an x pos
     */
    fun tx(x: Float): Float {
        return transformX + x * transformScale
    }

    /**
     * Apply the transform to a y pos
     */
    fun ty(y: Float): Float {
        return transformY + y * transformScale
    }

    /**
     * Apply the transform to a delta
     */
    fun td(delta: Float): Float {
        return delta * transformScale
    }

    interface DrawCommand {
        fun push(drawList: DrawList, canvas: NkCommandBuffer, stack: MemoryStack)

        fun nkColor(color: Color, stack: MemoryStack): NkColor {
            val nkColor = NkColor.mallocStack(stack)
            nkColor.set(color.red.toByte(), color.green.toByte(), color.blue.toByte(), color.alpha.toByte())
            return nkColor
        }

        fun nkRect(x: Float, y: Float, width: Float, height: Float, stack: MemoryStack): NkRect {
            val nkRect = NkRect.mallocStack(stack)
            nkRect.set(x, y, width, height)
            return nkRect
        }

        data class Draw(
            val x: Float, val y: Float,
            val scale: Float,
            val childList: DrawList
        ): DrawCommand {
            override fun push(drawList: DrawList, canvas: NkCommandBuffer, stack: MemoryStack) {
                val joinedList = DrawList(childList.drawCommands)
                joinedList.transformX = drawList.tx(childList.transformX + x)
                joinedList.transformY = drawList.ty(childList.transformY + y)
                joinedList.transformScale = drawList.td(childList.transformScale * scale)
                joinedList.push(canvas)
            }
        }

        data class Clip(
            val x: Float, val y: Float,
            val w: Float, val h: Float,
        ): DrawCommand {
            override fun push(drawList: DrawList, canvas: NkCommandBuffer, stack: MemoryStack) {
                nk_push_scissor(
                    canvas,
                    NkRect.mallocStack(stack).set(
                        drawList.tx(x), drawList.ty(y),
                        drawList.td(w), drawList.td(h)
                    )
                )
            }
        }

        data class Image(
            val x: Float, val y: Float,
            val w: Float, val h: Float,
            val image: NkImage,
            val color: Color
        ): DrawCommand {
            override fun push(drawList: DrawList, canvas: NkCommandBuffer, stack: MemoryStack) {
                nk_draw_image(
                    canvas,
                    nkRect(
                        drawList.tx(x), drawList.ty(y),
                        drawList.td(w), drawList.td(h),
                        stack
                    ),
                    image,
                    nkColor(color, stack)
                )
            }
        }

        data class PixelStroke(
            val x0: Float, val y0: Float,
            val x1: Float, val y1: Float,
            val color: Color
        ): DrawCommand {
            override fun push(drawList: DrawList, canvas: NkCommandBuffer, stack: MemoryStack) {
                val tx0 = drawList.tx(x0) + 1
                val ty0 = drawList.ty(y0)
                val tx1 = drawList.tx(x1) + 1
                val ty1 = drawList.ty(y1)

                val dx = tx1 - tx0
                val dy = ty1 - ty0
                val length = sqrt(dx * dx + dy * dy)
                val normalX = -abs(dy / length)
                val normalY = abs(dx / length)

                val points = floatArrayOf(
                    tx0, ty0,
                    tx1, ty1,
                    tx1 + normalX, ty1 + normalY,
                    tx0 + normalX, ty0 + normalY
                )
//                points.reverse()
                nnk_fill_polygon(
                    canvas.address(),
                    points,
                    4,
                    nkColor(color, stack).address()
                )
            }
        }

        data class PixelStrokeRect(
            val x: Float, val y: Float,
            val w: Float, val h: Float,
            val color: Color
        ): DrawCommand {
            override fun push(drawList: DrawList, canvas: NkCommandBuffer, stack: MemoryStack) {
                val tx = drawList.tx(x)
                val ty = drawList.ty(y)
                val tw = drawList.td(w)
                val th = drawList.td(h)

                val polygons = listOf(
                    floatArrayOf( // left (including corners)
                        tx, ty - 1,
                        tx, ty + 1 + th,
                        tx - 1, ty + 1 + th,
                        tx - 1, ty - 1
                    ),
                    floatArrayOf( // right (including corners)
                        tx + tw + 1, ty - 1,
                        tx + tw + 1, ty + 1 + th,
                        tx + tw, ty + 1 + th,
                        tx + tw, ty - 1
                    ),
                    floatArrayOf( // top
                        tx, ty - 1,
                        tx + tw, ty - 1,
                        tx + tw, ty,
                        tx, ty
                    ),
                    floatArrayOf( // bottom
                        tx, ty + th,
                        tx + tw, ty + th,
                        tx + tw, ty + th + 1,
                        tx, ty + th + 1
                    ),
                )
                for(polygon in polygons) {
                    nnk_fill_polygon(
                        canvas.address(),
                        polygon,
                        4,
                        nkColor(color, stack).address()
                    )
                }
            }
        }

        data class StrokeLine(
            val x0: Float, val y0: Float,
            val x1: Float, val y1: Float,
            val thickness: Float, val color: Color
        ): DrawCommand {
            override fun push(drawList: DrawList, canvas: NkCommandBuffer, stack: MemoryStack) {
                nk_stroke_line(canvas,
                    drawList.tx(x0), drawList.ty(y0),
                    drawList.tx(x1), drawList.ty(y1),
                    thickness,
                    nkColor(color, stack)
                )
            }
        }

        data class StrokeCurve(
            val ax: Float, val ay: Float,
            val ctrl0x: Float, val ctrl0y: Float,
            val ctrl1x: Float, val ctrl1y: Float,
            val bx: Float, val by: Float,
            val thickness: Float, val color: Color
        ): DrawCommand {
            override fun push(drawList: DrawList, canvas: NkCommandBuffer, stack: MemoryStack) {
                nk_stroke_curve(canvas,
                    drawList.tx(ax), drawList.ty(ay),
                    drawList.tx(ctrl0x), drawList.ty(ctrl0y),
                    drawList.tx(ctrl1x), drawList.ty(ctrl1y),
                    drawList.tx(bx), drawList.ty(by),
                    thickness,
                    nkColor(color, stack)
                )
            }
        }

        data class StrokeRect(
            val x: Float, val y: Float,
            val w: Float, val h: Float,
            val rounding: Float,
            val thickness: Float, val color: Color
        ): DrawCommand {
            override fun push(drawList: DrawList, canvas: NkCommandBuffer, stack: MemoryStack) {
                val rect = NkRect.mallocStack(stack)
                rect.x(drawList.tx(x))
                rect.y(drawList.ty(y))
                rect.w(drawList.td(w))
                rect.h(drawList.td(h))
                nk_stroke_rect(canvas,
                    rect,
                    drawList.td(rounding),
                    thickness,
                    nkColor(color, stack)
                )
            }
        }

        data class StrokeCircle(
            val x: Float, val y: Float,
            val w: Float, val h: Float,
            val thickness: Float, val color: Color
        ): DrawCommand {
            override fun push(drawList: DrawList, canvas: NkCommandBuffer, stack: MemoryStack) {
                val rect = NkRect.mallocStack(stack)
                rect.x(drawList.tx(x))
                rect.y(drawList.ty(y))
                rect.w(drawList.td(w))
                rect.h(drawList.td(h))
                nk_stroke_circle(canvas,
                    rect,
                    thickness,
                    nkColor(color, stack)
                )
            }
        }

        data class StrokeArc(
            val cx: Float, val cy: Float,
            val radius: Float,
            val minAngle: Float, val maxAngle: Float,
            val thickness: Float, val color: Color
        ): DrawCommand {
            override fun push(drawList: DrawList, canvas: NkCommandBuffer, stack: MemoryStack) {
                nk_stroke_arc(canvas,
                    drawList.tx(cx), drawList.ty(cy),
                    drawList.td(radius),
                    minAngle, maxAngle,
                    thickness,
                    nkColor(color, stack)
                )
            }
        }

        data class StrokeTriangle(
            val x0: Float, val y0: Float,
            val x1: Float, val y1: Float,
            val x2: Float, val y2: Float,
            val thickness: Float, val color: Color
        ): DrawCommand {
            override fun push(drawList: DrawList, canvas: NkCommandBuffer, stack: MemoryStack) {
                nk_stroke_triangle(canvas,
                    drawList.tx(x0), drawList.ty(y0),
                    drawList.tx(x1), drawList.ty(y1),
                    drawList.tx(x2), drawList.ty(y2),
                    thickness,
                    nkColor(color, stack)
                )
            }
        }

        data class StrokePolyline(
            val points: MutableList<Float>,
            val thickness: Float, val color: Color
        ): DrawCommand {
            override fun push(drawList: DrawList, canvas: NkCommandBuffer, stack: MemoryStack) {
                val nkPoints = FloatArray(points.size)
                for(i in 0 until points.size step 2) {
                    nkPoints[i] = drawList.tx(points[i])
                    nkPoints[i+1] = drawList.ty(points[i+1])
                }
                nnk_stroke_polyline(
                    canvas.address(),
                    nkPoints,
                    points.size / 2,
                    thickness,
                    nkColor(color, stack).address()
                )
            }
        }

        data class StrokePolygon(
            val points: MutableList<Float>,
            val thickness: Float, val color: Color
        ): DrawCommand {
            override fun push(drawList: DrawList, canvas: NkCommandBuffer, stack: MemoryStack) {
                val nkPoints = FloatArray(points.size)
                for(i in 0 until points.size step 2) {
                    nkPoints[i] = drawList.tx(points[i])
                    nkPoints[i+1] = drawList.ty(points[i+1])
                }
                nnk_stroke_polygon(
                    canvas.address(),
                    nkPoints,
                    points.size / 2,
                    thickness,
                    nkColor(color, stack).address()
                )
            }
        }

        data class FillRect(
            val x: Float, val y: Float,
            val w: Float, val h: Float,
            val rounding: Float,
            val color: Color
        ): DrawCommand {
            override fun push(drawList: DrawList, canvas: NkCommandBuffer, stack: MemoryStack) {
                val rect = NkRect.mallocStack(stack)
                rect.x(drawList.tx(x))
                rect.y(drawList.ty(y))
                rect.w(drawList.td(w))
                rect.h(drawList.td(h))
                nk_fill_rect(canvas,
                    rect,
                    drawList.td(rounding),
                    nkColor(color, stack)
                )
            }
        }

        data class FillArc(
            val cx: Float, val cy: Float,
            val radius: Float,
            val minAngle: Float, val maxAngle: Float,
            val color: Color
        ): DrawCommand {
            override fun push(drawList: DrawList, canvas: NkCommandBuffer, stack: MemoryStack) {
                nk_fill_arc(canvas,
                    drawList.tx(cx), drawList.ty(cy),
                    drawList.td(radius),
                    minAngle, maxAngle,
                    nkColor(color, stack)
                )
            }
        }

        data class FillTriangle(
            val x0: Float, val y0: Float,
            val x1: Float, val y1: Float,
            val x2: Float, val y2: Float,
            val color: Color
        ): DrawCommand {
            override fun push(drawList: DrawList, canvas: NkCommandBuffer, stack: MemoryStack) {
                nk_fill_triangle(canvas,
                    drawList.tx(x0), drawList.ty(y0),
                    drawList.tx(x1), drawList.ty(y1),
                    drawList.tx(x2), drawList.ty(y2),
                    nkColor(color, stack)
                )
            }
        }

        data class FillPolygon(
            val points: MutableList<Float>,
            val color: Color
        ): DrawCommand {
            override fun push(drawList: DrawList, canvas: NkCommandBuffer, stack: MemoryStack) {
                val nkPoints = FloatArray(points.size)
                for(i in 0 until points.size step 2) {
                    nkPoints[i] = drawList.tx(points[i])
                    nkPoints[i+1] = drawList.ty(points[i+1])
                }
                nnk_fill_polygon(
                    canvas.address(),
                    nkPoints,
                    points.size / 2,
                    nkColor(color, stack).address()
                )
            }
        }

        data class FillText(
            val x: Float,
            val y: Float,
            val height: Float,
            val font: NkUserFont,
            val text: String,
            val color: Color
        ): DrawCommand {
            override fun push(drawList: DrawList, canvas: NkCommandBuffer, stack: MemoryStack) {
                nk_draw_text(
                    canvas,
                    nkRect(drawList.tx(x), drawList.ty(y), 100000f, height, stack),
                    text,
                    font,
                    nkColor(Color(0, 0, 0, 0), stack),
                    nkColor(color, stack)
                )
            }
        }
    }

    class PolyBuilder(private val points: MutableList<Float>, private val drawList: DrawList) {
        fun point(x: Float, y: Float): PolyBuilder {
            points.add(x)
            points.add(y)
            return this
        }

        fun finish(): DrawList {
            return drawList
        }
    }
}