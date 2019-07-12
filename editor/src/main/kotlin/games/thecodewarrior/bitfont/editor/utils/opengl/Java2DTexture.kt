package games.thecodewarrior.bitfont.editor.utils.opengl

import java.util.concurrent.ConcurrentLinkedDeque

import java.awt.Color
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.nio.IntBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

class Java2DTexture(width: Int, height: Int) {

    var width = width
        private set
    var height = height
        private set
    var filters: Boolean = false
        set(value) {
            field = value
            updateFilters()
        }

    private var initialized = false
    private val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    val texID = 0//GL11.glGenTextures()

    init {
        edit(clear = true)
        updateFilters()
    }

    @JvmOverloads
    fun edit(clear: Boolean = true, antialiasing: Boolean = false): Graphics2D {
        texturesToUpdate.add(this)
        val g2d = image.graphics as Graphics2D
        if(antialiasing) {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        }
        g2d.background = Color(0, 0, 0, 0)
        if(clear) g2d.clearRect(0, 0, width, height)
        g2d.color = Color.WHITE
        return g2d
    }

    private fun initTexture() {
        initialized = true
//        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texID)
//        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, 0)
//        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MIN_LOD, 0)
//        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LOD, 0)
//        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_LOD_BIAS, 0.0F)
//        checkError("J2D texture init params")
//
//        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL12.GL_BGRA,
//            GL12.GL_UNSIGNED_INT_8_8_8_8_REV, null as IntBuffer?)
//        checkError("J2D texture init texImage2D")
    }

    private fun updateFilters() {
//        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texID)
//        if(filters) {
//            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR)
//            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR)
//        } else {
//            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST)
//            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST)
//        }
    }

    fun delete() {
        texturesToDelete.push(texID)
    }

    fun finalize() {
        texturesToDelete.push(texID)
    }

    companion object {
        val texturesToDelete = ConcurrentLinkedDeque<Int>()
        val texturesToUpdate = ConcurrentLinkedDeque<Java2DTexture>()

        val dataBufferSize = 4194304
        val DATA_BUFFER: IntBuffer = ByteBuffer.allocateDirect(dataBufferSize * 4).order(ByteOrder.nativeOrder()).asIntBuffer()

        private fun uploadTextureImageSub(image: BufferedImage, xOffset: Int, yOffset: Int) {
            val w = image.width
            val h = image.height
            val k = 4194304 / w
            val aint = IntArray(k * w)

            var l = 0
            while (l < w * h) {
                val startY = l / w
                val scanHeight = Math.min(k, h - startY)
                val scanPixelCount = w * scanHeight
                image.getRGB(0, startY, w, scanHeight, aint, 0, w)

                DATA_BUFFER.clear()
                DATA_BUFFER.put(aint, 0, scanPixelCount)
                DATA_BUFFER.position(0).limit(scanPixelCount)

//                GL11.glTexSubImage2D(3553, 0, xOffset, yOffset + startY, w, scanHeight,
//                    GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, DATA_BUFFER)
                l += w * k
            }
        }


        fun cleanUpTextures() {
            texturesToDelete.forEach {
//                GL11.glDeleteTextures(it)
            }
//            checkError("cleanUpTextures")
            texturesToDelete.clear()
        }

        fun updateTextures() {
            texturesToUpdate.forEach { texture ->
                if(!texture.initialized) texture.initTexture()
//                GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.texID)
                uploadTextureImageSub(texture.image, 0, 0)
            }
//            checkError("J2D texture upload")
            texturesToUpdate.clear()
        }
    }
}
