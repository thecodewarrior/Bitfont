package games.thecodewarrior.bitfont.editor.utils

import org.lwjgl.glfw.GLFW

object GlfwClipboard: Clipboard {
    override var contents: String?
        get() = GLFW.glfwGetClipboardString(0)
        set(value) { GLFW.glfwSetClipboardString(0, value ?: "") }
}