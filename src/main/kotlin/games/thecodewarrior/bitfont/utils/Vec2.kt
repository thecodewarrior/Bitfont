package games.thecodewarrior.bitfont.utils

data class Vec2(val x: Int, val y: Int) {
    operator fun plus(other: Vec2): Vec2 {
        return Vec2(x + other.x, y + other.y)
    }

    operator fun minus(other: Vec2): Vec2 {
        return Vec2(x - other.x, y - other.y)
    }

    operator fun unaryMinus(): Vec2 {
        return Vec2(-x, -y)
    }

    operator fun times(other: Vec2): Vec2 {
        return Vec2(x * other.x, y * other.y)
    }

    operator fun times(other: Number): Vec2 {
        return Vec2((x * other.toDouble()).toInt(), (y * other.toDouble()).toInt())
    }

    operator fun div(other: Vec2): Vec2 {
        return Vec2(x / other.x, y / other.y)
    }

    operator fun div(other: Number): Vec2 {
        return Vec2((x / other.toDouble()).toInt(), (y / other.toDouble()).toInt())
    }

    /**
     * Applies the passed function to each component of this vector and returns the results as a new vector
     */
    inline fun map(function: (Int) -> Int): Vec2 {
        return Vec2(function(x), function(y))
    }
}
