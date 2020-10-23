package dev.thecodewarrior.bitfont.editor

import dev.thecodewarrior.bitfont.data.file.BitfontFile
import dev.thecodewarrior.bitfont.data.file.BitfontFileFormat
import dev.thecodewarrior.bitfont.editor.data.BitfontEditorData
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
import java.text.ParseException
import java.util.UUID

class TypesetterWindow(val data: BitfontEditorData): AbstractFontTestWindow(500f, 200f) {
    private var testText: String = ""

    override fun pushControls(ctx: NkContext) {
        MemoryStack.stackPush().use { stack ->
            nk_layout_row_dynamic(ctx, 25f, 1)
            val baz = NkVec2.mallocStack(stack)
            nk_widget_position(ctx, baz)
            run {
                val buffer = stack.calloc(256)
                val length = MemoryUtil.memUTF8(testText, false, buffer)
                val len = stack.ints(length)
                nk_edit_string(ctx, NK_EDIT_FIELD, buffer, len, 255, null)
                try {
                    val oldText = testText
                    // `len` doesn't handle unicode well. It ends up with a bunch of extra null bytes, so we test it
                    // ourselves
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
                    drawGlyph(image, main.glyph,
                        originX + main.posX + attachment.posX,
                        originY + main.posY + attachment.posX,
                        Color.WHITE.rgb
                    )
                }
            }
        }
    }

    override fun free() {
        super.free()
    }

}