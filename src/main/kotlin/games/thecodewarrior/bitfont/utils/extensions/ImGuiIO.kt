package games.thecodewarrior.bitfont.utils.extensions

import games.thecodewarrior.bitfont.utils.ifMac

/**
 * The primary modifier key (Command on macOS, Control on others)
 */
val imgui.IO.primaryModifier: Boolean
    get() = ifMac(keySuper, keyCtrl)

/**
 * The secondary modifier key (Control on macOS, Super on others)
 */
val imgui.IO.secondaryModifier: Boolean
    get() = ifMac(keyCtrl, keySuper)
