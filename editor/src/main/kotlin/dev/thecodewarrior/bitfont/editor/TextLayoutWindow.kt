package dev.thecodewarrior.bitfont.editor

import dev.thecodewarrior.bitfont.editor.data.BitfontEditorData
import dev.thecodewarrior.bitfont.editor.utils.DistinctColors
import dev.thecodewarrior.bitfont.editor.utils.DrawList
import dev.thecodewarrior.bitfont.typesetting.AttributedString
import dev.thecodewarrior.bitfont.typesetting.GlyphGenerator
import dev.thecodewarrior.bitfont.typesetting.LineFragment
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
import org.lwjgl.nuklear.Nuklear.nk_combo
import org.lwjgl.nuklear.Nuklear.nk_combo_string
import org.lwjgl.nuklear.Nuklear.nk_edit_string_zero_terminated
import org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic
import org.lwjgl.nuklear.Nuklear.nk_property_int
import org.lwjgl.nuklear.Nuklear.nk_strlen
import org.lwjgl.nuklear.Nuklear.nk_widget_position
import org.lwjgl.nuklear.Nuklear.nk_window_get_width
import org.lwjgl.nuklear.Nuklear.nnk_combo_string
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import java.awt.Color
import java.awt.image.BufferedImage
import java.text.ParseException
import kotlin.math.max

class TextLayoutWindow(val data: BitfontEditorData): AbstractFontTestWindow(700f, 400f) {
    private var testText: String = ""
    private var currentWidth: Float = 0f

    private var showLines = false
    private var splitColumns = false
    private var exclusionZones = false
    private var alignment = TextLayoutManager.Alignment.LEFT

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

            nk_layout_row_dynamic(ctx, 25f, 5)
            val intBuf = stack.ints(0)
            fun checkbox(name: String, value: Boolean): Boolean {
                intBuf.put(0, if(value) 1 else 0)
                nk_checkbox_label(ctx, name, intBuf)
                return dirtyOnChange(intBuf[0] != 0, value)
            }

            intBuf.put(0, testAreaScale)
            nk_property_int(ctx, "Scale:", 1, intBuf, 5, 1, 1f)
            testAreaScale = dirtyOnChange(intBuf[0], testAreaScale)

            showLines = checkbox("Show lines", showLines)
            splitColumns = checkbox("Split columns", splitColumns)
            exclusionZones = checkbox("Exclusion zones", exclusionZones)

            val items = stack.bytes(*TextLayoutManager.Alignment.values().joinToString("") { "$it\u0000" }.toByteArray())
            alignment = dirtyOnChange(TextLayoutManager.Alignment.values()[
                nnk_combo_string(ctx.address(), MemoryUtil.memAddress(items),
                    alignment.ordinal, TextLayoutManager.Alignment.values().size,
                    25, NkVec2.mallocStack().set(150f, 100f).address()
                )
            ], alignment)
        }
    }

    override fun redraw(logicalWidth: Int, logicalHeight: Int, image: BufferedImage, drawList: DrawList) {
        val testAttributedText = AttributedString(testText)
        val containers = mutableListOf<Pair<TextContainer, Vec2i>>()
        if(splitColumns) {
            val column1Width = logicalWidth / 3 - 4
            val column2Width = 2 * logicalWidth / 3 - 4
            containers.add(ExclusionContainer(exclusionZones, column1Width, logicalHeight - 2) to Vec2i(1, 1))
            containers.add(ExclusionContainer(exclusionZones, column2Width, logicalHeight - 2) to Vec2i(logicalWidth - column2Width - 1, 1))
            drawList.strokeLine(logicalWidth / 3, 0, logicalWidth / 3, logicalHeight, 2, DistinctColors.cyan)
        } else {
            containers.add(ExclusionContainer(exclusionZones, logicalWidth - 2, logicalHeight - 2) to Vec2i(1, 1))
        }
//        if(exclusion) configureExclusion(imgui, container, area, bounds)

        val layoutManager = TextLayoutManager(listOf(data.font))
        containers.forEach { (it, _) -> layoutManager.textContainers.add(it) }
        layoutManager.alignment = alignment

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

    private class ExclusionContainer(val exclude: Boolean, width: Int, height: Int = Int.MAX_VALUE): TextContainer(width, height) {
        override fun fixLineFragment(line: LineFragment) {
            if(exclude && line.posY < 64 && line.posY + line.height >= 0) {
                if(width <= 64) {
                    val stepHeight = line.height + line.spacing
                    val steps = (64 + stepHeight - 1) / stepHeight
                    line.posY = steps * stepHeight
                } else {
                    line.width = width - 64
                    line.posX = 64
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