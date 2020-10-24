package dev.thecodewarrior.bitfont.editor

import dev.thecodewarrior.bitfont.editor.data.BitfontEditorData
import dev.thecodewarrior.bitfont.editor.utils.DistinctColors
import dev.thecodewarrior.bitfont.editor.utils.DrawList
import dev.thecodewarrior.bitfont.typesetting.AttributedString
import dev.thecodewarrior.bitfont.typesetting.GlyphGenerator
import dev.thecodewarrior.bitfont.typesetting.TextContainer
import dev.thecodewarrior.bitfont.typesetting.TextLayoutManager
import dev.thecodewarrior.bitfont.typesetting.Typesetter
import dev.thecodewarrior.bitfont.utils.Vec2i
import org.lwjgl.nuklear.NkContext
import org.lwjgl.nuklear.NkVec2
import org.lwjgl.nuklear.Nuklear.NK_EDIT_BOX
import org.lwjgl.nuklear.Nuklear.NK_EDIT_FIELD
import org.lwjgl.nuklear.Nuklear.nk_checkbox_label
import org.lwjgl.nuklear.Nuklear.nk_checkbox_text
import org.lwjgl.nuklear.Nuklear.nk_edit_string_zero_terminated
import org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic
import org.lwjgl.nuklear.Nuklear.nk_strlen
import org.lwjgl.nuklear.Nuklear.nk_widget_position
import org.lwjgl.nuklear.Nuklear.nk_window_get_width
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import java.awt.Color
import java.awt.image.BufferedImage
import java.text.ParseException

class TextLayoutWindow(val data: BitfontEditorData): AbstractFontTestWindow(500f, 300f) {
    private var testText: String = ""
    private var currentWidth: Float = 0f

    private var showLines = false
    private var splitColumns = false

    override fun pushControls(ctx: NkContext) {
        MemoryStack.stackPush().use { stack ->
            val newWidth = nk_window_get_width(ctx)
            if(newWidth != currentWidth)
                markDirty()
            currentWidth = newWidth

            nk_layout_row_dynamic(ctx, 100f, 1)
            val baz = NkVec2.mallocStack(stack)
            nk_widget_position(ctx, baz)
            run {
                val buffer = stack.calloc(4096)
                MemoryUtil.memUTF8(testText, true, buffer)
                nk_edit_string_zero_terminated(ctx, NK_EDIT_BOX, buffer, 4096, null)
                try {
                    val oldText = testText
                    testText = MemoryUtil.memUTF8(buffer, nk_strlen(buffer))
                    if(testText != oldText)
                        markDirty()
                } catch (e: ParseException) {
                    e.printStackTrace()
                }
            }

            nk_layout_row_dynamic(ctx, 25f, 2)
            val active = stack.ints(0)
            var previous: Boolean

            previous = showLines
            active.put(0, if(showLines) 1 else 0)
            nk_checkbox_label(ctx, "Show lines", active)
            showLines = active[0] != 0
            if(showLines != previous)
                markDirty()

            previous = splitColumns
            active.put(0, if(splitColumns) 1 else 0)
            nk_checkbox_label(ctx, "Split Columns", active)
            splitColumns = active[0] != 0
            if(splitColumns != previous)
                markDirty()
//            active.put(if(showRuns) 1 else 0)
//            nk_checkbox_label(ctx, "Show runs", active)
//            showRuns = active[0] != 0
        }
    }

    override fun redraw(logicalWidth: Int, logicalHeight: Int, image: BufferedImage, drawList: DrawList) {
        val testAttributedText = AttributedString(testText)
        val containers = mutableListOf<Pair<TextContainer, Vec2i>>()
        if(splitColumns) {
            val column1Width = logicalWidth / 3 - 4
            val column2Width = 2 * logicalWidth / 3 - 4
            containers.add(TextContainer(column1Width, logicalHeight - 2) to Vec2i(1, 1))
            containers.add(TextContainer(column2Width, logicalHeight - 2) to Vec2i(logicalWidth - column2Width - 1, 1))
            drawList.strokeLine(logicalWidth / 3, 0, logicalWidth / 3, logicalHeight, 2, DistinctColors.cyan)
        } else {
            containers.add(TextContainer(logicalWidth - 2, logicalHeight - 2) to Vec2i(1, 1))
        }
//        if(exclusion) configureExclusion(imgui, container, area, bounds)

        val layoutManager = TextLayoutManager(listOf(data.font))
        containers.forEach { (it, _) -> layoutManager.textContainers.add(it) }

//        layoutManager.typesetterOptions = options
        layoutManager.attributedString = testAttributedText
        layoutManager.layoutText()

//        if(fragmentBounds) {
//            for (line in container.lines) {
//                imgui.windowDrawList.addRectSized(
//                    area.x + line.posX * scalef, area.y + line.posY * scalef,
//                    line.width * scalef, line.height * scalef,
//                    Colors.textLayout.lineFragment.u32
//                )
//            }
//        }

        containers.forEach { (container, containerPos) ->
            for (line in container.lines) {
                val originX = line.posX + containerPos.x
                val originY = line.posY + containerPos.y
                for (main in line.glyphs) {
                    drawGlyph(image, main.glyph, originX + main.posX, originY + main.posY, Color.WHITE.rgb)
                    main.attachments?.also { attachments ->
                        for (attachment in attachments) {
                            drawGlyph(image, attachment.glyph,
                                originX + main.posX + attachment.posX,
                                originY + main.posY + attachment.posY,
                                Color.WHITE.rgb
                            )
                        }
                    }
                }

                if (showLines) {
                    drawList.strokeRect(
                        originX, originY,
                        line.width, line.height,
                        0, 2,
                        DistinctColors.green
                    )
                }
            }
        }
    }

    override fun onHide(ctx: NkContext) {
        testText = ""
        markDirty()
    }

    override fun free() {
        super.free()
    }

}