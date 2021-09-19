package dev.thecodewarrior.bitfont.fonteditor.jimgui.utils.extensions

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
