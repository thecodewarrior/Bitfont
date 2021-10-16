package dev.thecodewarrior.bitfont.fonteditor.widgets

import dev.thecodewarrior.bitfont.fonteditor.utils.DrawList
import org.lwjgl.nuklear.NkContext
import org.lwjgl.nuklear.NkRect
import org.lwjgl.nuklear.Nuklear.*
import org.lwjgl.system.MemoryStack

abstract class NkWidget {
    private var lastWidth: Float = -1f
    private var lastHeight: Float = -1f

    protected open fun resize(width: Float, height: Float) {}

    protected abstract fun draw(
        ctx: NkContext, drawList: DrawList,
        rom: Boolean,
        width: Float, height: Float,
        mouseX: Float, mouseY: Float
    )

    fun push(ctx: NkContext) {
        MemoryStack.stackPush().use { stack ->
            val bounds = NkRect.mallocStack(stack)
            val layoutState = nk_widget(bounds, ctx)
            if(layoutState != NK_WIDGET_INVALID) {
                val mousePos = ctx.input().mouse().pos()
                val mouseX = mousePos.x() - bounds.x()
                val mouseY = mousePos.y() - bounds.y()

                if(bounds.w() != lastWidth || bounds.h() != lastHeight) {
                    lastWidth = bounds.w()
                    lastHeight = bounds.h()
                    resize(bounds.w(), bounds.h())
                }

                val drawList = DrawList()
                draw(ctx, drawList, layoutState == NK_WIDGET_ROM, bounds.w(), bounds.h(), mouseX, mouseY)
                drawList.transformX = bounds.x()
                drawList.transformY = bounds.y()
                drawList.push(ctx)
            }

        }
    }
}

fun pushWidget(
    ctx: NkContext,
    drawFunction: (
        ctx: NkContext, drawList: DrawList,
        rom: Boolean,
        width: Float, height: Float,
        mouseX: Float, mouseY: Float
    ) -> Unit
) {
    object : NkWidget() {
        override fun draw(
            ctx: NkContext,
            drawList: DrawList,
            rom: Boolean,
            width: Float,
            height: Float,
            mouseX: Float,
            mouseY: Float
        ) {
            drawFunction(ctx, drawList, rom, width, height, mouseX, mouseY)
        }
    }.push(ctx)
}