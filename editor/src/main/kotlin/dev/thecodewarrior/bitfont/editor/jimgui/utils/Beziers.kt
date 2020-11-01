package dev.thecodewarrior.bitfont.editor.jimgui.utils

//b(t, p0, p1, ...)
//
//b(t, p0) = p0

//b(t) = b(t, p0, p1, ...) = (1-t)*b(t, p0, p1, ..., pn-1) + t*b(t, p1, p2, ... pn)

//I = 1-t
//linear: p0 + t(p1 - p0) = I*p0 + t*p1
//quad: I(I*p0 + t*p1) + t*(I*p1 + t*p2) = I*I*p0 + 2*I*t*p1 + t*t*p2
//cubic: I*I*I*p0 + 3*I*I*t*p1 + 3*I*t*t*p2 + t*t*t*p3

fun bezier(t: Double, points: List<Double>): Double {
    if(points.isEmpty()) throw IllegalArgumentException("illegal empty points list")
    if(points.size == 1) return points[0]
    return (1-t)* bezier(t, points.subList(0, points.size-1)) + t* bezier(t, points.subList(1, points.size))
}

fun quadraticBezier(t: Double, p0: Double, p1: Double, p2: Double): Double {
    val i = 1-t // inverse
    return i*i*p0 + 2*i*t*p1 + t*t*p2
}

fun cubicBezier(t: Double, p0: Double, p1: Double, p2: Double, p3: Double): Double {
    val i = 1-t // inverse
    return i*i*i*p0 + 3*i*i*t*p1 + 3*i*t*t*p2 + t*t*t*p3
}

