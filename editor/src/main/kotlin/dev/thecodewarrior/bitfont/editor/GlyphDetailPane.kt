package dev.thecodewarrior.bitfont.editor

import dev.thecodewarrior.bitfont.editor.imgui.ImGui
import dev.thecodewarrior.bitfont.editor.utils.Constants
import dev.thecodewarrior.bitfont.editor.utils.nameslist.AliasLine
import dev.thecodewarrior.bitfont.editor.utils.nameslist.CommentLine
import dev.thecodewarrior.bitfont.editor.utils.nameslist.CompatMapping
import dev.thecodewarrior.bitfont.editor.utils.nameslist.CrossRef
import dev.thecodewarrior.bitfont.editor.utils.nameslist.Decomposition
import dev.thecodewarrior.bitfont.editor.utils.nameslist.ExpandLine
import dev.thecodewarrior.bitfont.editor.utils.nameslist.ExpandLineElement
import dev.thecodewarrior.bitfont.editor.utils.nameslist.FormalAliasLine
import dev.thecodewarrior.bitfont.editor.utils.nameslist.NoticeLine
import dev.thecodewarrior.bitfont.editor.utils.nameslist.VariationLine

class GlyphDetailPane(val document: dev.thecodewarrior.bitfont.editor.BitfontDocument) {
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