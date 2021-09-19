package dev.thecodewarrior.bitfont.fonteditor.utils

import org.lwjgl.nuklear.NkColor
import org.lwjgl.nuklear.NkCommandBuffer
import org.lwjgl.nuklear.NkContext
import org.lwjgl.nuklear.NkRect
import org.lwjgl.nuklear.Nuklear.*
import org.lwjgl.system.MemoryStack
import java.awt.Color

@Suppress("NOTHING_TO_INLINE")
class DrawList {
    var transformX: Float = 0f
    var transformY: Float = 0f
    var transformScale: Float = 1f

    val drawCommands = mutableListOf<DrawCommand>()

    fun push(ctx: NkContext) {
        val canvas = nk_window_get_canvas(ctx)!!
        MemoryStack.stackGet().also { stack ->
            drawCommands.forEach {
                stack.push()
                it.push(this, canvas, stack)
                stack.pop()
            }
        }
    }

    fun clear() {
        drawCommands.clear()
    }

    inline fun strokeLine(
        x0: Number, y0: Number,
        x1: Number, y1: Number,
        thickness: Number, color: Color
    ): DrawList {
        drawCommands.add(DrawCommand.StrokeLine(
            x0.toFloat(), y0.toFloat(),
            x1.toFloat(), y1.toFloat(),
            thickness.toFloat(), color
        ))
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
        drawCommands.add(DrawCommand.StrokeRect(
            x.toFloat(), y.toFloat(),
            w.toFloat(), h.toFloat(),
            rounding.toFloat(),
            thickness.toFloat(), color
        ))
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

    /**
     * Apply the transform to an x pos
     */
    private fun tx(x: Float): Float {
        return transformX + x * transformScale
    }

    /**
     * Apply the transform to a y pos
     */
    private fun ty(y: Float): Float {
        return transformY + y * transformScale
    }

    /**
     * Apply the transform to a delta
     */
    private fun td(delta: Float): Float {
        return delta * transformScale
    }

    sealed class DrawCommand {
        abstract fun push(drawList: DrawList, canvas: NkCommandBuffer, stack: MemoryStack)

        fun nkColor(color: Color, stack: MemoryStack): NkColor {
            val nkColor = NkColor.mallocStack(stack)
            nkColor.set(color.red.toByte(), color.green.toByte(), color.blue.toByte(), color.alpha.toByte())
            return nkColor
        }

        data class StrokeLine(
            val x0: Float, val y0: Float,
            val x1: Float, val y1: Float,
            val thickness: Float, val color: Color
        ): DrawCommand() {
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
        ): DrawCommand() {
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
        ): DrawCommand() {
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
        ): DrawCommand() {
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
        ): DrawCommand() {
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
        ): DrawCommand() {
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
        ): DrawCommand() {
            override fun push(drawList: DrawList, canvas: NkCommandBuffer, stack: MemoryStack) {
                nk_stroke_polyline(canvas,
                    points.toFloatArray(),
                    thickness,
                    nkColor(color, stack)
                )
            }
        }

        data class StrokePolygon(
            val points: MutableList<Float>,
            val thickness: Float, val color: Color
        ): DrawCommand() {
            override fun push(drawList: DrawList, canvas: NkCommandBuffer, stack: MemoryStack) {
                nk_stroke_polygon(canvas,
                    points.toFloatArray(),
                    thickness,
                    nkColor(color, stack)
                )
            }
        }

        data class FillRect(
            val x: Float, val y: Float,
            val w: Float, val h: Float,
            val rounding: Float,
            val color: Color
        ): DrawCommand() {
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
        ): DrawCommand() {
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
        ): DrawCommand() {
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
        ): DrawCommand() {
            override fun push(drawList: DrawList, canvas: NkCommandBuffer, stack: MemoryStack) {
                nk_fill_polygon(canvas,
                    points.toFloatArray(),
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