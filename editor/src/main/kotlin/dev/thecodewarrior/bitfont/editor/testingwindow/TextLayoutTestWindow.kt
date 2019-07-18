package dev.thecodewarrior.bitfont.editor.testingwindow

import dev.thecodewarrior.bitfont.editor.BitfontDocument
import dev.thecodewarrior.bitfont.editor.imgui.ImGui
import dev.thecodewarrior.bitfont.editor.imgui.withNative
import dev.thecodewarrior.bitfont.editor.utils.Colors
import dev.thecodewarrior.bitfont.editor.utils.extensions.draw
import dev.thecodewarrior.bitfont.editor.utils.math.vec
import dev.thecodewarrior.bitfont.typesetting.AttributedString
import dev.thecodewarrior.bitfont.typesetting.GlyphGenerator
import dev.thecodewarrior.bitfont.typesetting.LineFragment
import dev.thecodewarrior.bitfont.typesetting.ShapeExclusionTextContainer
import dev.thecodewarrior.bitfont.typesetting.TextContainer
import dev.thecodewarrior.bitfont.typesetting.TextLayoutManager
import dev.thecodewarrior.bitfont.typesetting.Typesetter
import dev.thecodewarrior.bitfont.utils.Rect2i
import dev.thecodewarrior.bitfont.utils.Vec2i
import java.awt.Rectangle
import java.awt.geom.Area
import java.awt.geom.Ellipse2D
import java.awt.geom.Path2D
import java.awt.geom.Rectangle2D

class TextLayoutTestWindow(document: BitfontDocument): AbstractTestWindow(document, "Text Layout Test") {
    var text: AttributedString = AttributedString("")
    val options = Typesetter.Options()
    var exclusion = false

    override fun stringInput(string: String) {
        text = AttributedString(string)
    }

    override fun drawControls(imgui: ImGui) {
        imgui.sameLine()
        withNative(options::enableKerning) {
            imgui.checkbox("Kerning", it)
        }
        imgui.sameLine()
        withNative(options::enableCombiningCharacters) {
            imgui.checkbox("Combining Characters", it)
        }
        imgui.sameLine()
        withNative(::exclusion) {
            imgui.checkbox("Exclusion", it)
        }
    }

    override fun drawCanvas(imgui: ImGui) {
        val height = document.bitfont.ascent + document.bitfont.descent
        val area = Rect2i(
            canvas.min.xi + height * scale, canvas.min.yi + height * scale,
            canvas.widthi - height * scale * 2, canvas.heighti - height * scale * 2
        )
        val bounds = Rect2i(
            0, 0,
            area.width / scale, area.height / scale
        )

        val container = ShapeExclusionTextContainer()
        if(exclusion) {
            val scalef = scale.toFloat()

//            run {
//                val relativeMouse = (imgui.io.mousePos - vec(area.x, area.y)) / scale
//
//                val size = 10
//                val shape = Rectangle(
//                    relativeMouse.x.toInt() - size, relativeMouse.y.toInt() - size,
//                    size * 2, size * 2
//                )
//                container.exclusionPaths.add(shape)
//                imgui.windowDrawList.addRectFilled(
//                    area.x + shape.x * scalef, area.y + shape.y * scalef,
//                    area.x + (shape.x + shape.width) * scalef, area.y + (shape.y + shape.height) * scalef,
//                    Colors.layoutTest.originIndicator.rgb
//                )
//            }

//            run {
//                val radius = 60
//                val shape = Ellipse2D.Float(
//                    bounds.width - radius.toFloat(), bounds.height / 2 - radius.toFloat(),
//                    radius * 2f, radius * 2f
//                )
//                container.exclusionPaths.add(shape)
//                imgui.windowDrawList.addCircleFilled(
//                    area.x + shape.centerX.toFloat() * scale, area.y + shape.centerY.toFloat() * scale,
//                    shape.width * scale / 2, Colors.layoutTest.originIndicator.rgb, 64
//                )
//            }

//            run {
//                val radius = 60
//                val shape = Ellipse2D.Float(
//                    bounds.width - radius.toFloat(), bounds.height / 2 - radius.toFloat(),
//                    radius * 2f, radius * 2f
//                )
//                container.exclusionPaths.add(shape)
//                imgui.windowDrawList.addCircleFilled(
//                    area.x + shape.centerX.toFloat() * scale, area.y + shape.centerY.toFloat() * scale,
//                    shape.width * scale / 2, Colors.layoutTest.originIndicator.rgb, 64
//                )
//            }

//            run {
//                val path = Path2D.Float()
//                path.moveTo(bounds.width.toFloat(), 0f)
//                path.curveTo(
//                    (bounds.width - 60).toFloat(), 0f,
//                    (bounds.width / 2 + 60).toFloat(), bounds.height.toFloat(),
//                    (bounds.width / 2).toFloat(), bounds.height.toFloat()
//                )
//                path.lineTo(bounds.width.toFloat(), bounds.height.toFloat())
//                path.closePath()
//                container.exclusionPaths.add(path)
//                imgui.windowDrawList.addBezierCurve(
//                    area.x + bounds.width * scalef, area.y.toFloat(),
//                    area.x + (bounds.width - 60) * scalef, area.y.toFloat(),
//                    area.x + (bounds.width / 2 + 60) * scalef, area.y + bounds.height * scalef,
//                    area.x + (bounds.width / 2) * scalef, area.y + bounds.height * scalef,
//                    Colors.layoutTest.originIndicator.rgb
//                )
//            }

            run {
                val radius = 100
                val innerRadius = 50
                val outerShape = Ellipse2D.Float(
                    canvas.widthi / scale / 2 - radius.toFloat(), canvas.heighti / scale / 2 - radius.toFloat(),
                    radius * 2f, radius * 2f
                )
                val innerShape = Ellipse2D.Float(
                    canvas.widthi / scale / 2 - innerRadius.toFloat(), canvas.heighti / scale / 2 - innerRadius.toFloat(),
                    innerRadius * 2f, innerRadius * 2f
                )
                val shape = Area()
                shape.add(Area(outerShape))
                shape.subtract(Area(innerShape))

                container.exclusionPaths.add(shape)
                imgui.windowDrawList.addCircleFilled(
                    area.x + outerShape.centerX.toFloat() * scale, area.y + outerShape.centerY.toFloat() * scale,
                    outerShape.width * scale / 2, Colors.layoutTest.originIndicator.rgb, 64
                )
                imgui.windowDrawList.addCircleFilled(
                    area.x + innerShape.centerX.toFloat() * scale, area.y + innerShape.centerY.toFloat() * scale,
                    innerShape.width * scale / 2, Colors.layoutTest.background.rgb, 64
                )
            }
        }

        container.size = Vec2i(canvas.widthi / scale - height * 2, canvas.heighti / scale - height * 4)
        val layoutManager = TextLayoutManager(listOf(document.bitfont))
        layoutManager.textContainers.add(container)
        layoutManager.typesetterOptions = options
        layoutManager.attributedString = text
        layoutManager.layoutText()

        for(line in container.lines) {
            val origin = vec(area.x, area.y) + vec(line.posX, line.posY) * scale
            for(main in line.glyphs) {
                main.draw(imgui, origin + vec(main.posX, main.posY) * scale, scale, Colors.layoutTest.text.rgb)
                main.attachments?.also { attachments ->
                    for(attachment in attachments) {
                        attachment.draw(imgui,
                            origin + vec(main.posX + attachment.posX, main.posY + attachment.posY) * scale,
                            scale, Colors.layoutTest.text.rgb
                        )
                    }
                }
            }
        }
    }
}