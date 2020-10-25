package dev.thecodewarrior.bitfont.typesetting

import java.awt.Shape
import java.awt.geom.Area
import java.awt.geom.Path2D
import java.awt.geom.PathIterator
import java.awt.geom.Rectangle2D
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

open class ShapeExclusionTextContainer(width: Int, height: Int = Int.MAX_VALUE): TextContainer(width, height) {
    var verticalPadding: Int = 1
    val exclusionPaths: MutableList<Shape> = mutableListOf()

    override fun fixLineFragment(line: LineBounds) {
        val rect = Rectangle2D.Float(
            line.posX.toFloat(),
            line.posY.toFloat() - verticalPadding,
            line.width.toFloat(),
            line.height.toFloat() + verticalPadding * 2
        )
        if(exclusionPaths.none { it.intersects(rect) })
            return // super.fixLineFragment(line)

        val exclusion = Area()
        exclusionPaths.forEach {
            exclusion.add(Area(it))
        }

        exclusion.intersect(Area(rect))

        val ranges = exclusion.mapPaths { path ->
            floor(path.bounds2D.minX).toInt()..ceil(path.bounds2D.maxX).toInt()
        }.sortedBy { it.first }

        val merged = mutableListOf<IntRange>()

        ranges.forEach { range ->
            val lastRange = merged.lastOrNull()
            if(lastRange == null) {
                merged.add(range)
            } else {
                if(range.first <= lastRange.last) {
                    merged[merged.lastIndex] = lastRange.first .. max(lastRange.last, range.last)
                } else {
                    merged.add(range)
                }
            }
        }

        if(merged.isEmpty())
            return super.fixLineFragment(line)

        var range = merged.first()

        if(range.first <= line.posX) {
            line.width = line.posX + line.width - range.last
            line.posX = range.last
            if(merged.size > 1)
                range = merged[1]
            else
                return // null
        }

        when {
            range.first <= line.posX -> {
                // This should never happen. If this was true, the if statement above would catch it. The line's posX
                // would be set to the end of the first range, and `range` would be set to the second range. The
                // second range's start would by necessity be greater than the first range's end, or they would have
                // merged, and thus the line's posX is by necessity greater than the second range's start, so this is
                // always false
                return // null
            }
            line.posX + line.width <= range.last -> {
                line.width = range.first - line.posX
                return // null
            }
            else -> {
//                val next = LineFragment(line.spacing, range.last, line.posY, line.posX + line.width - range.last, line.height)
                line.width = range.first - line.posX
                return // next
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