package games.thecodewarrior.bitfont.utils

data class Pos(val x: Int, val y: Int) {
    operator fun plus(other: Pos): Pos {
        return Pos(x + other.x, y + other.y)
    }

    operator fun minus(other: Pos): Pos {
        return Pos(x - other.x, y - other.y)
    }

    operator fun unaryMinus(): Pos {
        return Pos(-x, -y)
    }

    operator fun times(other: Pos): Pos {
        return Pos(x * other.x, y * other.y)
    }

    operator fun times(other: Number): Pos {
        return Pos((x * other.toDouble()).toInt(), (y * other.toDouble()).toInt())
    }

    operator fun div(other: Pos): Pos {
        return Pos(x / other.x, y / other.y)
    }

    operator fun div(other: Number): Pos {
        return Pos((x / other.toDouble()).toInt(), (y / other.toDouble()).toInt())
    }

    fun lineTo(other: Pos): List<Pos> {
        val x1 = x
        val y1 = y
        val x2 = other.x
        val y2 = other.y

        var d = 0
        val dy = Math.abs(y2 - y1)
        val dx = Math.abs(x2 - x1)
        val dy2 = dy shl 1
        val dx2 = dx shl 1
        val ix = if (x1 < x2)  1 else -1
        val iy = if (y1 < y2)  1 else -1
        var xx = x1
        var yy = y1

        val list = mutableListOf<Pos>()
        if (dy <= dx) {
            while (true) {
                list.add(Pos(xx, yy))
                if (xx == x2) break
                xx += ix
                d  += dy2
                if (d > dx) {
                    yy += iy
                    d  -= dx2
                }
            }
        }
        else {
            while (true) {
                list.add(Pos(xx, yy))
                if (yy == y2) break
                yy += iy
                d  += dx2
                if (d > dy) {
                    xx += ix
                    d  -= dy2
                }
            }
        }
        return list
    }
}
