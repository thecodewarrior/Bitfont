package games.thecodewarrior.bitfonteditor.util

import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.input.KeyCombination
import javafx.scene.input.KeyEvent
import javafx.stage.Stage
import java.awt.BasicStroke
import java.awt.Graphics2D
import java.awt.geom.AffineTransform

var Graphics2D.strokeWidth: Float
    get() = (this.stroke as? BasicStroke)?.lineWidth ?: 1f
    set(value) {
        val current = this.stroke as? BasicStroke
        if(current != null) {
            this.stroke = BasicStroke(value, current.endCap, current.lineJoin,
                current.miterLimit, current.dashArray, current.dashPhase)
        } else {
            this.stroke = BasicStroke(value)
        }
    }

fun Graphics2D.loadIdentity() {
    this.transform = AffineTransform()
}

/**
 * Draws a polyline using an array in the form `[x, y, x, y, ...]`
 */
fun Graphics2D.drawPolyline(vararg points: Int) {
    if(points.size % 2 != 0)
        throw IllegalArgumentException("Points array must have an even number of elements. Actual size is ${points.size}")
    val xValues = IntArray(points.size/2)
    val yValues = IntArray(points.size/2)
    points.asSequence().chunked(2).forEachIndexed { i, (x, y) ->
        xValues[i] = x
        yValues[i] = y
    }
    this.drawPolyline(xValues, yValues, points.size/2)
}

/**
 * Draws a series of lines using an array in the form `[x1, y1, x2, y2, x1, y1, x2, y2, ...]`
 */
fun Graphics2D.drawLines(vararg points: Int) {
    if(points.size % 4 != 0)
        throw IllegalArgumentException("Points array must have a multiple of 4 elements. Actual size is ${points.size}")
    points.asSequence().chunked(4).forEach { (x1, y1, x2, y2) ->
        this.drawLine(x1, y1, x2, y2)
    }
}
