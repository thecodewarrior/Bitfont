package games.thecodewarrior.bitfont.utils

import glm_.vec2.Vec2
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import java.awt.Font
import java.awt.font.FontRenderContext
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import sun.text.normalizer.UTF16.append
import javax.swing.text.Segment
import java.awt.geom.GeneralPath
import sun.font.LayoutPathImpl
import java.awt.geom.PathIterator

private val context = FontRenderContext(AffineTransform(), true, true)

private val caches = mutableMapOf<Font, Int2ObjectOpenHashMap<List<List<Vec2>>>>()

fun Font.glyphProfile(codepoint: Int): List<List<Vec2>> {
    return caches.getOrPut(this) { Int2ObjectOpenHashMap() }.getOrPut(codepoint) {
        val resolution = 32
        val scaleFactor = 10000.0
        val vector = this.createGlyphVector(context, Character.toChars(codepoint))
        val shape = vector.getGlyphOutline(0)
        val pathIter = shape.getPathIterator(AffineTransform.getScaleInstance(scaleFactor, scaleFactor))
        val paths = mutableListOf<List<Vec2>>()
        var path = mutableListOf<Vec2>()

        val coords = DoubleArray(6)
        while (!pathIter.isDone) {
            val type = pathIter.currentSegment(coords)
            when (type) {
                PathIterator.SEG_CLOSE -> {
                    paths.add(path.map { it / scaleFactor })
                    path.clear()
                }
                PathIterator.SEG_MOVETO -> {
                    path.add(Vec2(coords[0], coords[1]))
                }
                PathIterator.SEG_LINETO -> {
                    path.add(Vec2(coords[0], coords[1]))
                }
                PathIterator.SEG_QUADTO -> {
                    val p0 = path.last()
                    for (i in 1..resolution) {
                        path.add(Vec2(
                            quadraticBezier(i / resolution.toDouble(), p0.x.toDouble(), coords[0], coords[2]),
                            quadraticBezier(i / resolution.toDouble(), p0.y.toDouble(), coords[1], coords[3])
                        ))
                    }
                }
                PathIterator.SEG_CUBICTO -> {
                    val p0 = path.last()
                    for (i in 1..resolution) {
                        path.add(Vec2(
                            cubicBezier(i / resolution.toDouble(), p0.x.toDouble(), coords[0], coords[2], coords[4]),
                            cubicBezier(i / resolution.toDouble(), p0.y.toDouble(), coords[1], coords[3], coords[5])
                        ))
                    }
                }
                else -> throw IllegalStateException("Unexpected PathIterator segment type: $type")
            }

            pathIter.next()
        }

        return@getOrPut paths
    }
}
