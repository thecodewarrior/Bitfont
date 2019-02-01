package games.thecodewarrior.bitfont.utils

data class Vec2i(val x: Int, val y: Int) {
    operator fun plus(other: Vec2i): Vec2i {
        return Vec2i(x + other.x, y + other.y)
    }

    operator fun minus(other: Vec2i): Vec2i {
        return Vec2i(x - other.x, y - other.y)
    }

    operator fun times(other: Vec2i): Vec2i {
        return Vec2i(x * other.x, y * other.y)
    }

    operator fun times(other: Int): Vec2i {
        return Vec2i(x * other, y * other)
    }
}

