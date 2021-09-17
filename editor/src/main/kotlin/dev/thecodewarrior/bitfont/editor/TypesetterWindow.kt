package dev.thecodewarrior.bitfont.editor

import dev.thecodewarrior.bitfont.editor.data.BitfontEditorData
import dev.thecodewarrior.bitfont.editor.utils.DistinctColors
import dev.thecodewarrior.bitfont.editor.utils.DrawList
import dev.thecodewarrior.bitfont.typesetting.AttributedString
import dev.thecodewarrior.bitfont.typesetting.GlyphGenerator
import dev.thecodewarrior.bitfont.typesetting.Typesetter
import org.lwjgl.nuklear.NkContext
import org.lwjgl.nuklear.NkVec2
import org.lwjgl.nuklear.Nuklear.NK_EDIT_FIELD
import org.lwjgl.nuklear.Nuklear.nk_edit_string_zero_terminated
import org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic
import org.lwjgl.nuklear.Nuklear.nk_strlen
import org.lwjgl.nuklear.Nuklear.nk_widget_position
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import java.awt.Color
import java.awt.image.BufferedImage
import java.text.ParseException

class TypesetterWindow(val data: BitfontEditorData): AbstractFontTestWindow(500f, 200f) {
    private var testText: String = ""

    override fun pushControls(ctx: NkContext) {
        MemoryStack.stackPush().use { stack ->
            nk_layout_row_dynamic(ctx, 25f, 1)
            val baz = NkVec2.mallocStack(stack)
            nk_widget_position(ctx, baz)
            run {
                val buffer = stack.calloc(1024)
                MemoryUtil.memUTF8(testText, true, buffer)
                nk_edit_string_zero_terminated(ctx, NK_EDIT_FIELD, buffer, 1024, null)
                try {
                    val oldText = testText
                    testText = MemoryUtil.memUTF8(buffer, nk_strlen(buffer))
                    if(testText != oldText)
                        markDirty()
                } catch (e: ParseException) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun redraw(logicalWidth: Int, logicalHeight: Int, image: BufferedImage, drawList: DrawList) {
        val testAttributedText = AttributedString(testText)
        val glyphs = GlyphGenerator(testAttributedText, data.font)
        val typesetter = Typesetter(glyphs)
//        typesetter.options = options

        val originX = data.font.ascent
        val originY = logicalHeight / 2

        for(cluster in typesetter) {
            for(glyph in cluster.glyphs) {
                drawGlyph(image, glyph.textObject,
                    originX + glyph.posX,
                    originY + glyph.posY,
                    Color.WHITE.rgb
                )
            }
        }

        drawList.strokeLine(originX, originY, logicalWidth, originY, 1, DistinctColors.orange)
    }

    override fun onHide(ctx: NkContext) {
        testText = ""
        markDirty()
    }

    override fun free() {
        super.free()
    }

}