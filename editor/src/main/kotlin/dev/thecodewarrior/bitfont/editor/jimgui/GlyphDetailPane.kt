package dev.thecodewarrior.bitfont.editor.jimgui

import dev.thecodewarrior.bitfont.editor.jimgui.BitfontDocument
import dev.thecodewarrior.bitfont.editor.jimgui.imgui.ImGui
import dev.thecodewarrior.bitfont.editor.jimgui.utils.Constants
import dev.thecodewarrior.bitfont.editor.jimgui.utils.nameslist.ExpandLine
import dev.thecodewarrior.bitfont.editor.jimgui.utils.nameslist.ExpandLineElement

class GlyphDetailPane(val document: BitfontDocument) {
    val width: Float
        get() = 175f
    var codepoint = 0

    fun draw(imgui: ImGui) {
        val defaultFontScale = imgui.font.scale
        val info = Constants.namesList.chars[codepoint]

        imgui.font.scale = 1.75f
        if(info == null) {
            imgui.text("U+%04X".format(codepoint))
            imgui.font.scale = defaultFontScale
            return
        }
        imgui.text(info.name)
        imgui.font.scale = defaultFontScale
/*
        info.lines.forEach { line ->
            when(line) {
                is AliasLine -> textWrapped("= ${line.text}")
                is FormalAliasLine -> textWrapped("% ${line.name}")
                is CommentLine -> {
                    if(line.hasBullet) {
                        bullet()
                    }
                    draw(line.expandLine)
                }
                is CrossRef -> {
                    textWrapped("-> %04X - %s".format(line.codepoint, line.name))
                }
                is Decomposition -> {}
                is CompatMapping -> {}
                is NoticeLine -> {}
                is VariationLine -> {}
            }
        }
*/

        if(imgui.button("Edit")) {
//            document.editorWindow.codepoint = codepoint
//            document.editorWindow.codepointHistory.push(codepoint)
//            document.editorWindow.visible = true
//            document.editorWindow.focus()
        }
    }

    fun draw(line: ExpandLine, imgui: ImGui) = with(ImGui) {
        line.elements.forEach { element ->
            when(element) {
                is ExpandLineElement.Text -> {
                    imgui.sameLine()
                    imgui.textWrapped(element.text)
                }
                is ExpandLineElement.Codepoint -> {
                    imgui.sameLine()
                    imgui.smallButton("%04X##%s".format(element.codepoint, System.identityHashCode(element)))
                }
            }
        }
    }
}