package dev.thecodewarrior.bitfont.editor

import dev.thecodewarrior.bitfont.data.file.BitfontFile
import dev.thecodewarrior.bitfont.data.file.BitfontFileFormat
import dev.thecodewarrior.bitfont.editor.data.BitfontEditorData
import dev.thecodewarrior.bitfont.editor.utils.DistinctColors
import dev.thecodewarrior.bitfont.editor.utils.DrawList
import dev.thecodewarrior.bitfont.typesetting.AttributedString
import dev.thecodewarrior.bitfont.typesetting.GlyphGenerator
import dev.thecodewarrior.bitfont.typesetting.Typesetter
import dev.thecodewarrior.bitfont.utils.Vec2i
import org.lwjgl.BufferUtils
import org.lwjgl.nuklear.NkColor
import org.lwjgl.nuklear.NkColorf
import org.lwjgl.nuklear.NkContext
import org.lwjgl.nuklear.NkRect
import org.lwjgl.nuklear.NkVec2
import org.lwjgl.nuklear.Nuklear.*
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL12
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.InputStream
import java.lang.RuntimeException
import java.text.ParseException
import java.util.UUID
import kotlin.math.min

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
        val glyphs = GlyphGenerator(testAttributedText, listOf(data.font))
        val typesetter = Typesetter(glyphs)
//        typesetter.options = options

        val originX = data.font.ascent
        val originY = logicalHeight / 2

        for(main in typesetter) {
            drawGlyph(image, main.glyph, originX + main.posX, originY + main.posY, Color.WHITE.rgb)
            main.attachments?.also { attachments ->
                for(attachment in attachments) {
                    drawGlyph(image, attachment.glyph,
                        originX + main.posX + attachment.posX,
                        originY + main.posY + attachment.posY,
                        Color.WHITE.rgb
                    )
                }
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