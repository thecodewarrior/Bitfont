package dev.thecodewarrior.bitfont.typesetting

import java.awt.Shape
import java.awt.geom.Area
import java.awt.geom.Path2D
import java.awt.geom.PathIterator
import java.awt.geom.Rectangle2D
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

open class ShapeExclusionTextContainer: TextContainer() {
    val exclusionPaths: MutableList<Shape> = mutableListOf()

    override fun fixLineFragment(line: LineFragment): LineFragment? {
        val rect = Rectangle2D.Float(line.posX.toFloat(), line.posY.toFloat(), line.width.toFloat(), line.height.toFloat())
        if(exclusionPaths.none { it.intersects(rect) })
            return super.fixLineFragment(line)

        val exclusion = Area()
        exclusionPaths.forEach {
            exclusion.add(Area(it))
        }

        exclusion.intersect(Area(rect))

        var range = IntRange.EMPTY

        val ranges = exclusion.mapPaths { path ->
            floor(path.bounds2D.minX).toInt()..ceil(path.bounds2D.maxX).toInt()
        }
        ranges.forEach { xRange ->
            if(range == IntRange.EMPTY) {
                range = xRange
            } else {
                if(xRange.last < range.first || range.last < xRange.first) { // if they don't overlap
                    if(xRange.first < range.first)
                        range = xRange
                } else { // if they overlap
                    range = min(range.first, xRange.first) .. max(range.last, xRange.last)
                }
            }
        }

        if(range == IntRange.EMPTY)
            return super.fixLineFragment(line)

        when {
            line.posX >= range.first -> {
                line.width = line.maxX - range.last
                line.posX = range.last
                return null
            }
            line.maxX <= range.last -> {
                line.width = range.first - line.posX
                return null
            }
            else -> {
                val next = LineFragment(range.last, line.posY, line.maxX - range.last, line.height)
                line.width = range.first - line.posX
                return next
            }
        }
    }

    private fun Area.forEachPath(action: (Path2D.Float) -> Unit) {
        val iter = this.getPathIterator(null)
        val poly = Path2D.Float()
        val point = FloatArray(6)
        while (!iter.isDone) {
            when (iter.currentSegment(point)) {
                PathIterator.SEG_MOVETO -> poly.moveTo(point[0], point[1])
                PathIterator.SEG_LINETO -> poly.lineTo(point[0], point[1])
                PathIterator.SEG_QUADTO -> poly.quadTo(point[0], point[1], point[2], point[3])
                PathIterator.SEG_CUBICTO -> poly.curveTo(point[0], point[1], point[2], point[3], point[4], point[5])
                PathIterator.SEG_CLOSE -> {
                    poly.closePath()
                    action(poly)
                    poly.reset()
                }
            }
            iter.next()
        }
    }

    private fun <T> Area.mapPaths(transform: (Path2D.Float) -> T): List<T> {
        val list = mutableListOf<T>()
        this.forEachPath {
            list.add(transform(it))
        }
        return list
    }
}