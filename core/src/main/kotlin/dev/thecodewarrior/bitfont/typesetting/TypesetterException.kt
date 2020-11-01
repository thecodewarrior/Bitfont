package dev.thecodewarrior.bitfont.typesetting

import java.lang.RuntimeException

public class TypesetterException: RuntimeException {
    public constructor(): super()
    public constructor(message: String?): super(message)
    public constructor(message: String?, cause: Throwable?): super(message, cause)
    public constructor(cause: Throwable?): super(cause)
}