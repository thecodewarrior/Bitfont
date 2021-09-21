package dev.thecodewarrior.bitfont.fonteditor

import dev.thecodewarrior.bitfont.fonteditor.data.BitfontEditorData
import dev.thecodewarrior.bitfont.fonteditor.utils.DistinctColors
import dev.thecodewarrior.bitfont.fonteditor.utils.DrawList
import dev.thecodewarrior.bitfont.typesetting.AttributedString
import dev.thecodewarrior.bitfont.typesetting.MutableAttributedString
import dev.thecodewarrior.bitfont.typesetting.SimpleTextContainer
import dev.thecodewarrior.bitfont.typesetting.TextAttribute
import dev.thecodewarrior.bitfont.typesetting.TextContainer
import dev.thecodewarrior.bitfont.typesetting.TextLayoutManager
import dev.thecodewarrior.bitfont.utils.Vec2i
import org.lwjgl.nuklear.NkContext
import org.lwjgl.nuklear.NkTextEdit
import org.lwjgl.nuklear.NkVec2
import org.lwjgl.nuklear.Nuklear.NK_EDIT_BOX
import org.lwjgl.nuklear.Nuklear.nk_checkbox_label
import org.lwjgl.nuklear.Nuklear.nk_edit_buffer
import org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic
import org.lwjgl.nuklear.Nuklear.nk_property_int
import org.lwjgl.nuklear.Nuklear.nk_str_len_char
import org.lwjgl.nuklear.Nuklear.nk_textedit_init
import org.lwjgl.nuklear.Nuklear.nk_widget_position
import org.lwjgl.nuklear.Nuklear.nnk_combo_string
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import java.awt.Color
import java.awt.image.BufferedImage
import java.text.ParseException
import javax.imageio.ImageIO

class TextLayoutWindow(val data: BitfontEditorData): AbstractFontTestWindow(700f, 400f) {
    private var testText: String = ""

    private var showLines = false
    private var splitColumns = false
    private var exclusionZones = false
    private var embeds = false
    private var alignment = TextLayoutManager.Alignment.LEFT
    private var truncation = false
    private var maxLines = 0

    private var containers = mutableListOf<Pair<TextContainer, Vec2i>>()

    private var textEditor = NkTextEdit.malloc()
    init {
        nk_textedit_init(textEditor, BitfontEditorApp.ALLOCATOR, 10)
    }

    override fun pushControls(ctx: NkContext) {
        MemoryStack.stackPush().use { stack ->
            nk_layout_row_dynamic(ctx, 100f, 1)
            val baz = NkVec2.mallocStack(stack)
            nk_widget_position(ctx, baz)
            run {
                nk_edit_buffer(ctx, NK_EDIT_BOX, textEditor, null)
                try {
                    val oldText = testText
                    val string = textEditor.string()
                    val pointer = string.buffer().memory().ptr()!!
                    testText = MemoryUtil.memUTF8(pointer, nk_str_len_char(string))
                    if(testText != oldText)
                        markDirty()
                } catch (e: ParseException) {
                    e.printStackTrace()
                }
            }

            nk_layout_row_dynamic(ctx, 25f, 3)
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

            val items = stack.bytes(*TextLayoutManager.Alignment.values().joinToString("") { "$it\u0000" }.toByteArray())
            alignment = dirtyOnChange(TextLayoutManager.Alignment.values()[
                nnk_combo_string(ctx.address(), MemoryUtil.memAddress(items),
                    alignment.ordinal, TextLayoutManager.Alignment.values().size,
                    25, NkVec2.mallocStack().set(150f, 100f).address()
                )
            ], alignment)

            nk_layout_row_dynamic(ctx, 25f, 3)
            splitColumns = checkbox("Split columns", splitColumns)
            exclusionZones = checkbox("Exclusion zones", exclusionZones)
            embeds = checkbox("Embeds (* = embed)", embeds)

            nk_layout_row_dynamic(ctx, 25f, 3)
            truncation = checkbox("Truncation", truncation)
            val maxLinesOnOff = if(maxLines != 0) "ON" else "OFF"
            intBuf.put(0, maxLines)
            nk_property_int(ctx, "Max lines [$maxLinesOnOff]:", 0, intBuf, 100, 1, 1f)
            maxLines = dirtyOnChange(intBuf[0], maxLines)
        }
    }

    override fun redraw(logicalWidth: Int, logicalHeight: Int, image: BufferedImage, drawList: DrawList) {
        val testAttributedText = MutableAttributedString(testText)

        if(embeds) {
            val testEmbed = BufferedImageEmbed(
                testEmbedImage.width + 1,
                0, -(data.font.xHeight + testEmbedImage.height) / 2,
                testEmbedImage
            )

            var index = testText.indexOf('*')
            while(index >= 0) {
                testAttributedText.setAttribute(index, index+1, TextAttribute.textEmbed, testEmbed)
                index = testText.indexOf('*', index+1)
            }
        }

        containers = mutableListOf<Pair<TextContainer, Vec2i>>()
        if(splitColumns) {
            val column1Width = logicalWidth / 3 - 4
            val column2Width = 2 * logicalWidth / 3 - 4
            containers.add(ExclusionContainer(exclusionZones, column1Width, logicalHeight - 2) to Vec2i(1, 1))
            containers.add(ExclusionContainer(exclusionZones, column2Width, logicalHeight - 2) to Vec2i(logicalWidth - column2Width - 1, 1))
            drawList.strokeLine(logicalWidth / 3, 0, logicalWidth / 3, logicalHeight, 2, DistinctColors.cyan)
        } else {
            containers.add(ExclusionContainer(exclusionZones, logicalWidth - 2, logicalHeight - 2) to Vec2i(1, 1))
        }

        val layoutManager = TextLayoutManager(data.font)
        containers.forEach { (it, _) -> layoutManager.textContainers.add(it) }
        layoutManager.options.alignment = alignment
        if(truncation)
            layoutManager.options.truncationString = AttributedString("...")
        if(maxLines != 0)
            containers.forEach { (it, _) -> it.maxLines = maxLines }

        layoutManager.attributedString = testAttributedText
        layoutManager.layoutText()

        containers.forEach { (container, containerPos) ->
            if (showLines) {
                for (line in container.lines) {
                    drawList.strokeRect(
                        containerPos.x + line.posX, containerPos.y + line.posY,
                        line.width, line.height,
                        0, 2,
                        DistinctColors.green
                    )
                }
            }
            for (glyph in container.glyphs) {
                drawGlyph(image, glyph.textObject, containerPos.x + glyph.posX, containerPos.y + glyph.posY, Color.WHITE.rgb)
            }
        }
    }

    private class ExclusionContainer(val exclude: Boolean, width: Int, height: Int = Int.MAX_VALUE): SimpleTextContainer(width, height) {
        override fun fixLineFragment(line: TextContainer.LineBounds) {
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
        textEditor.free()
        super.free()
    }

    companion object {
        val testEmbedImage: BufferedImage = ImageIO.read(TextLayoutWindow::class.java.getResourceAsStream("testEmbed.png"))
    }
}