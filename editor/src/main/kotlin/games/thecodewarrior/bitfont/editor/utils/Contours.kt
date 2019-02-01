package games.thecodewarrior.bitfont.editor.utils

import glm_.vec2.Vec2i

private val contourCache = mutableMapOf<Set<Vec2i>, List<List<Vec2i>>>()

fun Iterable<Vec2i>.contours(): List<List<Vec2i>> {
    val pixelSet = this.toSet()
    return contourCache.getOrPut(pixelSet) {
        val pixels = mutableMapOf<Vec2i, MutableList<ContourSide>>()
        for (pos in pixelSet) {
            val sides = ContourSide.values().filter {
                (pos + it.offset) !in pixelSet
            }.toMutableList()
            if (sides.isNotEmpty())
                pixels[pos] = sides
        }

        if (pixels.isEmpty()) return listOf()

        val contours = mutableListOf<List<Vec2i>>()
        while (pixels.any { it.value.isNotEmpty() }) {
            var pixel = pixels.entries.first { it.value.isNotEmpty() }.key
            var side = pixels[pixel]?.removeAt(0) ?: continue
            val points = mutableListOf<Vec2i>()

            if (side !in pixels[pixel - side.next.offset] ?: mutableListOf()) {
                // if the point isn't in the middle of a line its corner needs a point
                points.add(pixel + side.cornerOffset)
            }

            while (true) {
                pixels[pixel]?.remove(side)
//                println("Advancing from ${pixel.x},${pixel.y}@$side")
                val tests = listOf(
                    side.next to pixel,
                    side to pixel + side.next.offset,
                    side.prev to pixel + side.next.offset + side.offset
                )

                val result = tests.firstOrNull {
                    it.first in pixels[it.second] ?: mutableListOf()
                } ?: break

                if (result.first != side) {
//                    println("Turning corner from ${pixel.x},${pixel.y}@$side to " +
//                        "${result.second.x},${result.second.y}@${result.first}")
                    points.add(result.second + result.first.cornerOffset)
                    pixel = result.second
                    side = result.first
                } else {
                    pixel = result.second
                }
            }

            contours.add(points)
//            println("[")
//            println(points.joinToString("\n"))
//            println("]")
        }

        return contours
    }
}

private enum class ContourSide(
    /**
     * the offset in this cardinal direction
     */
    val offset: Vec2i,
    /**
     * The offset of the corner anticlockwise from the center with respect to the top left corner (draw a line
     * from the center out through this cardinal side, rotate it anticlockwise until it hits a corner)
     */
    val cornerOffset: Vec2i) {
    TOP(Vec2i(0, -1), Vec2i(0, 0)),
    RIGHT(Vec2i(1, 0), Vec2i(1, 0)),
    BOTTOM(Vec2i(0, 1), Vec2i(1, 1)),
    LEFT(Vec2i(-1, 0), Vec2i(0, 1));

    val next: ContourSide
        get() = ContourSide.values()[(ordinal + 1) % 4]
    val prev: ContourSide
        get() = ContourSide.values()[(ordinal + 4 - 1) % 4]
}
