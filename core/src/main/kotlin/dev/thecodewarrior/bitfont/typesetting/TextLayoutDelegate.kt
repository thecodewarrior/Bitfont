package dev.thecodewarrior.bitfont.typesetting

public interface TextLayoutDelegate {
    /**
     * Called before [layoutText][TextLayoutManager.layoutText] runs
     */
    public fun textWillLayout() {}

    /**
     * Called after [layoutText][TextLayoutManager.layoutText] completes
     */
    public fun textDidLayout() {}

    public abstract class Wrapper(protected val wrapped: TextLayoutDelegate?) : TextLayoutDelegate by wrapped.fixNull()
}

private fun TextLayoutDelegate?.fixNull(): TextLayoutDelegate {
    return this ?: object : TextLayoutDelegate {}
}