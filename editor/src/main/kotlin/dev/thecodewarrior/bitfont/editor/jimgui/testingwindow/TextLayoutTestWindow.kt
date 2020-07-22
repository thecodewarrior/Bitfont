package dev.thecodewarrior.bitfont.editor.jimgui.testingwindow

import dev.thecodewarrior.bitfont.editor.jimgui.BitfontDocument
import dev.thecodewarrior.bitfont.editor.jimgui.imgui.ImGui
import dev.thecodewarrior.bitfont.editor.jimgui.imgui.withNative
import dev.thecodewarrior.bitfont.editor.jimgui.utils.Colors
import dev.thecodewarrior.bitfont.editor.jimgui.utils.extensions.draw
import dev.thecodewarrior.bitfont.editor.jimgui.utils.extensions.u32
import dev.thecodewarrior.bitfont.editor.jimgui.utils.math.vec
import dev.thecodewarrior.bitfont.editor.jimgui.testingwindow.AbstractTestWindow
import dev.thecodewarrior.bitfont.typesetting.AttributedString
import dev.thecodewarrior.bitfont.typesetting.ShapeExclusionTextContainer
import dev.thecodewarrior.bitfont.typesetting.TextLayoutManager
import dev.thecodewarrior.bitfont.typesetting.Typesetter
import dev.thecodewarrior.bitfont.utils.Rect2i
import dev.thecodewarrior.bitfont.utils.Vec2i
import java.awt.Rectangle
import java.awt.geom.AffineTransform
import java.awt.geom.Area
import java.awt.geom.Ellipse2D
import java.awt.geom.Path2D

class TextLayoutTestWindow(document: BitfontDocument): AbstractTestWindow(document, "Text Layout Test") {
    var text: AttributedString = AttributedString("")
    val options = Typesetter.Options()
    var exclusion = false
    var fragmentBounds = false

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

        withNative(::exclusion) {
            imgui.checkbox("Exclusion", it)
        }
        imgui.sameLine()
        withNative(::fragmentBounds) {
            imgui.checkbox("Fragment Bounds", it)
        }
    }

    override fun drawCanvas(imgui: ImGui) {
        val scalef = scale.toFloat()
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
        if(exclusion) configureExclusion(imgui, container, area, bounds)

        container.size = Vec2i(canvas.widthi / scale - height * 2, canvas.heighti / scale - height * 4)
        val layoutManager = TextLayoutManager(listOf(document.bitfont))
        layoutManager.textContainers.add(container)
        layoutManager.typesetterOptions = options
        layoutManager.attributedString = text
        layoutManager.layoutText()

        if(fragmentBounds) {
            for (line in container.lines) {
                imgui.windowDrawList.addRectSized(
                    area.x + line.posX * scalef, area.y + line.posY * scalef,
                    line.width * scalef, line.height * scalef,
                    Colors.textLayout.lineFragment.u32
                )
            }
        }

        for(line in container.lines) {
            val origin = vec(area.x, area.y) + vec(line.posX, line.posY) * scale
            for(main in line.glyphs) {
                main.draw(imgui, origin + vec(main.posX, main.posY) * scale, scale, Colors.textLayout.text.u32)
                main.attachments?.also { attachments ->
                    for(attachment in attachments) {
                        attachment.draw(imgui,
                            origin + vec(main.posX + attachment.posX, main.posY + attachment.posY) * scale,
                            scale, Colors.textLayout.text.u32
                        )
                    }
                }
            }
        }
    }

    fun configureExclusion(imgui: ImGui, container: ShapeExclusionTextContainer, area: Rect2i, bounds: Rect2i) {
        container.verticalPadding = 3
        container.lineFragmentPadding = 3
        run {
            val radius = 100
            val innerRadius = 80
            val outerShape = Ellipse2D.Float(
                bounds.width / 2 - radius.toFloat(), bounds.height / 2 - radius.toFloat(),
                radius * 2f, radius * 2f
            )
            val innerShape = Ellipse2D.Float(
                bounds.width / 2 - innerRadius.toFloat(), bounds.height / 2 - innerRadius.toFloat(),
                innerRadius * 2f, innerRadius * 2f
            )
            val shape = Area()
            shape.add(Area(outerShape))
            shape.subtract(Area(innerShape))

            container.exclusionPaths.add(shape)
        }

        run {
            val relativeMouse = (imgui.io.mousePos - vec(area.x, area.y)) / scale

            val size = 10
            val shape = Rectangle(
                relativeMouse.x.toInt() - size, relativeMouse.y.toInt() - size,
                size * 2, size * 2
            )
            container.exclusionPaths.add(shape)
        }

//        run {
//            val radius = 60
//            val shape = Ellipse2D.Float(
//                bounds.width - radius.toFloat(), bounds.height / 2 - radius.toFloat(),
//                radius * 2f, radius * 2f
//            )
//            container.exclusionPaths.add(shape)
//        }

//        run {
//            val radius = 60
//            val shape = Ellipse2D.Float(
//                bounds.width - radius.toFloat(), bounds.height / 2 - radius.toFloat(),
//                radius * 2f, radius * 2f
//            )
//            container.exclusionPaths.add(shape)
//        }

        run {
            val path = Path2D.Float()
            path.moveTo(bounds.width.toFloat(), 0f)
            path.curveTo(
                (bounds.width - 60).toFloat(), 0f,
                (bounds.width / 2 + 60).toFloat(), bounds.height.toFloat(),
                (bounds.width / 2).toFloat(), bounds.height.toFloat()
            )
            path.lineTo(bounds.width.toFloat(), bounds.height.toFloat())
            path.lineTo(bounds.width.toFloat(), 0f)
            path.closePath()
            container.exclusionPaths.add(path)
        }

        val shapeTransform = AffineTransform()
        shapeTransform.translate(area.xd, area.yd)
        shapeTransform.scale(scale.toDouble(), scale.toDouble())

        container.exclusionPaths.forEach {
            imgui.windowDrawList.addShapeStroke(it, shapeTransform, Colors.textLayout.exclusionPath.u32, true, 2f)
        }
    }
}