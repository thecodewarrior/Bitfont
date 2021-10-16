package dev.thecodewarrior.bitfont.fonteditor.utils

import org.lwjgl.system.MemoryStack
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@UseExperimental(ExperimentalContracts::class)
inline fun <T> stackPush(block: (MemoryStack) -> T) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    MemoryStack.stackPush().use(block)
}

